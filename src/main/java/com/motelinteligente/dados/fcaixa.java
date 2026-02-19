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
import java.text.SimpleDateFormat;
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
        // Verifica se já existe caixa aberto
        if (new fazconexao().verificaCaixa() != 0) {
            JOptionPane.showMessageDialog(null, "O Caixa já está Aberto!");
            return false;
        }

        String sqlBuscaId = "SELECT COALESCE(MAX(id), 0) + 1 AS novo_id FROM caixa";
        String sqlInsert = "INSERT INTO caixa (id, horaabre, usuarioabre, saldoabre) VALUES (?, ?, ?, ?)";

        try (Connection link = new fazconexao().conectar();
                PreparedStatement psBusca = link.prepareStatement(sqlBuscaId);
                PreparedStatement psInsert = link.prepareStatement(sqlInsert)) {

            // Busca o próximo ID manualmente
            ResultSet rs = psBusca.executeQuery();
            int novoId = 1;
            if (rs.next()) {
                novoId = rs.getInt("novo_id");
            }

            Date dataAtual = new Date();
            Timestamp timestamp = new Timestamp(dataAtual.getTime());
            configGlobal config = configGlobal.getInstance();

            // Preenche o INSERT
            psInsert.setInt(1, novoId);
            psInsert.setTimestamp(2, timestamp);
            psInsert.setString(3, config.getUsuario());
            psInsert.setFloat(4, valorAbrir);

            int resultado = psInsert.executeUpdate();

            if (resultado > 0) {
                JOptionPane.showMessageDialog(null, "Caixa Aberto com Sucesso! ID: " + novoId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Erro ao tentar abrir o caixa: ", e);
            JOptionPane.showMessageDialog(null, "Erro ao abrir o caixa: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, "Erro ao fechar o caixa: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }

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
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, "Erro ao obter dados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }

    // --- NOVOS MÉTODOS PARA SUPORTE AO CAIXAFRAME REFATORADO ---

    public float getAntecipadoAtual(int idCaixaAtual) {
        String sql = "SELECT a.valor, a.tipo FROM registralocado rl "
                + "JOIN antecipado a ON rl.idlocacao = a.idlocacao "
                + "WHERE rl.horafim IS NULL AND a.idcaixaatual = ?";
        float valor = 0;
        try (Connection link = new fazconexao().conectar(); PreparedStatement statement = link.prepareStatement(sql)) {
            statement.setInt(1, idCaixaAtual);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    if (!"desconto".equals(resultado.getString("tipo"))) {
                        valor += resultado.getFloat("valor");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar antecipado atual: ", e);
        }
        return valor;
    }

    public valores getAntecipadoDetalhado(int idCaixaAtual) {
        valores v = new valores();
        String sql = "SELECT a.valor, a.tipo FROM registralocado rl "
                + "JOIN antecipado a ON rl.idlocacao = a.idlocacao "
                + "WHERE rl.horafim IS NULL AND a.idcaixaatual = ?";
        try (Connection link = new fazconexao().conectar(); PreparedStatement statement = link.prepareStatement(sql)) {
            statement.setInt(1, idCaixaAtual);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    float val = rs.getFloat("valor");
                    String tipo = rs.getString("tipo");
                    if (tipo != null) {
                        tipo = tipo.toLowerCase();
                        if (tipo.contains("dinheiro")) {
                            v.entradaD += val;
                        } else if (tipo.contains("cartao") || tipo.contains("cartão")) {
                            v.entradaC += val;
                        } else if (tipo.contains("pix")) {
                            v.entradaP += val;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar antecipado detalhado: ", e);
        }
        return v;
    }

    public float getAntecipadoOutros(int idCaixaAtual) {
        String sql = "SELECT a.valor, a.tipo FROM registralocado rl "
                + "JOIN antecipado a ON rl.idlocacao = a.idlocacao "
                + "WHERE rl.idcaixaatual = ? AND rl.horafim IS NOT NULL AND a.idcaixaatual != ?";
        float valor = 0;
        try (Connection link = new fazconexao().conectar(); PreparedStatement statement = link.prepareStatement(sql)) {
            statement.setInt(1, idCaixaAtual);
            statement.setInt(2, idCaixaAtual);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    if (!"desconto".equals(resultado.getString("tipo"))) {
                        valor += resultado.getFloat("valor");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar antecipado outros: ", e);
        }
        return valor;
    }

    public float getOutrosCaixas(int idCaixaAtual) {
        // Alias for getAntecipadoOutros to match internal method naming convention
        return getAntecipadoOutros(idCaixaAtual);
    }

    public float getCaixaAtual(int idCaixaAtual) {
        // Alias for getAntecipadoAtual to match internal method naming convention
        return getAntecipadoAtual(idCaixaAtual);
    }

    public float[] getTotaisJustificativas(List<Integer> idsLocacao) {
        float[] totais = new float[2]; // 0: descontos, 1: acrescimos
        if (idsLocacao == null || idsLocacao.isEmpty())
            return totais;

        StringBuilder idsClause = new StringBuilder();
        for (int i = 0; i < idsLocacao.size(); i++) {
            idsClause.append(idsLocacao.get(i));
            if (i < idsLocacao.size() - 1)
                idsClause.append(",");
        }

        String sqlDesconto = "SELECT SUM(valor) AS total FROM justificativa WHERE tipo = 'desconto' AND idlocacao IN ("
                + idsClause + ")";
        String sqlAcrescimo = "SELECT SUM(valor) AS total FROM justificativa WHERE tipo = 'acrescimo' AND idlocacao IN ("
                + idsClause + ")";

        try (Connection link = new fazconexao().conectar(); Statement stmt = link.createStatement()) {

            try (ResultSet rsD = stmt.executeQuery(sqlDesconto)) {
                if (rsD.next())
                    totais[0] = rsD.getFloat("total");
            }
            try (ResultSet rsA = stmt.executeQuery(sqlAcrescimo)) {
                if (rsA.next())
                    totais[1] = rsA.getFloat("total");
            }

        } catch (SQLException e) {
            logger.error("Erro ao calcular justificativas: ", e);
        }
        return totais;
    }

    public List<Object[]> getListaLocacoes(int idCaixa) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT rl.*, j.valor AS valor_just, j.tipo AS tipo_just "
                + "FROM registralocado rl "
                + "LEFT JOIN justificativa j ON rl.idlocacao = j.idlocacao "
                + "WHERE rl.idcaixaatual = ? ORDER BY rl.horainicio";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

        try (Connection link = new fazconexao().conectar(); PreparedStatement stmt = link.prepareStatement(sql)) {
            stmt.setInt(1, idCaixa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    float vQuarto = rs.getFloat("valorquarto");
                    float vConsumo = rs.getFloat("valorconsumo");
                    float desc = 0, acres = 0;

                    String tipo = rs.getString("tipo_just");
                    if ("acrescimo".equals(tipo))
                        acres = rs.getFloat("valor_just");
                    if ("desconto".equals(tipo))
                        desc = rs.getFloat("valor_just");

                    String ini = dateFormat.format(new Date(rs.getTimestamp("horainicio").getTime()));
                    String fim = rs.getTimestamp("horafim") != null
                            ? dateFormat.format(new Date(rs.getTimestamp("horafim").getTime()))
                            : "";

                    float total = vQuarto + vConsumo + acres - desc;

                    lista.add(new Object[] {
                            ini, fim, rs.getInt("numquarto"), vQuarto, vConsumo, desc, acres, total,
                            rs.getInt("idlocacao")
                    });
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar lista locações: ", e);
        }
        return lista;
    }
}
