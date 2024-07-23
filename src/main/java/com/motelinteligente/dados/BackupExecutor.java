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
    private final BackupQueueManager queueManager;

    public BackupExecutor(BackupQueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 1, TimeUnit.MINUTES);
    }

    private void processQueue() {
        try {
            BackupTask task = queueManager.getTask();
            performBackup(task);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        }
    }

    private void performBackup(BackupTask task) {
        new Thread(() -> {
            try (Connection linkOnline = DriverManager.getConnection(
                    "jdbc:mysql://srv1196.hstgr.io/seubanco_online",
                    "usuario_online",
                    "senha_online"
            )) {
                if (linkOnline != null) {
                    try (Statement stmt = linkOnline.createStatement()) {
                        stmt.execute(task.getSql());
                        System.out.println("Backup executado para o comando SQL: " + task.getSql());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        queueManager.addTask(task);
                    }
                } else {
                    System.err.println("Falha na conexão com o banco de dados online.");
                    queueManager.addTask(task);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                queueManager.addTask(task);
            }
        }).start();
    }
}