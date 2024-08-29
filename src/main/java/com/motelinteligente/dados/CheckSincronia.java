/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

/**
 *
 * @author MOTEL
 */
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
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
        String[] onlineParaLocal = {"login_acesso", "login_registros"};
        boolean isSynced = true;

        Connection localConn = null;
        Connection remoteConn = null;

        try {
            localConn = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD);
            remoteConn = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD);

            for (String table : tables) {
                String localChecksum = getTableChecksum(localConn, table);
                String remoteChecksum = getTableChecksum(remoteConn, table);
                if (!localChecksum.equals(remoteChecksum)) {
                    isSynced = false;
                    System.out.printf("Discrepância na tabela %s: Local (%s) vs Remoto (%s)%n", table, localChecksum, remoteChecksum);
                    // Exportar os dados para arquivos .txt antes de corrigir
                    exportTableData(localConn, table, table + "_home.txt");
                    exportTableData(remoteConn, table, table + "_online.txt");
                    copyDataToRemote(localConn, remoteConn, table);
                }
            }

            for (String tabela : onlineParaLocal) {
                String localChecksum = getTableChecksum(localConn, tabela);
                String remoteChecksum = getTableChecksum(remoteConn, tabela);
                if (!localChecksum.equals(remoteChecksum)) {
                    isSynced = false;
                    System.out.printf("Discrepância na tabela %s: Remota (%s) vs Local (%s)%n", tabela, localChecksum, remoteChecksum);
                    // Exportar os dados para arquivos .txt antes de corrigir
                    exportTableData(localConn, tabela, tabela + "_home.txt");
                    exportTableData(remoteConn, tabela, tabela + "_online.txt");
                    copyDataToRemote(remoteConn, localConn, tabela);
                }
            }

            if (isSynced) {
                System.out.println("Os bancos de dados estão sincronizados.");
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

    private void exportTableData(Connection conn, String table, String fileName) {
        String selectSql = String.format("SELECT * FROM %s", table);

        try ( Statement stmt = conn.createStatement();  ResultSet rs = stmt.executeQuery(selectSql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            try ( FileWriter writer = new FileWriter(fileName)) {
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        if (value instanceof byte[]) {
                            // Codifica dados BLOB em Base64
                            value = Base64.getEncoder().encodeToString((byte[]) value);
                        }
                        row.append(value).append("\t");
                    }
                    row.setLength(row.length() - 1); // Remove a última tabulação
                    writer.write(row.toString());
                    writer.write(System.lineSeparator());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getTableChecksum(Connection conn, String table) throws UnsupportedEncodingException {
        String sql = String.format("SELECT * FROM %s", table);

        try ( Statement stmt = conn.createStatement();  ResultSet rs = stmt.executeQuery(sql)) {

            MessageDigest digest = MessageDigest.getInstance("MD5");
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnType = metaData.getColumnTypeName(i);
                    String value = null;

                    if ("DATETIME".equalsIgnoreCase(columnType) || "TIMESTAMP".equalsIgnoreCase(columnType)) {
                        Timestamp timestamp = rs.getTimestamp(i);
                        if (timestamp != null) {
                            // Zera os segundos para evitar diferenças mínimas de 1 segundo
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(timestamp.getTime());
                            calendar.set(Calendar.SECOND, 0); // Zera os segundos
                            value = new SimpleDateFormat("yyyy-MM-dd HH:mm:00").format(calendar.getTime()); // Formata para garantir 00 nos segundos
                        }
                    } else {
                        value = rs.getString(i);
                    }

                    if (value != null) {
                        digest.update(value.getBytes("UTF-8"));
                    }
                }
            }

            byte[] checksumBytes = digest.digest();
            return Base64.getEncoder().encodeToString(checksumBytes);

        } catch (SQLException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao calcular checksum", e);
        }
    }

    private void copyDataToRemote(Connection localConn, Connection remoteConn, String table) {
        String deleteSql = String.format("DELETE FROM %s", table);

        try ( Statement remoteStmt = remoteConn.createStatement()) {
            // Log delete SQL
            System.out.println("Executing: " + deleteSql);
            remoteStmt.execute(deleteSql);

            String selectSql = String.format("SELECT * FROM %s", table);
            try ( Statement localStmt = localConn.createStatement();  ResultSet rs = localStmt.executeQuery(selectSql)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder columns = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    columns.append(metaData.getColumnName(i)).append(",");
                }
                columns.setLength(columns.length() - 1); // Remove a última vírgula

                StringBuilder placeholders = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    placeholders.append("?,");
                }
                placeholders.setLength(placeholders.length() - 1); // Remove a última vírgula

                String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", table, columns.toString(), placeholders.toString());

                try ( PreparedStatement pstmt = remoteConn.prepareStatement(insertSql)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            Object value = rs.getObject(i);
                            if (metaData.getColumnType(i) == java.sql.Types.TIMESTAMP
                                    || metaData.getColumnType(i) == java.sql.Types.DATE
                                    || metaData.getColumnType(i) == java.sql.Types.TIME) {
                                if (value != null) {
                                    Timestamp tsValue = rs.getTimestamp(i);
                                    pstmt.setString(i, dateFormat.format(tsValue));
                                } else {
                                    pstmt.setNull(i, java.sql.Types.TIMESTAMP);
                                }
                            } else if (metaData.getColumnType(i) == java.sql.Types.BLOB) {
                                byte[] blobData = rs.getBytes(i);
                                pstmt.setBytes(i, blobData);
                            } else if (value instanceof String) {
                                pstmt.setString(i, (String) value);
                            } else if (value == null) {
                                pstmt.setNull(i, metaData.getColumnType(i));
                            } else {
                                pstmt.setObject(i, value);
                            }
                        }
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
