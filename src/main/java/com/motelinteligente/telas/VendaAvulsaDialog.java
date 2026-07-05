package com.motelinteligente.telas;

import com.motelinteligente.dados.ffuncionario;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vfuncionario;
import com.motelinteligente.telas.controller.TelaPrincipalController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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

    // CardLayout switching
    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    // Step 1 components
    private final JTable tabelaItens;
    private final DefaultTableModel modeloItens;
    private final JTextField txtProdutoSelecionado;
    private final JSpinner spinnerQuantidade;

    // Step 2 components
    private final JComboBox<String> comboTipo;
    private final JComboBox<String> comboFuncionario;
    private final JComboBox<String> comboPagamento;
    private final JTextArea txtObservacao;

    // Bottom panels
    private final JLabel lblTotalVenda1;
    private final JLabel lblTotalVenda2;

    public VendaAvulsaDialog(JFrame parent, TelaPrincipalController controller) {
        super(parent, "Venda Avulsa", true);
        this.controller = controller;
        this.funcionarios = controller.buscarFuncionarios();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(650, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(Color.WHITE);

        // ==========================================
        // PASSO 1: SELEÇÃO DE PRODUTOS
        // ==========================================
        JPanel passo1Panel = new JPanel(new BorderLayout(10, 10));
        passo1Panel.setBackground(Color.WHITE);
        passo1Panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Topo: Seletor de Produtos
        JPanel painelSelecao = new JPanel(new GridBagLayout());
        painelSelecao.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblTitulo1 = new JLabel("Passo 1: Adicione os Produtos da Venda");
        lblTitulo1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo1.setForeground(new Color(33, 150, 243));
        painelSelecao.add(lblTitulo1, gbc);

        gbc.gridy++;
        JPanel prodBuscaPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        prodBuscaPane.setBackground(Color.WHITE);
        prodBuscaPane.add(new JLabel("Produto:"));
        txtProdutoSelecionado = new JTextField(20);
        txtProdutoSelecionado.setEditable(false);
        txtProdutoSelecionado.setBackground(Color.WHITE);
        prodBuscaPane.add(txtProdutoSelecionado);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarProduto());
        prodBuscaPane.add(btnBuscar);
        painelSelecao.add(prodBuscaPane, gbc);

        gbc.gridy++;
        JPanel qtdAddPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        qtdAddPane.setBackground(Color.WHITE);
        qtdAddPane.add(new JLabel("Quantidade:"));
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        qtdAddPane.add(spinnerQuantidade);
        JButton btnAdicionar = new JButton("Adicionar Item");
        btnAdicionar.setBackground(new Color(33, 150, 243));
        btnAdicionar.setForeground(Color.WHITE);
        btnAdicionar.addActionListener(e -> adicionarItem());
        qtdAddPane.add(btnAdicionar);
        painelSelecao.add(qtdAddPane, gbc);

        passo1Panel.add(painelSelecao, BorderLayout.NORTH);

        // Centro: Tabela de Itens
        modeloItens = new DefaultTableModel(new Object[] {"ID", "Produto", "Qtd", "Preço Unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaItens = new JTable(modeloItens);
        tabelaItens.setRowHeight(22);
        tabelaItens.getColumnModel().getColumn(0).setMinWidth(0);
        tabelaItens.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelaItens.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollItens = new JScrollPane(tabelaItens);
        scrollItens.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        passo1Panel.add(scrollItens, BorderLayout.CENTER);

        // Rodapé Passo 1: Remover Item, Total e Próximo
        JPanel rodape1 = new JPanel(new BorderLayout());
        rodape1.setBackground(Color.WHITE);

        JPanel botoesTabela = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        botoesTabela.setBackground(Color.WHITE);
        JButton btnRemover = new JButton("Remover Item");
        btnRemover.setBackground(new Color(244, 67, 54));
        btnRemover.setForeground(Color.WHITE);
        btnRemover.addActionListener(e -> removerItemSelecionado());
        botoesTabela.add(btnRemover);
        rodape1.add(botoesTabela, BorderLayout.WEST);

        JPanel navegacao1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        navegacao1.setBackground(Color.WHITE);
        lblTotalVenda1 = new JLabel("Total: R$ 0,00");
        lblTotalVenda1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalVenda1.setForeground(new Color(34, 197, 94));
        navegacao1.add(lblTotalVenda1);
        JButton btnProximo = new JButton("Avançar >");
        btnProximo.setBackground(new Color(76, 175, 80));
        btnProximo.setForeground(Color.WHITE);
        btnProximo.addActionListener(e -> avancarParaPasso2());
        navegacao1.add(btnProximo);
        rodape1.add(navegacao1, BorderLayout.EAST);

        passo1Panel.add(rodape1, BorderLayout.SOUTH);
        cardsPanel.add(passo1Panel, "passo1");

        // ==========================================
        // PASSO 2: DADOS DE PAGAMENTO E FINALIZAÇÃO
        // ==========================================
        JPanel passo2Panel = new JPanel(new BorderLayout(15, 15));
        passo2Panel.setBackground(Color.WHITE);
        passo2Panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(8, 8, 8, 8);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        gbc2.gridx = 0; gbc2.gridy = 0;
        JLabel lblTitulo2 = new JLabel("Passo 2: Informe os Dados de Pagamento");
        lblTitulo2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo2.setForeground(new Color(33, 150, 243));
        formPanel.add(lblTitulo2, gbc2);

        gbc2.gridy++;
        formPanel.add(new JLabel("Tipo de Venda:"), gbc2);
        comboTipo = new JComboBox<>(new String[] {"Cliente", "Funcionário"});
        comboTipo.addActionListener(e -> atualizarTipoVenda());
        gbc2.gridx = 1;
        formPanel.add(comboTipo, gbc2);

        gbc2.gridx = 0; gbc2.gridy++;
        formPanel.add(new JLabel("Funcionário:"), gbc2);
        comboFuncionario = new JComboBox<>();
        comboFuncionario.setEnabled(false);
        gbc2.gridx = 1;
        formPanel.add(comboFuncionario, gbc2);

        gbc2.gridx = 0; gbc2.gridy++;
        formPanel.add(new JLabel("Forma de Pagamento:"), gbc2);
        comboPagamento = new JComboBox<>(new String[] {"Dinheiro", "Crédito", "Débito", "Pix", "Outro"});
        gbc2.gridx = 1;
        formPanel.add(comboPagamento, gbc2);

        gbc2.gridx = 0; gbc2.gridy++;
        formPanel.add(new JLabel("Observação:"), gbc2);
        txtObservacao = new JTextArea(4, 25);
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);
        txtObservacao.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtObservacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollObs = new JScrollPane(txtObservacao);
        gbc2.gridx = 1;
        gbc2.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollObs, gbc2);

        passo2Panel.add(formPanel, BorderLayout.CENTER);

        // Rodapé Passo 2: Voltar, Total e Finalizar
        JPanel rodape2 = new JPanel(new BorderLayout());
        rodape2.setBackground(Color.WHITE);

        JPanel navegacao2Back = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        navegacao2Back.setBackground(Color.WHITE);
        JButton btnVoltar = new JButton("< Voltar");
        btnVoltar.addActionListener(e -> voltarParaPasso1());
        navegacao2Back.add(btnVoltar);
        rodape2.add(navegacao2Back, BorderLayout.WEST);

        JPanel navegacao2Next = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        navegacao2Next.setBackground(Color.WHITE);
        lblTotalVenda2 = new JLabel("Total: R$ 0,00");
        lblTotalVenda2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalVenda2.setForeground(new Color(34, 197, 94));
        navegacao2Next.add(lblTotalVenda2);
        
        JButton btnFinalizar = new JButton("Finalizar Venda");
        btnFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFinalizar.setBackground(new Color(76, 175, 80));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.addActionListener(e -> salvarVendaAvulsa());
        navegacao2Next.add(btnFinalizar);
        rodape2.add(navegacao2Next, BorderLayout.EAST);

        passo2Panel.add(rodape2, BorderLayout.SOUTH);
        cardsPanel.add(passo2Panel, "passo2");

        add(cardsPanel, BorderLayout.CENTER);
        
        // Inicializar opções
        atualizarTipoVenda();
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
        String formatado = "Total: R$ " + String.format(java.util.Locale.US, "%.2f", total);
        lblTotalVenda1.setText(formatado);
        lblTotalVenda2.setText(formatado);
    }

    private void avancarParaPasso2() {
        if (modeloItens.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um produto para continuar.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cardLayout.show(cardsPanel, "passo2");
    }

    private void voltarParaPasso1() {
        cardLayout.show(cardsPanel, "passo1");
    }

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
