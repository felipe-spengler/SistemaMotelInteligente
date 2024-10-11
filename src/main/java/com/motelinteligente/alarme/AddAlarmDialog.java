package com.motelinteligente.alarme;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class AddAlarmDialog extends JDialog {

    private JSpinner dateSpinner;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JTextField noteField;

    public AddAlarmDialog() {
    setTitle("Adicionar Novo Alarme");
    setLayout(new GridLayout(5, 2));
    setSize(300, 200);
    setLocationRelativeTo(null);

    // Obter a data e hora atuais
    Calendar calendar = Calendar.getInstance();

    // Criar e configurar o SpinnerDateModel para data
    SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    dateSpinner = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
    dateSpinner.setEditor(dateEditor);

    // Configurar centralização para o editor de data
    JTextField dateField = ((JSpinner.DateEditor) dateSpinner.getEditor()).getTextField();
    dateField.setHorizontalAlignment(JTextField.CENTER);

    // Criar e configurar os spinners para hora e minuto
    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
    int currentMinute = calendar.get(Calendar.MINUTE);
    
    hourSpinner = new JSpinner(new SpinnerNumberModel(currentHour, 0, 23, 1));
    minuteSpinner = new JSpinner(new SpinnerNumberModel(currentMinute, 0, 59, 1));

    // Configurar centralização para os editores de hora e minuto
    JTextField hourField = ((JSpinner.NumberEditor) hourSpinner.getEditor()).getTextField();
    hourField.setHorizontalAlignment(JTextField.CENTER);
    JTextField minuteField = ((JSpinner.NumberEditor) minuteSpinner.getEditor()).getTextField();
    minuteField.setHorizontalAlignment(JTextField.CENTER);

    noteField = new JTextField();

    JButton addButton = new JButton("Adicionar Alarme");
    addButton.addActionListener(e -> addAlarm());

    add(new JLabel("Data:"));
    add(dateSpinner);
    add(new JLabel("Hora:"));
    add(hourSpinner);
    add(new JLabel("Minuto:"));
    add(minuteSpinner);
    add(new JLabel("Título do Alarme:"));
    add(noteField);
    add(addButton);
}

    private void addAlarm() {
        // Obtendo a data selecionada no dateSpinner
        LocalDate date = ((SpinnerDateModel) dateSpinner.getModel()).getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        // Obtendo a hora e minutos do hourSpinner e minuteSpinner
        int hour = (int) hourSpinner.getValue();
        int minute = (int) minuteSpinner.getValue();

        // Criando LocalTime com a hora e minuto
        LocalTime time = LocalTime.of(hour, minute);

        // Combinando LocalDate e LocalTime em LocalDateTime
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        // Obtendo o texto da nota (descrição do alarme)
        String note = noteField.getText();

        // Validação simples: verificar se o campo de nota está vazio
        if (note.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha o título do alarme!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection conn = new fazconexao().conectar();
            // Inserir o alarme no banco de dados
            String sql = "INSERT INTO alarmes (hora_adicionado, hora_despertar, descricao) VALUES (NOW(), ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(dateTime));  // Convertendo LocalDateTime para Timestamp
            stmt.setString(2, note);  // Descrição do alarme
            stmt.executeUpdate();
            
            // Atualiza o contador de alarmes ativos
            configGlobal config = configGlobal.getInstance();
            config.incrementarAlarme();
            
            JOptionPane.showMessageDialog(this, "Alarme adicionado com sucesso!");
            dispose();  // Fecha o diálogo após o sucesso
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar o alarme no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
