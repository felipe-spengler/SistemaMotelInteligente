/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

/**
 *
 * @author MOTEL
 */


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BackupQueueManager {

    private static BackupQueueManager instance;
    private BlockingQueue<BackupTask> queue;
    private boolean isProcessing = false;

    private BackupQueueManager() {
        queue = new LinkedBlockingQueue<>();
    }

    public static synchronized BackupQueueManager getInstance() {
        if (instance == null) {
            instance = new BackupQueueManager();
        }
        return instance;
    }

    ;

    public void addTask(BackupTask task) {
        try {
            queue.put(task); // Usa put() para comportamento bloqueante
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Falha ao adicionar tarefa à fila: " + e.getMessage());
        }
    }

    public BackupTask takeTask() throws InterruptedException {
        BackupTask task = queue.take();
        return task;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

}
