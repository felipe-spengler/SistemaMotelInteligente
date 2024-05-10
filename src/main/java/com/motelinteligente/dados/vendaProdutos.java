/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author MOTEL
 */
public class vendaProdutos {

    public int idProduto;
    public int quantidade;
    public float valorUnd;
    public float valorTotal;

    public class gerenciaVenda {

        public List<vendaProdutos> listaDeProdutos;

        public List<vendaProdutos> carregaLista() {
            List<vendaProdutos> lista = new ArrayList<>();
            configGlobal config = configGlobal.getInstance();
            int idCaixa = config.getCaixa();
            String consultaSQL = "SELECT * FROM registravendido WHERE idcaixaatual = " + idCaixa;
            Connection link = null;

            try {
                link = new fazconexao().conectar();
                Statement statement = link.createStatement();
                boolean jaTem = false;
                ResultSet resultado = statement.executeQuery(consultaSQL);
                while (resultado.next()) {
                    vendaProdutos prod = new vendaProdutos();
                    prod.idProduto = resultado.getInt("idproduto");
                    prod.quantidade = resultado.getInt("quantidade");
                    prod.valorUnd = resultado.getFloat("valorunidade");
                    prod.valorTotal = resultado.getFloat("valortotal");

                    jaTem = false;
                    int x = lista.size();

                    for (int i = 0; i < x; i++) {
                        if (lista.get(i).idProduto == prod.idProduto && lista.get(i).valorUnd == prod.valorUnd) {
                            lista.get(i).quantidade += prod.quantidade;
                            jaTem = true;
                        }
                    }
                    if (!jaTem) {
                        lista.add(prod);
                    }
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
            return lista;
        }

        public List<vendaProdutos> listaPorLocacao(ArrayList<Integer> idList) {
            String consultaSQL = "SELECT * FROM registravendido";
            List<vendaProdutos> lista = new ArrayList<>();

            Connection link = null;
            try {
                link = new fazconexao().conectar();
                Statement statement = link.createStatement();
                ResultSet resultado = statement.executeQuery(consultaSQL);
                boolean jaTem = false;
                while (resultado.next()) {
                    int idAtual = resultado.getInt("idlocacao");
                    // Verifica se o idlocacao atual está contido na lista de IDs
                    if (idList.contains(idAtual)) {
                        vendaProdutos prod = new vendaProdutos();
                        prod.idProduto = resultado.getInt("idproduto");
                        prod.quantidade = resultado.getInt("quantidade");
                        prod.valorUnd = resultado.getFloat("valorunidade");
                        prod.valorTotal = resultado.getFloat("valortotal");

                        jaTem = false;
                        int x = lista.size();

                        for (int i = 0; i < x; i++) {
                            if (lista.get(i).idProduto == prod.idProduto && lista.get(i).valorUnd == prod.valorUnd) {
                                lista.get(i).quantidade += prod.quantidade;
                                jaTem = true;
                            }
                        }
                        if (!jaTem) {
                            lista.add(prod);
                        }
                    }
                }
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(null, e);
            } finally {
                try {
                    if (link != null && !link.isClosed()) {
                        link.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return lista;
        }
        public List<vendaProdutos> vendidoLocacao(int idpassado) {
            String consultaSQL = "SELECT * FROM registravendido WHERE idlocacao = " + idpassado;
            List<vendaProdutos> lista = new ArrayList<>();

            Connection link = null;
            try {
                link = new fazconexao().conectar();
                Statement statement = link.createStatement();
                ResultSet resultado = statement.executeQuery(consultaSQL);
                while (resultado.next()) {
                    int idAtual = resultado.getInt("idlocacao");
                    // Verifica se o idlocacao atual está contido na lista de IDs

                        vendaProdutos prod = new vendaProdutos();
                        prod.idProduto = resultado.getInt("idproduto");
                        prod.quantidade = resultado.getInt("quantidade");
                        prod.valorUnd = resultado.getFloat("valorunidade");
                        prod.valorTotal = resultado.getFloat("valortotal");

                        lista.add(prod);
                    }
                
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(null, e);
            } finally {
                try {
                    if (link != null && !link.isClosed()) {
                        link.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return lista;
        }
    }

    public static void main(String[] args) {
        vendaProdutos venda = new vendaProdutos();
        vendaProdutos.gerenciaVenda gerenciaVenda = venda.new gerenciaVenda();

        // Agora você pode usar gerenciaVenda
        List<vendaProdutos> listaDeProdutos = gerenciaVenda.carregaLista();
    }
}
