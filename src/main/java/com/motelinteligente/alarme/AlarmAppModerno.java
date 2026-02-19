package com.motelinteligente.alarme;

import com.formdev.flatlaf.FlatClientProperties;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AlarmAppModerno extends JFrame {

    private JTable alarmTable;
    private DefaultTableModel tableModel;

    public AlarmAppModerno() {
        initComponents();
        loadAlarmsFromDatabase();
    }

    private void initComponents() {
        setTitle("Despertador");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // Header
        JLabel lblHeader = new JLabel("Alarmes");
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        add(lblHeader, "center, wrap 20");

        // Table
        tableModel = new DefaultTableModel(new String[] { "Data", "Horário", "Descrição" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        alarmTable = new JTable(tableModel);
        alarmTable.setRowHeight(35);
        alarmTable.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "font: bold");
        alarmTable.setFocusable(false);
        alarmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(alarmTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Alarmes Agendados"));
        add(scrollPane, "grow, wrap 20");

        // Buttons
        JPanel pnlButtons = new JPanel(new MigLayout("fillx, insets 0", "[grow]10[grow]", "[]"));

        JButton btnNovo = new JButton("Novo Alarme");
        btnNovo.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        btnNovo.addActionListener(e -> adicionarAlarme());
        pnlButtons.add(btnNovo, "growx, h 40!");

        JButton btnEditar = new JButton("Editar Alarme");
        btnEditar.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        btnEditar.addActionListener(e -> editarAlarme());
        pnlButtons.add(btnEditar, "growx, h 40!");

        JButton btnExcluir = new JButton("Excluir Alarme");
        btnExcluir.putClientProperty(FlatClientProperties.STYLE,
                "font: bold; background: #FF4444; foreground: #FFFFFF");
        btnExcluir.addActionListener(e -> excluirAlarme());
        pnlButtons.add(btnExcluir, "growx, h 40!");

        add(pnlButtons, "growx");

        // Refresh on focus gain
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                loadAlarmsFromDatabase();
            }
        });
    }

    private void adicionarAlarme() {
        // Reuse existing dialog
        AddAlarmDialog dialog = new AddAlarmDialog();
        dialog.setVisible(true);
        // The windowActivated listener will handle refresh when dialog closes (and
        // focus returns)
    }

    private void editarAlarme() {
        int selectedRow = alarmTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                int id = Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString()); // Hidden ID column
                String dateStr = (String) tableModel.getValueAt(selectedRow, 0);
                String timeStr = (String) tableModel.getValueAt(selectedRow, 1);
                String desc = (String) tableModel.getValueAt(selectedRow, 2);

                // Parse LocalDateTime
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                java.time.LocalTime time = java.time.LocalTime.parse(timeStr);
                java.time.LocalDateTime ldt = java.time.LocalDateTime.of(date, time);

                EditAlarmDialog dialog = new EditAlarmDialog(this, id, ldt, desc);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    loadAlarmsFromDatabase();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao preparar edição: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um alarme para editar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void excluirAlarme() {
        int selectedRow = alarmTable.getSelectedRow();
        if (selectedRow != -1) {
            String date = tableModel.getValueAt(selectedRow, 0).toString();
            String time = tableModel.getValueAt(selectedRow, 1).toString();
            String datetime = date + " " + time;

            int resposta = JOptionPane.showConfirmDialog(this, "Excluir alarme de " + datetime + "?", "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                if (removerAlarmeBD(datetime)) {
                    tableModel.removeRow(selectedRow);
                    configGlobal.getInstance().decrementarAlarme();
                    JOptionPane.showMessageDialog(this, "Alarme excluído.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um alarme para excluir.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean removerAlarmeBD(String datetime) {
        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmtSelect = conn.prepareStatement(
                        "SELECT id FROM alarmes WHERE CONCAT(DATE(hora_despertar), ' ', TIME(hora_despertar)) = ?")) {

            stmtSelect.setString(1, datetime);
            ResultSet rs = stmtSelect.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                // Calling FAlarmes logic manually or we can instantiate FAlarmes
                new FAlarmes().removeAlarmFromDatabase(id);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Alarme não encontrado no banco.");
                return false;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage());
            return false;
        }
    }

    private void loadAlarmsFromDatabase() {
        tableModel.setRowCount(0);

        // Add ID column if not exists (initially created with 3)
        if (tableModel.getColumnCount() == 3) {
            tableModel.addColumn("ID");
            // Hide ID column
            alarmTable.getColumnModel().getColumn(3).setMinWidth(0);
            alarmTable.getColumnModel().getColumn(3).setMaxWidth(0);
            alarmTable.getColumnModel().getColumn(3).setWidth(0);
        }

        try (Connection conn = new fazconexao().conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT id, hora_adicionado, hora_despertar, descricao FROM alarmes")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String data = rs.getTimestamp("hora_despertar").toLocalDateTime().toLocalDate().toString();
                String horario = rs.getTime("hora_despertar").toString();
                String descricao = rs.getString("descricao");

                tableModel.addRow(new Object[] { data, horario, descricao, id });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
