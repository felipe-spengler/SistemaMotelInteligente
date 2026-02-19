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
    private static final String MQTT_URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final String FILIAL;

    static {
        // Constrói o caminho completo do arquivo
        String userHome = System.getProperty("user.home");
        String filePath = userHome + File.separator + "Documents" + File.separator + "logs" + File.separator
                + "application.properties";

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);

            // Carregando as URLs e o usuário diretamente
            LOCAL_DB_URL = properties.getProperty("LOCAL_DB_URL");
            REMOTE_DB_URL = properties.getProperty("REMOTE_DB_URL");

            // Extrai o IP da URL do banco remoto para usar no MQTT
            // Exemplo esperado: jdbc:mysql://145.223.30.211/u876938716_motel
            String ipRemoto = "145.223.30.211"; // Default fall-back
            try {
                if (REMOTE_DB_URL != null && REMOTE_DB_URL.contains("//")) {
                    // Pega tudo depois de "//"
                    String temp = REMOTE_DB_URL.split("//")[1];
                    // Pega tudo antes da primeira "/" ou ":"
                    if (temp.contains("/")) {
                        ipRemoto = temp.split("/")[0];
                    } else if (temp.contains(":")) {
                        ipRemoto = temp.split(":")[0];
                    } else {
                        ipRemoto = temp;
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao extrair IP do REMOTE_DB_URL: " + e.getMessage());
            }

            MQTT_URL = "tcp://" + ipRemoto + ":1883";

            USER = properties.getProperty("USER_DB");
            FILIAL = properties.getProperty("SISTEMA");
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
                JOptionPane.showMessageDialog(null, "Erro ao converter valor ofuscado para número.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
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

    public static String getMqttUrl() {
        return MQTT_URL;
    }

    public static String getFilial() {
        return FILIAL;
    }
}
