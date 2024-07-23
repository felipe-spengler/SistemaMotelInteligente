/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import java.sql.SQLException;
/**
 *
 * @author MOTEL
 */

public class fazconexao {
    
    private final BackupQueueManager queueManager;

     public fazconexao(BackupQueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public fazconexao() {
        this(new BackupQueueManager());
    }
    // conexao online
    public  Connection conectar(){
        try{
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://srv1196.hstgr.io/u876938716_motel",
  "u876938716_contato",
  "Felipe0110@");
            return conn;
        }catch(Exception e){
            JOptionPane.showConfirmDialog(null, e);
        }
        
        return null;
    }
    private Connection createConnectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName()) && args[0] instanceof String) {
                        String sql = (String) args[0];
                        // Adiciona o comando SQL à fila de backup
                        queueManager.addTask(new BackupTask(sql));
                    }
                    return method.invoke(connection, args);
                }
        );
    }
    //conexao local
    public  Connection conectarLocal(){
        try{
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/u876938716_motel",
                    "u876938716_contato",
                    "Felipe0110@");

            return createConnectionProxy(conn);
        }catch(Exception e){
            JOptionPane.showConfirmDialog(null, e);
        }
        
        return null;
    }
    public int verificaCaixa(){
        String consultaSQL = "SELECT id FROM caixa WHERE saldofecha IS NULL";

        try {
            
            Connection link = conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                int numero = resultado.getInt("id");
                link.close();
                resultado.close();
                return numero;
            }else{
                return 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return 0;
    }

}