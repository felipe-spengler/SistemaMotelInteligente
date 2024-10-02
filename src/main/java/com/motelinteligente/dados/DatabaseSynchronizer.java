package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSynchronizer {

    private static final String LOCAL_DB_URL = "jdbc:mysql://localhost:3306/u876938716_motel";
    private static final String REMOTE_DB_URL = "jdbc:mysql://srv1196.hstgr.io/u876938716_motel";
    private static final String USER = "u876938716_contato";
    private static final String PASSWORD = "Felipe0110@";

    public void sincronizarBanco() throws SQLException {
        String[] tables = getTables();
        try ( Connection conexaoLocal = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD);  Connection conexaoRemoto = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD)) {

            for (String tabela : tables) {
                if (!sincronizarTabela(tabela, conexaoLocal, conexaoRemoto)) {
                    System.out.printf("Tabela %s não sincronizada.%n", tabela);
                }
            }
        }
    }

    private String[] getTables() throws SQLException {
        List<String> tableList = new ArrayList<>();
        try ( Connection conexao = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD)) {
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
        System.out.printf("Iniciando sincronização para a tabela: %s%n", tabela);  // Mensagem de log

        if (!tableExists(tabela, conexaoLocal) || !tableExists(tabela, conexaoRemoto)) {
            System.out.printf("A tabela %s não existe em um dos bancos de dados.%n", tabela);
            return false;
        }

        List<String> columns = getTableColumns(tabela, conexaoLocal);
        if (columns.isEmpty()) {
            System.out.printf("Tabela %s não possui colunas.%n", tabela);
            return false;
        }

        String columnList = String.join(", ", columns);
        String queryLocal = "SELECT *, CRC32(CONCAT_WS('|', " + columnList + ")) AS checksum FROM " + tabela;
        String queryRemoto = "SELECT *, CRC32(CONCAT_WS('|', " + columnList + ")) AS checksum FROM " + tabela;

        String pkColumnName = getPrimaryKeyColumn(tabela, conexaoLocal);
        if (pkColumnName == null) {
            System.out.printf("Tabela %s não possui chave primária definida.%n", tabela);
            return false;
        }

        try ( PreparedStatement stmtLocal = conexaoLocal.prepareStatement(queryLocal);  PreparedStatement stmtRemoto = conexaoRemoto.prepareStatement(queryRemoto);  ResultSet rsLocal = stmtLocal.executeQuery();  ResultSet rsRemoto = stmtRemoto.executeQuery()) {

            // Armazenar resultados do remoto em uma lista
            List<Long> remoteChecksums = new ArrayList<>();
            List<Integer> remotePkValues = new ArrayList<>();
            while (rsRemoto.next()) {
                remoteChecksums.add(rsRemoto.getLong("checksum"));
                remotePkValues.add(rsRemoto.getInt(pkColumnName));
            }

            while (rsLocal.next()) {
                Long checksumLocal = rsLocal.getLong("checksum");
                int pkValueLocal = rsLocal.getInt(pkColumnName);

                int index = remotePkValues.indexOf(pkValueLocal);
                if (index != -1) {
                    Long checksumRemoto = remoteChecksums.get(index);
                    if (!checksumLocal.equals(checksumRemoto)) {
                        System.out.printf("Discrepância encontrada na tabela %s para %s %d: Local: %d, Remoto: %d%n",
                                tabela, pkColumnName, pkValueLocal, checksumLocal, checksumRemoto);
                        corrigirLinha(tabela, pkValueLocal, conexaoLocal, conexaoRemoto);
                    }
                } else {
                    System.out.printf("Registro com %s %d não encontrado na tabela remota %s.%n", pkColumnName, pkValueLocal, tabela);
                }
            }
        }
        return true;
    }

    private boolean tableExists(String tableName, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
        return rs.next();
    }

    private List<String> getTableColumns(String tabela, Connection conexao) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData metaData = conexao.getMetaData();
        ResultSet resultSet = metaData.getColumns(null, null, tabela, null);

        while (resultSet.next()) {
            columns.add(resultSet.getString("COLUMN_NAME"));
        }
        return columns;
    }

    private String getPrimaryKeyColumn(String tabela, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        ResultSet pkResultSet = metaData.getPrimaryKeys(null, null, tabela);

        if (pkResultSet.next()) {
            return pkResultSet.getString("COLUMN_NAME");
        }
        return null;
    }

    private void corrigirLinha(String tabela, int pkValue, Connection conexaoLocal, Connection conexaoRemoto) throws SQLException {
        String pkColumnName = getPrimaryKeyColumn(tabela, conexaoLocal);

        String selectQuery = "SELECT * FROM " + tabela + " WHERE " + pkColumnName + " = ?";
        try ( PreparedStatement stmtSelect = conexaoLocal.prepareStatement(selectQuery)) {
            stmtSelect.setInt(1, pkValue);
            ResultSet rs = stmtSelect.executeQuery();

            if (rs.next()) {
                StringBuilder updateQuery = new StringBuilder("UPDATE " + tabela + " SET ");
                int columnCount = rs.getMetaData().getColumnCount();
                List<Object> parameters = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    if (!columnName.equals(pkColumnName)) {
                        updateQuery.append(columnName).append(" = ?, ");
                        parameters.add(rs.getObject(i));  // Adiciona o valor correspondente à lista
                    }
                }

                updateQuery.setLength(updateQuery.length() - 2); // Remove última vírgula
                updateQuery.append(" WHERE " + pkColumnName + " = ?");
                parameters.add(pkValue);  // Adiciona o valor da chave primária

                try ( PreparedStatement stmtUpdate = conexaoRemoto.prepareStatement(updateQuery.toString())) {
                    for (int i = 0; i < parameters.size(); i++) {
                        stmtUpdate.setObject(i + 1, parameters.get(i));  // Define todos os parâmetros
                    }
                    stmtUpdate.executeUpdate();
                    System.out.printf("Linha com %s %d na tabela %s foi corrigida.%n", pkColumnName, pkValue, tabela);
                }
            }
        }
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
