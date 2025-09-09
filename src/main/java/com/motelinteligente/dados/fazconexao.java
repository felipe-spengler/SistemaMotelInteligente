package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

public class fazconexao {

    private static fazconexao instance;
    private Connection connection;

    private String LOCAL_DB_URL;
    private String USER;
    private String PASSWORD;

    public fazconexao() {
        this.LOCAL_DB_URL = CarregarVariaveis.getLocalDbUrl();
        this.USER = CarregarVariaveis.getUser();
        this.PASSWORD = CarregarVariaveis.getPassword();
    }

    public Connection conectar() {
        try {

            Connection conn = DriverManager.getConnection(LOCAL_DB_URL, USER, PASSWORD);
            // Retorna a conexão global
            return createConnectionProxy(conn);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados: " + e.getMessage());
            e.printStackTrace();
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

    // Remova o link.close() daqui!
    public int verificaCaixa() {
        String consultaSQL = "SELECT id FROM caixa WHERE saldofecha IS NULL";
        Connection link = null;
        try {
            // Agora, conectar() vai sempre pegar a mesma conexão
            link = conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            ResultSet resultado = statement.executeQuery();
            if (resultado.next()) {
                int numero = resultado.getInt("id");
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
