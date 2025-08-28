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
import java.util.Optional;
import java.util.stream.Stream;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TelaSistema extends JFrame {

    private static final String GITHUB_RELEASE_PAGE_URL
            = "https://github.com/felipe-spengler/SistemaMotelInteligente/releases/tag/AbelardoV1.0";
    private static final String TARGET_FILE_PREFIX = "MotelInteligente";

    private JTextArea logTextArea;
    private JProgressBar progressBar;
    private JButton startButton;

    public TelaSistema() {
        super("Atualizador de Sistema");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        startButton.addActionListener(e -> {
            startButton.setEnabled(false);
            progressBar.setVisible(true);
            logMessage("Iniciando verificação de atualização...");
            startUpdateProcess();
        });
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
                publish("Buscando arquivo JAR local...");
                Optional<Path> foundFile = findJarFile(Paths.get("."), TARGET_FILE_PREFIX);

                if (foundFile.isPresent()) {
                    Path localJarPath = foundFile.get();
                    String localFileName = localJarPath.getFileName().toString();
                    publish("Arquivo JAR local encontrado: " + localFileName);

                    publish("Buscando URL de download remoto...");
                    String remoteDownloadUrl = getRemoteJarDownloadUrl();
                    String remoteFileName = getFileNameFromUrl(remoteDownloadUrl);
                    publish("Nome do arquivo remoto: " + remoteFileName);

                    if (!localFileName.equals(remoteFileName)) {
                        publish("O nome do arquivo local é diferente do remoto. Baixando atualização...");

                        Path tempDownloadPath = Paths.get(localJarPath.getParent().toString(), "temp_update.jar");
                        downloadFile(new URL(remoteDownloadUrl), tempDownloadPath);
                        publish("Download concluído para: " + tempDownloadPath);

                        publish("Aguardando finalização do processo para atualizar o arquivo...");
                        startUpdaterScript(localJarPath, tempDownloadPath);

                        System.exit(0);

                    } else {
                        publish("Os nomes dos arquivos são iguais. Já está na versão mais recente.");
                    }
                } else {
                    publish("Nenhum arquivo 'MotelInteligente.jar' foi encontrado.");
                }
            }

            private String getRemoteJarDownloadUrl() throws IOException {
                // Endpoint da API
                String apiUrl = "https://api.github.com/repos/felipe-spengler/SistemaMotelInteligente/releases/latest";

                // Faz a requisição HTTP
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

                try (InputStream inputStream = connection.getInputStream()) {
                    String jsonText = new String(inputStream.readAllBytes());

                    // Parse do JSON
                    JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
                    JsonArray assets = json.getAsJsonArray("assets");

                    for (int i = 0; i < assets.size(); i++) {
                        JsonObject asset = assets.get(i).getAsJsonObject();
                        String name = asset.get("name").getAsString();
                        String downloadUrl = asset.get("browser_download_url").getAsString();

                        if (name.endsWith(".jar")) {
                            return downloadUrl; // retorna o link do JAR
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

            private void startUpdaterScript(Path oldJar, Path newJar) throws IOException {
                String scriptContent
                        = "@echo off\n"
                        + "setlocal enabledelayedexpansion\n"
                        + "\n"
                        + "rem 1) Descobre o jar atual (MotelInteligente-*.jar)\n"
                        + "for %%f in (MotelInteligente-*.jar) do (\n"
                        + "    set CURRENT_JAR=%%f\n"
                        + ")\n"
                        + "echo Jar atual: %CURRENT_JAR%\n"
                        + "\n"
                        + "rem 2) Fecha o programa antigo (se ainda estiver rodando)\n"
                        + "taskkill /IM javaw.exe /F >nul 2>&1\n"
                        + "\n"
                        + "rem 3) Espera até o jar ser liberado e deleta\n"
                        + ":waitloop\n"
                        + "del \"%CURRENT_JAR%\" >nul 2>&1\n"
                        + "if exist \"%CURRENT_JAR%\" (\n"
                        + "    timeout /t 2 >nul\n"
                        + "    goto waitloop\n"
                        + ")\n"
                        + "\n"
                        + "rem 4) Renomeia o novo jar para o nome antigo\n"
                        + "rename \"" + newJar.getFileName().toString() + "\" \"%CURRENT_JAR%\"\n"
                        + "\n"
                        + "rem 5) Inicia o novo jar\n"
                        + "start javaw -jar \"%CURRENT_JAR%\"\n"
                        + "endlocal\n"
                        + "exit\n";

                Path updaterScriptPath = oldJar.getParent().resolve("updater.bat");
                Files.write(updaterScriptPath, scriptContent.getBytes());

                new ProcessBuilder("cmd", "/c", updaterScriptPath.toString()).start();
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
        logMessage("ERRO: " + e.getMessage());
        e.printStackTrace();
    }

    private static Optional<Path> findJarFile(Path rootDir, String fileNamePrefix) throws IOException {
        try (Stream<Path> walk = Files.walk(rootDir)) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(fileNamePrefix))
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .findFirst();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaSistema frame = new TelaSistema();
            frame.setVisible(true);
        });
    }
}
