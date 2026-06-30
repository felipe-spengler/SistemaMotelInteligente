package com.motelinteligente.telas;

import com.fazecast.jserialcomm.SerialPort;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TelaTesteControtec extends javax.swing.JFrame {

    private javax.swing.JComboBox<String> comboPorta;
    private javax.swing.JComboBox<String> comboSuite;
    private javax.swing.JComboBox<String> comboCanal;
    private javax.swing.JTextField txtRele;
    private javax.swing.JTextArea txtAreaLog;
    private javax.swing.JButton btnLigar;
    private javax.swing.JButton btnDesligar;
    private javax.swing.JButton btnVarredura;

    public TelaTesteControtec() {
        setTitle("Painel de Testes Físicos Controtec");
        setSize(580, 520);
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(30, 41, 59)); // Slate-800 Dark
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Controle de Teste Serial Controtec", JLabel.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(56, 189, 248)); // Cyan
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Porta COM
        JLabel lblPorta = new JLabel("Porta Serial:");
        lblPorta.setForeground(Color.WHITE);
        lblPorta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblPorta, gbc);

        gbc.gridx = 1;
        comboPorta = new JComboBox<>(new String[]{"COM4", "COM3"});
        comboPorta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(comboPorta, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Suíte
        JLabel lblSuite = new JLabel("Suíte (ID Placa):");
        lblSuite.setForeground(Color.WHITE);
        lblSuite.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblSuite, gbc);

        gbc.gridx = 1;
        comboSuite = new JComboBox<>(new String[]{
            "Suíte 101 (ID 1)",
            "Suíte 102 (ID 2)",
            "Suíte 104 (ID 4)",
            "Suíte 105 (ID 5)",
            "Suíte 108 (ID 8)",
            "Suíte 109 (ID 9)",
            "Suíte 110 (ID 10)",
            "Portão Entrada/Saída (ID 88/99)"
        });
        comboSuite.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(comboSuite, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Canal
        JLabel lblCanal = new JLabel("Canal da Placa:");
        lblCanal.setForeground(Color.WHITE);
        lblCanal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblCanal, gbc);

        gbc.gridx = 1;
        comboCanal = new JComboBox<>(new String[]{
            "Canal 32 (Garagem/Luzes - LB032)",
            "Canal 128 (Sinal Corredor - PCA)",
            "Canal 0 (Portões - LP000)"
        });
        comboCanal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(comboCanal, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Relé
        JLabel lblRele = new JLabel("Relé de Saída:");
        lblRele.setForeground(Color.WHITE);
        lblRele.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblRele, gbc);

        gbc.gridx = 1;
        txtRele = new JTextField("1");
        txtRele.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(txtRele, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        // Botões de Ação
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setOpaque(false);

        btnLigar = new JButton("LIGAR (204)");
        btnLigar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLigar.setBackground(new Color(16, 185, 129)); // Verde
        btnLigar.setForeground(Color.WHITE);
        btnLigar.addActionListener(this::btnLigarActionPerformed);
        buttonPanel.add(btnLigar);

        btnDesligar = new JButton("DESLIGAR (0)");
        btnDesligar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDesligar.setBackground(new Color(239, 68, 68)); // Vermelho
        btnDesligar.setForeground(Color.WHITE);
        btnDesligar.addActionListener(this::btnDesligarActionPerformed);
        buttonPanel.add(btnDesligar);

        btnVarredura = new JButton("VARREDURA MÚLTIPLA");
        btnVarredura.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVarredura.setBackground(new Color(30, 144, 255)); // Azul
        btnVarredura.setForeground(Color.WHITE);
        btnVarredura.addActionListener(this::btnVarreduraActionPerformed);
        buttonPanel.add(btnVarredura);

        mainPanel.add(buttonPanel, gbc);

        gbc.gridy++;
        
        // Área de Log
        txtAreaLog = new JTextArea(8, 40);
        txtAreaLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtAreaLog.setBackground(new Color(15, 23, 42)); // Slate-900 Darker
        txtAreaLog.setForeground(new Color(74, 222, 128)); // Verde Terminal
        txtAreaLog.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtAreaLog);
        mainPanel.add(scroll, gbc);

        add(mainPanel);
    }

    private void btnLigarActionPerformed(ActionEvent evt) {
        enviarComando(204);
    }

    private void btnDesligarActionPerformed(ActionEvent evt) {
        enviarComando(0);
    }

    private void btnVarreduraActionPerformed(ActionEvent evt) {
        String porta = (String) comboPorta.getSelectedItem();
        int placa = extrairIDPlaca();
        int canal = extrairCanal();
        int rele = extrairRele();

        txtAreaLog.append("=== INICIANDO VARREDURA MÚLTIPLA DE VALORES ===\n");
        txtAreaLog.append("Testando valores lógicos comuns para descobrir qual a Controtec aceita...\n");

        SerialPort serialPort = SerialPort.getCommPort(porta);
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        if (!serialPort.openPort()) {
            txtAreaLog.append("ERRO: Porta " + porta + " ocupada. Feche o gateway antigo!\n\n");
            return;
        }

        try {
            // Valores lógicos comuns de ativação física:
            // 0 (Desligar), 1 (Ligar padrão), 3 (Canal WB), 48 (LD), 204 (WA Suíte 101), 255 (Ativação total)
            int[] valoresDeTeste = {0, 1, 3, 48, 204, 255};

            for (int val : valoresDeTeste) {
                byte[] packet = new byte[5];
                packet[0] = (byte) placa;
                packet[1] = (byte) canal;
                packet[2] = (byte) rele;
                packet[3] = (byte) val;

                int sum = 0;
                for (int i = 0; i < 4; i++) { sum += (packet[i] & 0xFF); }
                packet[4] = (byte) ((-sum) & 0xFF);

                serialPort.writeBytes(packet, packet.length);
                txtAreaLog.append("Enviado valor: " + val + " (Checksum: 0x" + String.format("%02X", packet[4]) + ")\n");
                
                Thread.sleep(150); // Delay entre tentativas
            }
            txtAreaLog.append("=== FIM DA VARREDURA MÚLTIPLA ===\n\n");
        } catch (Exception e) {
            txtAreaLog.append("ERRO na varredura: " + e.getMessage() + "\n\n");
        } finally {
            serialPort.closePort();
        }
    }

    private int extrairIDPlaca() {
        String suiteStr = (String) comboSuite.getSelectedItem();
        if (suiteStr.contains("Portão")) {
            return 88; // IDs fictícios ou padrão de portão
        }
        try {
            String temp = suiteStr.substring(suiteStr.indexOf("ID ") + 3);
            return Integer.parseInt(temp.replace(")", "").trim());
        } catch (Exception e) {
            return 1;
        }
    }

    private int extrairCanal() {
        int sel = comboCanal.getSelectedIndex();
        if (sel == 0) return 32;
        if (sel == 1) return 128;
        return 0; // LP000
    }

    private int extrairRele() {
        try {
            return Integer.parseInt(txtRele.getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    private void enviarComando(int valor) {
        String porta = (String) comboPorta.getSelectedItem();
        int placa = extrairIDPlaca();
        int canal = extrairCanal();
        int rele = extrairRele();

        txtAreaLog.append("Enviando -> Porta: " + porta + " | Placa ID: " + placa + " | Canal: " + canal + " | Relé: " + rele + " | Valor: " + valor + "\n");

        SerialPort serialPort = SerialPort.getCommPort(porta);
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        if (!serialPort.openPort()) {
            txtAreaLog.append("ERRO: Porta " + porta + " ocupada. Feche o gateway antigo!\n\n");
            return;
        }

        try {
            byte[] packet = new byte[5];
            packet[0] = (byte) placa;
            packet[1] = (byte) canal;
            packet[2] = (byte) rele;
            packet[3] = (byte) valor;

            // Calcula Checksum LRC 2-complementos
            int sum = 0;
            for (int i = 0; i < 4; i++) {
                sum += (packet[i] & 0xFF);
            }
            packet[4] = (byte) ((-sum) & 0xFF);

            serialPort.writeBytes(packet, packet.length);

            // Aguarda resposta
            Thread.sleep(200);
            if (serialPort.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                StringBuilder sb = new StringBuilder("Resposta Placa: ");
                for (int i = 0; i < numRead; i++) {
                    sb.append(String.format("0x%02X ", readBuffer[i]));
                }
                txtAreaLog.append("SUCESSO: " + sb.toString() + "\n\n");
            } else {
                txtAreaLog.append("SUCESSO: Comando enviado (sem resposta da placa).\n\n");
            }
        } catch (Exception e) {
            txtAreaLog.append("ERRO ao transmitir: " + e.getMessage() + "\n\n");
        } finally {
            serialPort.closePort();
        }
    }
}
