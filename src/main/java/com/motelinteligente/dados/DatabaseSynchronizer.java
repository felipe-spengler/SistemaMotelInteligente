package com.motelinteligente.dados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

        // Sincronizar registros do local para o remoto (Inserts/Updates)
        sincronizarRegistros(tabela, pkColumnName, conexaoLocal, conexaoRemoto, "local");

        // Sincronizar registros do remoto para o local (Inserts/Updates)
        sincronizarRegistros(tabela, pkColumnName, conexaoRemoto, conexaoLocal, "remoto");

        // Sincronizar remoções do local para o remoto
        sincronizarRemocoes(tabela, pkColumnName, conexaoLocal, conexaoRemoto, "local");

        // Sincronizar remoções do remoto para o local
        sincronizarRemocoes(tabela, pkColumnName, conexaoRemoto, conexaoLocal, "remoto");

        return true;
    }

    private boolean tableExists(String tableName, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private String getPrimaryKeyColumn(String tabela, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        try (ResultSet pkResultSet = metaData.getPrimaryKeys(null, null, tabela)) {
            if (pkResultSet.next()) {
                return pkResultSet.getString("COLUMN_NAME");
            }
        }
        return null;
    }

    private void sincronizarRegistros(String tabela, String pkColumnName, Connection origem, Connection destino, String tipoOrigem) throws SQLException {
        String selectQuery = "SELECT * FROM " + tabela;
        String insertQuery = buildInsertQuery(tabela, origem);
        String updateQuery = buildUpdateQuery(tabela, pkColumnName, origem);
        
        try (Statement stmtOrigem = origem.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rsOrigem = stmtOrigem.executeQuery(selectQuery);
             Statement stmtDestino = destino.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rsDestino = stmtDestino.executeQuery(selectQuery)) {

            Map<Object, Map<String, Object>> destinoData = new HashMap<>();
            ResultSetMetaData metaDataDestino = rsDestino.getMetaData();
            int columnCountDestino = metaDataDestino.getColumnCount();

            while (rsDestino.next()) {
                Object pkValue = rsDestino.getObject(pkColumnName);
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCountDestino; i++) {
                    rowData.put(metaDataDestino.getColumnName(i), rsDestino.getObject(i));
                }
                destinoData.put(pkValue, rowData);
            }

            ResultSetMetaData metaDataOrigem = rsOrigem.getMetaData();
            int columnCountOrigem = metaDataOrigem.getColumnCount();

            while (rsOrigem.next()) {
                Object pkValorOrigem = rsOrigem.getObject(pkColumnName);
                if (!destinoData.containsKey(pkValorOrigem)) {
                    // Registro não existe no destino, insere
                    try (PreparedStatement insertStmt = destino.prepareStatement(insertQuery)) {
                        for (int i = 1; i <= columnCountOrigem; i++) {
                            insertStmt.setObject(i, rsOrigem.getObject(i));
                        }
                        insertStmt.executeUpdate();
                        System.out.printf("Registro com %s = %s inserido de %s para %s na tabela %s.%n",
                                pkColumnName, pkValorOrigem, tipoOrigem, tipoOrigem.equals("local") ? "remoto" : "local", tabela);
                    }
                } else {
                    // Registro existe, verifica se precisa de update
                    Map<String, Object> destinoRow = destinoData.get(pkValorOrigem);
                    boolean isDifferent = false;
                    for (int i = 1; i <= columnCountOrigem; i++) {
                        String columnName = metaDataOrigem.getColumnName(i);
                        Object origemValue = rsOrigem.getObject(i);
                        Object destinoValue = destinoRow.get(columnName);

                        if (origemValue != null && !origemValue.equals(destinoValue)) {
                            isDifferent = true;
                            break;
                        } else if (origemValue == null && destinoValue != null) {
                            isDifferent = true;
                            break;
                        }
                    }

                    if (isDifferent) {
                        try (PreparedStatement updateStmt = destino.prepareStatement(updateQuery)) {
                            int paramIndex = 1;
                            for (int i = 1; i <= columnCountOrigem; i++) {
                                if (!metaDataOrigem.getColumnName(i).equalsIgnoreCase(pkColumnName)) {
                                    updateStmt.setObject(paramIndex++, rsOrigem.getObject(i));
                                }
                            }
                            updateStmt.setObject(paramIndex, pkValorOrigem);
                            updateStmt.executeUpdate();
                            System.out.printf("Registro com %s = %s atualizado de %s para %s na tabela %s.%n",
                                    pkColumnName, pkValorOrigem, tipoOrigem, tipoOrigem.equals("local") ? "remoto" : "local", tabela);
                        }
                    }
                }
            }
        }
    }
    
    private void sincronizarRemocoes(String tabela, String pkColumnName, Connection origem, Connection destino, String tipoOrigem) throws SQLException {
        String selectOrigem = "SELECT " + pkColumnName + " FROM " + tabela;
        String selectDestino = "SELECT " + pkColumnName + " FROM " + tabela;
        String deleteQuery = "DELETE FROM " + tabela + " WHERE " + pkColumnName + " = ?";

        try (PreparedStatement stmtOrigem = origem.prepareStatement(selectOrigem);
             ResultSet rsOrigem = stmtOrigem.executeQuery();
             PreparedStatement stmtDestino = destino.prepareStatement(selectDestino);
             ResultSet rsDestino = stmtDestino.executeQuery();
             PreparedStatement deleteStmt = destino.prepareStatement(deleteQuery)) {

            List<Object> pksOrigem = new ArrayList<>();
            while (rsOrigem.next()) {
                pksOrigem.add(rsOrigem.getObject(1));
            }

            while (rsDestino.next()) {
                Object pkValorDestino = rsDestino.getObject(1);
                if (!pksOrigem.contains(pkValorDestino)) {
                    deleteStmt.setObject(1, pkValorDestino);
                    deleteStmt.executeUpdate();
                    System.out.printf("Registro com %s = %s deletado do %s na tabela %s.%n",
                            pkColumnName, pkValorDestino, tipoOrigem.equals("local") ? "remoto" : "local", tabela);
                }
            }
        }
    }

    private String buildInsertQuery(String tabela, Connection conexao) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData metaData = conexao.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tabela, null)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        String columnList = String.join(", ", columns);
        String valuePlaceholders = String.join(", ", columns.stream().map(c -> "?").toList());
        return "INSERT INTO " + tabela + " (" + columnList + ") VALUES (" + valuePlaceholders + ")";
    }

    private String buildUpdateQuery(String tabela, String pkColumnName, Connection conexao) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData metaData = conexao.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tabela, null)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                if (!columnName.equalsIgnoreCase(pkColumnName)) {
                    columns.add(columnName + " = ?");
                }
            }
        }
        String updateSet = String.join(", ", columns);
        return "UPDATE " + tabela + " SET " + updateSet + " WHERE " + pkColumnName + " = ?";
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