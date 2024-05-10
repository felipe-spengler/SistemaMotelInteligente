/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;
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
    //private static final String URL = "jdbc:mysql://0.tcp.sa.ngrok.io.:18233/motel";
    /*private static final String URL = "jdbc:mysql://localhost:3306/motel";
    private static final String USER = "root";
    private static final String PASS = "";
*/

    public  Connection conectar(){
        try{
            //CONEXAO COM O BANCO PLANETSCALE
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