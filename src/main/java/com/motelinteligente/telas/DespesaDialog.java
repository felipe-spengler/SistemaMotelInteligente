package com.motelinteligente.telas;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fcaixa;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class DespesaDialog extends JDialog {

    private final JTextField txtDescricao;
    private final JComboBox<String> comboCategoria;
    private final JTextField txtValor;
    private final JComboBox<String> comboPagamento;
    private final JComboBox<String> comboStatus;
    private final JCheckBox chkCaixaAtual;
    private Integer idEdicao = null;

    public DespesaDialog(JFrame parent) {
        super(parent, "Lançamento de Despesa", true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(460, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        content.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titulo = new JLabel("Lançar Nova Despesa");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(Color.DARK_GRAY);
        content.add(titulo, gbc);

        gbc.gridy++;
        content.add(new JLabel("Descrição:"), gbc);
        txtDescricao = new JTextField(20);
        gbc.gridx = 1;
        content.add(txtDescricao, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Categoria:"), gbc);
        comboCategoria = new JComboBox<>(new String[] {
            "Limpeza", "Manutenção", "Água/Luz/Internet", "Lavanderia", "Aluguel", "Salários", "Outros"
        });
        gbc.gridx = 1;
        content.add(comboCategoria, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Valor (R$):"), gbc);
        txtValor = new JTextField(10);
        gbc.gridx = 1;
        content.add(txtValor, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Forma de Pagamento:"), gbc);
        comboPagamento = new JComboBox<>(new String[] {"Dinheiro", "Pix", "Cartão", "Boleto", "Outro"});
        gbc.gridx = 1;
        content.add(comboPagamento, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Status:"), gbc);
        comboStatus = new JComboBox<>(new String[] {"Pago", "Pendente"});
        gbc.gridx = 1;
        content.add(comboStatus, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Tirar do Caixa:"), gbc);
        chkCaixaAtual = new JCheckBox("Pagar com o Caixa do Dia");
        chkCaixaAtual.setSelected(true);
        chkCaixaAtual.setBackground(Color.WHITE);
        gbc.gridx = 1;
        content.add(chkCaixaAtual, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setBackground(Color.WHITE);
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> salvarDespesa());
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        buttons.add(btnCancelar);
        buttons.add(btnSalvar);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public DespesaDialog(JFrame parent, int id, String descricao, String categoria, float valor, String formaPgto, String status, boolean doCaixa) {
        this(parent);
        this.idEdicao = id;
        setTitle("Editar Despesa");
        txtDescricao.setText(descricao);
        comboCategoria.setSelectedItem(categoria);
        txtValor.setText(String.valueOf(valor));
        
        String pgtoText = switch (formaPgto.toLowerCase()) {
            case "pix" -> "Pix";
            case "cartao", "cartão" -> "Cartão";
            case "boleto" -> "Boleto";
            case "outro" -> "Outro";
            default -> "Dinheiro";
        };
        comboPagamento.setSelectedItem(pgtoText);
        
        String statusText = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        comboStatus.setSelectedItem(statusText);
        
        chkCaixaAtual.setSelected(doCaixa);
    }

    private void salvarDespesa() {
        String descricao = txtDescricao.getText().trim();
        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A descrição é obrigatória.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        float valor = 0;
        try {
            valor = Float.parseFloat(txtValor.getText().replace(",", "."));
            if (valor <= 0) {
                JOptionPane.showMessageDialog(this, "O valor deve ser maior que zero.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Digite um valor numérico válido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String categoria = comboCategoria.getSelectedItem().toString();
        String formaPgto = switch (comboPagamento.getSelectedItem().toString()) {
            case "Pix" -> "pix";
            case "Cartão" -> "cartao";
            case "Boleto" -> "boleto";
            case "Outro" -> "outro";
            default -> "dinheiro";
        };
        String status = comboStatus.getSelectedItem().toString().toLowerCase();

        Integer idCaixa = null;
        if (chkCaixaAtual.isSelected()) {
            int caixa = configGlobal.getInstance().getCaixa();
            if (caixa == 0) {
                JOptionPane.showMessageDialog(this, "Não há nenhum caixa aberto no momento. Desmarque 'Pagar com o Caixa do Dia' se for uma despesa geral.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            idCaixa = caixa;
        }

        boolean sucesso;
        if (idEdicao != null) {
            sucesso = new fcaixa().editarDespesa(idEdicao, idCaixa, descricao, categoria, valor, formaPgto, status);
        } else {
            sucesso = new fcaixa().salvarDespesa(idCaixa, descricao, categoria, valor, formaPgto, status);
        }
        
        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Despesa salva com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao salvar despesa no banco.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
