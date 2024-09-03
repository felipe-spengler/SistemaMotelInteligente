/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

/**
 *
 * @author MOTEL
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Proxy;
import javax.swing.JOptionPane;

public class fazconexao {
    // Conexão online
    public Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(
                    "testes",
                "",
                ""
            );
            return createConnectionProxy(conn);
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        }
        return null;
    }
    private Connection createConnectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
            connection.getClass().getClassLoader(),
            new Class[]{Connection.class},
            new BackupProxyHandler(connection)
        );
    }

    // Método para verificar caixa
    public int verificaCaixa() {
        String consultaSQL = "SELECT id FROM caixa WHERE saldofecha IS NULL";
        try {
            Connection link = conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            ResultSet resultado = statement.executeQuery();
            if (resultado.next()) {
                int numero = resultado.getInt("id");
                link.close();
                resultado.close();
                return numero;
            } else {
                return 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return 0;
    }
}
