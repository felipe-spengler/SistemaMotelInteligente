/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

/**
 *
 * @author MOTEL
 */
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.sql.Connection;

@Component
public class BackupConfig implements CommandLineRunner {

    private final BackupQueueManager queueManager = new BackupQueueManager();
    private final BackupExecutor backupExecutor = new BackupExecutor(queueManager);
    private final fazconexao fazConexao = new fazconexao(queueManager);

    @Override
    public void run(String... args) throws Exception {
        backupExecutor.start(); // Inicia o executor de tarefas

        // Teste a conexão e adicione um comando SQL para verificar o backup
        Connection conn = fazConexao.conectar();
        // Realize operações com a conexão (que agora está usando o proxy)
    }
}