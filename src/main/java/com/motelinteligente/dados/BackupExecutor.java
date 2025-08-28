package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 1, TimeUnit.SECONDS);
        
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
                    // Recoloca a tarefa na fila para tentar novamente mais tarde
                    BackupQueueManager.getInstance().addTask(task);
                    logger.warn("Tarefa recolocada na fila devido a erro: " + task.getSqlCommand());
                    // Interrompe a execução e mantém a tarefa na fila
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
