package com.motelinteligente.dados;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;

public class ProgramaLeituraConteudo {

    private static final String URL_CONTEUDO_TXT = "https://motelinteligente.com/conteudo.txt";

    public static int verificarConteudoArquivo() {
        // Realiza a leitura do conteúdo do arquivo
        String conteudo = lerConteudoArquivo();
        if (conteudo != null) {
            if (!conteudo.isEmpty()) {
                if (!(conteudo.equals("nada"))) {
                    try {
                        int resposta = Integer.parseInt(conteudo);
                        alterarConteudoArquivo("");
                        return resposta;
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "verificarConteudoArquivo: " + e);
                    }
                }

            }
        }

        return 0;
    }

    private static String lerConteudoArquivo() {
        StringBuilder content = new StringBuilder();
        try {
            // Criando a URL para o arquivo PHP que contém a função lerConteudo()
            URL url = new URL("https://motelinteligente.com/mudousistema.php");

            // Abrindo conexão HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); // Definindo o método de solicitação POST
            connection.setConnectTimeout(5000); // Tempo limite de conexão de 5 segundos

            // Definindo que a conexão permite a saída de dados
            connection.setDoOutput(true);

            // Obtendo o fluxo de saída da conexão
            OutputStream outputStream = connection.getOutputStream();

            // Escrevendo o nome da função no fluxo de saída
            String postData = "funcao=lerConteudo"; // Especificando que a função a ser chamada é lerConteudo()
            outputStream.write(postData.getBytes());
            outputStream.flush();
            outputStream.close();

            // Lendo a resposta da conexão
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return content.toString();
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler conteúdo do arquivo via HTTP: " + e.getMessage());
            return null;
        }
    }

    private static void alterarConteudoArquivo(String novoConteudo) {
        String url = "https://motelinteligente.com/mudousistema.php"; // URL local para teste
        try {
            // Conteúdo a ser enviado
            String conteudo = novoConteudo;

            // Construindo os parâmetros do POST
            String postData = "conteudo=" + conteudo;

            // Criando a conexão
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false); // Desativa o seguimento de redirecionamento
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true); // Permitir saída de dados

            // Enviando os dados
            try ( OutputStream os = connection.getOutputStream()) {
                byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
                os.write(postDataBytes);
                os.flush();
            }

            // Lendo a resposta (opcional)
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                // Exibindo a resposta (opcional)
                System.out.println("Resposta do servidor:" + content.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
