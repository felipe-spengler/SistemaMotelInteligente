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
            remoteConfig.setMaximumPoolSize(5);  // menos conexões para remoto
            remoteConfig.setMinimumIdle(0);
            remoteConfig.setIdleTimeout(15_000); // 15s, menor que os 20s do servidor
            remoteConfig.setMaxLifetime(18_000); // 18s, também menor que os 20s do servidor
            remoteConfig.setKeepaliveTime(300_000); // ping a cada 5 min
            remoteConfig.setConnectionTimeout(10_000); // timeout rápido
            remoteConfig.setConnectionTestQuery("SELECT 1");

            remoteDataSource = new HikariDataSource(remoteConfig);
            log.info("Conexão remota inicializada com sucesso.");

        } catch (Exception e) {
            log.error("Falha ao inicializar conexão remota: " + e.getMessage(), e);
            remoteDataSource = null;
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
            log.info("Fechando pool de conexões remotas...");
            remoteDataSource.close();
            remoteDataSource = null;
        }
    }
}
