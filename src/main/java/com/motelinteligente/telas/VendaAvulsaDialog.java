package com.motelinteligente.telas;

import com.motelinteligente.dados.ffuncionario;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vfuncionario;
import com.motelinteligente.telas.controller.TelaPrincipalController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class VendaAvulsaDialog extends JDialog {

    private final TelaPrincipalController controller;
    private List<vfuncionario> funcionarios;
    private int produtoSelecionadoId = -1;
    private String produtoSelecionadoDescricao = "";
    private float produtoSelecionadoValor = 0f;

    private final JComboBox<String> comboTipo;
    private final JComboBox<String> comboFuncionario;
    private final JCheckBox chkAdiantamento;
    private final JTextField txtProdutoSelecionado;
    private final JSpinner spinnerQuantidade;
    private final JLabel lblTotalVenda;
    private final JComboBox<String> comboPagamento;
    private final JTextArea txtObservacao;

    public VendaAvulsaDialog(JFrame parent, TelaPrincipalController controller) {
        super(parent, "Venda Avulsa", true);
        this.controller = controller;
        this.funcionarios = controller.buscarFuncionarios();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(520, 420);
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

        content.add(criarTitulo("Dados da Venda Avulsa"), gbc);

        gbc.gridy++;
        content.add(new JLabel("Tipo:"), gbc);
        comboTipo = new JComboBox<>(new String[] {"Cliente", "Funcionário"});
        comboTipo.addActionListener(e -> atualizarFuncionarioDisponivel());
        gbc.gridx = 1;
        content.add(comboTipo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Funcionário:"), gbc);
        comboFuncionario = new JComboBox<>();
        comboFuncionario.setEnabled(false);
        atualizarFuncionarioDisponivel();
        gbc.gridx = 1;
        content.add(comboFuncionario, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Adiantamento:"), gbc);
        chkAdiantamento = new JCheckBox("Lançar como adiantamento");
        chkAdiantamento.setEnabled(false);
        gbc.gridx = 1;
        content.add(chkAdiantamento, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Produto:"), gbc);
        JPanel produtoPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        produtoPane.setBackground(Color.WHITE);
        txtProdutoSelecionado = new JTextField(24);
        txtProdutoSelecionado.setEditable(false);
        txtProdutoSelecionado.setBackground(Color.WHITE);
        produtoPane.add(txtProdutoSelecionado);
        JButton btnBuscarProduto = new JButton("Buscar Produto");
        btnBuscarProduto.addActionListener(e -> buscarProduto());
        produtoPane.add(btnBuscarProduto);
        gbc.gridx = 1;
        content.add(produtoPane, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Quantidade:"), gbc);
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinnerQuantidade.addChangeListener(e -> atualizarTotal());
        gbc.gridx = 1;
        content.add(spinnerQuantidade, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Total:"), gbc);
        lblTotalVenda = new JLabel("R$ 0,00");
        lblTotalVenda.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalVenda.setForeground(new Color(34, 197, 94));
        gbc.gridx = 1;
        content.add(lblTotalVenda, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Forma de pagamento:"), gbc);
        comboPagamento = new JComboBox<>(new String[] {"Dinheiro", "Crédito", "Débito", "Pix", "Outro"});
        gbc.gridx = 1;
        content.add(comboPagamento, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Observação:"), gbc);
        txtObservacao = new JTextArea(5, 1);
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);
        txtObservacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtObservacao.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(txtObservacao, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setBackground(Color.WHITE);
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> salvarVendaAvulsa());
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        buttons.add(btnCancelar);
        buttons.add(btnSalvar);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private JLabel criarTitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private void atualizarFuncionarioDisponivel() {
        boolean funcionarioSelecionado = "Funcionário".equals(comboTipo.getSelectedItem());
        comboFuncionario.setEnabled(funcionarioSelecionado);
        chkAdiantamento.setEnabled(funcionarioSelecionado);
        comboFuncionario.removeAllItems();
        if (funcionarioSelecionado) {
            if (funcionarios.isEmpty()) {
                comboFuncionario.addItem("Nenhum funcionário cadastrado");
                comboFuncionario.setEnabled(false);
            } else {
                for (vfuncionario func : funcionarios) {
                    comboFuncionario.addItem(func.getNomefuncionario());
                }
            }
        }
    }

    private void buscarProduto() {
        DialogBuscaProduto dialog = new DialogBuscaProduto(this);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            produtoSelecionadoId = dialog.getIdProdutoSelecionado();
            int qtd = dialog.getQuantidadeSelecionada();
            spinnerQuantidade.setValue(qtd);
            produtoSelecionadoDescricao = new fprodutos().getDescicao(String.valueOf(produtoSelecionadoId));
            produtoSelecionadoValor = new fprodutos().getValorProduto(produtoSelecionadoId);
            txtProdutoSelecionado.setText(produtoSelecionadoDescricao + " (R$ " + String.format("%.2f", produtoSelecionadoValor) + ")");
            atualizarTotal();
        }
    }

    private void atualizarTotal() {
        int qtd = (Integer) spinnerQuantidade.getValue();
        float total = produtoSelecionadoValor * qtd;
        lblTotalVenda.setText("R$ " + String.format(java.util.Locale.US, "%.2f", total));
    }

    private void salvarVendaAvulsa() {
        if (produtoSelecionadoId <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto antes de salvar.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade = (Integer) spinnerQuantidade.getValue();
        if (quantidade <= 0) {
            JOptionPane.showMessageDialog(this, "Quantidade deve ser maior que zero.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipo = comboTipo.getSelectedItem().toString().equals("Funcionário")
                ? (chkAdiantamento.isSelected() ? "adiantamento" : "funcionario")
                : "cliente";

        String formaPgto = switch (comboPagamento.getSelectedItem().toString()) {
            case "Crédito" -> "credito";
            case "Débito" -> "debito";
            case "Pix" -> "pix";
            case "Outro" -> "outro";
            default -> "dinheiro";
        };

        String descricaoCompleta = produtoSelecionadoDescricao;
        if (comboTipo.getSelectedItem().toString().equals("Funcionário") && comboFuncionario.getSelectedItem() != null) {
            descricaoCompleta += " [Funcionário: " + comboFuncionario.getSelectedItem().toString() + "]";
        }
        String observacao = txtObservacao.getText().trim();
        if (!observacao.isEmpty()) {
            descricaoCompleta += " - " + observacao;
        }

        float valorTotal = produtoSelecionadoValor * quantidade;
        boolean sucesso = controller.salvarVendaAvulsa(produtoSelecionadoId, descricaoCompleta, quantidade,
                produtoSelecionadoValor, valorTotal, tipo, formaPgto);
        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Venda avulsa salva no caixa com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            // Imprimir o extrato
            String funcionarioNome = "";
            if (comboTipo.getSelectedItem().toString().equals("Funcionário") && comboFuncionario.getSelectedItem() != null) {
                funcionarioNome = comboFuncionario.getSelectedItem().toString();
            }
            try {
                com.motelinteligente.dados.ImpressoraService.imprimirVendaAvulsa(
                    produtoSelecionadoDescricao,
                    quantidade,
                    produtoSelecionadoValor,
                    valorTotal,
                    tipo,
                    formaPgto,
                    funcionarioNome
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            dispose();
        }
    }
}
