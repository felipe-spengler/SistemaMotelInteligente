package com.motelinteligente.dados;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import javax.swing.*;

public class MiniCalendario {

    boolean setou = false;
    int selectedYear;
    int selectedMonth;
    String dataSelecionada = null;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // ...
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void setDataSelecionada(String novaDataSelecionada) {
        System.out.println("setando data");
        if (!Objects.equals(this.dataSelecionada, novaDataSelecionada)) {
            String antigaDataSelecionada = this.dataSelecionada;
            this.dataSelecionada = novaDataSelecionada;
            propertyChangeSupport.firePropertyChange("dataSelecionada", antigaDataSelecionada, novaDataSelecionada);
        }
    }

    public void notifyPropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void MiniCalendario(int x, int y) {
        JFrame frame = new JFrame("Mini Calendário");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocation(x, y);

        JPanel panel = new JPanel(new BorderLayout());

        JComboBox<String> yearComboBox = new JComboBox<>();
        JComboBox<String> monthComboBox = new JComboBox<>();

        JButton showCalendarButton = new JButton("Mostrar Calendário");

        JPanel calendarPanel = new JPanel(new GridLayout(0, 7));

        String[] daysOfWeek = {"DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day);
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            calendarPanel.add(dayLabel);
        }

        JButton[] dayButtons = new JButton[42];

        for (int i = 0; i < 42; i++) {
            dayButtons[i] = new JButton("");
            dayButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton clickedButton = (JButton) e.getSource();
                    String buttonText = clickedButton.getText();
                    if (!buttonText.isEmpty()) {
                        int day = Integer.parseInt(buttonText);
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, day);
                        dataSelecionada = selectedDate;
                        // Notifique os ouvintes na tela principal diretamente
                        notifyPropertyChange("dataSelecionada", null, selectedDate);
                        frame.dispose();
                    }
                }
            });
            calendarPanel.add(dayButtons[i]);
        }

        for (int year = 2017; year <= 2100; year++) {
            yearComboBox.addItem(Integer.toString(year));
        }

        String[] months = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        for (int i = 0; i < 12; i++) {
            monthComboBox.addItem(months[i]);
        }

        showCalendarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());
                selectedMonth = monthComboBox.getSelectedIndex();

                Calendar calendar = Calendar.getInstance();
                calendar.set(selectedYear, selectedMonth, 1);
                int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                for (int i = 0; i < 42; i++) {
                    dayButtons[i].setText("");
                }

                int dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                for (int i = 1; i <= maxDay; i++) {
                    dayButtons[dayIndex].setText(Integer.toString(i));
                    dayIndex++;
                }
            }
        });
        JPanel inputPanel = new JPanel();
        inputPanel.add(yearComboBox);
        inputPanel.add(monthComboBox);
        inputPanel.add(showCalendarButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(calendarPanel), BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);

    }

}
