/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.telas;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import com.motelinteligente.dados.fazconexao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Felipe
 */
public class CodigosPortoes extends JFrame {

    private JTable tabelaPortoes;
    private DefaultTableModel modeloTabela;
    private LookAndFeel temaAnterior;
    private Connection conexao;

    public CodigosPortoes() {
        // --- 1. Salva o tema atual e aplica o tema FlatDarcula ---
        temaAnterior = UIManager.getLookAndFeel();
        FlatDarculaLaf.setup();
        SwingUtilities.updateComponentTreeUI(this);

        // --- Conexão com o Banco de Dados ---
        try {
            // Assumindo que a classe fazconexao.Conecta() existe
            this.conexao = new fazconexao().conectar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados: " + e.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // --- Configuração da Interface Gráfica (Swing) ---
        setTitle("Dados da Tabela Portões");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela

        setLayout(new BorderLayout(10, 10));

        // Título estilizado
        JLabel titulo = new JLabel("Tabela de Portões");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Configura o modelo da tabela
        String[] colunas = {"Portão", "Código", "Bit Length"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Torna as células da tabela não editáveis
            }
        };
        tabelaPortoes = new JTable(modeloTabela);

        // Estilização da tabela
        tabelaPortoes.setFillsViewportHeight(true);
        tabelaPortoes.setRowHeight(25);
        tabelaPortoes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabelaPortoes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabelaPortoes.getTableHeader().setBackground(new Color(50, 50, 50));
        tabelaPortoes.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tabelaPortoes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        add(scrollPane, BorderLayout.CENTER);

        // Carrega os dados na tabela ao iniciar
        carregarDadosNaTabela();
        // --- Adicionando o MouseListener para Duplo Clique ---
        tabelaPortoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int linhaSelecionada = tabelaPortoes.getSelectedRow();
                    if (linhaSelecionada != -1) {
                        editarLinha(linhaSelecionada);
                    }
                }
            }
        });
        // Adiciona um evento para fechar a conexão ao sair
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                fecharConexao();
                if (temaAnterior != null) {
                    FlatLaf.setup(temaAnterior);
                    SwingUtilities.updateComponentTreeUI(getRootPane());
                }
            }
        });
    }

    private void editarLinha(int linha) {
        int portao = (int) modeloTabela.getValueAt(linha, 0);
        int codigo = (int) modeloTabela.getValueAt(linha, 1);
        int bitLength = (int) modeloTabela.getValueAt(linha, 2);

        PortaoEdicaoDialog dialog = new PortaoEdicaoDialog(this, portao, codigo, bitLength);
        dialog.setVisible(true);

        if (dialog.getSalvou()) {
            int novoCodigo = dialog.getCodigo();
            int novoBitLength = dialog.getBitLength();

            atualizarDadosNoBanco(portao, novoCodigo, novoBitLength);
        }
    }

    private void atualizarDadosNoBanco(int portao, int novoCodigo, int novoBitLength) {
        String sql = "UPDATE portoes SET codigo = ?, bitLength = ? WHERE portao = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, novoCodigo);
            stmt.setInt(2, novoBitLength);
            stmt.setInt(3, portao);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(this, "Dados atualizados com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarDadosNaTabela(); // Recarrega a tabela para mostrar as mudanças
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum registro foi atualizado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar dados: " + e.getMessage(), "Erro de Atualização", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void carregarDadosNaTabela() {
        // Limpa a tabela antes de adicionar novos dados
        modeloTabela.setRowCount(0);

        String sql = "SELECT portao, codigo, bitLength FROM portoes";

        try (PreparedStatement stmt = conexao.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] rowData = {
                    rs.getInt("portao"),
                    rs.getInt("codigo"),
                    rs.getInt("bitLength")
                };
                modeloTabela.addRow(rowData);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(), "Erro de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void fecharConexao() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                System.out.println("Conexão com o banco de dados fechada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar a conexão: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CodigosPortoes().setVisible(true);
        });
    }
}
