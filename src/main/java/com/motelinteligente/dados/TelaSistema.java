package com.motelinteligente.dados;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import org.springframework.stereotype.Component;

@Component
public class TelaSistema extends JFrame {

    private static final String GITHUB_RELEASE_PAGE_URL
            = "https://github.com/felipe-spengler/SistemaMotelInteligente/releases/tag/AbelardoV1.0";
    private static final String TARGET_FILE_PREFIX = "MotelInteligente";

    private JTextArea logTextArea;
    private JProgressBar progressBar;
    private JButton startButton;
    private JButton backupButton;

    public TelaSistema() {
        super("Atualizador de Sistema");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Iniciar Verificação");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(startButton);
        backupButton = new JButton("Sincronizar Backup");
        backupButton.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(backupButton);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        startButton.addActionListener(e -> {
            startButton.setEnabled(false);
            progressBar.setVisible(true);
            logMessage("Iniciando verificação de atualização...");
            startUpdateProcess();
        });
        backupButton.addActionListener(e -> {
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
                    try {
                        get();
                    } catch (Exception ex) {
                        logError(ex);
                    }
                }
            }.execute();
        });
    }

    public void startSyncProcess() throws SQLException {
        new DatabaseSynchronizer().sincronizarBanco(logTextArea);
    }

    public JButton getStartButton() {
        return startButton;
    }

    private void startUpdateProcess() {
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateJarIfNecessary();
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    logTextArea.append(message + "\n");
                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    logMessage("Processo de verificação concluído.");
                } catch (Exception e) {
                    logError(e);
                    logMessage("Erro durante a atualização. Verifique o log.");
                    JOptionPane.showMessageDialog(TelaSistema.this,
                            "Ocorreu um erro durante a atualização. Verifique o log para mais detalhes.",
                            "Erro de Atualização", JOptionPane.ERROR_MESSAGE);
                } finally {
                    startButton.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }

            private void updateJarIfNecessary() throws Exception {
                publish("Buscando arquivos executáveis locais (.exe)...");
                List<Path> localExes = findAllExeFiles(Paths.get("."), TARGET_FILE_PREFIX);

                if (localExes.isEmpty()) {
                    publish("Nenhum arquivo 'MotelInteligente.exe' foi encontrado.");
                    return;
                }

                publish("Arquivos locais encontrados:");
                for (Path path : localExes) {
                    publish(" - " + path.toAbsolutePath());
                }

                publish("Buscando URL de download remoto...");
                String remoteDownloadUrl = getRemoteExeDownloadUrl();
                String remoteFileName = getFileNameFromUrl(remoteDownloadUrl);
                String firstLocalFileName = localExes.get(0).getFileName().toString();

                publish("Arquivo Remoto: " + remoteFileName);

                if (!firstLocalFileName.equals(remoteFileName)) {
                    publish("A versão é diferente. Baixando atualização...");
                    Path tempDownloadPath = Paths.get(localExes.get(0).getParent().toString(), remoteFileName);

                    // Se o arquivo temporário já existe, deleta para evitar erro de download
                    if (Files.exists(tempDownloadPath)) {
                        Files.delete(tempDownloadPath);
                    }

                    downloadFile(new URL(remoteDownloadUrl), tempDownloadPath);
                    publish("Download concluído");

                    publish("Aguardando finalização do processo para atualizar os arquivos...");
                    startUpdaterScript(localExes, tempDownloadPath);
                    Thread.sleep(1000);
                    System.exit(0);
                } else {
                    publish("Já está na versão mais recente.");
                }
            }

            private String getRemoteExeDownloadUrl() throws IOException {
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

                        if (name.endsWith(".exe")) {
                            return downloadUrl;
                        }
                    }
                }
                throw new IOException("Nenhum arquivo EXE encontrado na release do GitHub.");
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

                try (InputStream in = connection.getInputStream(); FileOutputStream fileOutputStream = new FileOutputStream(dest.toFile())) {
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

            private void startUpdaterScript(List<Path> oldExes, Path newExe) throws IOException {
                Path logsDirectory = Paths.get(System.getProperty("user.home"), "Documents", "logs");
                if (!Files.exists(logsDirectory)) Files.createDirectories(logsDirectory);

                Path updaterScriptPath = logsDirectory.resolve("updater.bat");
                Path newExeInLogs = logsDirectory.resolve(newExe.getFileName());

                Files.copy(newExe, newExeInLogs, StandardCopyOption.REPLACE_EXISTING);

                StringBuilder scriptContent = new StringBuilder();
                scriptContent.append("@echo off\n")
                        .append("setlocal enabledelayedexpansion\n\n")
                        .append("echo Aguardando fechamento do sistema...\n")
                        .append("timeout /t 2 >nul\n")
                        // Encerra qualquer versão do MotelInteligente que esteja rodando
                        .append("taskkill /IM MotelInteligente* /F >nul 2>&1\n")
                        .append("timeout /t 1 >nul\n");

                // Deleta os EXEs antigos
                for (Path oldPath : oldExes) {
                    scriptContent.append("if exist \"").append(oldPath.toAbsolutePath()).append("\" (\n")
                            .append("    del /f /q \"").append(oldPath.toAbsolutePath()).append("\"\n")
                            .append(")\n");
                }

                // Copia o novo EXE para o destino original
                Path targetPath = oldExes.get(0).getParent().resolve(newExe.getFileName());
                scriptContent.append("copy /y \"").append(newExeInLogs.toAbsolutePath()).append("\" \"")
                        .append(targetPath.toAbsolutePath()).append("\" >nul\n");

                scriptContent.append("del /f /q \"").append(newExeInLogs.toAbsolutePath()).append("\" >nul\n");

                // Inicia o novo EXE
                scriptContent.append("\n")
                        .append("echo Iniciando nova versao...\n")
                        .append("start \"\" /D \"").append(targetPath.getParent()).append("\" \"")
                        .append(targetPath.toAbsolutePath()).append("\"\n");

                scriptContent.append("timeout /t 2 >nul\n");
                scriptContent.append("del \"%~f0\" >nul 2>&1\n")
                        .append("endlocal\n")
                        .append("exit\n");

                Files.write(updaterScriptPath, scriptContent.toString().getBytes(StandardCharsets.UTF_8));
                Files.deleteIfExists(newExe);

                new ProcessBuilder("cmd", "/c", "start", "/b", updaterScriptPath.toString())
                        .directory(logsDirectory.toFile())
                        .start();
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

    private static List<Path> findAllExeFiles(Path rootDir, String fileNamePrefix) throws IOException {
        try (Stream<Path> walk = Files.walk(rootDir)) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(fileNamePrefix))
                    .filter(path -> path.getFileName().toString().endsWith(".exe"))
                    .collect(Collectors.toList());
        }
    }

    public boolean temNovaVersaoDisponivel() {
        try {
            List<Path> localExes = findAllExeFiles(Paths.get("."), TARGET_FILE_PREFIX);
            if (localExes.isEmpty()) {
                logMessage("Nenhum arquivo local .exe encontrado para comparação.");
                return false;
            }

            String apiUrl = "https://api.github.com/repos/felipe-spengler/SistemaMotelInteligente/releases/latest";
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            try (InputStream inputStream = connection.getInputStream()) {
                String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
                JsonArray assets = json.getAsJsonArray("assets");

                for (int i = 0; i < assets.size(); i++) {
                    JsonObject asset = assets.get(i).getAsJsonObject();
                    String name = asset.get("name").getAsString();

                    if (name.endsWith(".exe")) {
                        String localName = localExes.get(0).getFileName().toString();
                        boolean diferente = !name.equals(localName);

                        logMessage("Comparando exe local (" + localName + ") com remoto (" + name + "): " + (diferente ? "DIFERENTE" : "IGUAL"));
                        return diferente;
                    }
                }
            }
        } catch (Exception e) {
            logError(e);
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaSistema frame = new TelaSistema();
            frame.setVisible(true);
        });
    }
}
