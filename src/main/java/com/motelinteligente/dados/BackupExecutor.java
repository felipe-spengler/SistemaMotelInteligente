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
        System.out.println("BackupExecutor iniciado e agendado para rodar a cada 2 segundos.");
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

            stmt = linkOnline.createStatement();
            BackupTask task;
            while ((task = BackupQueueManager.getInstance().peekTask()) != null) {
                try {
                    // Remove a tarefa da fila antes de tentar executar
                    task = BackupQueueManager.getInstance().takeTask();
                    stmt.execute(task.getSqlCommand());
                    configGlobal config = configGlobal.getInstance();
                    config.incrementarContadorExecucoes();

                    System.out.println("Backup executado para o comando SQL: " + task.getSqlCommand());
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Recoloca a tarefa na fila para tentar novamente mais tarde
                    BackupQueueManager.getInstance().addTask(task);
                    System.out.println("Tarefa recolocada na fila devido a erro: " + task.getSqlCommand());
                    // Interrompe a execução e mantém a tarefa na fila
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Falha na conexão com o banco de dados. As tarefas serão mantidas na fila.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
            System.err.println("Thread interrompida: " + e.getMessage());
        } finally {
            if (linkOnline != null) {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    linkOnline.close();
                    System.out.println("Conexão fechada com o banco de dados.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Erro ao fechar a conexão: " + e.getMessage());
                }
            }
        }
    }
}
