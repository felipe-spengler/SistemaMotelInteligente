package com.motelinteligente.dados;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

public class CheckSincronia {


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CheckSincronia.class);
    private final AtomicBoolean executando = new AtomicBoolean(false);

    public void start() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (executando.compareAndSet(false, true)) {
                    try {
                        if (temRegistrosParaSincronizar()) {
                            logger.info("Iniciando sincronização de registros...");
                            checkDatabaseSync();
                            logger.info("Sincronização concluída.");
                        } 
                    } catch (Exception ex) {
                        logger.error("Erro durante execução de CheckSincronia: {}", ex.getMessage(), ex);
                    } finally {
                        executando.set(false);
                    }
                } else {
                    logger.warn("CheckSincronia já em execução. Ignorando nova chamada.");
                }
            }
        }, 0, 10_000); // Executa a cada 10 segundos
    }

    private boolean temRegistrosParaSincronizar() {
        String queryCount = "SELECT COUNT(*) FROM log_sincronizacao";
        try (Connection localConn = new fazconexao().conectar();
             PreparedStatement stmt = localConn.prepareStatement(queryCount);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar registros para sincronizar: {}", e.getMessage(), e);
        }
        return false;
    }

    private void checkDatabaseSync() throws UnsupportedEncodingException, IOException {
        String[] tables = {
            "alarmes", "antecipado", "caixa", "configuracoes", "desistencia", "funcionario", "imagens",
            "justificativa", "prevendidos", "produtos", "quartos",
            "registralimpeza", "registralocado", "registramanutencao", "registrareserva", "registravendido",
            "status", "valorcartao", "portoes", "reservas"
        };

        try (Connection localConn = new fazconexao().conectar();
             Connection remoteConn = ConexaoRemota.getConnection()) {

            if (remoteConn == null) {
                logger.error("Não foi possível obter conexão remota (remoteConn é null). Sincronização abortada.");
                return;
            }

            boolean isSynced = true;
            for (String tabela : tables) {
                try {
                    if (!sincronizarTabela(tabela, localConn, remoteConn)) {
                        isSynced = false;
                    }
                } catch (Exception e) {
                    logger.error("Erro ao sincronizar tabela '{}': {}", tabela, e.getMessage(), e);
                    isSynced = false;
                }
            }

            if (!isSynced) {
                logger.warn("Algumas tabelas não foram sincronizadas corretamente. Verifique os logs para detalhes.");
            }

        } catch (SQLException e) {
            logger.error("Erro de conexão ao tentar sincronizar bancos: {}", e.getMessage(), e);
        }
    }

    private boolean sincronizarTabela(String tabela, Connection conexaoLocal, Connection conexaoRemoto)
            throws SQLException, IOException {

        String campoId = obterCampoId(tabela);
        if (campoId == null) {
            logger.error("Não foi possível identificar campo ID para tabela '{}'.", tabela);
            return false;
        }

        String queryLog = "SELECT * FROM log_sincronizacao WHERE tabela_nome = ?";
        boolean isSynced = true;

        try (PreparedStatement stmtLog = conexaoLocal.prepareStatement(queryLog)) {
            stmtLog.setString(1, tabela);

            try (ResultSet rsLog = stmtLog.executeQuery()) {
                while (rsLog.next()) {
                    Object id = rsLog.getObject("registro_id");
                    try {
                        sincronizarRegistro(tabela, campoId, id, conexaoLocal, conexaoRemoto);
                        deletarRegistroLog(conexaoLocal, tabela, id);
                    } catch (Exception e) {
                        logger.error("Erro ao sincronizar registro ID '{}' da tabela '{}': {}",
                                id, tabela, e.getMessage(), e);
                        isSynced = false;
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
                } else {
                    copyRowToPreparedStatement(rsLocal, stmtUpdateRemoto, tabela, campoId, false); // Para UPDATE
                    stmtUpdateRemoto.setObject(getColumnCount(tabela, conexaoRemoto), id);
                    stmtUpdateRemoto.executeUpdate();
                }
            } else {
                if (rsRemoto.next()) {
                    String queryDeleteRemoto = "DELETE FROM " + tabela + " WHERE " + campoId + " = ?";
                    try (PreparedStatement stmtDeleteRemoto = conexaoRemoto.prepareStatement(queryDeleteRemoto)) {
                        stmtDeleteRemoto.setObject(1, id);
                        stmtDeleteRemoto.executeUpdate();
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

   
}
