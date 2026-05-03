package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupExecutor {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger logger = LoggerFactory.getLogger(BackupExecutor.class);

    public void start() {
        // O scheduler é iniciado, mas as variáveis já foram carregadas no construtor
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 2, TimeUnit.SECONDS);
    }

    private void processQueue() {
        if (BackupQueueManager.getInstance().isEmpty()) {
            return;
        }

        try (Connection linkOnline = ConexaoRemota.getConnection(); Statement stmt = linkOnline.createStatement()) {

            BackupTask task;

            // O loop processa todas as tarefas da fila usando a mesma conexão e statement
            while ((task = BackupQueueManager.getInstance().peekTask()) != null) {
                try {
                    task = BackupQueueManager.getInstance().takeTask(); // Remove antes de executar
                    stmt.execute(task.getSqlCommand());
                    logger.info("Backup executado para o comando SQL: " + task.getSqlCommand());
                } catch (SQLException e) {
                    logger.error("Ocorreu um erro ao executar a tarefa: " + e.getMessage(), e);

                    String mensagem = "Ocorreu um erro ao executar a tarefa:\n\n"
                            + e.getMessage()
                            + "\n\nAvise o suporte sobre este erro.";

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, mensagem, "Erro de SQL", JOptionPane.ERROR_MESSAGE);
                    });
                    logger.warn("Tarefa removida da fila devido a erro: " + task.getSqlCommand());
                    break;
                }
            }

        } catch (SQLException e) {
            // Este bloco trata erros na criação da conexão ou do statement
            logger.error("Falha ao obter conexão ou criar statement: " + e.getMessage(), e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Erro na conexão ao banco de dados.",
                        "Erro de Conexão",
                        JOptionPane.ERROR_MESSAGE);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
            logger.error("Thread do executor de backup foi interrompida: " + e.getMessage());
        }

    }
}
