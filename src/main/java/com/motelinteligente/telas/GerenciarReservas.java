/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.telas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.fazconexao;

public class GerenciarReservas extends JFrame {

    private JTable tabelaReservas;
    private DefaultTableModel modeloTabela;
    private JButton btnExcluir, btnModificar, btnAdicionar;

    public GerenciarReservas() {
        //FlatIntelliJLaf.setup(); // aplica o tema FlatLaf
        setTitle("Gerenciar Reservas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Quarto", "Data", "Hora", "Permanência", "Valor", "Observação"}, 0);
        tabelaReservas = new JTable(modeloTabela);
        carregarReservas();

        JScrollPane scrollPane = new JScrollPane(tabelaReservas);
        add(scrollPane, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel();
        btnAdicionar = new JButton("Adicionar Reserva");
        btnExcluir = new JButton("Excluir");
        btnModificar = new JButton("Modificar");

        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnModificar);
        add(painelBotoes, BorderLayout.SOUTH);
         btnAdicionar.addActionListener(e -> adicionarReserva());
        btnExcluir.addActionListener(e -> excluirReserva());
        btnModificar.addActionListener(e -> modificarReserva());

        setVisible(true);
    }

    private void adicionarReserva() {
        NovaReserva novaReserva = new NovaReserva();

        novaReserva.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                carregarReservas(); // Atualiza a tabela ao fechar
                GerenciarReservas.this.toFront(); // Ganha foco
            }
        });
    }

    private void carregarReservas() {
        modeloTabela.setNumRows(0);
        String sql = "SELECT id_reserva, numero_quarto, data_entrada, horario_entrada, tempo_permanencia, valor_pago, observacao FROM reservas";
        try (Connection conn = new fazconexao().conectar(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{
                    rs.getInt("id_reserva"),
                    rs.getInt("numero_quarto"),
                    rs.getDate("data_entrada"),
                    rs.getTime("horario_entrada"),
                    rs.getString("tempo_permanencia"),
                    rs.getDouble("valor_pago"),
                    rs.getString("observacao")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar reservas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirReserva() {
        int linhaSelecionada = tabelaReservas.getSelectedRow();
        if (linhaSelecionada != -1) {
            int idReserva = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
            String sql = "DELETE FROM reservas WHERE id_reserva = ?";
            try (Connection conn = new fazconexao().conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idReserva);
                stmt.executeUpdate();
                modeloTabela.removeRow(linhaSelecionada);
                JOptionPane.showMessageDialog(this, "Reserva excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir reserva: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma reserva para excluir.", "Nenhuma Seleção", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modificarReserva() {
        int linhaSelecionada = tabelaReservas.getSelectedRow();
        if (linhaSelecionada != -1) {
            int idReserva = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
            NovaReserva novaReserva = new NovaReserva(idReserva);
            novaReserva.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    carregarReservas(); // Atualiza a tabela ao fechar
                    GerenciarReservas.this.toFront(); // Ganha foco
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma reserva para modificar.", "Nenhuma Seleção", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GerenciarReservas::new);
    }
}
