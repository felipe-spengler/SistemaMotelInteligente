/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.sql.Connection;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MOTEL
 */
public class ffuncionario {

    private static final Logger logger = LoggerFactory.getLogger(fprodutos.class);

    public int totalRegistros;

    public List<vfuncionario> mostrar() {
        List<vfuncionario> lista = new ArrayList<>();
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            String consultaSQL = "select * from funcionario";
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            ResultSet resultado = statement.executeQuery();

            // Processar o resultado
            while (resultado.next()) {
                //vquartos quarto = new vquartos(0,null, 0, 0 ,0);
                vfuncionario funcionario = new vfuncionario();
                funcionario.setNomefuncionario(resultado.getString("nomefuncionario"));
                funcionario.setLoginfuncionario(resultado.getString("loginfuncionario"));
                funcionario.setCargofuncionario(resultado.getString("cargofuncionario"));

                lista.add(funcionario);
            }

            // Fechar os recursos
            resultado.close();
            statement.close();
            link.close();
        } catch (Exception e) {
            logger.error("Erro : ffuncionario(): ", e);
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
        return lista;
    }

    public boolean fazUpdate(String sql) {
        int n = 0;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(sql);

            n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
            logger.error("Erro : ffuncionario(): ", e);
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
        return false;

    }

    public boolean insercao(vfuncionario dados) {
        String consultaSQL = "INSERT INTO funcionario (nomefuncionario,  cargofuncionario, loginfuncionario, senhafuncionario) VALUES ( ?, ?, ?, ?)";

        //String inserir = Tipo de Quarto","Valor do Período","Valor Pernoite"};
        /* String consultaSQL = "insert into quartos (tipoquarto, numeroquarto, valorquarto, pernoitequarto) values (" +
               dados.getTipoquarto() + "', '" +
               dados.getNumeroquarto() + "', '" +
               dados.getValorquarto() + "', '" +
               dados.getPernoitequarto()+ "')";*/
        Connection link = null;

        try {
            //faz a conexao com banco
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            //statement.setInt(1, dados.getIdquartos());
            statement.setString(1, dados.getNomefuncionario());
            statement.setString(2, dados.getCargofuncionario());
            statement.setString(3, dados.getLoginfuncionario());
            statement.setString(4, dados.getSenhafuncionario());
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : ffuncionario(): ", e);

            JOptionPane.showConfirmDialog(null, e);
            return false;
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
    }

    public boolean verExiste(String login) {
        String consultaSQL = "SELECT COUNT(*) FROM funcionario WHERE loginfuncionario = ?";
        Connection link = null;

        try {
            link = new fazconexao().conectar();

            PreparedStatement statement = link.prepareStatement(consultaSQL);

            // Substitua numeroQuartoDigitado pelo número de quarto que deseja verificar.
            statement.setString(1, login);
            ResultSet resultado = statement.executeQuery();

            // A consulta retornará um único valor, que é o número de vezes que o número de quarto foi encontrado.
            if (resultado.next()) {
                int count = resultado.getInt(1);
                System.out.println(count);
                if (count > 0) {
                    link.close();
                    return true;

                } else {
                    link.close();
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.error("Erro : ffuncionario(): ", e);
            JOptionPane.showConfirmDialog(null, e);
            return true;
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

        return true;
    }

    public String verificaLogin(String texto_login, String texto_senha) {
        String consulta = "SELECT * FROM funcionario WHERE loginfuncionario = ? AND senhafuncionario = ?";
        String cargo = null;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consulta);

            statement.setString(1, texto_login);
            statement.setString(2, texto_senha);

            try ( ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    cargo = resultado.getString("cargofuncionario"); // Supondo que a coluna na tabela seja chamada "cargo".
                    return cargo; // Retorna o cargo do usuário.
                }
            }
        } catch (SQLException e) {
            logger.error("Erro : ffuncionario(): ", e);
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

}
