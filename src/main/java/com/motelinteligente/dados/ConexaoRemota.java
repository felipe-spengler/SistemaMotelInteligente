package com.motelinteligente.dados;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ConexaoRemota {

    private static final Logger log = LogManager.getLogger(ConexaoRemota.class);

    private static HikariDataSource remoteDataSource;

    static {
        inicializarDataSource();
    }

    private static void inicializarDataSource() {
        try {
            HikariConfig remoteConfig = new HikariConfig();
            remoteConfig.setJdbcUrl(CarregarVariaveis.getRemoteDbUrl());
            remoteConfig.setUsername(CarregarVariaveis.getUser());
            remoteConfig.setPassword(CarregarVariaveis.getPassword());
            remoteConfig.setMaximumPoolSize(5); // menos conexões para remoto
            remoteConfig.setMinimumIdle(0);
            remoteConfig.setIdleTimeout(300_000); // 5 minutos
            remoteConfig.setMaxLifetime(600_000); // 10 minutos
            remoteConfig.setKeepaliveTime(60_000); // ping a cada 1 min
            remoteConfig.setConnectionTimeout(30_000); // 30s
            remoteConfig.setConnectionTestQuery("SELECT 1");

            remoteDataSource = new HikariDataSource(remoteConfig);
            //log.info("Conexão remota inicializada com sucesso.");
            inicializarBancoDeDadosRemoto();

        } catch (Exception e) {
            log.error("Falha ao inicializar conexão remota: " + e.getMessage(), e);
            remoteDataSource = null;
        }
    }

    private static void inicializarBancoDeDadosRemoto() {
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

        String sqlRetiradas = "CREATE TABLE IF NOT EXISTS retiradas_caixa (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idcaixa INT NOT NULL," +
                "  valor FLOAT NOT NULL," +
                "  quem VARCHAR(100) NOT NULL," +
                "  justificativa VARCHAR(255) NOT NULL," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_retiradas_caixa_idcaixa (idcaixa)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        String sqlAuditoria = "CREATE TABLE IF NOT EXISTS auditoria_locacoes (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  idlocacao INT NOT NULL," +
                "  usuario VARCHAR(100) NOT NULL," +
                "  horario TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  campo_alterado VARCHAR(100) NOT NULL," +
                "  valor_antigo VARCHAR(255) DEFAULT NULL," +
                "  valor_novo VARCHAR(255) DEFAULT NULL," +
                "  PRIMARY KEY (id)," +
                "  KEY idx_auditoria_locacoes_idlocacao (idlocacao)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";

        try (Connection link = remoteDataSource.getConnection();
             java.sql.Statement stmt = link.createStatement()) {
            stmt.executeUpdate(sqlVendasAvulsas);
            stmt.executeUpdate(sqlDespesas);
            stmt.executeUpdate(sqlRetiradas);
            stmt.executeUpdate(sqlAuditoria);
            //log.info("Tabelas no banco remoto inicializadas/verificadas com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao inicializar tabelas no banco de dados remoto: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (remoteDataSource == null) {
            log.warn("remoteDataSource estava null. Tentando reinicializar...");
            inicializarDataSource();
            if (remoteDataSource == null) {
                throw new SQLException("Não foi possível inicializar remoteDataSource.");
            }
        }
        return remoteDataSource.getConnection();
    }

    public static void fecharPool() {
        if (remoteDataSource != null) {
            //log.info("Fechando pool de conexões remotas...");
            remoteDataSource.close();
            remoteDataSource = null;
        }
    }
}
