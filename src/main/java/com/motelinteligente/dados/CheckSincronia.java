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

public class CheckSincronia extends TimerTask {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CheckSincronia.class);

    // Singleton
    private static CheckSincronia instance;
    private static Timer timer;
    private static final long INTERVALO = 7_000; // 7 segundos

    private final AtomicBoolean executando = new AtomicBoolean(false);

    private CheckSincronia() {
    }

    public static synchronized void start() {
        if (instance == null) {
            instance = new CheckSincronia();
            timer = new Timer(true);
            timer.scheduleAtFixedRate(instance, 0, INTERVALO);
            logger.info(">>> CheckSincronia iniciado (única instância).");
        } else {
            logger.info(">>> CheckSincronia já em execução, ignorando novo start().");
        }
    }

    public static CheckSincronia getInstance() {
        return instance;
    }

    public static synchronized void stop() {
        if (timer != null) {
            timer.cancel(); // Termina a thread do timer
            timer = null;
        }
        instance = null; // Permite reiniciar se necessário
        logger.info("CheckSincronia parado com sucesso.");
    }

    public void solicitarSincroniaImediata() {
        // Executa em uma nova thread para não travar a operação original (do Proxy)
        new Thread(this::run).start();
    }

    @Override
    public void run() {
        if (!executando.compareAndSet(false, true)) {
            // logger.warn("CheckSincronia já em execução. Ignorando nova chamada.");
            return;
        }

        try {
            if (temRegistrosParaSincronizar()) {
                logger.info("Iniciando sincronização de registros...");
                checkDatabaseSync();
                logger.info("Sincronização concluída.");
            }
        } catch (Exception ex) {
            logger.error("Erro durante execução de CheckSincronia", ex);
        } finally {
            executando.set(false);
        }
    }

    private boolean temRegistrosParaSincronizar() {
        String query = "SELECT COUNT(*) FROM log_sincronizacao";

        try (
                Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Erro ao verificar registros para sincronizar", e);
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

        try (
                Connection localConn = new fazconexao().conectar();
                Connection remoteConn = ConexaoRemota.getConnection()) {
            if (remoteConn == null) {
                logger.error("Não foi possível conectar no banco remoto.");
                return;
            }

            boolean ok = true;

            for (String tabela : tables) {
                try {
                    if (!sincronizarTabela(tabela, localConn, remoteConn)) {
                        ok = false;
                    }
                } catch (Exception e) {
                    logger.error("Erro ao sincronizar tabela " + tabela, e);
                    ok = false;
                }
            }

            if (!ok)
                logger.warn("Nem todas tabelas foram sincronizadas corretamente.");

        } catch (SQLException e) {
            // Log simplificado para falhas de conexão, evitando poluição excessiva dos logs
            logger.warn("Falha na sincronização (banco remoto indisponível?): " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao sincronizar", e);
        }
    }

    private boolean sincronizarTabela(String tabela, Connection local, Connection remoto)
            throws SQLException, IOException {

        String campoId = obterCampoId(tabela);
        if (campoId == null) {
            logger.error("Campo ID não identificado para tabela " + tabela);
            return false;
        }

        String sql = "SELECT * FROM log_sincronizacao WHERE tabela_nome = ?";

        try (
                PreparedStatement stmt = local.prepareStatement(sql)) {
            stmt.setString(1, tabela);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Object id = rs.getObject("registro_id");

                    try {
                        sincronizarRegistro(tabela, campoId, id, local, remoto);
                        deletarRegistroLog(local, tabela, id);

                    } catch (Exception e) {
                        logger.error("Erro ao sincronizar registro " + id + " da tabela " + tabela, e);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void sincronizarRegistro(String tabela, String campoId, Object id,
            Connection local, Connection remoto)
            throws SQLException {

        String sqlSelectLocal = "SELECT * FROM " + tabela + " WHERE " + campoId + " = ?";

        // 1. Buscamos o dado LOCAL (Source of Truth para envio)
        try (PreparedStatement sl = local.prepareStatement(sqlSelectLocal)) {
            sl.setObject(1, id);

            try (ResultSet rl = sl.executeQuery()) {
                if (rl.next()) {
                    // O registro existe localmente -> Vamos enviar (UPSERT) para o remoto

                    // 1.1 Descobre colunas do remoto
                    List<String> columns = getRemoteColumnNames(tabela, remoto);
                    if (columns.isEmpty())
                        return; // Tabela remota não tem colunas?

                    // 1.2 Monta Query: INSERT INTO ... VALUES ... ON DUPLICATE KEY UPDATE ...
                    StringBuilder colNames = new StringBuilder();
                    StringBuilder placeholders = new StringBuilder();
                    StringBuilder updateClause = new StringBuilder();

                    for (String col : columns) {
                        if (colNames.length() > 0) {
                            colNames.append(", ");
                            placeholders.append(", ");
                            updateClause.append(", ");
                        }
                        colNames.append(col);
                        placeholders.append("?");
                        // Cláusula UPDATE: coluna = VALUES(coluna)
                        updateClause.append(col).append(" = VALUES(").append(col).append(")");
                    }

                    String sqlUpsert = "INSERT INTO " + tabela + " (" + colNames + ") VALUES (" + placeholders + ") "
                            + "ON DUPLICATE KEY UPDATE " + updateClause;

                    try (PreparedStatement su = remoto.prepareStatement(sqlUpsert)) {
                        // Preenche os valores do INSERT
                        for (int i = 0; i < columns.size(); i++) {
                            su.setObject(i + 1, rl.getObject(columns.get(i)));
                        }
                        su.executeUpdate();
                    }

                } else {
                    // O registro NÃO existe localmente (foi deletado?) -> Deleta no remoto também
                    // AVISO: Cuidado com deleção em massa se o log estiver desatualizado.
                    // Por segurança, você pode comentar essa parte se não quiser propagar deletes.
                    String sqlDelete = "DELETE FROM " + tabela + " WHERE " + campoId + " = ?";
                    try (PreparedStatement sd = remoto.prepareStatement(sqlDelete)) {
                        sd.setObject(1, id);
                        sd.executeUpdate();
                    }
                }
            }
        }
    }

    // Método auxiliar corrigido para o novo fluxo
    private List<String> getRemoteColumnNames(String tabela, Connection conn) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tabela, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }

    private void deletarRegistroLog(Connection conn, String tabela, Object id) throws SQLException {
        String sql = "DELETE FROM log_sincronizacao WHERE tabela_nome = ? AND registro_id = ?";

        try (
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tabela);
            stmt.setObject(2, id);
            stmt.executeUpdate();
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
        }
        return null;
    }

}
