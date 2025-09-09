package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupExecutor {

    private String LOCAL_DB_URL;
    private String REMOTE_DB_URL;
    private String USER;
    private String PASSWORD;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isProcessing = false;
    private static final Logger logger = LoggerFactory.getLogger(fquartos.class);

    public void start() {
        this.LOCAL_DB_URL = CarregarVariaveis.getLocalDbUrl();
        this.REMOTE_DB_URL = CarregarVariaveis.getRemoteDbUrl();
        this.USER = CarregarVariaveis.getUser();
        this.PASSWORD = CarregarVariaveis.getPassword();
        //scheduler.scheduleAtFixedRate(this::processQueue, 0, 1, TimeUnit.SECONDS);

    }

    private void processQueue() {

        if (BackupQueueManager.getInstance().isEmpty()) {
            System.out.println("nao há nada da fila");
            return;
        }

        Connection linkOnline = null;
        Statement stmt = null;
        try {
            linkOnline = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD);
            
            stmt = linkOnline.createStatement();
            BackupTask task;
            while ((task = BackupQueueManager.getInstance().peekTask()) != null) {
                try {
                    // Remove a tarefa da fila antes de tentar executar
                    task = BackupQueueManager.getInstance().takeTask();
                    stmt.execute(task.getSqlCommand());
                    configGlobal config = configGlobal.getInstance();
                    config.incrementarContadorExecucoes();

                    //logger.info("Backup executado para o comando SQL: " + task.getSqlCommand());
                } catch (SQLException e) {
                    e.printStackTrace();

                    String mensagem = "Ocorreu um erro ao executar a tarefa:\n\n"
                            + e.getMessage()
                            + "\n\nDeseja recolocar a tarefa na fila?\n\n"
                            + "⚠️ Avise o suporte sobre este erro.";

                    int opcao = JOptionPane.showConfirmDialog(
                            null,
                            mensagem,
                            "Erro de SQL",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE
                    );

                    if (opcao == JOptionPane.YES_OPTION) {
                        // Recoloca na fila
                        // BackupQueueManager.getInstance().addTask(task);
                        logger.warn("Tarefa recolocada na fila devido a erro: " + task.getSqlCommand());
                    } else {
                        // Remove da fila
                        logger.warn("Tarefa REMOVIDA da fila devido a erro: " + task.getSqlCommand());
                    }

                    // Interrompe a execução da fila atual
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
            logger.error("Thread interrompida: " + e.getMessage());
        } finally {
            if (linkOnline != null) {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    linkOnline.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
