package com.motelinteligente.dados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JTextArea;
import org.springframework.stereotype.Service; // Import the Service annotation

@Service // Add this annotation
public class DatabaseSynchronizer {

    private JTextArea exibirLogs;

    public void sincronizarBanco(JTextArea logs) throws SQLException {
        if (logs != null) {
            exibirLogs = logs;
        }

        List<String> tabelasIgnoradas = List.of("login_acesso", "login_registros", "log_sincronizacao");
        String[] tables = getTables();

        try (
                Connection conexaoLocal = new fazconexao().conectar(); Connection conexaoRemoto = ConexaoRemota.getConnection()) {
            for (String tabela : tables) {
                if (tabelasIgnoradas.contains(tabela)) {
                    if (exibirLogs != null) {
                        exibirLogs.append(String.format("Tabela %s ignorada na sincronização.%n", tabela));
                    }
                    System.out.printf("Tabela %s ignorada na sincronização.%n", tabela);
                    continue;
                }

                if (!sincronizarTabela(tabela, conexaoLocal, conexaoRemoto)) {
                    System.out.printf("Tabela %s não sincronizada.%n", tabela);
                    if (exibirLogs != null) {
                        exibirLogs.append(String.format("Tabela %s não sincronizada.%n", tabela));
                    }
                }
            }

        } catch (SQLException e) {
            if (exibirLogs != null) {
                exibirLogs.append(String.format("Erro na sincronização: %s%n", e.getMessage()));
            }
        } catch (Exception e) {
            if (exibirLogs != null) {
                exibirLogs.append(String.format("Erro inesperado: %s%n", e.getMessage()));
            }
        }
    }

