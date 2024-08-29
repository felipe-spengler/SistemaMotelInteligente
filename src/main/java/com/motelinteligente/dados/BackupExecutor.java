/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

/**
 *
 * @author MOTEL
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupExecutor {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isProcessing = false;

    public void start() {
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 2, TimeUnit.SECONDS);
        System.out.println("BackupExecutor iniciado e agendado para rodar a cada 5 segundos.");
    }

    private void processQueue() {

        if (BackupQueueManager.getInstance().isEmpty()) {
            return;
        }

        try {
            BackupTask task = BackupQueueManager.getInstance().takeTask();
            performBackup(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrompida: " + e.getMessage());
        }
    }

    private void performBackup(BackupTask task) {
        if (!isProcessing) {
            new Thread(() -> {
                Connection linkOnline = null;
                try {
                    linkOnline = DriverManager.getConnection(
                            "jdbc:mysql://srv1196.hstgr.io/u876938716_motel",
                            "u876938716_contato",
                            "Felipe0110@"
                    );
                    if (linkOnline != null) {
                        configGlobal config = configGlobal.getInstance();
                        try ( Statement stmt = linkOnline.createStatement()) {
                            stmt.execute(task.getSqlCommand());
                            System.out.println("Backup executado para o comando SQL: " + task.getSqlCommand());
                        } catch (SQLException e) {
                            e.printStackTrace();
                            // Recoloca a tarefa na fila para tentar novamente mais tarde
                            BackupQueueManager.getInstance().addTask(task);
                            System.out.println("Tarefa recolocada na fila devido a erro: " + task.getSqlCommand());
                        }
                    } else {
                        System.err.println("Falha na conexão com o banco de dados.");
                        // Recoloca a tarefa na fila para tentar novamente mais tarde
                        BackupQueueManager.getInstance().addTask(task);
                        System.out.println("Tarefa recolocada na fila devido a falha na conexão: " + task.getSqlCommand());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Recoloca a tarefa na fila para tentar novamente mais tarde
                    BackupQueueManager.getInstance().addTask(task);
                    System.out.println("Tarefa recolocada na fila devido a exceção: " + task.getSqlCommand());
                } finally {
                    if (linkOnline != null) {
                        try {
                            linkOnline.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.err.println("Erro ao fechar a conexão: " + e.getMessage());
                        }
                    }
                }
            }).start();
            isProcessing = false;
        }
    }
}
