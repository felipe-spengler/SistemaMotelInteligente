/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.alarme;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author MOTEL
 */
public class FAlarmes {
    public void removeAlarmFromDatabase(int id) {
        // Excluir o alarme do banco de dados
        try ( Connection conn = new fazconexao().conectar();  PreparedStatement stmt = conn.prepareStatement("DELETE FROM alarmes WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            // Atualiza o contador de alarmes ativos
            configGlobal config = configGlobal.getInstance();
            config.decrementarAlarme();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
