/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TelaSistema {

    private static final String REMOTE_JAR_URL = "http://motelinteligente.com/ToledoJar/";

    public static void main(String[] args) {
        try {
            updateJarIfNecessary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateJarIfNecessary() throws Exception {
        // Obter o nome do JAR executado
        String localJarName = new java.io.File(TelaSistema.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
        System.out.println("JAR local: " + localJarName);

        // Verificar a versão remota
        String remoteJarName = getRemoteJarName();
        System.out.println("JAR remoto: " + remoteJarName);

        // Comparar e atualizar se necessário
        if (!localJarName.equals(remoteJarName)) {
            System.out.println("Atualizando JAR...");
            downloadNewJar(remoteJarName);
            System.exit(0); // Fechar a aplicação para reiniciar com o novo JAR
        } else {
            System.out.println("Você já está na versão mais recente.");
        }
    }

    private static String getRemoteJarName() throws IOException {
        // Conectar-se à URL e obter o HTML
        Document doc = Jsoup.connect(REMOTE_JAR_URL).get();

        // Encontrar o nome do JAR. Supondo que o link tenha a extensão ".jar"
        for (Element link : doc.select("a[href]")) {
            String fileName = link.attr("href");
            if (fileName.endsWith(".jar")) {
                return fileName; // Retorna o primeiro JAR encontrado
            }
        }

        throw new IOException("Nenhum arquivo JAR encontrado na URL: " + REMOTE_JAR_URL);
    }

    private static void downloadNewJar(String jarName) throws IOException {
        URL url = new URL(REMOTE_JAR_URL + jarName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(jarName)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
        System.out.println("Novo JAR baixado: " + jarName);
    }
}