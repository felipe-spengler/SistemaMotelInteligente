package com.motelinteligente.dados;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoggingInitializer {

    private static final Path LOG_DIR = Paths.get(System.getProperty("user.home"), "Documents", "logs");
    private static final Path INIT_LOG = LOG_DIR.resolve("login-init.log");
    private static final Path HTML_LOG = LOG_DIR.resolve("app-logs.html");

    public static void init() {
        try {
            Files.createDirectories(LOG_DIR);
        } catch (IOException e) {
            System.err.println("Falha ao criar diretório de logs: " + e.getMessage());
        }

        // Deleta o login-init.log caso exista, pois não é mais necessário
        try {
            Files.deleteIfExists(INIT_LOG);
        } catch (IOException e) {
            System.err.println("Falha ao deletar login-init.log legado: " + e.getMessage());
        }

        // Verifica se app-logs.html é um arquivo legado/texto plano e limpa se necessário
        if (Files.exists(HTML_LOG)) {
            try {
                String firstLine = "";
                try (java.io.BufferedReader reader = Files.newBufferedReader(HTML_LOG, java.nio.charset.StandardCharsets.UTF_8)) {
                    firstLine = reader.readLine();
                }
                if (firstLine != null && !firstLine.trim().toLowerCase().startsWith("<html")) {
                    Files.delete(HTML_LOG);
                    System.out.println("Arquivo de log legado app-logs.html removido para recriação limpa.");
                }
            } catch (Exception e) {
                System.err.println("Erro ao verificar/limpar log legado: " + e.getMessage());
            }
        }

        Logger logger = LoggerFactory.getLogger(LoggingInitializer.class);
        logger.info("Inicializando logging antes do login. Diretório: {}", LOG_DIR.toAbsolutePath());
    }
}
