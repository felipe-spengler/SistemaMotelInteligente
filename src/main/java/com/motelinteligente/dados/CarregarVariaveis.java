package com.motelinteligente.dados;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.JOptionPane;
import java.io.File;

public class CarregarVariaveis {

    private static final int MULTIPLIER = 3;

    private static final String LOCAL_DB_URL;
    private static final String REMOTE_DB_URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Constrói o caminho completo do arquivo
        String userHome = System.getProperty("user.home");
        String filePath = userHome + File.separator + "Documents" + File.separator + "logs" + File.separator + "application.properties";

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);

            // Carregando as URLs e o usuário diretamente
            LOCAL_DB_URL = properties.getProperty("LOCAL_DB_URL");
            REMOTE_DB_URL = properties.getProperty("REMOTE_DB_URL");
            USER = properties.getProperty("USER_DB");

            // Carrega a senha ofuscada e a descriptografa
            String obfuscatedPassword = properties.getProperty("PASS_DB");
            if (obfuscatedPassword != null) {
                PASSWORD = deobfuscate(obfuscatedPassword);
            } else {
                PASSWORD = "";
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de propriedades: " + filePath);
            e.printStackTrace();
            throw new RuntimeException("Erro ao carregar o arquivo de propriedades.", e);
        }
    }

    /**
     * Reverte a ofuscação de uma senha.
     *
     * @param obfuscatedPassword A senha ofuscada.
     * @return A senha original.
     */
    private static String deobfuscate(String obfuscatedPassword) {
        StringBuilder original = new StringBuilder();
        // Divide a string ofuscada em valores numéricos
        String[] obfuscatedValues = obfuscatedPassword.split("-");
        for (String value : obfuscatedValues) {
            try {
                // Converte o valor de volta para um número e o divide
                int originalValue = Integer.parseInt(value) / MULTIPLIER;
                // Converte o valor numérico de volta para um caractere
                original.append((char) originalValue);
            } catch (NumberFormatException e) {
                // Em caso de erro, exibe um alerta
                JOptionPane.showMessageDialog(null, "Erro ao converter valor ofuscado para número.", "Erro", JOptionPane.ERROR_MESSAGE);
                return ""; // Retorna uma string vazia em caso de erro
            }
        }
        return original.toString();
    }

    public static String getLocalDbUrl() {
        return LOCAL_DB_URL;
    }

    public static String getRemoteDbUrl() {
        return REMOTE_DB_URL;
    }

    public static String getUser() {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}