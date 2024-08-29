package com.motelinteligente.dados;

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

        Connection linkOnline = null;
        Statement stmt = null;
        try {
            linkOnline = DriverManager.getConnection(
                    "jdbc:mysql://srv1196.hstgr.io/u876938716_motel",
                    "u876938716_contato",
                    "Felipe0110@"
            );
            System.out.println("Conexão aberta com o banco de dados.");

<<<<<<< HEAD
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
=======
            if (linkOnline != null) {
                stmt = linkOnline.createStatement();
                BackupTask task;
                while ((task = BackupQueueManager.getInstance().peekTask()) != null) {
                    try {
                        // Remove a tarefa da fila antes de tentar executar
                        task = BackupQueueManager.getInstance().takeTask();
                        stmt.execute(task.getSqlCommand());
                        System.out.println("Backup executado para o comando SQL: " + task.getSqlCommand());
                    } catch (SQLException e) {
                        e.printStackTrace();
>>>>>>> 9040fe3256c8ea54e2e29f996a8742a4bff3b2d9
                        // Recoloca a tarefa na fila para tentar novamente mais tarde
                        BackupQueueManager.getInstance().addTask(task);
                        System.out.println("Tarefa recolocada na fila devido a erro: " + task.getSqlCommand());
                        // Interrompe a execução e mantém a tarefa na fila
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Conexão falhou, manter as tarefas na fila
            System.err.println("Falha na conexão com o banco de dados. As tarefas serão mantidas na fila.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
            System.err.println("Thread interrompida: " + e.getMessage());
            // Tarefas serão mantidas na fila, a execução será parada
        } finally {
            if (linkOnline != null) {
                try {
                    linkOnline.close();
                    System.out.println("Conexão fechada com o banco de dados.");
                } catch (SQLException e) {
                    e.printStackTrace();
<<<<<<< HEAD
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
=======
>>>>>>> 9040fe3256c8ea54e2e29f996a8742a4bff3b2d9
                }
            }
        }
    }
}
