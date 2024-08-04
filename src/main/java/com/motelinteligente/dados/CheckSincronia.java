/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

/**
 *
 * @author MOTEL
 */
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckSincronia {

    private static final String REMOTE_DB_URL = "jdbc:mysql://localhost:3306/u876938716_motel";
    private static final String LOCAL_DB_URL = "jdbc:mysql://srv1196.hstgr.io/u876938716_motel";
    //private static final String LOCAL_DB_URL = "jdbc:mysql://localhost:3306/u876938716_motel";
    //private static final String REMOTE_DB_URL = "jdbc:mysql://srv1196.hstgr.io/u876938716_motel";
    private static final String USER = "u876938716_contato";
    private static final String PASSWORD = "Felipe0110@";

    public void start() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkDatabaseSync();
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(CheckSincronia.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 0, 60 * 60 * 1000); // Executa a cada 1 hora
    }

    private void checkDatabaseSync() throws UnsupportedEncodingException {
        String[] tables = {
            "antecipado", "caixa", "configuracoes", "desistencia", "funcionario", "imagens",
            "justificativa", "login_acesso", "login_registros", "prevendidos", "produtos", "quartos",
            "registralimpeza", "registralocado", "registramanutencao", "registrareserva", "registravendido",
            "status"
        };

        boolean isSynced = true;

        try ( Connection localConn = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD);  Connection remoteConn = DriverManager.getConnection(REMOTE_DB_URL, USER, PASSWORD)) {

            for (String table : tables) {
                String localChecksum = getTableChecksum(localConn, table);
                String remoteChecksum = getTableChecksum(remoteConn, table);
                if (!localChecksum.equals(remoteChecksum)) {
                    isSynced = false;
                    System.out.printf("Discrepância na tabela %s: Local (%s) vs Remoto (%s)%n", table, localChecksum, remoteChecksum);
                    copyDataToRemote(localConn, remoteConn, table);
                }
            }

            if (isSynced) {
                System.out.println("Os bancos de dados estão sincronizados.");
            } else {
                System.out.println("Os bancos de dados não estão sincronizados e foram corrigidos.");
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
                    String value = rs.getString(i);
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

        try (Statement remoteStmt = remoteConn.createStatement()) {
            // Log delete SQL
            System.out.println("Executing: " + deleteSql);
            remoteStmt.execute(deleteSql);

            String selectSql = String.format("SELECT * FROM %s", table);
            try (Statement localStmt = localConn.createStatement(); ResultSet rs = localStmt.executeQuery(selectSql)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder columns = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    columns.append(metaData.getColumnName(i)).append(",");
                }
                columns.setLength(columns.length() - 1); // Remove a última vírgula

                StringBuilder insertSql = new StringBuilder();
                insertSql.append(String.format("INSERT INTO %s (%s) VALUES ", table, columns.toString()));

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                while (rs.next()) {
                    StringBuilder values = new StringBuilder();
                    values.append("(");
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        if (metaData.getColumnType(i) == java.sql.Types.TIMESTAMP || 
                            metaData.getColumnType(i) == java.sql.Types.DATE || 
                            metaData.getColumnType(i) == java.sql.Types.TIME) {
                            Timestamp tsValue = rs.getTimestamp(i);
                            values.append("'").append(dateFormat.format(tsValue)).append("',");
                        } else if (value instanceof String) {
                            values.append("'").append(value).append("',");
                        } else if (value == null) {
                            values.append("NULL,");
                        } else if (metaData.getColumnType(i) == java.sql.Types.BLOB) {
                            // Tratar colunas BLOB
                            byte[] blobData = rs.getBytes(i);
                            String encodedBlob = Base64.getEncoder().encodeToString(blobData);
                            values.append("'").append(encodedBlob).append("',");
                        } else {
                            values.append(value).append(",");
                        }
                    }
                    values.setLength(values.length() - 1); // Remove a última vírgula
                    values.append("),");
                    insertSql.append(values.toString());
                }
                insertSql.setLength(insertSql.length() - 1); // Remove a última vírgula

                // Log insert SQL
                System.out.println("Executing: " + insertSql.toString());
                remoteStmt.execute(insertSql.toString());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
