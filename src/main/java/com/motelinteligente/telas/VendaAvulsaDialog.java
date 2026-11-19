package com.motelinteligente.telas;

import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vfuncionario;
import com.motelinteligente.telas.controller.TelaPrincipalController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class VendaAvulsaDialog extends JDialog {

    private final TelaPrincipalController controller;
    private final List<vfuncionario> funcionarios;
    
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
    private final JTextField txtDescontoReais;
    private final JTextField txtDescontoPorcento;
    private final JTextArea txtObservacao;
    private final JLabel lblSubtotalPasso2;
    private final JLabel lblDescontoPasso2;
    private final JLabel lblTotalLiquidoPasso2;
    private final JButton btnFinalizar;

    private boolean isUpdatingDesconto = false;
    private float subtotalBruto = 0f;
    private float valorDesconto = 0f;
    private float totalLiquido = 0f;

    // Bottom panels
    private final JLabel lblTotalVenda1;
    private final JLabel lblTotalVenda2;

    public VendaAvulsaDialog(JFrame parent, TelaPrincipalController controller) {
        super(parent, "Venda Avulsa", true);
        this.controller = controller;
        this.funcionarios = controller.buscarFuncionarios();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(680, 540);
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
        lblTitulo1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo1.setForeground(new Color(33, 150, 243));
        painelSelecao.add(lblTitulo1, gbc);

        gbc.gridy++;
        JPanel prodBuscaPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        prodBuscaPane.setBackground(Color.WHITE);
        prodBuscaPane.add(new JLabel("Produto:"));
        txtProdutoSelecionado = new JTextField(22);
        txtProdutoSelecionado.setEditable(false);
        txtProdutoSelecionado.setBackground(Color.WHITE);
        prodBuscaPane.add(txtProdutoSelecionado);
        JButton btnBuscar = new JButton("Buscar Produto");
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
        tabelaItens.setRowHeight(24);
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
        lblTotalVenda1 = new JLabel("Subtotal: R$ 0,00");
        lblTotalVenda1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalVenda1.setForeground(new Color(34, 197, 94));
        navegacao1.add(lblTotalVenda1);
        JButton btnProximo = new JButton("Avançar >");
        btnProximo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProximo.setBackground(new Color(76, 175, 80));
        btnProximo.setForeground(Color.WHITE);
        btnProximo.addActionListener(e -> avancarParaPasso2());
        navegacao1.add(btnProximo);
        rodape1.add(navegacao1, BorderLayout.EAST);

        passo1Panel.add(rodape1, BorderLayout.SOUTH);
        cardsPanel.add(passo1Panel, "passo1");

        // ==========================================
        // PASSO 2: DESCONTO, TIPO E PAGAMENTO
        // ==========================================
        JPanel passo2Panel = new JPanel(new BorderLayout(15, 15));
        passo2Panel.setBackground(Color.WHITE);
        passo2Panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(7, 8, 7, 8);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.gridwidth = 2;
        JLabel lblTitulo2 = new JLabel("Passo 2: Desconto e Finalização da Venda");
        lblTitulo2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo2.setForeground(new Color(33, 150, 243));
        formPanel.add(lblTitulo2, gbc2);

        gbc2.gridwidth = 1;
        gbc2.gridy++;
        formPanel.add(new JLabel("Modalidade da Venda:"), gbc2);
        comboTipo = new JComboBox<>(new String[] {
            "Cliente (Pagamento na Hora)",
            "Funcionário (Adiantamento na Folha)",
            "Funcionário (Pagamento na Hora)"
        });
        comboTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboTipo.addActionListener(e -> atualizarTipoVenda());
        gbc2.gridx = 1;
        gbc2.weightx = 1.0;
        formPanel.add(comboTipo, gbc2);

        gbc2.gridx = 0; gbc2.gridy++; gbc2.weightx = 0;
        formPanel.add(new JLabel("Funcionário:"), gbc2);
        comboFuncionario = new JComboBox<>();
        comboFuncionario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboFuncionario.setEnabled(false);
        gbc2.gridx = 1; gbc2.weightx = 1.0;
        formPanel.add(comboFuncionario, gbc2);

        // Linha de Desconto (R$ e %)
        gbc2.gridx = 0; gbc2.gridy++; gbc2.weightx = 0;
        formPanel.add(new JLabel("Desconto:"), gbc2);
        JPanel pnlDesconto = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlDesconto.setBackground(Color.WHITE);

        pnlDesconto.add(new JLabel("R$"));
        txtDescontoReais = new JTextField("0,00", 8);
        txtDescontoReais.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlDesconto.add(txtDescontoReais);

        pnlDesconto.add(new JLabel("ou  %"));
        txtDescontoPorcento = new JTextField("0,00", 6);
        txtDescontoPorcento.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlDesconto.add(txtDescontoPorcento);

        JButton btnLimparDesc = new JButton("Zerar Desconto");
        btnLimparDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLimparDesc.addActionListener(e -> {
            isUpdatingDesconto = true;
            txtDescontoReais.setText("0,00");
            txtDescontoPorcento.setText("0,00");
            valorDesconto = 0f;
            isUpdatingDesconto = false;
            recalcularTotaisFinanceiros();
        });
        pnlDesconto.add(btnLimparDesc);

        gbc2.gridx = 1; gbc2.weightx = 1.0;
        formPanel.add(pnlDesconto, gbc2);

        // Card de Resumo de Valores (Subtotal | Desconto | Total a Pagar)
        gbc2.gridx = 0; gbc2.gridy++; gbc2.gridwidth = 2;
        JPanel cardResumo = new JPanel(new GridLayout(1, 3, 12, 0));
        cardResumo.setBackground(new Color(248, 250, 252));
        cardResumo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(10, 14, 10, 14)
        ));

        lblSubtotalPasso2 = new JLabel("Subtotal: R$ 0,00");
        lblSubtotalPasso2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSubtotalPasso2.setForeground(new Color(71, 85, 105));

        lblDescontoPasso2 = new JLabel("Desconto: - R$ 0,00");
        lblDescontoPasso2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDescontoPasso2.setForeground(new Color(220, 38, 38));

        lblTotalLiquidoPasso2 = new JLabel("A Pagar: R$ 0,00");
        lblTotalLiquidoPasso2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalLiquidoPasso2.setForeground(new Color(22, 163, 74));

        cardResumo.add(lblSubtotalPasso2);
        cardResumo.add(lblDescontoPasso2);
        cardResumo.add(lblTotalLiquidoPasso2);
        formPanel.add(cardResumo, gbc2);

        gbc2.gridwidth = 1;
        gbc2.gridx = 0; gbc2.gridy++; gbc2.weightx = 0;
        formPanel.add(new JLabel("Observação:"), gbc2);
        txtObservacao = new JTextArea(3, 25);
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);
        txtObservacao.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtObservacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollObs = new JScrollPane(txtObservacao);
        gbc2.gridx = 1; gbc2.weightx = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollObs, gbc2);

        passo2Panel.add(formPanel, BorderLayout.CENTER);

        // Rodapé Passo 2: Voltar, Total e Finalizar (Abre o DialogoPagamento / chamaJOP)
        JPanel rodape2 = new JPanel(new BorderLayout());
        rodape2.setBackground(Color.WHITE);

        JPanel navegacao2Back = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        navegacao2Back.setBackground(Color.WHITE);
        JButton btnVoltar = new JButton("< Voltar aos Itens");
        btnVoltar.addActionListener(e -> voltarParaPasso1());
        navegacao2Back.add(btnVoltar);
        rodape2.add(navegacao2Back, BorderLayout.WEST);

        JPanel navegacao2Next = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        navegacao2Next.setBackground(Color.WHITE);
        lblTotalVenda2 = new JLabel("Total: R$ 0,00");
        lblTotalVenda2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalVenda2.setForeground(new Color(22, 163, 74));
        navegacao2Next.add(lblTotalVenda2);
        
        btnFinalizar = new JButton("Informar Pagamento e Finalizar ($)");
        btnFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFinalizar.setBackground(new Color(13, 110, 253));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.addActionListener(e -> salvarVendaAvulsa());
        navegacao2Next.add(btnFinalizar);
        rodape2.add(navegacao2Next, BorderLayout.EAST);

        passo2Panel.add(rodape2, BorderLayout.SOUTH);
        cardsPanel.add(passo2Panel, "passo2");

        add(cardsPanel, BorderLayout.CENTER);
        
        configurarListenersDesconto();
        atualizarTipoVenda();
    }

    private void configurarListenersDesconto() {
        txtDescontoReais.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onReaisChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onReaisChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onReaisChanged(); }

            private void onReaisChanged() {
                if (isUpdatingDesconto) return;
                try {
                    String txt = txtDescontoReais.getText().trim().replace(",", ".");
                    float desc = txt.isEmpty() ? 0f : Float.parseFloat(txt);
                    if (desc < 0f) desc = 0f;
                    if (subtotalBruto > 0f && desc > subtotalBruto) desc = subtotalBruto;
                    valorDesconto = desc;

                    float perc = subtotalBruto > 0f ? (valorDesconto / subtotalBruto) * 100f : 0f;
                    isUpdatingDesconto = true;
                    txtDescontoPorcento.setText(String.format(Locale.US, "%.2f", perc));
                    isUpdatingDesconto = false;
                    recalcularTotaisFinanceiros();
                } catch (Exception ignored) {}
            }
        });

        txtDescontoPorcento.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onPorcentoChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onPorcentoChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onPorcentoChanged(); }

            private void onPorcentoChanged() {
                if (isUpdatingDesconto) return;
                try {
                    String txt = txtDescontoPorcento.getText().trim().replace("%", "").replace(",", ".");
                    float perc = txt.isEmpty() ? 0f : Float.parseFloat(txt);
                    if (perc < 0f) perc = 0f;
                    if (perc > 100f) perc = 100f;

                    valorDesconto = Math.round((perc / 100f) * subtotalBruto * 100f) / 100f;
                    isUpdatingDesconto = true;
                    txtDescontoReais.setText(String.format(Locale.US, "%.2f", valorDesconto));
                    isUpdatingDesconto = false;
                    recalcularTotaisFinanceiros();
                } catch (Exception ignored) {}
            }
        });
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
        subtotalBruto = 0f;
        for (int i = 0; i < modeloItens.getRowCount(); i++) {
            subtotalBruto += (Float) modeloItens.getValueAt(i, 4);
        }
        if (valorDesconto > subtotalBruto) {
            valorDesconto = subtotalBruto;
        }
        recalcularTotaisFinanceiros();
    }

    private void recalcularTotaisFinanceiros() {
        totalLiquido = Math.max(0f, Math.round((subtotalBruto - valorDesconto) * 100f) / 100f);
        lblTotalVenda1.setText(String.format("Subtotal: R$ %.2f", subtotalBruto));
        lblSubtotalPasso2.setText(String.format("Subtotal: R$ %.2f", subtotalBruto));
        lblDescontoPasso2.setText(String.format("Desconto: - R$ %.2f", valorDesconto));
        lblTotalLiquidoPasso2.setText(String.format("A Pagar: R$ %.2f", totalLiquido));
        lblTotalVenda2.setText(String.format("Total: R$ %.2f", totalLiquido));
    }

    private void avancarParaPasso2() {
        if (modeloItens.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um produto para continuar.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        atualizarTotalGeral();
        cardLayout.show(cardsPanel, "passo2");
    }

    private void voltarParaPasso1() {
        cardLayout.show(cardsPanel, "passo1");
    }

    private void atualizarTipoVenda() {
        int idx = comboTipo.getSelectedIndex();
        boolean isFuncionario = (idx == 1 || idx == 2);
        boolean isAdiantamento = (idx == 1);

        comboFuncionario.setEnabled(isFuncionario);
        comboFuncionario.removeAllItems();
        if (isFuncionario) {
            if (funcionarios.isEmpty()) {
                comboFuncionario.addItem("Nenhum funcionário cadastrado");
                comboFuncionario.setEnabled(false);
            } else {
                for (vfuncionario func : funcionarios) {
                    comboFuncionario.addItem(func.getNomefuncionario());
                }
            }
        }

        if (btnFinalizar != null) {
            if (isAdiantamento) {
                btnFinalizar.setText("Confirmar Adiantamento");
                btnFinalizar.setBackground(new Color(76, 175, 80));
            } else {
                btnFinalizar.setText("Informar Pagamento e Finalizar ($)");
                btnFinalizar.setBackground(new Color(13, 110, 253));
            }
        }
    }

    private void salvarVendaAvulsa() {
        if (modeloItens.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um produto na tabela.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idxTipo = comboTipo.getSelectedIndex();
        boolean isFuncionario = (idxTipo == 1 || idxTipo == 2);
        boolean isAdiantamento = (idxTipo == 1);

        String funcionarioNome = "";
        if (isFuncionario && comboFuncionario.getSelectedItem() != null) {
            funcionarioNome = comboFuncionario.getSelectedItem().toString();
            if ("Nenhum funcionário cadastrado".equals(funcionarioNome)) {
                JOptionPane.showMessageDialog(this, "Cadastre um funcionário antes de lançar venda para funcionário.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String tipoDb = isAdiantamento ? "adiantamento" : (isFuncionario ? "funcionario" : "cliente");

        // Mapa de pagamentos (formaPgtoDb -> valorPago)
        Map<String, Float> pagamentos = new LinkedHashMap<>();
        String resumoFormaPgtoImpressao;

        if (isAdiantamento) {
            pagamentos.put("adiantamento", totalLiquido);
            resumoFormaPgtoImpressao = "ADIANTAMENTO";
        } else if (totalLiquido <= 0.009f) {
            pagamentos.put("dinheiro", 0f);
            resumoFormaPgtoImpressao = "DESCONTO 100%";
        } else {
            // Abre o mesmo DialogoPagamento (chamaJOP) do EncerraQuarto para permitir 1, 2 ou mais formas de pagamento!
            DialogoPagamento dialogPgto = new DialogoPagamento(this, totalLiquido, 0f, null);
            dialogPgto.setVisible(true);
            if (!dialogPgto.isSucesso()) {
                return; // Usuário cancelou a tela de pagamento
            }

            float vDin = dialogPgto.getValD();
            float vPix = dialogPgto.getValP();
            float vCred = dialogPgto.getValCredito();
            float vDeb = dialogPgto.getValDebito();

            List<String> partesResumo = new ArrayList<>();
            if (vDin > 0.009f) {
                pagamentos.put("dinheiro", vDin);
                partesResumo.add(String.format("DINHEIRO (R$ %.2f)", vDin));
            }
            if (vPix > 0.009f) {
                pagamentos.put("pix", vPix);
                partesResumo.add(String.format("PIX (R$ %.2f)", vPix));
            }
            if (vCred > 0.009f) {
                pagamentos.put("credito", vCred);
                partesResumo.add(String.format("CREDITO (R$ %.2f)", vCred));
            }
            if (vDeb > 0.009f) {
                pagamentos.put("debito", vDeb);
                partesResumo.add(String.format("DEBITO (R$ %.2f)", vDeb));
            }

            if (pagamentos.isEmpty()) {
                pagamentos.put("dinheiro", totalLiquido);
                resumoFormaPgtoImpressao = "DINHEIRO";
            } else if (partesResumo.size() == 1) {
                resumoFormaPgtoImpressao = pagamentos.keySet().iterator().next().toUpperCase();
            } else {
                resumoFormaPgtoImpressao = String.join(" + ", partesResumo);
            }
        }

        // Prepara itens com o desconto rateado proporcionalmente (garantindo soma exata de totalLiquido)
        int numItens = modeloItens.getRowCount();
        int[] idsProd = new int[numItens];
        String[] descsProd = new String[numItens];
        int[] qtdsProd = new int[numItens];
        float[] valsUnit = new float[numItens];
        float[] valsTotalLiquido = new float[numItens];

        float acumuladoLiquido = 0f;
        List<Object[]> itensParaImpressao = new ArrayList<>();

        for (int i = 0; i < numItens; i++) {
            idsProd[i] = (Integer) modeloItens.getValueAt(i, 0);
            descsProd[i] = String.valueOf(modeloItens.getValueAt(i, 1));
            qtdsProd[i] = (Integer) modeloItens.getValueAt(i, 2);
            valsUnit[i] = (Float) modeloItens.getValueAt(i, 3);
            float subItem = (Float) modeloItens.getValueAt(i, 4);

            if (i == numItens - 1) {
                valsTotalLiquido[i] = Math.max(0f, Math.round((totalLiquido - acumuladoLiquido) * 100f) / 100f);
            } else if (subtotalBruto > 0f) {
                valsTotalLiquido[i] = Math.round((subItem / subtotalBruto) * totalLiquido * 100f) / 100f;
                acumuladoLiquido += valsTotalLiquido[i];
            } else {
                valsTotalLiquido[i] = 0f;
            }

            itensParaImpressao.add(new Object[] { descsProd[i], qtdsProd[i], valsUnit[i], subItem });
        }

        // Sufixo de descrição (Funcionário, Desconto, Observação)
        String sufixoBase = "";
        if (!funcionarioNome.isEmpty()) {
            sufixoBase += " [Funcionário: " + funcionarioNome + "]";
        }
        if (valorDesconto > 0.009f) {
            sufixoBase += String.format(" [Desc: -R$ %.2f]", valorDesconto);
        }
        String obs = txtObservacao.getText().trim();
        if (!obs.isEmpty()) {
            sufixoBase += " - " + obs;
        }

        boolean todasSalvas = true;

        if (pagamentos.size() == 1) {
            // Caso 1: Uma única forma de pagamento -> Salva 1 linha por item normalmente
            String unicaFormaPgto = pagamentos.keySet().iterator().next();
            for (int i = 0; i < numItens; i++) {
                String descricaoCompleta = descsProd[i] + sufixoBase;
                boolean ok = controller.salvarVendaAvulsa(
                    idsProd[i], descricaoCompleta, qtdsProd[i], valsUnit[i], valsTotalLiquido[i], tipoDb, unicaFormaPgto
                );
                if (!ok) todasSalvas = false;
            }
        } else {
            // Caso 2: Duas ou mais formas de pagamento -> Distribui os valores dos itens nos meios de pagamento
            // A 1ª fatia de cada item leva a quantidade real do produto; fatias adicionais do mesmo item levam qtd = 0
            // para que os relatórios de estoque/produtos somem a quantidade exata e os relatórios financeiros somem cada meio de pagamento exato!
            List<Map.Entry<String, Float>> filaPgto = new ArrayList<>(pagamentos.entrySet());
            int idxPgto = 0;
            float saldoPgtoAtual = filaPgto.get(0).getValue();

            for (int i = 0; i < numItens; i++) {
                float faltaAlocarItem = valsTotalLiquido[i];
                boolean primeiraFatiaDoItem = true;
                String descricaoCompleta = descsProd[i] + sufixoBase;

                while (faltaAlocarItem > 0.009f && idxPgto < filaPgto.size()) {
                    String formaAtual = filaPgto.get(idxPgto).getKey();
                    float valorAlocado = Math.min(faltaAlocarItem, saldoPgtoAtual);
                    valorAlocado = Math.round(valorAlocado * 100f) / 100f;

                    int qtdRegistrar = primeiraFatiaDoItem ? qtdsProd[i] : 0;
                    boolean ok = controller.salvarVendaAvulsa(
                        idsProd[i], descricaoCompleta, qtdRegistrar, valsUnit[i], valorAlocado, tipoDb, formaAtual
                    );
                    if (!ok) todasSalvas = false;

                    primeiraFatiaDoItem = false;
                    faltaAlocarItem = Math.round((faltaAlocarItem - valorAlocado) * 100f) / 100f;
                    saldoPgtoAtual = Math.round((saldoPgtoAtual - valorAlocado) * 100f) / 100f;

                    if (saldoPgtoAtual <= 0.009f) {
                        idxPgto++;
                        if (idxPgto < filaPgto.size()) {
                            saldoPgtoAtual = filaPgto.get(idxPgto).getValue();
                        }
                    }
                }
            }
        }

        if (todasSalvas) {
            JOptionPane.showMessageDialog(this, "Venda avulsa salva com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            // Imprimir extrato unificado de todos os itens com desconto e formas de pagamento
            try {
                com.motelinteligente.dados.ImpressoraService.imprimirMultiplasVendasAvulsas(
                    itensParaImpressao,
                    tipoDb,
                    resumoFormaPgtoImpressao,
                    funcionarioNome,
                    valorDesconto,
                    totalLiquido
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
