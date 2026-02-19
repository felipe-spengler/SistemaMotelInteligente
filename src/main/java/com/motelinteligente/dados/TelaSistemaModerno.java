package com.motelinteligente.dados;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.function.Consumer;

public class TelaSistemaModerno extends JFrame {

    private static final String TARGET_FILE_PREFIX = "MotelInteligente";

    private JTextArea logTextArea;
    private JProgressBar progressBar;
    private JButton startButton;
    private JButton backupButton;

    public TelaSistemaModerno() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Atualizador de Sistema");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill, insets 15", "[grow]", "[]5[]10[grow]5[]"));

        // Header
        JLabel lblHeader = new JLabel("Verificação do Sistema");
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +8");
        add(lblHeader, "center, wrap");

        // Action Buttons
        JPanel pnlActions = new JPanel(new MigLayout("fillx, insets 0", "[grow]10[grow]", "[]"));
        startButton = new JButton("Iniciar Verificação");
        startButton.putClientProperty(FlatClientProperties.STYLE, "font: bold;");
        startButton.addActionListener(e -> iniciarVerificacao());

        backupButton = new JButton("Sincronizar Backup");
        backupButton.putClientProperty(FlatClientProperties.STYLE, "font: bold;");
        backupButton.addActionListener(e -> iniciarBackup());

        pnlActions.add(startButton, "growx, h 35!");
        pnlActions.add(backupButton, "growx, h 35!");
        add(pnlActions, "growx, wrap");

        // Log Area
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTextArea.putClientProperty(FlatClientProperties.STYLE, "font: 12 monospaced");

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Log de Processamento"));
        add(scrollPane, "grow, wrap");

        // Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(0, 25));
        add(progressBar, "growx");
    }

    // Logic copied from TelaSistema.java and adapted
    private void updateJarIfNecessary(Consumer<String> logger) throws Exception {
        logger.accept("Buscando arquivos JAR locais...");
        List<Path> localJars = findAllJarFiles(Paths.get("."), TARGET_FILE_PREFIX);

        if (localJars.isEmpty()) {
            logger.accept("Nenhum arquivo 'MotelInteligente.jar' foi encontrado.");
            return;
        }

        logger.accept("Arquivos locais encontrados:");
        for (Path path : localJars) {
            logger.accept(" - " + path.toAbsolutePath());
        }

        logger.accept("Buscando URL de download remoto...");
        String remoteDownloadUrl = getRemoteJarDownloadUrl();
        String remoteFileName = getFileNameFromUrl(remoteDownloadUrl);
        String firstLocalFileName = localJars.get(0).getFileName().toString();

        logger.accept("Arquivo Remoto: " + remoteFileName);

        if (!firstLocalFileName.equals(remoteFileName)) {
            logger.accept("A versão é diferente. Baixando atualização...");
            Path tempDownloadPath = Paths.get(localJars.get(0).getParent().toString(), remoteFileName);

            if (Files.exists(tempDownloadPath)) {
                Files.delete(tempDownloadPath);
            }

            downloadFile(new URL(remoteDownloadUrl), tempDownloadPath);
            logger.accept("Download concluído");

            logger.accept("Aguardando finalização do processo para atualizar os arquivos...");
            startUpdaterScript(localJars, tempDownloadPath);
            Thread.sleep(1000);
            System.exit(0);
        } else {
            logger.accept("Já está na versão mais recente.");
        }
    }

    private void iniciarVerificacao() {
        startButton.setEnabled(false);
        progressBar.setVisible(true);
        logMessage("Iniciando verificação de atualização...");

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateJarIfNecessary(msg -> publish(msg));
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    logMessage(message);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    logMessage("Processo de verificação concluído.");
                } catch (Exception e) {
                    logError(e);
                    JOptionPane.showMessageDialog(TelaSistemaModerno.this,
                            "Erro durante a atualização: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    startButton.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        }.execute();
    }

    private void iniciarBackup() {
        backupButton.setEnabled(false);
        progressBar.setVisible(true);
        logMessage("Iniciando sincronização de bancos de dados...");

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                new DatabaseSynchronizer().sincronizarBanco(logTextArea);
                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                backupButton.setEnabled(true);
                logMessage("Sincronização concluída.");
            }
        }.execute();
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    private void logError(Exception e) {
        logMessage("ERRO: " + e);
        e.printStackTrace();
    }

    private List<Path> findAllJarFiles(Path rootDir, String fileNamePrefix) throws IOException {
        try (Stream<Path> walk = Files.walk(rootDir)) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(fileNamePrefix))
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .collect(Collectors.toList());
        }
    }

    private String getRemoteJarDownloadUrl() throws IOException {
        String apiUrl = "https://api.github.com/repos/felipe-spengler/SistemaMotelInteligente/releases/latest";
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        try (InputStream inputStream = connection.getInputStream()) {
            String jsonText = new String(inputStream.readAllBytes());
            JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
            JsonArray assets = json.getAsJsonArray("assets");

            for (int i = 0; i < assets.size(); i++) {
                JsonObject asset = assets.get(i).getAsJsonObject();
                String name = asset.get("name").getAsString();
                String downloadUrl = asset.get("browser_download_url").getAsString();

                if (name.endsWith(".jar")) {
                    return downloadUrl;
                }
            }
        }
        throw new IOException("Nenhum arquivo JAR encontrado na release.");
    }

    private String getFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            return new File(path).getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter o nome do arquivo remoto.";
        }
    }

    private void downloadFile(URL url, Path dest) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int fileSize = connection.getContentLength();
        int totalBytesRead = 0;

        try (InputStream in = connection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(dest.toFile())) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                final int progress = (int) ((double) totalBytesRead / fileSize * 100);
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
            }
        }
    }

    private void startUpdaterScript(List<Path> oldJars, Path newJar) throws IOException {
        Path logsDirectory = Paths.get(System.getProperty("user.home"), "Documents", "logs");
        if (!Files.exists(logsDirectory)) {
            Files.createDirectories(logsDirectory);
        }

        Path updaterScriptPath = logsDirectory.resolve("updater.bat");
        Path newJarInLogs = logsDirectory.resolve(newJar.getFileName());

        Files.copy(newJar, newJarInLogs, StandardCopyOption.REPLACE_EXISTING);

        StringBuilder scriptContent = new StringBuilder();
        scriptContent.append("@echo off\n")
                .append("setlocal enabledelayedexpansion\n\n")
                .append("echo Encerrando processo javaw.exe...\n")
                .append("taskkill /IM javaw.exe /F >nul 2>&1\n")
                .append(":waitJavaw\n")
                .append("tasklist /FI \"IMAGENAME eq javaw.exe\" | find /I \"javaw.exe\" >nul\n")
                .append("if %ERRORLEVEL% equ 0 (\n")
                .append("    timeout /t 2 >nul\n")
                .append("    goto waitJavaw\n")
                .append(")\n\n");

        for (Path oldJarPath : oldJars) {
            scriptContent.append("if exist \"").append(oldJarPath.toAbsolutePath()).append("\" (\n")
                    .append("    del /f /q \"").append(oldJarPath.toAbsolutePath()).append("\"\n")
                    .append(")\n");
        }

        scriptContent.append("\n");

        for (Path oldJarPath : oldJars) {
            Path targetPath = oldJarPath.getParent().resolve(newJar.getFileName());
            // Important: Use double quotes around paths to handle spaces
            scriptContent.append("copy /y \"").append(newJarInLogs.toAbsolutePath()).append("\" \"")
                    .append(targetPath.toAbsolutePath()).append("\" >nul\n");
        }

        scriptContent.append("del /f /q \"").append(newJarInLogs.toAbsolutePath()).append("\" >nul\n");

        Path firstTarget = oldJars.get(0).getParent().resolve(newJar.getFileName());
        scriptContent.append("\n")
                .append("echo Iniciando nova versão...\n")
                .append("start \"\" /D \"").append(firstTarget.getParent()).append("\" javaw -jar \"")
                .append(firstTarget.toAbsolutePath()).append("\"\n");

        scriptContent.append("timeout /t 2 >nul\n");
        scriptContent.append("del \"%~f0\" >nul 2>&1\n")
                .append("endlocal\n")
                .append("exit\n");

        Files.write(updaterScriptPath, scriptContent.toString().getBytes(StandardCharsets.UTF_8));
        Files.deleteIfExists(newJar);

        new ProcessBuilder("cmd", "/c", "start", "/b", updaterScriptPath.toString())
                .directory(logsDirectory.toFile())
                .start();
    }
}
