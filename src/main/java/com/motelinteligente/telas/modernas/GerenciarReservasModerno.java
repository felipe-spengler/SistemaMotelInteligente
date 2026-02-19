package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.telas.NovaReserva;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.border.TitledBorder;

public class GerenciarReservasModerno extends JFrame {

    private JTable tabelaReservas;
    private DefaultTableModel modeloTabela;
    private JButton btnExcluir, btnModificar, btnAdicionar;

    public GerenciarReservasModerno() {
        initComponents();
        carregarReservas();
    }

    private void initComponents() {
        setTitle("Gerenciar Reservas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // Header
        JLabel lblHeader = new JLabel("Reservas");
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        add(lblHeader, "center, wrap 20");

        // Table
        modeloTabela = new DefaultTableModel(
                new Object[] { "ID", "Quarto", "Data", "Hora", "Permanência", "Valor", "Observação" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaReservas = new JTable(modeloTabela);
        tabelaReservas.setRowHeight(30);
        tabelaReservas.setFillsViewportHeight(true);
        tabelaReservas.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "font: bold");

        JScrollPane scrollPane = new JScrollPane(tabelaReservas);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Reservas"));
        add(scrollPane, "grow, wrap 20");

        // Buttons
        JPanel pnlBotoes = new JPanel(new MigLayout("fillx, insets 0", "[grow]10[grow]10[grow]", "[]"));

        btnAdicionar = new JButton("Adicionar Reserva");
        btnAdicionar.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        btnAdicionar.addActionListener(e -> adicionarReserva());
        pnlBotoes.add(btnAdicionar, "growx, h 40!");

        btnModificar = new JButton("Modificar");
        btnModificar.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        btnModificar.addActionListener(e -> modificarReserva());
        pnlBotoes.add(btnModificar, "growx, h 40!");

        btnExcluir = new JButton("Excluir");
        btnExcluir.putClientProperty(FlatClientProperties.STYLE,
                "font: bold; background: #FF4444; foreground: #FFFFFF");
        btnExcluir.addActionListener(e -> excluirReserva());
        pnlBotoes.add(btnExcluir, "growx, h 40!");

        add(pnlBotoes, "growx");

        // Refresh on focus
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                carregarReservas();
            }
        });
    }

    private void adicionarReserva() {
        // Reuse existing dialog for now, assuming it works or will be updated later
        // Ideally we should create NovaReservaModerno
        NovaReserva novaReserva = new NovaReserva();
        novaReserva.setVisible(true);
        // The windowActivated listener above handles refresh
    }

    private void modificarReserva() {
        int linhaSelecionada = tabelaReservas.getSelectedRow();
        if (linhaSelecionada != -1) {
            int idReserva = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
            NovaReserva novaReserva = new NovaReserva(idReserva);
            novaReserva.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma reserva para modificar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void excluirReserva() {
        int linhaSelecionada = tabelaReservas.getSelectedRow();
        if (linhaSelecionada != -1) {
            int resposta = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir esta reserva?",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                int idReserva = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
                if (executarExclusao(idReserva)) {
                    modeloTabela.removeRow(linhaSelecionada);
                    JOptionPane.showMessageDialog(this, "Reserva excluída com sucesso!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma reserva para excluir.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean executingExclusao = false;

    private boolean executarExclusao(int idReserva) {
        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM reservas WHERE id_reserva = ?")) {
            stmt.setInt(1, idReserva);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir reserva: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void carregarReservas() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT id_reserva, numero_quarto, data_entrada, horario_entrada, tempo_permanencia, valor_pago, observacao FROM reservas";
        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                modeloTabela.addRow(new Object[] {
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
            e.printStackTrace(); // Log only to console to avoid spamming dialogs during auto-refresh
        }
    }
}
