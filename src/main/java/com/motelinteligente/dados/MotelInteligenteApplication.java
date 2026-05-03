package com.motelinteligente.dados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableScheduling // Habilita o agendamento de tarefas
public class MotelInteligenteApplication {

    private static final Logger logger = LoggerFactory.getLogger(MotelInteligenteApplication.class);

    @Autowired
    private DatabaseSynchronizer databaseSynchronizer;

    public static void main(String[] args) {
        SSLUtil.disableSSLCertificateChecking();

        // Substituindo SpringApplication.run(...) para configurar a porta externa
        SpringApplication app = new SpringApplication(MotelInteligenteApplication.class);
        // app.setDefaultProperties(
        // Collections.singletonMap("server.port", portaExterna)
        // );
        app.run(args);
        new VerificaComandosRemotos().start();

        try {
            URL url = URI.create("http://checkip.amazonaws.com/").toURL();
            URLConnection con = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String externalIP = reader.readLine();
            System.out.println("IP externo é: " + externalIP);
            reader.close();

            try (Connection link = new fazconexao().conectar();
                    PreparedStatement statement = link.prepareStatement("SELECT meuip FROM configuracoes");
                    ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {
                    String meuip = resultado.getString("meuip");
                    if ((meuip == null) || (!meuip.equals(externalIP))) {
                        String sql = "UPDATE configuracoes SET meuip = ?";
                        try (PreparedStatement statementUpdate = link.prepareStatement(sql)) {
                            statementUpdate.setString(1, externalIP);
                            statementUpdate.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro de SQL: " + e.getMessage());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro de E/S: " + e.getMessage());
        }
    }
}

@Component
class Agendamentos {

    private static final Logger logger = LoggerFactory.getLogger(Agendamentos.class);

    // 1. Injete a dependência da classe DatabaseSynchronizer
    @Autowired
    private DatabaseSynchronizer databaseSynchronizer;
    @Autowired
    private TelaSistema telaSistema;

    // Agendar para as 03:00 da manhã
    @Scheduled(cron = "0 0 3 * * ?")
    public void agendarTresDaManha() {
        logger.info("Executando a tarefa agendada para 03:00.");
        try {
            // 2. Chame o método a partir da instância injetada
            databaseSynchronizer.sincronizarBanco(null);
            logger.info("Sincronização agendada concluída com sucesso.");
        } catch (SQLException e) {
            logger.error("Erro durante a sincronização agendada: " + e.getMessage(), e);
        }
    }

    // Agendar para o meio-dia (12:00)
    @Scheduled(cron = "0 0 12 * * ?")
    public void agendarMeioDia() {
        logger.info("Verificando se há nova versão do sistema...");

        // Verifica antes de abrir a tela
        boolean precisaAtualizar = false;
        try {
            precisaAtualizar = telaSistema.temNovaVersaoDisponivel();
        } catch (Exception e) {
            logger.error("Erro ao verificar atualização: ", e);
        }

        if (precisaAtualizar) {
            logger.info("Nova versão detectada. Exibindo tela de atualização...");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Uma nova versão do sistema foi encontrada! Iniciando atualização...",
                        "Atualização Disponível",
                        JOptionPane.INFORMATION_MESSAGE);

                telaSistema.setVisible(true);
                telaSistema.getStartButton().doClick();
            });
        } else {
            logger.info("Nenhuma atualização disponível no momento.");
        }
    }
}