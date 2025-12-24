package com.motelinteligente.alarme;

import com.formdev.flatlaf.FlatClientProperties;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class EditAlarmDialog extends JDialog {

    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JTextField txtDescricao;
    private int alarmId;
    private boolean saved = false;

    public EditAlarmDialog(Window parent, int alarmId, LocalDateTime currentDateTime, String currentDesc) {
        super(parent, "Editar Alarme", ModalityType.APPLICATION_MODAL);
        this.alarmId = alarmId;
        initComponents(currentDateTime, currentDesc);
    }

    private void initComponents(LocalDateTime currentDateTime, String currentDesc) {
        setLayout(new MigLayout("fill, insets 20", "[right]10[grow, fill]", "[]10[]10[]20[]"));
        setSize(400, 300);
        setLocationRelativeTo(getParent());

        // Header
        JLabel lblHeader = new JLabel("Editar Alarme");
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        add(lblHeader, "span 2, center, wrap 20");

        // Date
        add(new JLabel("Data:"));
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        add(dateSpinner, "wrap");

        // Time
        add(new JLabel("Hora:"));
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        add(timeSpinner, "wrap");

        // Description
        add(new JLabel("Descrição:"));
        txtDescricao = new JTextField(currentDesc);
        add(txtDescricao, "wrap");

        // Set Values
        Date date = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
        dateSpinner.setValue(date);
        timeSpinner.setValue(date);

        // Buttons
        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Salvar");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        btnSave.addActionListener(e -> salvar());

        JPanel pnlButtons = new JPanel(new MigLayout("insets 0", "[grow][grow]"));
        pnlButtons.add(btnCancel, "growx");
        pnlButtons.add(btnSave, "growx");

        add(pnlButtons, "span 2, growx");
    }

    private void salvar() {
        Date datePart = (Date) dateSpinner.getValue();
        Date timePart = (Date) timeSpinner.getValue();

        LocalDateTime ldtDate = datePart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime ldtTime = timePart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        LocalDateTime finalDateTime = LocalDateTime.of(
                ldtDate.toLocalDate(),
                ldtTime.toLocalTime());

        Timestamp ts = Timestamp.valueOf(finalDateTime);
        String desc = txtDescricao.getText();

        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn
                        .prepareStatement("UPDATE alarmes SET hora_despertar = ?, descricao = ? WHERE id = ?")) {

            stmt.setTimestamp(1, ts);
            stmt.setString(2, desc);
            stmt.setInt(3, alarmId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar: Alarme não encontrado.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro BD: " + e.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
