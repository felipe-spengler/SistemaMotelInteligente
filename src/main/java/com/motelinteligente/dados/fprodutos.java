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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MOTEL
 */
public class fprodutos {

        private static final Logger logger = LoggerFactory.getLogger(fprodutos.class);


    public List<vprodutos> mostrarProduto() {
        List<vprodutos> produtos = new ArrayList<>();

        String registros[] = new String[5];
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            String consultaSQL = "select * from produtos order by idproduto";
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            ResultSet resultado = statement.executeQuery();

            // Processar o resultado
            while (resultado.next()) {
                vprodutos produto = new vprodutos();
                produto.setIdProduto(resultado.getInt("idproduto"));
                produto.setDescricao(resultado.getString("descricao"));
                produto.setValor(resultado.getFloat("valorproduto"));
                produto.setEstoque(resultado.getString("estoque"));
                produto.setDataCompra(resultado.getTimestamp("ultimacompra"));

                produtos.add(produto);
            }

            // Fechar os recursos
            resultado.close();
            statement.close();
            link.close();

        } catch (Exception e) {
            logger.error("Erro ao obter produto: mostrarProduto(): ", e);

            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : motraProduto() finally: ", e);

            }
        }
        return produtos;
    }

    public boolean diminuiEstoque(int idProduto, int quantidade) {
        String estoqueQuery = "SELECT estoque FROM produtos WHERE idproduto = ?";
        String updateQuery = "UPDATE produtos SET estoque = ? WHERE idproduto = ?";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(estoqueQuery);
            statement.setInt(1, idProduto);
            ResultSet resultado = statement.executeQuery();
            if (resultado.next()) {
                String estoque = resultado.getString("estoque");
                try {
                    int estoqueAtual = Integer.parseInt(estoque);
                    int novoEstoque = estoqueAtual - quantidade;
                    if (novoEstoque >= 0) {
                        logger.debug("Executando consulta SQL: {}", updateQuery);
                        statement = link.prepareStatement(updateQuery);
                        statement.setInt(1, novoEstoque);
                        statement.setInt(2, idProduto);
                        int linhasAfetadas = statement.executeUpdate();
                        return linhasAfetadas > 0;
                    } else {
                        // Não há estoque suficiente
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // O estoque não é um valor numérico inteiro
                    return false;
                }
            } else {
                // Produto não encontrado
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erro : diminuiEstoque(): ", e);
            JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : diminuiEstoque() finally: ", e);

            }
        }
    }

    public boolean verExiste(int numero) {
        String consultaSQL = "SELECT COUNT(*) FROM produtos WHERE idproduto = ?";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            statement.setInt(1, numero);
            ResultSet resultado = statement.executeQuery();

            if (resultado.next()) {
                int count = resultado.getInt(1);
                if (count > 0) {
                    link.close();
                    statement.close();
                    return true;

                } else {
                    link.close();
                    statement.close();
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Erro : verExiste(): ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : verExiste() finally: ", e);

            }
        }

        return false;
    }

    public boolean insercao(vprodutos dados) {
        String consultaSQL = "INSERT INTO produtos (idproduto, descricao, valorproduto, estoque, ultimacompra) VALUES (?, ?, ?, ?, ?)";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, dados.getIdProduto());
            statement.setString(2, dados.getDescricao());
            statement.setFloat(3, dados.getValor());
            statement.setString(4, dados.getEstoque());
            statement.setTimestamp(5, dados.getDataCompra());
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : insercao(): ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : insercao() finally: ", e);

            }
        }

    }

    public boolean exclusao(int idpassado) {
        String consultaSQL = "delete from produtos "
                + "where idproduto = " + idpassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : exclusao(): ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
                logger.error("Erro : exclusao() finally: ", e);

            }
        }
    }

    public vprodutos getProduto(int idPassado) {
        vprodutos produto = new vprodutos();
        String consultaSQL = "SELECT * FROM produtos WHERE idproduto = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                produto.setIdProduto(idPassado);
                produto.setDescricao(resultado.getString("descricao"));
                produto.setValor(resultado.getFloat("valorproduto"));
                produto.setEstoque(resultado.getString("estoque"));

                link.close();
                statement.close();
                return produto;
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : getProduto(): ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : getproduto() finally: ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public boolean exclui(int idpassado) {
        String consultaSQL = "delete from produtos "
                + "where idproduto = " + idpassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
                return true;
            } else {
                link.close();
                statement.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro : exclui(): ", e);
            JOptionPane.showConfirmDialog(null, e);
            return false;
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : exclui() finally: ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public String getDescicao(String idPassado) {
        String consultaSQL = "SELECT descricao FROM produtos WHERE idproduto = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();
            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return (String) resultado.getString("descricao");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : getDescicao(): ", e);
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : getDescicao() finally: ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

    public int getIdProduto(String desc) {
        String consultaSQL = "SELECT idproduto FROM produtos WHERE descricao = ?";
        Connection link = null;
        int idProduto = -1;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setString(1, desc);
            ResultSet resultado = statement.executeQuery();
            if (resultado.next()) {
                idProduto = resultado.getInt("idproduto");
                System.out.println("id do " + desc + " é " + idProduto);
            }

            resultado.close();
            statement.close();
        } catch (SQLException e) {
            logger.error("Erro : getIdProduto() : ", e);
            JOptionPane.showMessageDialog(null, "Erro ao buscar o ID do produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : getIdProduto() finally: ", e);
                JOptionPane.showMessageDialog(null, "Erro ao fechar a conexão: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        return idProduto;
    }

    public void removePreVendido(int numeroQuarto, String descricao) {
        Connection link = null;
        try {
            // Obter o idLocacao e idProduto
            int idLocacao = new fquartos().getIdLocacao(numeroQuarto);
            int idProduto = new fprodutos().getIdProduto(descricao);

            if (idProduto == -1) {
                JOptionPane.showMessageDialog(null, "Erro: Produto não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            link = new fazconexao().conectar();
            // Consulta SQL para deletar registros da tabela prevendidos
            String consultaSQL = "DELETE FROM prevendidos WHERE idlocacao = ? AND idproduto = ?";
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, idLocacao);
            statement.setInt(2, idProduto);
            statement.executeUpdate();
            statement.close();
            link.close();
        } catch (SQLException e) {
            // Tratamento de erro
            JOptionPane.showMessageDialog(null, "Erro ao deletar prevendidos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            logger.error("Erro : removePreVendido(): ", e);

        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : removePreVendido() finally: ", e);

            }
        }
    }

    public float getValorProduto(int idPassado) {
        String consultaSQL = "SELECT valorproduto FROM produtos WHERE idproduto = " + idPassado;
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            ResultSet resultado = statement.executeQuery(consultaSQL);
            if (resultado.next()) {
                return resultado.getFloat("valorproduto");
            } else {
                link.close();
                statement.close();
            }
        } catch (Exception e) {
            logger.error("Erro : getValorProduto(): ", e);

            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : getValorProduto() finally: ", e);
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return 0;
    }

    public void inserirPrevendido(int idLocacao, int idProduto, int quantidade) {
        Connection link = null;
        try {
            link = new fazconexao().conectar();

            // Consulta SQL para inserção de dados na tabela prevendidos
            String consultaSQL = "INSERT INTO prevendidos (idlocacao, idproduto, quantidade) VALUES (?, ?, ?)";
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            // Define os parâmetros da consulta
            statement.setInt(1, idLocacao);
            statement.setInt(2, idProduto);
            statement.setInt(3, quantidade);

            // Executa a consulta
            statement.executeUpdate();

            // Fecha os recursos
            statement.close();
            link.close();
        } catch (SQLException e) {
            logger.error("Erro : inserirPrevendido(): ", e);
            JOptionPane.showMessageDialog(null, "Erro ao inserir prevendido: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                logger.error("Erro : inserirPrevendido() finally: ", e);
            }
        }
    }
}
