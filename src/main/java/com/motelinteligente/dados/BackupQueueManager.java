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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BackupQueueManager {
    private final BlockingQueue<BackupTask> taskQueue = new LinkedBlockingQueue<>();

   public void addTask(BackupTask task) {
        taskQueue.offer(task);
        System.out.println("Tarefa adicionada à fila: " + task.getSql());
    }

    public BackupTask getTask() throws InterruptedException {
        BackupTask task = taskQueue.take();
        System.out.println("Tarefa retirada da fila: " + task.getSql());
        return task;
    }

    public void startProcessing() {
        new Thread(() -> {
            while (true) {
                try {
                    BackupTask task = getTask();
                    System.out.println("Processando tarefa: " + task.getSql());
                    // Execute a tarefa usando a conexão de backup
                    try (Connection backupConn = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/u876938716_motel",
                    "u876938716_contato",
                    "Felipe0110@"
                    )) {
                        task.execute(backupConn);
                    }
                } catch (InterruptedException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}