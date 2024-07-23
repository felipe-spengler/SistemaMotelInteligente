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
        System.out.println("BackupExecutor iniciado");
    }

    private void processQueue() {
        try {
            System.out.println("Processando fila de backup");
            BackupTask task = queueManager.getTask();
            performBackup(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrompida: " + e.getMessage());
        }
    }

     private void performBackup(BackupTask task) {
        new Thread(() -> {
            try {
                System.out.println("Executando backup para o comando SQL: " + task.getSql());
                Connection linkOnline = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/u876938716_motel",
                    "u876938716_contato",
                    "Felipe0110@"
                );
                if (linkOnline != null) {
                    // Executa o backup
                    try (Statement stmt = linkOnline.createStatement()) {
                        stmt.execute(task.getSql());
                        System.out.println("Backup executado para o comando SQL: " + task.getSql());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // Recoloca a tarefa na fila para tentar novamente mais tarde
                        queueManager.addTask(task);
                        System.out.println("Falha ao executar o backup, tarefa recolocada na fila: " + task.getSql());
                    }
                } else {
                    System.err.println("Falha na conexão com o banco de dados online.");
                    // Recoloca a tarefa na fila para tentar novamente mais tarde
                    queueManager.addTask(task);
                    System.out.println("Falha na conexão, tarefa recolocada na fila: " + task.getSql());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Recoloca a tarefa na fila para tentar novamente mais tarde
                queueManager.addTask(task);
                System.out.println("Erro de SQL, tarefa recolocada na fila: " + task.getSql());
            }
        }).start();
    }
}