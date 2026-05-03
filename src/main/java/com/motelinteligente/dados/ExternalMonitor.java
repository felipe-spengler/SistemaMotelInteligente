/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ExternalMonitor implements Runnable {
    private static final long CHECK_INTERVAL = 5000; // Intervalo de 5 segundos

    @Override
    public void run() {
        String mainProcessId = getProcessId();

        while (true) {
            try {
                Thread.sleep(CHECK_INTERVAL);
                if (!isMainProcessRunning(mainProcessId)) {
                    updateIsRunning(false);
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getProcessId() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        return jvmName.split("@")[0];
    }

    private boolean isMainProcessRunning(String pid) {
        String line;
        try {
            Process proc = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
            java.io.BufferedReader input = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line.contains(pid)) {
                    return true;
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateIsRunning(boolean status) {
        String query = "UPDATE configuracoes SET isRunning = ?";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement stmt = link.prepareStatement(query)) {
            stmt.setBoolean(1, status);
            stmt.executeUpdate();
            System.out.println("isRunning atualizado para " + status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}