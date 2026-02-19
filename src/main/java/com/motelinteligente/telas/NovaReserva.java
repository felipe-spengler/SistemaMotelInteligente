package com.motelinteligente.telas;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.fazconexao;
import com.toedter.calendar.JDateChooser;

public class NovaReserva extends JFrame {

    private JDateChooser campoData;
    private JTextField campoHora, campoValor;
    private JComboBox<String> comboPermanencia, comboQuartos;
    private JTextArea campoObs;
    private JButton btnSalvar;
    private int idReserva;

    public NovaReserva() {
        this(0); // Chama o construtor com ID 0 para nova reserva
    }

    public NovaReserva(int idReserva) {
        //FlatIntelliJLaf.setup();// aplica o tema FlatLaf
        setTitle("Nova Reserva");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        this.idReserva = idReserva;

        // Painel principal com BorderLayout
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel de campos com GridBagLayout
        JPanel painelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNumero = new JLabel("Número do Quarto:");
        comboQuartos = new JComboBox<>();
        gbc.gridx = 0;
        gbc.gridy = 0;
        painelCampos.add(lblNumero, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        painelCampos.add(comboQuartos, gbc);

        // Data de Entrada com JDateChooser
        gbc.gridx = 0;
        gbc.gridy = 1;
        painelCampos.add(new JLabel("Data de Entrada:"), gbc);

        gbc.gridx = 1;
        campoData = new JDateChooser();
        painelCampos.add(campoData, gbc);

        // Hora de Entrada
        gbc.gridx = 0;
        gbc.gridy = 2;
        painelCampos.add(new JLabel("Hora de Entrada (HH:mm):"), gbc);

        gbc.gridx = 1;
        campoHora = new JTextField(10);
        painelCampos.add(campoHora, gbc);

        // Tempo de Permanência
        gbc.gridx = 0;
        gbc.gridy = 3;
        painelCampos.add(new JLabel("Tempo de Permanência:"), gbc);

        gbc.gridx = 1;
        comboPermanencia = new JComboBox<>(new String[]{"periodo", "pernoite"});
        painelCampos.add(comboPermanencia, gbc);

        // Valor Pago
        gbc.gridx = 0;
        gbc.gridy = 4;
        painelCampos.add(new JLabel("Valor Pago:"), gbc);

        gbc.gridx = 1;
        campoValor = new JTextField(10);
        painelCampos.add(campoValor, gbc);

        painelPrincipal.add(painelCampos, BorderLayout.NORTH);

        // Painel para observação e botão
        JPanel painelInferior = new JPanel(new BorderLayout(5, 5));

        // Campo Observação
        JLabel lblObs = new JLabel("Observação:");
        campoObs = new JTextArea(4, 30);
        campoObs.setLineWrap(true);
        campoObs.setWrapStyleWord(true);
        campoObs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scrollObs = new JScrollPane(campoObs);
        scrollObs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollObs.setPreferredSize(new Dimension(100, 80));

        // Painel separado só para o label + text area
        JPanel painelObs = new JPanel(new BorderLayout());
        painelObs.add(lblObs, BorderLayout.NORTH);
        painelObs.add(scrollObs, BorderLayout.CENTER);

        // Botão Salvar centralizado
        btnSalvar = new JButton("Salvar Reserva");
        JPanel painelBotao = new JPanel();
        painelBotao.add(btnSalvar);

        painelInferior.add(painelObs, BorderLayout.CENTER);
        painelInferior.add(painelBotao, BorderLayout.SOUTH);

        painelPrincipal.add(painelInferior, BorderLayout.CENTER);

        // Finaliza a montagem da janela
        setContentPane(painelPrincipal);
        setVisible(true);
        btnSalvar.addActionListener(e -> salvarReserva());
        carregarQuartos(comboQuartos);

        // Se um ID foi passado, carregar os dados da reserva
        if (idReserva > 0) {
            carregarDadosReserva(idReserva);
        }
    }

    private void carregarQuartos(JComboBox<String> combo) {
        String sql = "SELECT numeroquarto FROM quartos ORDER BY numeroquarto";
        try (Connection conn = new fazconexao().conectar(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.addItem(rs.getString("numeroquarto"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar quartos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarDadosReserva(int idReserva) {
        String sql = "SELECT numero_quarto, data_entrada, horario_entrada, tempo_permanencia, valor_pago, observacao FROM reservas WHERE id_reserva = ?";
        try (Connection conn = new fazconexao().conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReserva);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int numeroQuarto = rs.getInt("numero_quarto");
                comboQuartos.setSelectedItem(String.valueOf(numeroQuarto)); // Converte para String se necessário
                campoData.setDate(rs.getDate("data_entrada"));
                campoHora.setText(rs.getTime("horario_entrada").toString());
                comboPermanencia.setSelectedItem(rs.getString("tempo_permanencia"));
                campoValor.setText(String.valueOf(rs.getDouble("valor_pago")));
                campoObs.setText(rs.getString("observacao"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados da reserva: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarReserva() {
        try {
            int numero = Integer.parseInt(comboQuartos.getSelectedItem().toString().trim());
            java.util.Date dataUtil = campoData.getDate();
            if (dataUtil == null) {
                JOptionPane.showMessageDialog(this, "Selecione uma data válida!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate data = new java.sql.Date(dataUtil.getTime()).toLocalDate();
            LocalTime hora = LocalTime.parse(campoHora.getText());
            String permanencia = comboPermanencia.getSelectedItem().toString();
            double valor = Double.parseDouble(campoValor.getText().replace(",", ".").trim());
            String obs = campoObs.getText().trim();

            if (idReserva == 0) {
                // Inserir nova reserva
                String sql = "INSERT INTO reservas (numero_quarto, data_entrada, horario_entrada, tempo_permanencia, valor_pago, observacao) VALUES (?, ?, ?, ?, ?, ?)";
                try (Connection conn = new fazconexao().conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, numero);
                    stmt.setDate(2, Date.valueOf(data));
                    stmt.setTime(3, Time.valueOf(hora));
                    stmt.setString(4, permanencia);
                    stmt.setDouble(5, valor);
                    stmt.setString(6, obs.isEmpty() ? null : obs);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Reserva salva com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // Atualizar reserva existente
                String sql = "UPDATE reservas SET numero_quarto = ?, data_entrada = ?, horario_entrada = ?, tempo_permanencia = ?, valor_pago = ?, observacao = ? WHERE id_reserva = ?";
                try (Connection conn = new fazconexao().conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, numero);
                    stmt.setDate(2, Date.valueOf(data));
                    stmt.setTime(3, Time.valueOf(hora));
                    stmt.setString(4, permanencia);
                    stmt.setDouble(5, valor);
                    stmt.setString(6, obs.isEmpty() ? null : obs);
                    stmt.setInt(7, idReserva);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Reserva atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            this.dispose(); // Fecha a janela após salvar
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar reserva: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NovaReserva().setVisible(true));
    }
}