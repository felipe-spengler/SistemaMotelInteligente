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

/**
 *
 * @author MOTEL
 */
public class fcaixa {

    public boolean abrirCaixa(float valorAbrir) {

        //primeiro ter certeza que não tem caixa aberto!
        if (new fazconexao().verificaCaixa() != 0) {
            JOptionPane.showMessageDialog(null, "O Caixa já está Aberto!");
            return false;
        }

        String consultaSQL = "INSERT INTO caixa (horaabre, usuarioabre, saldoabre) VALUES (?, ?, ?)";
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            Date dataAtual = new Date();
            Timestamp timestamp = new Timestamp(dataAtual.getTime());
            configGlobal config = configGlobal.getInstance();

            statement.setTimestamp(1, timestamp);
            statement.setString(2, config.getUsuario());
            statement.setFloat(3, valorAbrir);

            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                JOptionPane.showMessageDialog(null, "Caixa Aberto com Sucesso!");
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean fecharCaixa(float valorFinal) {
        //primeiro ter certeza que não tem caixa aberto!
        if (new fazconexao().verificaCaixa() == 0) {
            JOptionPane.showMessageDialog(null, "Caixa Não Está Aberto!");
            return false;
        }
        configGlobal config = configGlobal.getInstance();

        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        Connection link = null;
        String consultaSQL = "UPDATE caixa SET horaabre=? ,horafecha = ?, usuariofecha = ?, saldofecha = ? WHERE usuariofecha IS NULL";
        Timestamp horaAbre = getDataAbriu(config.getCaixa());
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            statement.setTimestamp(1, horaAbre);
            statement.setTimestamp(2, timestamp);
            statement.setString(3, config.getUsuario());
            statement.setFloat(4, valorFinal);

            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String getUsuarioAbriu(int idPassado) {
        String consultaSQL = "SELECT usuarioabre FROM caixa WHERE id = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return (String) resultado.getString("usuarioabre");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public valores getValores(int idCaixa) {
        valores val = new valores();

        String consultaSQL = "SELECT * FROM registralocado WHERE idcaixaatual=" + idCaixa;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            while (resultado.next()) {
                val.entradaC += resultado.getFloat("pagocartao");
                val.entradaD += resultado.getFloat("pagodinheiro");
                val.entradaP += resultado.getFloat("pagopix");

                val.entradaConsumo += resultado.getFloat("valorconsumo");
                val.entradaQuarto += resultado.getFloat("valorquarto");
            }
            link.close();
            statement.close();
            return val;
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getIdCaixa() {

        String consultaSQL = "SELECT id FROM caixa WHERE horafecha IS NULL";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return  resultado.getInt("id");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public Timestamp getDataAbriu(int idPassado) {
        String consultaSQL = "SELECT horaabre FROM caixa WHERE id = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return resultado.getTimestamp("horaabre");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public float getValorAbriu(int idPassado) {
        String consultaSQL = "SELECT saldoabre FROM caixa WHERE id = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return resultado.getFloat("saldoabre");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

}
