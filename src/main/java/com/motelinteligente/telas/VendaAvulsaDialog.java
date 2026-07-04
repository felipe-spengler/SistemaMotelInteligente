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
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class VendaAvulsaDialog extends JDialog {

    private final TelaPrincipalController controller;
    private List<vfuncionario> funcionarios;
    
    private int produtoSelecionadoId = -1;
    private String produtoSelecionadoDescricao = "";
    private float produtoSelecionadoValor = 0f;

    private final JComboBox<String> comboTipo;
    private final JComboBox<String> comboFuncionario;
    
    private final JTextField txtProdutoSelecionado;
    private final JSpinner spinnerQuantidade;
    private final JLabel lblTotalVenda;
    private final JComboBox<String> comboPagamento;
    private final JTextArea txtObservacao;
    
    private final JTable tabelaItens;
    private final DefaultTableModel modeloItens;

    public VendaAvulsaDialog(JFrame parent, TelaPrincipalController controller) {
        super(parent, "Venda Avulsa", true);
        this.controller = controller;
        this.funcionarios = controller.buscarFuncionarios();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(780, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        content.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        content.add(criarTitulo("Dados da Venda"), gbc);

        gbc.gridy++;
        content.add(new JLabel("Tipo:"), gbc);
        comboTipo = new JComboBox<>(new String[] {"Cliente", "Funcionário"});
        comboTipo.addActionListener(e -> atualizarTipoVenda());
        gbc.gridx = 1;
        content.add(comboTipo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Funcionário:"), gbc);
        comboFuncionario = new JComboBox<>();
        comboFuncionario.setEnabled(false);
        gbc.gridx = 1;
        content.add(comboFuncionario, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Produto:"), gbc);
        JPanel produtoPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        produtoPane.setBackground(Color.WHITE);
        txtProdutoSelecionado = new JTextField(15);
        txtProdutoSelecionado.setEditable(false);
        txtProdutoSelecionado.setBackground(Color.WHITE);
        produtoPane.add(txtProdutoSelecionado);
        JButton btnBuscarProduto = new JButton("Buscar");
        btnBuscarProduto.addActionListener(e -> buscarProduto());
        produtoPane.add(btnBuscarProduto);
        gbc.gridx = 1;
        content.add(produtoPane, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Quantidade:"), gbc);
        JPanel qtdPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        qtdPane.setBackground(Color.WHITE);
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        qtdPane.add(spinnerQuantidade);
        JButton btnAdicionar = new JButton("Adicionar Item");
        btnAdicionar.setBackground(new Color(33, 150, 243));
        btnAdicionar.setForeground(Color.WHITE);
        btnAdicionar.addActionListener(e -> adicionarItem());
        qtdPane.add(btnAdicionar);
        gbc.gridx = 1;
        content.add(qtdPane, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Forma de Pagamento:"), gbc);
        comboPagamento = new JComboBox<>(new String[] {"Dinheiro", "Crédito", "Débito", "Pix", "Outro"});
        gbc.gridx = 1;
        content.add(comboPagamento, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        content.add(new JLabel("Observação:"), gbc);
        txtObservacao = new JTextArea(3, 20);
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);
        txtObservacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtObservacao.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollObs = new JScrollPane(txtObservacao);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(scrollObs, gbc);

        // Painel Direita (Tabela de Itens Selecionados)
        JPanel painelTabela = new JPanel(new BorderLayout(5, 5));
        painelTabela.setBackground(Color.WHITE);
        painelTabela.setBorder(BorderFactory.createTitledBorder("Itens da Venda"));

        modeloItens = new DefaultTableModel(new Object[] {"ID", "Produto", "Qtd", "Preço Unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaItens = new JTable(modeloItens);
        tabelaItens.setRowHeight(22);
        
        // Hide ID column
        tabelaItens.getColumnModel().getColumn(0).setMinWidth(0);
        tabelaItens.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelaItens.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollTabela = new JScrollPane(tabelaItens);
        painelTabela.add(scrollTabela, BorderLayout.CENTER);

        JPanel painelTabelaBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelTabelaBotoes.setBackground(Color.WHITE);
        JButton btnRemover = new JButton("Remover Item");
        btnRemover.setBackground(new Color(244, 67, 54));
        btnRemover.setForeground(Color.WHITE);
        btnRemover.addActionListener(e -> removerItemSelecionado());
        painelTabelaBotoes.add(btnRemover);
        painelTabela.add(painelTabelaBotoes, BorderLayout.SOUTH);

        // Layout Geral: Divide a tela entre o formulário de entrada (Oeste) e a tabela (Centro)
        add(content, BorderLayout.WEST);
        add(painelTabela, BorderLayout.CENTER);

        // Painel Sul (Salvar / Total)
        JPanel painelSul = new JPanel(new BorderLayout());
        painelSul.setBackground(Color.WHITE);
        painelSul.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JPanel painelTotal = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        painelTotal.setBackground(Color.WHITE);
        painelTotal.add(new JLabel("TOTAL DA VENDA:"));
        lblTotalVenda = new JLabel("R$ 0,00");
        lblTotalVenda.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalVenda.setForeground(new Color(34, 197, 94));
        painelTotal.add(lblTotalVenda);
        painelSul.add(painelTotal, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttons.setBackground(Color.WHITE);
        JButton btnSalvar = new JButton("Salvar Venda");
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSalvar.setBackground(new Color(76, 175, 80));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.addActionListener(e -> salvarVendaAvulsa());
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        buttons.add(btnCancelar);
        buttons.add(btnSalvar);
        painelSul.add(buttons, BorderLayout.EAST);

        add(painelSul, BorderLayout.SOUTH);

        atualizarTipoVenda();
    }

    private JLabel criarTitulo(String texto) {
        label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }
    private JLabel label;

    private void atualizarTipoVenda() {
        boolean isFuncionario = "Funcionário".equals(comboTipo.getSelectedItem());
        comboFuncionario.setEnabled(isFuncionario);
        comboFuncionario.removeAllItems();
        
        comboPagamento.removeAllItems();
        comboPagamento.addItem("Dinheiro");
        comboPagamento.addItem("Crédito");
        comboPagamento.addItem("Débito");
        comboPagamento.addItem("Pix");
        if (isFuncionario) {
            comboPagamento.addItem("Adiantamento");
            if (funcionarios.isEmpty()) {
                comboFuncionario.addItem("Nenhum funcionário cadastrado");
                comboFuncionario.setEnabled(false);
            } else {
                for (vfuncionario func : funcionarios) {
                    comboFuncionario.addItem(func.getNomefuncionario());
                }
            }
        }
        comboPagamento.addItem("Outro");
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
        }
    }

    private void adicionarItem() {
        if (produtoSelecionadoId <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto primeiro.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qtd = (Integer) spinnerQuantidade.getValue();
        if (qtd <= 0) {
            JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        float subtotal = produtoSelecionadoValor * qtd;
        modeloItens.addRow(new Object[] {
            produtoSelecionadoId,
            produtoSelecionadoDescricao,
            qtd,
            produtoSelecionadoValor,
            subtotal
        });

        // Limpar seleção
        produtoSelecionadoId = -1;
        produtoSelecionadoDescricao = "";
        produtoSelecionadoValor = 0f;
        txtProdutoSelecionado.setText("");
        spinnerQuantidade.setValue(1);

        atualizarTotalGeral();
    }

    private void removerItemSelecionado() {
        int row = tabelaItens.getSelectedRow();
        if (row >= 0) {
            modeloItens.removeRow(row);
            atualizarTotalGeral();
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um item para remover.", "Atenção", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarTotalGeral() {
        float total = 0f;
        for (int i = 0; i < modeloItens.getRowCount(); i++) {
            total += (Float) modeloItens.getValueAt(i, 4);
        }
        lblTotalVenda.setText("R$ " + String.format(java.util.Locale.US, "%.2f", total));
    }

    private void salvarVendaAvulsa() {
        if (modeloItens.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um produto na tabela.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipoVenda = comboTipo.getSelectedItem().toString();
        String pgto = comboPagamento.getSelectedItem().toString();

        String tipoDb = tipoVenda.equals("Funcionário")
                ? ("Adiantamento".equals(pgto) ? "adiantamento" : "funcionario")
                : "cliente";

        String formaPgtoDb = switch (pgto) {
            case "Crédito" -> "credito";
            case "Débito" -> "debito";
            case "Pix" -> "pix";
            case "Adiantamento" -> "adiantamento";
            case "Outro" -> "outro";
            default -> "dinheiro";
        };

        String funcionarioNome = "";
        if (tipoVenda.equals("Funcionário") && comboFuncionario.getSelectedItem() != null) {
            funcionarioNome = comboFuncionario.getSelectedItem().toString();
        }

        List<Object[]> itensParaImpressao = new ArrayList<>();
        boolean todasSalvas = true;

        for (int i = 0; i < modeloItens.getRowCount(); i++) {
            int idProd = (Integer) modeloItens.getValueAt(i, 0);
            String descProd = String.valueOf(modeloItens.getValueAt(i, 1));
            int qtd = (Integer) modeloItens.getValueAt(i, 2);
            float valUnit = (Float) modeloItens.getValueAt(i, 3);
            float valTotal = (Float) modeloItens.getValueAt(i, 4);

            String descricaoCompleta = descProd;
            if (!funcionarioNome.isEmpty()) {
                descricaoCompleta += " [Funcionário: " + funcionarioNome + "]";
            }
            String obs = txtObservacao.getText().trim();
            if (!obs.isEmpty()) {
                descricaoCompleta += " - " + obs;
            }

            boolean ok = controller.salvarVendaAvulsa(idProd, descricaoCompleta, qtd, valUnit, valTotal, tipoDb, formaPgtoDb);
            if (ok) {
                itensParaImpressao.add(new Object[] { descProd, qtd, valUnit, valTotal });
            } else {
                todasSalvas = false;
            }
        }

        if (todasSalvas) {
            JOptionPane.showMessageDialog(this, "Venda avulsa salva com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            // Imprimir extrato unificado de todos os itens
            try {
                com.motelinteligente.dados.ImpressoraService.imprimirMultiplasVendasAvulsas(
                    itensParaImpressao,
                    tipoDb,
                    formaPgtoDb,
                    funcionarioNome
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro ao salvar uma ou mais vendas no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
