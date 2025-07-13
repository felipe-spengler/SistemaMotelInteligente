package com.motelinteligente.alarme;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.sql.*;

public class AlarmApp extends JFrame {

    private JTable alarmTable;
    private DefaultTableModel tableModel;

    public AlarmApp() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(600, 400);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        // Modelo da Tabela (sem header)
        tableModel = new DefaultTableModel(new String[]{"Data", "Horário", "Descrição"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Impede a edição das células
            }
        };

        // JTable com renderizador customizado para alternar as cores das linhas
        alarmTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
                }
                return c;
            }
        };

        // Ajustando a fonte da tabela
        alarmTable.setFont(new Font("Arial", Font.PLAIN, 14));
        alarmTable.setRowHeight(25);  // Altura da linha

        // Remover o cabeçalho da tabela
        alarmTable.setTableHeader(null);

        // **Remover bordas da tabela**
        alarmTable.setShowGrid(false);
        alarmTable.setIntercellSpacing(new Dimension(0, 0));
        alarmTable.setBorder(null);

        // Definir o tamanho das colunas
        TableColumnModel columnModel = alarmTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120);  // Data (ajustado para 120)
        columnModel.getColumn(1).setPreferredWidth(120);  // Horário (ajustado para 120)
        columnModel.getColumn(2).setPreferredWidth(350);  // Descrição (ajustado para preencher o restante)

        // Carregar alarmes do banco de dados
        loadAlarmsFromDatabase();

        JPanel panel = new JPanel();
        JButton addButton = new JButton("Novo Alarme");
        JButton deleteButton = new JButton("Excluir Alarme");

        addButton.addActionListener(this::addAlarm);
        deleteButton.addActionListener(this::deleteAlarm);

        panel.add(addButton);
        panel.add(deleteButton);

        this.add(new JScrollPane(alarmTable), BorderLayout.CENTER);
        this.add(panel, BorderLayout.SOUTH);

        // Adiciona um WindowFocusListener para recarregar os alarmes sempre que a janela ganhar o foco
        this.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                loadAlarmsFromDatabase(); // Recarrega os alarmes ao ganhar o foco
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                // Não é necessário fazer nada ao perder o foco
            }
        });
        this.setVisible(true);
    }

    private void addAlarm(ActionEvent e) {
        // Criar e mostrar o diálogo para adicionar um novo alarme
        AddAlarmDialog dialog = new AddAlarmDialog();
        dialog.setVisible(true);
    }

    private void deleteAlarm(ActionEvent e) {
        int selectedRow = alarmTable.getSelectedRow();
        if (selectedRow != -1) {
            // Extract date and time from the selected row
            String date = tableModel.getValueAt(selectedRow, 0).toString();
            String time = tableModel.getValueAt(selectedRow, 1).toString();

            // Combine date and time
            String datetime = date + " " + time;

            // Query the database to find the ID
            try ( Connection conn = new fazconexao().conectar();  PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM alarmes WHERE CONCAT(DATE(hora_despertar), ' ', TIME(hora_despertar)) = ?")) {

                stmt.setString(1, datetime);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int alarmId = rs.getInt("id");

                    // Remove the alarm from the database
                    new FAlarmes().removeAlarmFromDatabase(alarmId);
                    tableModel.removeRow(selectedRow);

                    configGlobal config = configGlobal.getInstance();
                    config.decrementarAlarme();
                } else {
                    JOptionPane.showMessageDialog(this, "Alarme não encontrado.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir alarme.");
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um alarme para excluir.");
        }
    }

    private void loadAlarmsFromDatabase() {
        tableModel.setNumRows(0);  // Limpa a tabela antes de recarregar
        // Carregar alarmes do banco de dados
        try ( Connection conn = new fazconexao().conectar();  Statement stmt = conn.createStatement();  ResultSet rs = stmt.executeQuery("SELECT id, hora_adicionado, hora_despertar, descricao FROM alarmes")) {

            while (rs.next()) {
                String data = rs.getTimestamp("hora_despertar").toLocalDateTime().toLocalDate().toString();
                String horario = rs.getTime("hora_despertar").toString();
                String descricao = rs.getString("descricao");

                tableModel.addRow(new Object[]{data, horario, descricao});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