    private String[] getTables() throws SQLException {
        List<String> tableList = new ArrayList<>();
        try (Connection conexao = new fazconexao().conectar()) {
            DatabaseMetaData metaData = conexao.getMetaData();
            String catalog = conexao.getCatalog();
            try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    tableList.add(tableName);
                }
            }
        }
        return tableList.toArray(new String[0]);
    }

    private static class ColumnDef {
        String name;
        String type;
        boolean nullable;
        String defaultValue;
        String extra;
    }

    private boolean sincronizarTabela(String tabela, Connection conexaoLocal, Connection conexaoRemoto) throws SQLException {
        System.out.printf("Iniciando sincronização para a tabela: %s%n", tabela);
        if (exibirLogs != null) {
            //exibirLogs.append(String.format("Iniciando sincronização para a tabela: %s%n", tabela));
        }

        boolean localExists = tableExists(tabela, conexaoLocal);
        boolean remoteExists = tableExists(tabela, conexaoRemoto);

        // Se a tabela não existe em um dos bancos, cria-a dinamicamente
        if (!localExists && remoteExists) {
            criarTabelaAusente(tabela, conexaoRemoto, conexaoLocal);
        } else if (localExists && !remoteExists) {
            criarTabelaAusente(tabela, conexaoLocal, conexaoRemoto);
        } else if (!localExists && !remoteExists) {
            System.out.printf("A tabela %s não existe em nenhum dos bancos de dados.%n", tabela);
            return false;
        }

        // Sincroniza a estrutura das colunas (Auto-healing de campos ausentes como impressora_ativa)
        sincronizarColunas(tabela, conexaoLocal, conexaoRemoto);

        String pkColumnName = getPrimaryKeyColumn(tabela, conexaoLocal);
        if (pkColumnName == null) {
            if (exibirLogs != null) {
                exibirLogs.append(String.format("Tabela %s não possui chave primária definida.%n", tabela));
            }
            System.out.printf("Tabela %s não possui chave primária definida.%n", tabela);
            return false;
        }

        // Sincronizar registros do local para o remoto (Inserts/Updates)
        sincronizarRegistros(tabela, pkColumnName, conexaoLocal, conexaoRemoto, "local");

        // Sincronizar remoções do local para o remoto
        sincronizarRemocoes(tabela, pkColumnName, conexaoLocal, conexaoRemoto, "local");

        return true;
    }

    private void criarTabelaAusente(String tabela, Connection origem, Connection destino) throws SQLException {
        String showCreate = "SHOW CREATE TABLE " + tabela;
        String createSql = null;
        try (Statement stmt = origem.createStatement(); ResultSet rs = stmt.executeQuery(showCreate)) {
            if (rs.next()) {
                createSql = rs.getString(2);
            }
        }
        if (createSql != null) {
            System.out.println("Criando tabela ausente com SQL: " + createSql);
            if (exibirLogs != null) {
                exibirLogs.append(String.format("Criando tabela ausente '%s' no destino...%n", tabela));
            }
            try (Statement stmt = destino.createStatement()) {
                stmt.executeUpdate(createSql);
            }
        }
    }

    private void sincronizarColunas(String tabela, Connection local, Connection remoto) throws SQLException {
        List<ColumnDef> localCols = getColumns(tabela, local);
        List<ColumnDef> remoteCols = getColumns(tabela, remoto);

        // Colunas locais que não existem no remoto -> Adiciona no remoto
        for (ColumnDef col : localCols) {
            if (!hasColumn(remoteCols, col.name)) {
                adicionarColuna(tabela, col, remoto);
            }
        }

        // Colunas remotas que não existem no local -> Adiciona no local
        for (ColumnDef col : remoteCols) {
            if (!hasColumn(localCols, col.name)) {
                adicionarColuna(tabela, col, local);
            }
        }
    }

    private List<ColumnDef> getColumns(String tabela, Connection conn) throws SQLException {
        List<ColumnDef> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + tabela)) {
            while (rs.next()) {
                ColumnDef col = new ColumnDef();
                col.name = rs.getString("Field");
                col.type = rs.getString("Type");
                col.nullable = "YES".equalsIgnoreCase(rs.getString("Null"));
                col.defaultValue = rs.getString("Default");
                col.extra = rs.getString("Extra");
                list.add(col);
            }
        }
        return list;
     }

     private boolean hasColumn(List<ColumnDef> list, String name) {
         for (ColumnDef col : list) {
             if (col.name.equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }

     private void adicionarColuna(String tabela, ColumnDef col, Connection conn) throws SQLException {
         StringBuilder sql = new StringBuilder("ALTER TABLE " + tabela + " ADD COLUMN `" + col.name + "` " + col.type);
         if (!col.nullable) {
             sql.append(" NOT NULL");
         } else {
             sql.append(" NULL");
         }
         if (col.defaultValue != null) {
             if (col.defaultValue.equalsIgnoreCase("CURRENT_TIMESTAMP") || col.defaultValue.equalsIgnoreCase("NULL")) {
                 sql.append(" DEFAULT ").append(col.defaultValue);
             } else {
                 sql.append(" DEFAULT '").append(col.defaultValue.replace("'", "''")).append("'");
             }
         }
         if (col.extra != null && !col.extra.isEmpty()) {
             sql.append(" ").append(col.extra);
         }
         
         String alterSql = sql.toString();
         System.out.println("Executando: " + alterSql);
         if (exibirLogs != null) {
             exibirLogs.append(String.format("Adicionando coluna ausente '%s' na tabela '%s'...%n", col.name, tabela));
         }
         try (Statement stmt = conn.createStatement()) {
             stmt.executeUpdate(alterSql);
         }
     }

    private boolean tableExists(String tableName, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        String catalog = conexao.getCatalog();
        try (ResultSet rs = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private String getPrimaryKeyColumn(String tabela, Connection conexao) throws SQLException {
        DatabaseMetaData metaData = conexao.getMetaData();
        String catalog = conexao.getCatalog();
        try (ResultSet pkResultSet = metaData.getPrimaryKeys(catalog, null, tabela)) {
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

        try (Statement stmtOrigem = origem.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rsOrigem = stmtOrigem.executeQuery(selectQuery); Statement stmtDestino = destino.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rsDestino = stmtDestino.executeQuery(selectQuery)) {

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

        try (PreparedStatement stmtOrigem = origem.prepareStatement(selectOrigem); ResultSet rsOrigem = stmtOrigem.executeQuery(); PreparedStatement stmtDestino = destino.prepareStatement(selectDestino); ResultSet rsDestino = stmtDestino.executeQuery(); PreparedStatement deleteStmt = destino.prepareStatement(deleteQuery)) {

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
        String catalog = conexao.getCatalog();
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tabela, null)) {
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
        String catalog = conexao.getCatalog();
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tabela, null)) {
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

}
