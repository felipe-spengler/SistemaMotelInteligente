package com.motelinteligente.dados;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoggingInitializer {

    private static final Path LOG_DIR = Paths.get(System.getProperty("user.home"), "Documents", "logs");

    public static void init() {
        try {
            Files.createDirectories(LOG_DIR);
        } catch (IOException e) {
            System.err.println("Falha ao criar diretório de logs: " + e.getMessage());
        }

        Logger logger = LoggerFactory.getLogger(LoggingInitializer.class);
        logger.info("Inicializando logging antes do login. Diretório: {}", LOG_DIR.toAbsolutePath());
    }
}
