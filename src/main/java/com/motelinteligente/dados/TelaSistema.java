package com.motelinteligente.dados;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class TelaSistema extends JFrame {

    private JTextArea logTextArea;
    private JProgressBar progressBar;
    private JButton startButton;

    // Constante para a URL remota. Supondo que 'fazconexao' exista com essa variável.
    // Substitua pela URL real se necessário.
    

    public TelaSistema() {
        super("Atualizador de Sistema");
        // Configuração básica da janela
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Área de log com scroll
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Barra de progresso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false); // Inicia invisível
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        // Painel para o botão
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Iniciar Verificação");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(startButton);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        // Ação do botão
        startButton.addActionListener(e -> {
            startButton.setEnabled(false);
            progressBar.setVisible(true);
            logMessage("Iniciando verificação de atualização...");
            startUpdateProcess();
        });
    }

    /**
     * Inicia o processo de atualização em uma thread em segundo plano.
     */
    private void startUpdateProcess() {
        // SwingWorker é usado para executar tarefas em segundo plano e atualizar
        // a GUI de forma segura na thread principal.
        new SwingWorker<Void, String>() {

            /**
             * Este método é executado na thread de segundo plano.
             */
            @Override
            protected Void doInBackground() throws Exception {
                updateJarIfNecessary();
                return null;
            }

            /**
             * Recebe as mensagens publicadas e as exibe na área de texto.
             * Este método é executado na thread da GUI (EDT).
             */
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    logTextArea.append(message + "\n");
                    // Rola automaticamente para o final da área de texto
                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                }
            }

            /**
             * Chamado quando o processo é concluído (com sucesso ou com erro).
             * Este método é executado na thread da GUI (EDT).
             */
            @Override
            protected void done() {
                try {
                    get(); // Tenta obter o resultado para verificar se houve exceção
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

            // A Lógica do código original foi movida para este bloco interno
            private void updateJarIfNecessary() throws Exception {
                // Obter o nome do JAR executado de forma segura
                String localJarName = getLocalJarName();
                publish("JAR local: " + localJarName);

                // Verificar a versão remota
                String remoteJarName = getRemoteJarName();
                publish("JAR remoto: " + remoteJarName);

                // Comparar e atualizar se necessário
                if (!localJarName.equals(remoteJarName)) {
                    publish("Atualizando JAR...");
                    downloadNewJar(remoteJarName);
                    publish("Atualização concluída com sucesso. Reinicie a aplicação.");
                    // exit(0) foi removido para que o usuário possa ler as mensagens.
                    // Uma mensagem de instrução é exibida em vez de fechar a aplicação imediatamente.
                } else {
                    publish("Você já está na versão mais recente.");
                }
            }

            /**
             * Método para obter o nome do JAR local de forma robusta.
             * Lida com URIs que não são hierárquicas.
             */
            private String getLocalJarName() throws URISyntaxException {
                URL url = TelaSistema.class.getProtectionDomain().getCodeSource().getLocation();
                String path = url.getPath();
                // O URI pode ter a forma "file:/path/to/jar.jar" ou "jar:file:/path/to/jar.jar!/".
                if (path.contains("!")) {
                    path = path.substring(0, path.lastIndexOf('!'));
                }
                // Decodifica a string para lidar com espaços e caracteres especiais
                String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
                // Extrai o nome do arquivo a partir do caminho decodificado
                return new File(decodedPath).getName();
            }

            private String getRemoteJarName() throws IOException {
                publish("Buscando a versão remota em " + fazconexao.REMOTE_JAR_URL);
                Document doc = Jsoup.connect(fazconexao.REMOTE_JAR_URL).get();
                for (Element link : doc.select("a[href]")) {
                    String fileName = link.attr("href");
                    if (fileName.endsWith(".jar")) {
                        return fileName;
                    }
                }
                throw new IOException("Nenhum arquivo JAR encontrado na URL: " + fazconexao.REMOTE_JAR_URL);
            }

            private void downloadNewJar(String jarName) throws IOException {
                publish("Iniciando download de " + jarName);
                URL url = new URL(fazconexao.REMOTE_JAR_URL + jarName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int fileSize = connection.getContentLength();
                int totalBytesRead = 0;

                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(jarName)) {
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        // Calcula e atualiza o progresso na barra
                        int progress = (int) ((double) totalBytesRead / fileSize * 100);
                        setProgress(progress); // Método do SwingWorker para atualizar o progresso
                    }
                }
                publish("Download concluído.");
            }
        }.execute(); // Inicia a execução do SwingWorker
    }

    /**
     * Método para exibir mensagens na área de log.
     * Utiliza SwingUtilities.invokeLater para garantir que a atualização
     * da interface seja feita na thread correta (EDT).
     */
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(message + "\n");
            // Rola automaticamente para o final
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    /**
     * Método para exibir e logar o erro.
     */
    private void logError(Exception e) {
        logMessage("ERRO: " + e.getMessage());
        e.printStackTrace(); // Imprime o stack trace no console para depuração
    }

    public static void main(String[] args) {
        // Garante que a interface seja criada na thread de eventos (EDT)
        SwingUtilities.invokeLater(() -> {
            TelaSistema frame = new TelaSistema();
            frame.setVisible(true);
        });
    }
}