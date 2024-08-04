/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.sql.PreparedStatement;

/**
 *
 * @author MOTEL
 */
public class BackupTask {
    private String sql;
    private PreparedStatement preparedStatement;

    public BackupTask(String sql, PreparedStatement preparedStatement) {
        this.sql = sql;
        this.preparedStatement = preparedStatement;
    }

    public String getSqlCommand() {
        return sql;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

}
