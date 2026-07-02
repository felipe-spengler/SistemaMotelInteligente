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

    public static void init() {
        try {
            Files.createDirectories(LOG_DIR);
        } catch (IOException e) {
            System.err.println("Falha ao criar diretório de logs: " + e.getMessage());
        }

        try {
            String content = "Inicializando logging antes do login em " + java.time.LocalDateTime.now() + System.lineSeparator();
            Files.writeString(INIT_LOG, content, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            System.out.println("LoggingInitializer escreveu em: " + INIT_LOG.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Falha ao escrever arquivo de log inicial: " + e.getMessage());
        }

        Logger logger = LoggerFactory.getLogger(LoggingInitializer.class);
        logger.info("Inicializando logging antes do login. Diretório: {}", LOG_DIR.toAbsolutePath());
    }
}
