package com.motelinteligente.dados;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

public class CheckSincronia {

    private static final String LOCAL_DB_URL = "jdbc:mysql://localhost:3306/u876938716_motel";
    private static final String REMOTE_DB_URL = "jdbc:mysql://srv1196.hstgr.io/u876938716_motel";
    private static final String USER = "u876938716_contato";
    private static final String PASSWORD = "Felipe0110@";

    public void start() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Executa a sincronização em uma nova thread
                new Thread(() -> {
                    try {
                        checkDatabaseSync();
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        }, 0, 60 * 60 * 1000); // Executa a cada 1 hora
    }

    private void checkDatabaseSync() throws UnsupportedEncodingException {
        String[] tables = {
            "antecipado", "caixa", "configuracoes", "desistencia", "funcionario", "imagens",
            "justificativa", "prevendidos", "produtos", "quartos",
            "registralimpeza", "registralocado", "registramanutencao", "registrareserva", "registravendido",
            "status"
        };

        Connection localConn = null;
        Connection remoteConn = null;

        try {
            localConn = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD);
            remoteConn = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD);

            boolean isSynced = true;

            // Verificar e sincronizar com base na tabela de logs
            for (String tabela : tables) {
                System.out.println("Verificando tabela: " + tabela);
                isSynced &= sincronizarTabela(tabela, localConn, remoteConn);
            }

