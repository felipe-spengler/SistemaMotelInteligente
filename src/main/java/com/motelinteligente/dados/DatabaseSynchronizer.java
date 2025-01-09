package com.motelinteligente.dados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSynchronizer {

    private static final String LOCAL_DB_URL = "jdbc:mysql://localhost:3306/u876938716_motel";
    private static final String REMOTE_DB_URL = "jdbc:mysql://srv1196.hstgr.io/u876938716_motel";
    private static final String USER = "u876938716_contato";
    private static final String PASSWORD = "Felipe0110@";

    public void sincronizarBanco() throws SQLException {
        String[] tables = getTables();
        try (Connection conexaoLocal = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD);
             Connection conexaoRemoto = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD)) {

            for (String tabela : tables) {
                if (!sincronizarTabela(tabela, conexaoLocal, conexaoRemoto)) {
                    System.out.printf("Tabela %s não sincronizada.%n", tabela);
                }
            }
        }
    }

    private String[] getTables() throws SQLException {
        List<String> tableList = new ArrayList<>();
        try (Connection conexao = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD)) {
            DatabaseMetaData metaData = conexao.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableList.add(tableName);
            }
        }
        return tableList.toArray(new String[0]);
    }

    private boolean sincronizarTabela(String tabela, Connection conexaoLocal, Connection conexaoRemoto) throws SQLException {
        System.out.printf("Iniciando sincronização para a tabela: %s%n", tabela);

        if (!tableExists(tabela, conexaoLocal) || !tableExists(tabela, conexaoRemoto)) {
            System.out.printf("A tabela %s não existe em um dos bancos de dados.%n", tabela);
            return false;
        }

        String pkColumnName = getPrimaryKeyColumn(tabela, conexaoLocal);
        if (pkColumnName == null) {
            System.out.printf("Tabela %s não possui chave primária definida.%n", tabela);
            return false;
        }

        // Sincronizar registros do local para o remoto
        sincronizarRegistros(tabela, pkColumnName, conexaoLocal, conexaoRemoto, "local");

        // Sincronizar registros do remoto para o local
        sincronizarRegistros(tabela, pkColumnName, conexaoRemoto, conexaoLocal, "remoto");

        return true;
    }

    private boolean tableExists(String tableName, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
        return rs.next();
    }

    private String getPrimaryKeyColumn(String tabela, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        ResultSet pkResultSet = metaData.getPrimaryKeys(null, null, tabela);

        if (pkResultSet.next()) {
            return pkResultSet.getString("COLUMN_NAME");
        }
        return null;
    }

    private void sincronizarRegistros(String tabela, String pkColumnName, Connection origem, Connection destino, String tipoOrigem) throws SQLException {
        String selectQuery = "SELECT * FROM " + tabela;
        String insertQuery = buildInsertQuery(tabela, origem);

        try (PreparedStatement stmtOrigem = origem.prepareStatement(selectQuery);
             ResultSet rsOrigem = stmtOrigem.executeQuery();
             PreparedStatement stmtDestino = destino.prepareStatement(selectQuery);
             ResultSet rsDestino = stmtDestino.executeQuery()) {

            List<Object> pkDestinos = new ArrayList<>();
            while (rsDestino.next()) {
                pkDestinos.add(rsDestino.getObject(pkColumnName));
            }

            while (rsOrigem.next()) {
                Object pkValorOrigem = rsOrigem.getObject(pkColumnName);

                if (!pkDestinos.contains(pkValorOrigem)) {
                    // Registro do origem não está no destino, então insere
                    try (PreparedStatement insertStmt = destino.prepareStatement(insertQuery)) {
                        for (int i = 1; i <= rsOrigem.getMetaData().getColumnCount(); i++) {
                            insertStmt.setObject(i, rsOrigem.getObject(i));
                        }
                        insertStmt.executeUpdate();
                        System.out.printf("Registro com %s = %s inserido de %s para %s na tabela %s.%n",
                                pkColumnName, pkValorOrigem, tipoOrigem, tipoOrigem.equals("local") ? "remoto" : "local", tabela);
                    }
                }
            }
        }
    }

    private String buildInsertQuery(String tabela, Connection conexao) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData metaData = conexao.getMetaData();
        ResultSet resultSet = metaData.getColumns(null, null, tabela, null);

        while (resultSet.next()) {
            columns.add(resultSet.getString("COLUMN_NAME"));
        }

        String columnList = String.join(", ", columns);
        String valuePlaceholders = String.join(", ", columns.stream().map(c -> "?").toList());

        return "INSERT INTO " + tabela + " (" + columnList + ") VALUES (" + valuePlaceholders + ")";
    }

    public static void main(String[] args) {
        DatabaseSynchronizer synchronizer = new DatabaseSynchronizer();
        try {
            synchronizer.sincronizarBanco();
            System.out.println("Sincronização concluída.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
