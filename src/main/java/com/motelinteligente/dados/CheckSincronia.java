package com.motelinteligente.dados;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

public class CheckSincronia {

    private String LOCAL_DB_URL;
    private String REMOTE_DB_URL;
    private String USER;
    private String PASSWORD;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CheckSincronia.class);

    public void start() {
        this.LOCAL_DB_URL = CarregarVariaveis.getLocalDbUrl();
        this.REMOTE_DB_URL = CarregarVariaveis.getRemoteDbUrl();
        this.USER = CarregarVariaveis.getUser();
        this.PASSWORD = CarregarVariaveis.getPassword();
        
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
                    } catch (IOException ex) {
                        Logger.getLogger(CheckSincronia.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }).start();
            }
        }, 0, 60 * 1000); // Executa a cada 1 min
    }

    private void checkDatabaseSync() throws UnsupportedEncodingException, IOException {
        String[] tables = {
            "alarmes", "antecipado", "caixa", "configuracoes", "desistencia", "funcionario", "imagens",
            "justificativa", "prevendidos", "produtos", "quartos",
            "registralimpeza", "registralocado", "registramanutencao", "registrareserva", "registravendido",
            "status", "valorcartao", "portoes", "reservas"
        };

        Connection localConn = null;
        Connection remoteConn = null;

        try {
            localConn = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD);
            remoteConn = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD);

            boolean isSynced = true;

            // Verificar e sincronizar com base na tabela de logs
            for (String tabela : tables) {
                isSynced &= sincronizarTabela(tabela, localConn, remoteConn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (localConn != null) {
                try {
                    localConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error("Erro ao fechar a conexão local: " + e.getMessage());
                }
            }
            if (remoteConn != null) {
                try {
                    remoteConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error("Erro ao fechar a conexão remota: " + e.getMessage());
                }
            }
        }
    }

    private boolean sincronizarTabela(String tabela, Connection conexaoLocal, Connection conexaoRemoto) throws SQLException, IOException {
        String campoId = obterCampoId(tabela);

        if (campoId == null) {
            System.err.println("Campo de " + campoId + " não encontrado para a tabela: " + tabela);
            return false;
        }

        String queryLog = "SELECT * FROM log_sincronizacao WHERE tabela_nome = ?";
        String querySelectLocal = "SELECT * FROM " + tabela + " WHERE " + campoId + " = ?";
        String querySelectRemoto = "SELECT * FROM " + tabela + " WHERE " + campoId + " = ?";

        boolean isSynced = true;

        try (PreparedStatement stmtLog = conexaoLocal.prepareStatement(queryLog)) {
            stmtLog.setString(1, tabela);

            try (ResultSet rsLog = stmtLog.executeQuery()) {
                while (rsLog.next()) {
                    Object id = rsLog.getObject("registro_id");

                    // Verificar registro no banco de dados local
                    try (PreparedStatement stmtSelectLocal = conexaoLocal.prepareStatement(querySelectLocal); PreparedStatement stmtSelectRemoto = conexaoRemoto.prepareStatement(querySelectRemoto)) {

                        stmtSelectLocal.setObject(1, id);
                        stmtSelectRemoto.setObject(1, id);

                        try (ResultSet rsLocal = stmtSelectLocal.executeQuery(); ResultSet rsRemoto = stmtSelectRemoto.executeQuery()) {

                            if (rsLocal.next() && rsRemoto.next()) {
                                // Comparar registros e logar discrepâncias
                                List<String> discrepancias = compararRegistrosDetalhado(rsLocal, rsRemoto);
                                if (!discrepancias.isEmpty()) {
                                    isSynced = false;
                                    logger.warn(String.format("DISCREPÂNCIA ENCONTRADA NA TABELA '%s' PARA ID '%s': %s",
                                            tabela, id, String.join(", ", discrepancias)));
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

    /**
     * Compara registros detalhadamente e retorna uma lista de discrepâncias.
     */
    private List<String> compararRegistrosDetalhado(ResultSet rsLocal, ResultSet rsRemoto) throws SQLException {
        List<String> discrepancias = new ArrayList<>();

        ResultSetMetaData metaData = rsLocal.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object valorLocal = rsLocal.getObject(columnName);
            Object valorRemoto = rsRemoto.getObject(columnName);

            // Truncar para comparação se for um Timestamp (removendo milissegundos)
            if (valorLocal instanceof Timestamp && valorRemoto instanceof Timestamp) {
                valorLocal = Timestamp.valueOf(((Timestamp) valorLocal).toLocalDateTime().withNano(0));
                valorRemoto = Timestamp.valueOf(((Timestamp) valorRemoto).toLocalDateTime().withNano(0));
            }

            // Comparar valores (tratando nulos)
            if ((valorLocal == null && valorRemoto != null) || (valorLocal != null && !valorLocal.equals(valorRemoto))) {
                discrepancias.add(String.format("Campo '%s': LOCAL='%s', REMOTO='%s'", columnName, valorLocal, valorRemoto));
            }
        }

        return discrepancias;
    }

    private String getColumnsList(String tabela, Connection conexaoRemoto) throws SQLException {
        try (ResultSet rs = conexaoRemoto.getMetaData().getColumns(null, null, tabela, null)) {
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

        try (
                PreparedStatement stmtSelectLocal = conexaoLocal.prepareStatement(querySelectLocal); PreparedStatement stmtSelectRemoto = conexaoRemoto.prepareStatement(querySelectRemoto); PreparedStatement stmtInsertRemoto = conexaoRemoto.prepareStatement(queryInsertRemoto); PreparedStatement stmtUpdateRemoto = conexaoRemoto.prepareStatement(queryUpdateRemoto)) {
            // Selecionar do banco local
            stmtSelectLocal.setObject(1, id);
            ResultSet rsLocal = stmtSelectLocal.executeQuery();

            // Selecionar do banco remoto
            stmtSelectRemoto.setObject(1, id);
            ResultSet rsRemoto = stmtSelectRemoto.executeQuery();

            if (rsLocal.next()) {
                if (!rsRemoto.next()) {
                    copyRowToPreparedStatement(rsLocal, stmtInsertRemoto, tabela, campoId, true); // Para INSERT
                    stmtInsertRemoto.executeUpdate();
                    config.incrementarContadorExecucoes();
                } else {
                    copyRowToPreparedStatement(rsLocal, stmtUpdateRemoto, tabela, campoId, false); // Para UPDATE
                    stmtUpdateRemoto.setObject(getColumnCount(tabela, conexaoRemoto), id);
                    stmtUpdateRemoto.executeUpdate();
                    config.incrementarContadorExecucoes();
                }
            } else {
                if (rsRemoto.next()) {
                    String queryDeleteRemoto = "DELETE FROM " + tabela + " WHERE " + campoId + " = ?";
                    try (PreparedStatement stmtDeleteRemoto = conexaoRemoto.prepareStatement(queryDeleteRemoto)) {
                        stmtDeleteRemoto.setObject(1, id);
                        stmtDeleteRemoto.executeUpdate();
                        config.incrementarContadorExecucoes();
                    }
                }
            }
        }
    }

    public String getSQLString(PreparedStatement stmt, Object... params) {
        String sql = stmt.toString();
        for (Object param : params) {
            String value = (param == null) ? "NULL" : param.toString();
            // Se for uma string, adicionar aspas
            if (param instanceof String || param instanceof java.sql.Date || param instanceof java.sql.Timestamp) {
                value = "'" + value + "'";
            }
            sql = sql.replaceFirst("\\?", value);
        }
        return sql;
    }

    private void copyRowToPreparedStatement(ResultSet rsLocal, PreparedStatement stmt, String tabela, String campoId, boolean includeId) throws SQLException {
        ResultSetMetaData metaData = rsLocal.getMetaData();
        int columnCount = metaData.getColumnCount();

        int paramIndex = 1;
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);

            // Inclui o campo ID se necessário (para o INSERT, por exemplo)
            if (!columnName.equals(campoId) || includeId) {
                Object value = rsLocal.getObject(i);
                stmt.setObject(paramIndex++, value);
            }
        }

        // No caso de UPDATE, definimos o campo ID como o último parâmetro
        if (!includeId) {
            stmt.setObject(paramIndex, rsLocal.getObject(campoId));
        }
    }

    private int getColumnCount(String tabela, Connection conexaoRemoto) throws SQLException {
        try (ResultSet rs = conexaoRemoto.getMetaData().getColumns(null, null, tabela, null)) {
            int count = 0;
            while (rs.next()) {
                count++;
            }
            return count;
        }
    }

    private String getUpdateSetClause(String tabela, String campoId, Connection conexaoRemoto) throws SQLException {
        try (ResultSet rs = conexaoRemoto.getMetaData().getColumns(null, null, tabela, null)) {
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

            // Truncar para comparação
            if (localValue instanceof Timestamp && remotoValue instanceof Timestamp) {
                localValue = Timestamp.valueOf(localValue.toString().substring(0, 19)); // Até segundos
                remotoValue = Timestamp.valueOf(remotoValue.toString().substring(0, 19)); // Até segundos
            }

            if ((localValue != null && !localValue.equals(remotoValue)) || (localValue == null && remotoValue != null)) {
                return false;
            }
        }

        return true;
    }

    private String obterCampoId(String tabela) {
        switch (tabela) {
            case "alarmes":
                return "id";
            case "reservas":
                return "id";
            case "portoes":
                return "id";
            case "antecipado":
            case "caixa":
            case "configuracoes":
            case "desistencia":
            case "prevendidos":
            case "valorcartao":
                return "id";
            case "justificativa":
                return "id";
            case "funcionario":
                return "idfuncionario";
            case "imagens":
                return "id";
            case "registralimpeza":
                return "id";
            case "registralocado":
                return "idlocacao";
            case "registravendido":
                return "id";
            case "registramanutencao":
                return "id";
            case "registrareserva":
                return "id";
            case "produtos":
                return "idproduto";
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
        try (PreparedStatement stmtDeleteLog = conexaoLocal.prepareStatement(queryDeleteLog)) {
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
