/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author MOTEL
 */
public class BackupTask {
    private final String sql;

    public BackupTask(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void execute(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }
}
