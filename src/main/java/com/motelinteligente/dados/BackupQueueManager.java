package com.motelinteligente.dados;

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

    public void addTask(BackupTask task) {
        try {
            queue.put(task); // Usa put() para comportamento bloqueante
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Falha ao adicionar tarefa à fila: " + e.getMessage());
        }
    }

    public BackupTask takeTask() throws InterruptedException {
        return queue.take();
    }

    public BackupTask peekTask() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