            if (isSynced) {
                System.out.println("Sincronismo verificado com sucesso!");
            } else {
                System.out.println("Os bancos de dados não estão sincronizados e foram corrigidos.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (localConn != null) {
                try {
                    localConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Erro ao fechar a conexão local: " + e.getMessage());
                }
            }
            if (remoteConn != null) {
                try {
                    remoteConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Erro ao fechar a conexão remota: " + e.getMessage());
                }
            }
        }
    }

    private boolean sincronizarTabela(String tabela, Connection conexaoLocal, Connection conexaoRemoto) throws SQLException {
        String campoId = obterCampoId(tabela);

        if (campoId == null) {
            System.err.println("Campo de ID não encontrado para a tabela: " + tabela);
            return false;
        }

        String queryLog = "SELECT * FROM log_sincronizacao WHERE tabela_nome = ?";
        String querySelectLocal = "SELECT * FROM " + tabela + " WHERE " + campoId + " = ?";
        String querySelectRemoto = "SELECT * FROM " + tabela + " WHERE " + campoId + " = ?";

        boolean isSynced = true;

        try ( PreparedStatement stmtLog = conexaoLocal.prepareStatement(queryLog)) {
            stmtLog.setString(1, tabela);
            try ( ResultSet rsLog = stmtLog.executeQuery()) {

                while (rsLog.next()) {
                    Object id = rsLog.getObject("registro_id");

                    // Verificar registro no banco de dados local
                    try ( PreparedStatement stmtSelectLocal = conexaoLocal.prepareStatement(querySelectLocal);  PreparedStatement stmtSelectRemoto = conexaoRemoto.prepareStatement(querySelectRemoto)) {

                        stmtSelectLocal.setObject(1, id);
                        stmtSelectRemoto.setObject(1, id);

                        try ( ResultSet rsLocal = stmtSelectLocal.executeQuery();  ResultSet rsRemoto = stmtSelectRemoto.executeQuery()) {

                            if (rsLocal.next() && rsRemoto.next()) {
                                if (!compararRegistros(rsLocal, rsRemoto)) {
                                    isSynced = false;
                                    System.out.printf("Discrepância encontrada na tabela %s para ID %s%n", tabela, id);
                                    // Exportar dados para arquivos .txt
                                    exportarDados(rsLocal, tabela + "_local.txt");
                                    exportarDados(rsRemoto, tabela + "_remoto.txt");
                                    // Sincronizar registros
                                    sincronizarRegistro(tabela, campoId, id, conexaoLocal, conexaoRemoto);
                                }
                            } else {
                                // Sincronizar registros se necessário
                                sincronizarRegistro(tabela, campoId, id, conexaoLocal, conexaoRemoto);
                            }

                            // Excluir registro do log após sincronização
                            deletarRegistroLog(conexaoLocal, tabela, id);
                        }
                    }
                }
            }
        }

        return isSynced;
    }

    private String getColumnsList(String tabela, Connection conexaoRemoto) throws SQLException {
        try ( ResultSet rs = conexaoRemoto.getMetaData().getColumns(null, null, tabela, null)) {
            StringBuilder columns = new StringBuilder();
            while (rs.next()) {
                if (columns.length() > 0) {
                    columns.append(", ");
                }
                columns.append(rs.getString("COLUMN_NAME"));
            }
            return columns.toString();
        }
    }

    private String getPlaceholders(String tabela, Connection conexaoRemoto) throws SQLException {
        int columnCount = getColumnCount(tabela, conexaoRemoto);
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < columnCount; i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }
        return placeholders.toString();
    }

    private void sincronizarRegistro(String tabela, String campoId, Object id, Connection conexaoLocal, Connection conexaoRemoto) throws SQLException {
        String querySelectLocal = "SELECT * FROM " + tabela + " WHERE " + campoId + " = ?";
        String querySelectRemoto = "SELECT * FROM " + tabela + " WHERE " + campoId + " = ?";
        String queryInsertRemoto = "INSERT INTO " + tabela + " (" + getColumnsList(tabela, conexaoRemoto) + ") VALUES (" + getPlaceholders(tabela, conexaoRemoto) + ")";
        String queryUpdateRemoto = "UPDATE " + tabela + " SET " + getUpdateSetClause(tabela, campoId, conexaoRemoto) + " WHERE " + campoId + " = ?";
        configGlobal config = configGlobal.getInstance();

        try ( PreparedStatement stmtSelectLocal = conexaoLocal.prepareStatement(querySelectLocal);  PreparedStatement stmtSelectRemoto = conexaoRemoto.prepareStatement(querySelectRemoto);  PreparedStatement stmtInsertRemoto = conexaoRemoto.prepareStatement(queryInsertRemoto);  PreparedStatement stmtUpdateRemoto = conexaoRemoto.prepareStatement(queryUpdateRemoto)) {

            stmtSelectLocal.setObject(1, id);
            ResultSet rsLocal = stmtSelectLocal.executeQuery();

            stmtSelectRemoto.setObject(1, id);
            ResultSet rsRemoto = stmtSelectRemoto.executeQuery();

            if (rsLocal.next()) {
                if (!rsRemoto.next()) {
                    System.out.println("Inserindo registro na tabela remota: " + tabela + " ID: " + id);
                    copyRowToPreparedStatement(rsLocal, stmtInsertRemoto, tabela, campoId);
                    stmtInsertRemoto.executeUpdate();
                    config.incrementarContadorExecucoes();
                } else {
                    System.out.println("Atualizando registro na tabela remota: " + tabela + " ID: " + id);
                    copyRowToPreparedStatement(rsLocal, stmtUpdateRemoto, tabela, campoId);
                    stmtUpdateRemoto.setObject(getColumnCount(tabela, conexaoRemoto), id);
                    stmtUpdateRemoto.executeUpdate();
                    config.incrementarContadorExecucoes();
                }
            } else {
                if (rsRemoto.next()) {
                    System.out.println("Deletando registro na tabela remota: " + tabela + " ID: " + id);
                    String queryDeleteRemoto = "DELETE FROM " + tabela + " WHERE " + campoId + " = ?";
                    try ( PreparedStatement stmtDeleteRemoto = conexaoRemoto.prepareStatement(queryDeleteRemoto)) {
                        stmtDeleteRemoto.setObject(1, id);
                        stmtDeleteRemoto.executeUpdate();
                        config.incrementarContadorExecucoes();
                    }
                }
            }
        }
    }

    private void copyRowToPreparedStatement(ResultSet rs, PreparedStatement ps, String tableName, String idColumn) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName).append(" SET ");
        int paramIndex = 1;

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            if (!columnName.equals(idColumn)) {
                if (paramIndex > 1) {
                    sql.append(", ");
                }
                sql.append(columnName).append(" = ?");
                paramIndex++;
            }
        }

        sql.append(" WHERE ").append(idColumn).append(" = ?");
        System.out.println("Generated SQL: " + sql.toString());

        paramIndex = 1;
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            if (!columnName.equals(idColumn)) {
                Object value = rs.getObject(i);
                System.out.println("Parameter " + paramIndex + ": " + value);
                ps.setObject(paramIndex++, value);
            }
        }

        Object idValue = rs.getObject(idColumn);
        ps.setObject(paramIndex, idValue);
        System.out.println("Parameter " + paramIndex + " (ID): " + idValue);
    }

    private int getColumnCount(String tabela, Connection conexaoRemoto) throws SQLException {
        try ( ResultSet rs = conexaoRemoto.getMetaData().getColumns(null, null, tabela, null)) {
            int count = 0;
            while (rs.next()) {
                count++;
            }
            return count;
        }
    }

    private String getUpdateSetClause(String tabela, String campoId, Connection conexaoRemoto) throws SQLException {
        try ( ResultSet rs = conexaoRemoto.getMetaData().getColumns(null, null, tabela, null)) {
            StringBuilder setClause = new StringBuilder();
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (!columnName.equals(campoId)) {
                    if (setClause.length() > 0) {
                        setClause.append(", ");
                    }
                    setClause.append(columnName).append(" = ?");
                }
            }
            return setClause.toString();
        }
    }

    private void exportarDados(ResultSet rs, String filename) throws SQLException {
        try ( FileWriter writer = new FileWriter(filename)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                writer.write(metaData.getColumnName(i));
                if (i < columnCount) {
                    writer.write(", ");
                }
            }
            writer.write("\n");
            do {
                for (int i = 1; i <= columnCount; i++) {
                    writer.write(rs.getString(i));
                    if (i < columnCount) {
                        writer.write(", ");
                    }
                }
                writer.write("\n");
            } while (rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean compararRegistros(ResultSet rsLocal, ResultSet rsRemoto) throws SQLException {
        ResultSetMetaData metaDataLocal = rsLocal.getMetaData();
        ResultSetMetaData metaDataRemoto = rsRemoto.getMetaData();

        int columnCountLocal = metaDataLocal.getColumnCount();
        int columnCountRemoto = metaDataRemoto.getColumnCount();

        if (columnCountLocal != columnCountRemoto) {
            return false;
        }

        for (int i = 1; i <= columnCountLocal; i++) {
            Object localValue = rsLocal.getObject(i);
            Object remotoValue = rsRemoto.getObject(i);

            if ((localValue != null && !localValue.equals(remotoValue)) || (localValue == null && remotoValue != null)) {
                return false;
            }
        }

        return true;
    }

    private String obterCampoId(String tabela) {
        switch (tabela) {
            case "antecipado":
            case "caixa":
            case "configuracoes":
            case "desistencia":
            case "prevendidos":
            case "produtos":
            case "justificativa":
                return "id";
            case "funcionario":
                return "idfuncionario";
            case "imagens":
                return "idfuncionario";
            case "registralimpeza":
                return "id";
            case "registralocado":
            case "registravendido":
                return "idlocacao";
            case "registramanutencao":
                return "id";
            case "registrareserva":
                return "id";
            case "status":
                return "numeroquarto";
            case "quartos":
                return "numeroquarto";
            default:
                return null;
        }
    }

    private void deletarRegistroLog(Connection conexaoLocal, String tabela, Object id) throws SQLException {
        String queryDeleteLog = "DELETE FROM log_sincronizacao WHERE tabela_nome = ? AND registro_id = ?";
        try ( PreparedStatement stmtDeleteLog = conexaoLocal.prepareStatement(queryDeleteLog)) {
            stmtDeleteLog.setString(1, tabela);
            stmtDeleteLog.setObject(2, id);
            stmtDeleteLog.executeUpdate();
        }
    }

    public static void main(String[] args) {
        CheckSincronia checkSincronia = new CheckSincronia();
        checkSincronia.start();
    }
}
