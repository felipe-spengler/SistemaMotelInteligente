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
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling // Habilita o agendamento de tarefas
public class MotelInteligenteApplication {
    private static final Logger logger = LoggerFactory.getLogger(MotelInteligenteApplication.class);

    @Autowired
    private DatabaseSynchronizer databaseSynchronizer;

    public static void main(String[] args) {
        SSLUtil.disableSSLCertificateChecking();
        SpringApplication.run(MotelInteligenteApplication.class, args);
        
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
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

    /**
     * Agenda a execução do método de sincronização para todos os dias às 3:00 da manhã.
     * A expressão cron "0 0 3 * * ?" significa:
     * - 0 segundos
     * - 0 minutos
     * - 3 horas
     * - * (qualquer dia do mês)
     * - * (qualquer mês)
     * - ? (qualquer dia da semana)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void sincronizarBancoDeDados() {
        logger.info("Executando a sincronização do banco de dados agendada para 03:00.");
        try {
            databaseSynchronizer.sincronizarBanco();
            logger.info("Sincronização agendada concluída com sucesso.");
        } catch (SQLException e) {
            logger.error("Erro durante a sincronização agendada: " + e.getMessage(), e);
        }
    }
}