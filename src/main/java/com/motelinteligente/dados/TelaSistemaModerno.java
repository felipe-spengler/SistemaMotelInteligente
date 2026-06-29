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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TelaSistemaModerno extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(TelaSistemaModerno.class);
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

        // Estilização extra para os botões
        startButton.setBackground(new Color(37, 99, 235));
        startButton.setForeground(Color.WHITE);
        backupButton.setBackground(new Color(71, 85, 105));
        backupButton.setForeground(Color.WHITE);
    }

    private Path getRootDir() {
        String exeDir = System.getProperty("launch4j.exedir");
        Path rootDir = null;
        if (exeDir != null && !exeDir.isEmpty()) {
            rootDir = Paths.get(exeDir);
        } else {
            try {
                java.net.URI uri = TelaSistemaModerno.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                Path codeSourcePath = Paths.get(uri).toAbsolutePath();
                rootDir = codeSourcePath.getParent();
            } catch (Exception e) {
                // fallback
            }
        }
        if (rootDir == null) {
            rootDir = Paths.get(".").toAbsolutePath().normalize();
        }
        return rootDir.toAbsolutePath().normalize();
    }

    public boolean temNovaVersaoDisponivel() {
        try {
            List<Path> localExes = findAllAppFiles(getRootDir(), TARGET_FILE_PREFIX);
            if (localExes.isEmpty()) {
                return false;
            }

            String apiUrl = "https://api.github.com/repos/felipe-spengler/SistemaMotelInteligente/releases/latest";
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            try (InputStream inputStream = connection.getInputStream()) {
                String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
                JsonArray assets = json.getAsJsonArray("assets");

                for (int i = 0; i < assets.size(); i++) {
                    JsonObject asset = assets.get(i).getAsJsonObject();
                    String name = asset.get("name").getAsString();

                    if (name.endsWith(".exe") || name.endsWith(".jar")) {
                        String localName = localExes.get(0).getFileName().toString();
                        boolean diferente = !name.equals(localName);
                        return diferente;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Logic copied from TelaSistema.java and adapted
    private void updateAppIfNecessary(Consumer<String> logger) throws Exception {
        logger.accept("Buscando arquivos EXE/JAR locais...");
        Path rootDir = getRootDir();
        List<Path> localApps = findAllAppFiles(rootDir, TARGET_FILE_PREFIX);

        if (localApps.isEmpty()) {
            logger.accept("Nenhum arquivo executável 'MotelInteligente' foi encontrado na pasta raiz.");
            return;
        }

        logger.accept("Arquivos locais encontrados:");
        for (Path path : localApps) {
            logger.accept(" - " + path.toAbsolutePath());
        }

        logger.accept("Buscando URL de download remoto (EXE/JAR)...");
        String remoteDownloadUrl = getRemoteAppDownloadUrl();
        String remoteFileName = getFileNameFromUrl(remoteDownloadUrl);
        String firstLocalFileName = localApps.get(0).getFileName().toString();

        logger.accept("Arquivo Remoto na Nuvem: " + remoteFileName);

        if (!firstLocalFileName.equals(remoteFileName)) {
            logger.accept("A versão é diferente. Baixando atualização...");
            Path tempDownloadPath = localApps.get(0).toAbsolutePath().getParent().resolve(remoteFileName);

            if (Files.exists(tempDownloadPath)) {
                Files.delete(tempDownloadPath);
            }

            downloadFile(new URL(remoteDownloadUrl), tempDownloadPath);
            logger.accept("Download concluído com sucesso!");

            logger.accept("Aguardando finalização do processo para substituir as versões...");
            startUpdaterScript(localApps, tempDownloadPath);
            Thread.sleep(1000);
            System.exit(0);
        } else {
            logger.accept("Já está na versão mais recente.");
        }
    }

    public void iniciarVerificacao() {
        startButton.setEnabled(false);
        progressBar.setVisible(true);
        logMessage("Iniciando verificação de atualização...");

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateAppIfNecessary(msg -> publish(msg));
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
                    com.motelinteligente.telas.modernas.EstiloModerno.mensagemErro(TelaSistemaModerno.this,
                            "Erro na Atualização", "Ocorreu um erro durante o processo de verificação/atualização: " + e.getMessage());
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

    private List<Path> findAllAppFiles(Path rootDir, String fileNamePrefix) throws IOException {
        try (Stream<Path> walk = Files.walk(rootDir, 1)) { // Limita a profundidade 1 (apenas pasta onde está rodando)
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName() != null)
                    .filter(path -> path.getFileName().toString().startsWith(fileNamePrefix))
                    .filter(path -> path.getFileName().toString().endsWith(".exe") || path.getFileName().toString().endsWith(".jar"))
                    .collect(Collectors.toList());
        }
    }

    private String getRemoteAppDownloadUrl() throws IOException {
        String apiUrl = "https://api.github.com/repos/felipe-spengler/SistemaMotelInteligente/releases/latest";
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        try (InputStream inputStream = connection.getInputStream()) {
            String jsonText = new String(inputStream.readAllBytes());
            JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
            JsonArray assets = json.getAsJsonArray("assets");

            // Prioriza baixar .exe. Se não achar, procura .jar.
            String exeUrl = null;
            String jarUrl = null;

            for (int i = 0; i < assets.size(); i++) {
                JsonObject asset = assets.get(i).getAsJsonObject();
                String name = asset.get("name").getAsString();
                String downloadUrl = asset.get("browser_download_url").getAsString();

                if (name.endsWith(".exe")) {
                    exeUrl = downloadUrl;
                } else if (name.endsWith(".jar")) {
                    jarUrl = downloadUrl;
                }
            }
            if(exeUrl != null) return exeUrl;
            if(jarUrl != null) return jarUrl;
        }
        throw new IOException("Nenhum arquivo EXE ou JAR encontrado na release do GitHub.");
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
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
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

    private void startUpdaterScript(List<Path> oldApps, Path newApp) throws IOException {
        // SALVA A SESSÃO ATUAL ANTES DE FECHAR PARA O AUTO-LOGIN FUNCIONAR
        salvarSessaoAtual();

        Path logsDirectory = Paths.get(System.getProperty("user.home"), "Documents", "logs");
        if (!Files.exists(logsDirectory)) {
            Files.createDirectories(logsDirectory);
        }

        Path updaterScriptPath = logsDirectory.resolve("updater.bat");
        Path newAppInLogs = logsDirectory.resolve(newApp.getFileName());

        Files.copy(newApp, newAppInLogs, StandardCopyOption.REPLACE_EXISTING);

        long pid = ProcessHandle.current().pid();

        StringBuilder scriptContent = new StringBuilder();
        scriptContent.append("@echo off\n")
                .append("setlocal enabledelayedexpansion\n\n")
                .append("set LOG_FILE=update_log.txt\n")
                .append("echo ======================================== > \"!LOG_FILE!\"\n")
                .append("echo [%%DATE%% %%TIME%%] Iniciando processo de atualizacao... >> \"!LOG_FILE!\"\n\n")
                .append("echo Aguardando encerramento natural do processo original... >> \"!LOG_FILE!\"\n")
                .append("timeout /t 3 >nul\n\n")
                .append("echo Forcando encerramento do PID ").append(pid).append(" e processos MotelInteligente... >> \"!LOG_FILE!\"\n")
                .append("taskkill /F /PID ").append(pid).append(" /T >> \"!LOG_FILE!\" 2>&1\n")
                .append("taskkill /F /IM \"MotelInteligente*\" /T >> \"!LOG_FILE!\" 2>&1\n")
                .append("timeout /t 2 >nul\n\n");

        for (int idx = 0; idx < oldApps.size(); idx++) {
            Path oldAppPath = oldApps.get(idx);
            scriptContent.append("echo Tentando excluir versao antiga: \"").append(oldAppPath.toAbsolutePath()).append("\" >> \"!LOG_FILE!\"\n")
                    .append("for /l %%i in (1,1,10) do (\n")
                    .append("    if exist \"").append(oldAppPath.toAbsolutePath()).append("\" (\n")
                    .append("        del /f /q \"").append(oldAppPath.toAbsolutePath()).append("\" >> \"!LOG_FILE!\" 2>&1\n")
                    .append("        if not exist \"").append(oldAppPath.toAbsolutePath()).append("\" (\n")
                    .append("            echo [%%DATE%% %%TIME%%] Versao antiga excluida com sucesso na tentativa %%i. >> \"!LOG_FILE!\"\n")
                    .append("            goto del_ok_").append(idx).append("_%%i\n")
                    .append("        )\n")
                    .append("        timeout /t 1 >nul\n")
                    .append("    ) else (\n")
                    .append("        echo [%%DATE%% %%TIME%%] Arquivo antigo nao encontrado ou ja excluido. >> \"!LOG_FILE!\"\n")
                    .append("        goto del_ok_").append(idx).append("_%%i\n")
                    .append("    )\n")
                    .append(")\n")
                    .append("echo [WARNING] Nao foi possivel excluir a versao antiga \"").append(oldAppPath.toAbsolutePath()).append("\" apos 10 tentativas. >> \"!LOG_FILE!\"\n");
            for (int i = 1; i <= 10; i++) {
                scriptContent.append(":del_ok_").append(idx).append("_").append(i).append("\n");
            }
            scriptContent.append("\n");
        }

        scriptContent.append("\n");

        // Pega o caminho do primeiro executável para reiniciar
        Path targetPath = oldApps.get(0).toAbsolutePath().getParent().resolve(newApp.getFileName());
        scriptContent.append("echo Copiando nova versao para: \"").append(targetPath.toAbsolutePath()).append("\" >> \"!LOG_FILE!\"\n")
                .append("for /l %%i in (1,1,10) do (\n")
                .append("    copy /y \"").append(newAppInLogs.toAbsolutePath()).append("\" \"")
                .append(targetPath.toAbsolutePath()).append("\" >> \"!LOG_FILE!\" 2>&1\n")
                .append("    if !errorlevel! equ 0 (\n")
                .append("        echo [%%DATE%% %%TIME%%] Copia concluida com sucesso na tentativa %%i. >> \"!LOG_FILE!\"\n")
                .append("        goto copy_ok\n")
                .append("    )\n")
                .append("    timeout /t 1 >nul\n")
                .append(")\n")
                .append("echo [ERROR] Falha ao copiar nova versao para \"").append(targetPath.toAbsolutePath()).append("\" apos 10 tentativas. >> \"!LOG_FILE!\"\n")
                .append(":copy_ok\n\n");

        scriptContent.append("\n")
                .append("echo Iniciando nova versao em \"").append(targetPath.toAbsolutePath()).append("\"... >> \"!LOG_FILE!\"\n");
        
        String command = targetPath.getFileName().toString().endsWith(".jar")
            ? "start \"\" javaw -jar \"" + targetPath.toAbsolutePath() + "\" --after-update"
            : "start \"\" \"" + targetPath.toAbsolutePath() + "\" --after-update";
        
        scriptContent.append(command).append(" >> \"!LOG_FILE!\" 2>&1\n")
                .append("echo [%%DATE%% %%TIME%%] Processo de atualizacao finalizado. >> \"!LOG_FILE!\"\n")
                .append("del \"%~f0\" >nul 2>&1\n")
                .append("exit\n");

        Files.write(updaterScriptPath, scriptContent.toString().getBytes(StandardCharsets.UTF_8));
        
        new ProcessBuilder("cmd", "/c", "start", "/b", updaterScriptPath.toString())
                .directory(logsDirectory.toFile())
                .start();
        
        System.exit(0);
    }

    private void salvarSessaoAtual() {
        try {
            com.motelinteligente.dados.configGlobal config = com.motelinteligente.dados.configGlobal.getInstance();
            String user = config.getUsuario();
            String pass = config.getSenhaTemporaria();
            
            if (user != null && pass != null) {
                String combined = user + ":" + pass;
                String encoded = java.util.Base64.getEncoder().encodeToString(combined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                java.nio.file.Files.write(java.nio.file.Paths.get("session.tmp"), encoded.getBytes());
                logger.info("Sessão salva com sucesso (Base64) para o usuário: " + user);
            }
        } catch (Exception e) {
            logger.error("Erro ao preparar sessão: " + e.getMessage());
        }
    }
}
