package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class fazconexao {

    // 🔑 Usar um único DataSource (pool) em toda a aplicação
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(CarregarVariaveis.getLocalDbUrl());
            config.setUsername(CarregarVariaveis.getUser());
            config.setPassword(CarregarVariaveis.getPassword());

            // Configurações recomendadas
            config.setMaximumPoolSize(15);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 min
            config.setConnectionTimeout(30000);
            config.setLeakDetectionThreshold(60000);
            // Previne conexões zumbis
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(3000);  // 3s pra testar
            config.setMaxLifetime(1800000);     // recicla a conexão a cada 30 min
            config.setKeepaliveTime(300000);    // manda ping a cada 5 min
            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao inicializar pool de conexões: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection conectar() throws SQLException {
        // Agora, cada chamada pega uma conexão do pool
        Connection conn = dataSource.getConnection();
        return createConnectionProxy(conn);
    }

    private Connection createConnectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                new Class[]{Connection.class},
                new BackupProxyHandler(connection)
        );
    }

    public int verificaCaixa() {
        String consultaSQL = "SELECT id FROM caixa WHERE saldofecha IS NULL";
        try (Connection link = conectar(); PreparedStatement statement = link.prepareStatement(consultaSQL); ResultSet resultado = statement.executeQuery()) {

            if (resultado.next()) {
                return resultado.getInt("id");
            }
            return 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
            return 0;
        }
    }

    // Método opcional para fechar o pool ao encerrar o sistema
    public static void fecharPool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
