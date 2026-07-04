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
            config.setValidationTimeout(3000); // 3s pra testar
            config.setMaxLifetime(600000); // recicla a conexão a cada 10 min
            config.setKeepaliveTime(60000); // manda ping a cada 1 min
            dataSource = new HikariDataSource(config);
            inicializarBancoDeDados();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao inicializar pool de conexões: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void inicializarBancoDeDados() {
        String sqlVendasAvulsas = "CREATE TABLE IF NOT EXISTS vendas_avulsas (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idcaixa INT NOT NULL," +
                "  idproduto INT NOT NULL," +
                "  descricao VARCHAR(255) NOT NULL," +
                "  quantidade INT NOT NULL," +
                "  valorunidade FLOAT NOT NULL," +
                "  valortotal FLOAT NOT NULL," +
                "  tipo VARCHAR(50) NOT NULL," +
                "  formapagamento VARCHAR(50) NOT NULL," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_vendas_avulsas_idcaixa (idcaixa)," +
                "  KEY idx_vendas_avulsas_idproduto (idproduto)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        String sqlDespesas = "CREATE TABLE IF NOT EXISTS despesas (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idcaixa INT DEFAULT NULL," +
                "  descricao VARCHAR(255) NOT NULL," +
                "  categoria VARCHAR(100) NOT NULL," +
                "  valor FLOAT NOT NULL," +
                "  formapagamento VARCHAR(50) NOT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'pago'," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_despesas_idcaixa (idcaixa)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        try (Connection link = dataSource.getConnection();
             java.sql.Statement stmt = link.createStatement()) {
            stmt.executeUpdate(sqlVendasAvulsas);
            stmt.executeUpdate(sqlDespesas);
        } catch (Exception e) {
            System.err.println("Erro ao inicializar tabelas no banco de dados: " + e.getMessage());
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
                new Class[] { Connection.class },
                new BackupProxyHandler(connection));
    }

    public int verificaCaixa() {
        String consultaSQL = "SELECT id FROM caixa WHERE saldofecha IS NULL";
        try (Connection link = conectar();
                PreparedStatement statement = link.prepareStatement(consultaSQL);
                ResultSet resultado = statement.executeQuery()) {

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
