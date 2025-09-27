/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MOTEL
 */
public class fcaixa {

    private static final Logger logger = LoggerFactory.getLogger(fcaixa.class);

    public boolean abrirCaixa(float valorAbrir) {
        // Primeiro, verifique se o caixa já está aberto
        if (new fazconexao().verificaCaixa() != 0) {
            JOptionPane.showMessageDialog(null, "O Caixa já está Aberto!");
            return false;
        }

        String consultaSQL = "INSERT INTO caixa (horaabre, usuarioabre, saldoabre) VALUES (?, ?, ?)";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            Date dataAtual = new Date();
            Timestamp timestamp = new Timestamp(dataAtual.getTime());
            configGlobal config = configGlobal.getInstance();

            statement.setTimestamp(1, timestamp);
            statement.setString(2, config.getUsuario());
            statement.setFloat(3, valorAbrir);

            int n = statement.executeUpdate();
            if (n > 0) { // Alterado para 'n > 0' para ser mais genérico
                JOptionPane.showMessageDialog(null, "Caixa Aberto com Sucesso!");
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erro ao tentar abrir o caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao abrir o caixa: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public boolean fecharCaixa(float valorFinal) {
        // Verifique se o caixa está aberto
        if (new fazconexao().verificaCaixa() == 0) {
            JOptionPane.showMessageDialog(null, "Caixa Não Está Aberto!");
            return false;
        }

        configGlobal config = configGlobal.getInstance();
        String consultaSQL = "UPDATE caixa SET horafecha = ?, usuariofecha = ?, saldofecha = ? WHERE id = ?";
        
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            Date dataAtual = new Date();
            Timestamp timestamp = new Timestamp(dataAtual.getTime());

            statement.setTimestamp(1, timestamp);
            statement.setString(2, config.getUsuario());
            statement.setFloat(3, valorFinal);
            statement.setInt(4, config.getCaixa()); // Usando o ID do caixa da configGlobal

            int n = statement.executeUpdate();
            if (n > 0) { // Alterado para 'n > 0'
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erro ao tentar fechar o caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao fechar o caixa: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public String getUsuarioAbriu(int idPassado) {
        String consultaSQL = "SELECT usuarioabre FROM caixa WHERE id = ?";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idPassado);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getString("usuarioabre");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário que abriu o caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public valores getValores(int idCaixa) {
        valores val = new valores();
        String consultaSQL = "SELECT pagocartao, pagodinheiro, pagopix, valorconsumo, valorquarto FROM registralocado WHERE idcaixaatual = ?";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idCaixa);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    val.entradaC += resultado.getFloat("pagocartao");
                    val.entradaD += resultado.getFloat("pagodinheiro");
                    val.entradaP += resultado.getFloat("pagopix");
                    val.entradaConsumo += resultado.getFloat("valorconsumo");
                    val.entradaQuarto += resultado.getFloat("valorquarto");
                }
                return val;
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar valores do caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public List<Integer> getIdsLocacoes(int idCaixa) {
        List<Integer> idsLocacaoList = new ArrayList<>();
        String consultaSQL = "SELECT idlocacao FROM registralocado WHERE idcaixaatual = ?";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idCaixa);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    idsLocacaoList.add(resultado.getInt("idlocacao"));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar IDs de locações: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return idsLocacaoList;
    }

    public int getIdCaixa() {
        String consultaSQL = "SELECT id FROM caixa WHERE horafecha IS NULL";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL);
             ResultSet resultado = statement.executeQuery()) {
            if (resultado.next()) {
                return resultado.getInt("id");
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar ID do caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }
    
    // Este método é redundante, pois a lógica de fechamento já usa o ID. 
    // Mantenho para compatibilidade, mas sua lógica pode ser simplificada na aplicação.
    public Timestamp getDataAbriu(int idPassado) {
        String consultaSQL = "SELECT horaabre FROM caixa WHERE id = ?";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idPassado);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getTimestamp("horaabre");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar data de abertura do caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public float getValorAbriu(int idPassado) {
        String consultaSQL = "SELECT saldoabre FROM caixa WHERE id = ?";
        try (Connection link = new fazconexao().conectar();
             PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setInt(1, idPassado);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getFloat("saldoabre");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar saldo de abertura do caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }

}