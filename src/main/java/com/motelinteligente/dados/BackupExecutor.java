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
    private static final Logger logger = LoggerFactory.getLogger(BackupExecutor.class);

    public void start() {
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 1, TimeUnit.SECONDS);
    }

    private void processQueue() {
        if (BackupQueueManager.getInstance().isEmpty()) {
            System.out.println("Não há nada na fila de backup.");
            return;
        }

        Connection linkOnline = null;

        try {

            if (configGlobal.conexaoRemota == null || configGlobal.conexaoRemota.isClosed()) {
                linkOnline = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD);
                configGlobal.conexaoRemota = linkOnline; // Armazena a nova conexão
                configGlobal.incrementarContadorExecucoes();
            } else {

                linkOnline = configGlobal.conexaoRemota;

            }


            try (Statement stmt = linkOnline.createStatement()) {
                BackupTask task;

                // O loop processa todas as tarefas da fila usando a mesma conexão e statement
                while ((task = BackupQueueManager.getInstance().peekTask()) != null) {
                    try {
                        // Remove a tarefa da fila antes de tentar executar
                        task = BackupQueueManager.getInstance().takeTask();
                        stmt.execute(task.getSqlCommand());

                        configGlobal config = configGlobal.getInstance();
                        

                        logger.info("Backup executado para o comando SQL: " + task.getSqlCommand());
                    } catch (SQLException e) {
                        logger.error("Ocorreu um erro ao executar a tarefa: " + e.getMessage(), e);

                        String mensagem = "Ocorreu um erro ao executar a tarefa:\n\n"
                                + e.getMessage()
                                + "\n\nAvise o suporte sobre este erro.";

                        JOptionPane.showMessageDialog(null, mensagem, "Erro de SQL", JOptionPane.ERROR_MESSAGE);
                        logger.warn("Tarefa removida da fila devido a erro: " + task.getSqlCommand());
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            // Este bloco trata erros na criação da conexão ou do statement
            logger.error("Falha ao obter conexão ou criar statement: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(null, "Erro na conexão ao banco de dados.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
            logger.error("Thread do executor de backup foi interrompida: " + e.getMessage());
        }
    }
}