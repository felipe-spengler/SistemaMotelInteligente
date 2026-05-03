/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.telas;

import javax.swing.*;
import java.awt.*;

public class PortaoEdicaoDialog extends JDialog {

    private JTextField txtPortao;
    private JTextField txtCodigo;
    private JTextField txtBitLength;
    private boolean salvou = false;

    public PortaoEdicaoDialog(JFrame parent, int portao, int codigo, int bitLength) {
        super(parent, "Editar Portão", true);
        
        setLayout(new BorderLayout(10, 10));
        
        // --- Painel de Campos de Entrada ---
        JPanel painelCampos = new JPanel(new GridLayout(3, 2, 5, 5));
        painelCampos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painelCampos.add(new JLabel("Portão:"));
        txtPortao = new JTextField(String.valueOf(portao));
        txtPortao.setEditable(false); // O campo "Portão" não pode ser editado
        painelCampos.add(txtPortao);

        painelCampos.add(new JLabel("Código:"));
        txtCodigo = new JTextField(String.valueOf(codigo));
        painelCampos.add(txtCodigo);

        painelCampos.add(new JLabel("Bit Length:"));
        txtBitLength = new JTextField(String.valueOf(bitLength));
        painelCampos.add(txtBitLength);

        // --- Painel de Botões ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> {
            salvou = true;
            dispose();
        });

        btnCancelar.addActionListener(e -> {
            salvou = false;
            dispose();
        });

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);

        add(painelCampos, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        pack(); // Ajusta o tamanho da janela para os componentes
        setLocationRelativeTo(parent);
    }
    
    // Métodos para obter os valores atualizados
    public boolean getSalvou() {
        return salvou;
    }

    public int getCodigo() {
        return Integer.parseInt(txtCodigo.getText());
    }

    public int getBitLength() {
        return Integer.parseInt(txtBitLength.getText());
    }
}