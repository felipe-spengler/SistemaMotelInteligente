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
    }

    public BackupTask getTask() throws InterruptedException {
        return taskQueue.take();
    }

    public void startProcessing() {
        new Thread(() -> {
            while (true) {
                try {
                    BackupTask task = getTask();
                    // Aqui você pode executar a tarefa em um banco de dados secundário ou backup
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