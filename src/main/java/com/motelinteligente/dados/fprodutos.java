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
        String consultaSQL = "select * from produtos order by idproduto";

        try (Connection link = new fazconexao().conectar();
                PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    vprodutos produto = new vprodutos();
                    produto.setIdProduto(resultado.getInt("idproduto"));
                    produto.setDescricao(resultado.getString("descricao"));
                    produto.setValor(resultado.getFloat("valorproduto"));
                    produto.setEstoque(resultado.getString("estoque"));
                    produto.setDataCompra(resultado.getTimestamp("ultimacompra"));
                    produtos.add(produto);
                }
            }

        } catch (SQLException e) {
            logger.error("Erro : mostrarProduto(): ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao mostrar produtos: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } // 'link' e 'statement' fechados automaticamente

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
                        if (linhasAfetadas > 0)
                            logger.info("Estoque atualizado. Produto ID: " + idProduto + " - " + quantidade
                                    + " unidades. Restam: " + novoEstoque);
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

        String sql = "INSERT INTO produtos (idproduto, descricao, valorproduto, estoque, ultimacompra) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection link = new fazconexao().conectar();
                PreparedStatement st = link.prepareStatement(sql)) {

            st.setInt(1, dados.getIdProduto());
            st.setString(2, dados.getDescricao());
            st.setFloat(3, dados.getValor());
            st.setString(4, dados.getEstoque());
            st.setTimestamp(5, dados.getDataCompra());

            boolean result = st.executeUpdate() > 0;
            if (result)
                logger.info("Novo produto cadastrado: " + dados.getDescricao());
            return result;

        } catch (SQLException e) {
            logger.error("Erro : insercao(): ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao inserir produto: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean exclusao(int idpassado) {

        String sql = "DELETE FROM produtos WHERE idproduto = ?";

        try (Connection link = new fazconexao().conectar();
                PreparedStatement st = link.prepareStatement(sql)) {

            st.setInt(1, idpassado);
            return st.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Erro : exclusao(): ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao excluir produto: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public vprodutos getProduto(int idPassado) {

        String sql = "SELECT * FROM produtos WHERE idproduto = ?";
        vprodutos produto = null;

        try (Connection link = new fazconexao().conectar();
                PreparedStatement st = link.prepareStatement(sql)) {

            st.setInt(1, idPassado);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    produto = new vprodutos();
                    produto.setIdProduto(idPassado);
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setValor(rs.getFloat("valorproduto"));
                    produto.setEstoque(rs.getString("estoque"));
                }
            }

        } catch (SQLException e) {
            logger.error("Erro : getProduto(): ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao buscar produto: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        return produto;
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
        String consultaSQL = "SELECT descricao FROM produtos WHERE idproduto = ?";

        try (
                Connection link = new fazconexao().conectar();
                PreparedStatement statement = link.prepareStatement(consultaSQL)) {
            statement.setString(1, idPassado);

            try (ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getString("descricao");
                }
            }

        } catch (Exception e) {
            logger.error("Erro : getDescicao(): ", e);
            JOptionPane.showMessageDialog(null, e);
        }

        return null;
    }

    public int getIdProduto(String desc) {
        String sql = "SELECT idproduto FROM produtos WHERE descricao = ?";

        try (Connection link = new fazconexao().conectar(); PreparedStatement st = link.prepareStatement(sql)) {

            st.setString(1, desc);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idproduto");
                }
            }

        } catch (SQLException e) {
            logger.error("Erro : getIdProduto() : ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao buscar o ID do produto: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        return -1;
    }

    public void removePreVendido(int numeroQuarto, String descricao, int qntd) {

        int idLocacao = new fquartos().getIdLocacao(numeroQuarto);
        int idProduto = getIdProduto(descricao);

        if (idProduto == -1) {
            JOptionPane.showMessageDialog(null, "Erro: Produto não encontrado.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "DELETE FROM prevendidos "
                + "WHERE idlocacao = ? AND idproduto = ? AND quantidade = ? LIMIT 1";

        try (Connection link = new fazconexao().conectar(); PreparedStatement st = link.prepareStatement(sql)) {

            st.setInt(1, idLocacao);
            st.setInt(2, idProduto);
            st.setInt(3, qntd);
            st.executeUpdate();

        } catch (SQLException e) {
            logger.error("Erro : removePreVendido(): ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao deletar prevendidos: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public float getValorProduto(int idPassado) {
        String sql = "SELECT valorproduto FROM produtos WHERE idproduto = ?";

        try (Connection link = new fazconexao().conectar(); PreparedStatement st = link.prepareStatement(sql)) {

            st.setInt(1, idPassado);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("valorproduto");
                }
            }

        } catch (SQLException e) {
            logger.error("Erro : getValorProduto(): ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao buscar valor do produto: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        return 0;
    }

    public void inserirPrevendido(int idLocacao, int idProduto, int quantidade) {

        String sql = "INSERT INTO prevendidos (idlocacao, idproduto, quantidade) "
                + "VALUES (?, ?, ?)";

        try (Connection link = new fazconexao().conectar(); PreparedStatement st = link.prepareStatement(sql)) {

            st.setInt(1, idLocacao);
            st.setInt(2, idProduto);
            st.setInt(3, quantidade);
            st.executeUpdate();

        } catch (SQLException e) {
            logger.error("Erro : inserirPrevendido(): ", e);
            JOptionPane.showMessageDialog(null,
                    "Erro ao inserir prevendido: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

}
