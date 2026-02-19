package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fcaixa;
import com.motelinteligente.dados.valores;
import com.motelinteligente.dados.vendaProdutos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.List;

public class CaixaFrameModerno extends JFrame {

    // Componentes UI
    private JLabel lblStatusTitle;
    private JLabel lblUsuario;
    private JLabel lblDataAbre;
    private JTextField txtValorInicial;

    private JButton btnAbrir, btnFechar;

    // Cards de Resumo
    private JLabel lblEntradaTotal;
    private JLabel lblSaldoFinal;

    // Labels Detalhados
    private JLabel valDinheiro, valCartao, valPix, valLocacoes, valVendas, valDescontos, valAcrescimos;
    private JLabel lblAntecipadoOutro, lblAntecipadoEste;

    // Tabelas
    private JTable tblProdutos;
    private JTable tblLocacoes;
    private DefaultTableModel modelProdutos, modelLocacoes;

    // Labels de Totais das Tabelas
    private JLabel lblQtdProdutos, lblTotalProdutos;
    private JLabel lblQtdLocacoes, lblTotalQuartos, lblTotalConsumo, lblTotalLocacoes;

    private final DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    public CaixaFrameModerno() {
        initUI();
    }

    private void initUI() {
        setTitle("Controle de Caixa");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        EstiloModerno.aplicarEstiloFrame(this);

        // Listener para atualizar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                atualizarDados();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                atualizarDados();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                atualizarDados();
            }
        });

        // Layout Principal: Topo (Status/Ações), Meio (KPIs), Baixo (Tabelas)
        JPanel main = new JPanel(new MigLayout("fill, insets 25", "[grow]", "[]20[]20[grow]"));
        main.setOpaque(false);

        // === 1. TOPO: HEADER & CONTROLES ===
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[]push[]"));
        header.setOpaque(false);

        lblStatusTitle = EstiloModerno.criarTitulo("Caixa Fechado");
        header.add(lblStatusTitle);

        JPanel infoPanel = new JPanel(new MigLayout("insets 0"));
        infoPanel.setOpaque(false);
        lblUsuario = EstiloModerno.criarLabel("Usuário: -");
        lblDataAbre = EstiloModerno.criarLabel("Abertura: -");
        infoPanel.add(lblUsuario);
        infoPanel.add(new JSeparator(JSeparator.VERTICAL), "h 20!");
        infoPanel.add(lblDataAbre);

        header.add(infoPanel);
        main.add(header, "growx, wrap");

        // === 2. BARRA DE AÇÃO (ABRIR/FECHAR) ===
        JPanel actionCard = EstiloModerno.criarCard();
        actionCard.setLayout(new MigLayout("fillx, insets 15", "[][grow]push[]", "[]"));

        actionCard.add(EstiloModerno.criarLabel("Saldo Inicial / Troco:"));
        txtValorInicial = EstiloModerno.criarInput();
        txtValorInicial.putClientProperty("FlatLaf.style", "font: bold 16; foreground: #374151");
        actionCard.add(txtValorInicial, "w 150!");

        btnAbrir = EstiloModerno.criarBotaoPrincipal("ABRIR CAIXA", null);
        btnAbrir.setBackground(EstiloModerno.SUCCESS); // Verde para abrir
        btnAbrir.addActionListener(e -> abrirCaixa());

        btnFechar = EstiloModerno.criarBotaoPerigo("FECHAR CAIXA", null);
        btnFechar.addActionListener(e -> fecharCaixa());

        actionCard.add(btnAbrir);
        actionCard.add(btnFechar);

        main.add(actionCard, "growx, wrap");

        // === 3. DASHBOARD DE VALORES ===
        JPanel dashboard = new JPanel(new MigLayout("fill, insets 0", "[grow]20[grow]", "[]"));
        dashboard.setOpaque(false);

        // Coluna Esquerda: Detalhamento
        JPanel detailsCard = EstiloModerno.criarCard();
        detailsCard.setLayout(new MigLayout("fillx, wrap 2", "[grow][grow]", "[]10[]10[]"));
        detailsCard.setBorder(BorderFactory.createTitledBorder(" Detalhamento de Entradas "));

        // Grupo 1: Métodos de Pagamento (Separado visualmente com colunas distintas)
        // [Label] push [Value] garante que fiquem nos extremos
        JPanel payPanel = new JPanel(new MigLayout("fillx, insets 0", "[left]push[right]", "[]5[]5[]"));
        payPanel.setOpaque(false);

        // Passamos o painel com o layout correto para o addDetailRow
        valDinheiro = createValueLabel();
        valCartao = createValueLabel();
        valPix = createValueLabel();

        addDetailRow(payPanel, "Dinheiro", valDinheiro, Color.BLACK);
        addDetailRow(payPanel, "Cartão", valCartao, Color.BLACK);
        addDetailRow(payPanel, "Pix", valPix, Color.BLACK);

        detailsCard.add(payPanel, "span 2, growx, wrap, gapbottom 10");

        // Separator
        detailsCard.add(new JSeparator(), "span 2, growx, wrap, gapbottom 10");

        // Grupo 2: Origens
        // Usando um painel interno também para garantir alinhamento
        JPanel origPanel = new JPanel(new MigLayout("fillx, insets 0", "[left]push[right]", "[]5[]5[]5[]"));
        origPanel.setOpaque(false);

        valLocacoes = createValueLabel();
        valVendas = createValueLabel();
        valDescontos = createValueLabel();
        valAcrescimos = createValueLabel();

        addDetailRow(origPanel, "Locações", valLocacoes, new Color(37, 99, 235));
        addDetailRow(origPanel, "Vendas (Prod)", valVendas, new Color(37, 99, 235));
        addDetailRow(origPanel, "Descontos", valDescontos, Color.RED);
        addDetailRow(origPanel, "Acréscimos", valAcrescimos, new Color(22, 163, 74));

        detailsCard.add(origPanel, "span 2, growx");

        dashboard.add(detailsCard, "grow");

        // Coluna Direita: BIG NUMBERS e Antecipados
        JPanel rightCol = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]20[]"));
        rightCol.setOpaque(false);

        JPanel totalCard = EstiloModerno.criarCard();
        totalCard.setLayout(new MigLayout("fill, insets 20, wrap 1", "[center]", "[]5[]20[]5[]"));

        totalCard.add(new JLabel("Movimentação Total"));
        lblEntradaTotal = new JLabel("R$ 0,00");
        lblEntradaTotal.putClientProperty("FlatLaf.style", "font: bold +8; foreground: #374151");
        totalCard.add(lblEntradaTotal);

        totalCard.add(new JLabel("SALDO EM CAIXA (inicial + vendas"));
        lblSaldoFinal = new JLabel("R$ 0,00");
        lblSaldoFinal.putClientProperty("FlatLaf.style", "font: bold +14; foreground: #2563EB");
        totalCard.add(lblSaldoFinal);

        rightCol.add(totalCard, "growx, wrap");

        // Card Antecipados (Avisos)
        JPanel antecipadoCard = EstiloModerno.criarCard();
        antecipadoCard.setLayout(new MigLayout("fillx, insets 15, wrap 1", "[left]", "[]5[]"));
        antecipadoCard.setBorder(BorderFactory.createLineBorder(new Color(251, 191, 36), 1)); // Borda amarela
        antecipadoCard.setBackground(new Color(255, 251, 235)); // Amarelo bem claro

        lblAntecipadoOutro = EstiloModerno.criarLabel("Recebido Antecipado (Outro Caixa): R$ 0,00");
        lblAntecipadoEste = EstiloModerno.criarLabel("Neste Caixa (Antecipado): R$ 0,00");

        antecipadoCard.add(lblAntecipadoOutro);
        antecipadoCard.add(lblAntecipadoEste);

        rightCol.add(antecipadoCard, "growx");

        dashboard.add(rightCol, "grow, top");
        main.add(dashboard, "growx, wrap");

        // === 4. TABELAS (Abas Modernas) ===
        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty("FlatLaf.style", "tabType: card"); // Estilo aba FlatLaf

        tabs.addTab("Produtos Vendidos", createTableProdutos());
        tabs.addTab("Histórico de Locações", createTableLocacoes());

        main.add(tabs, "grow, push");

        add(main);

        // Window State - ensure it's visible and focused after building
        // setExtendedState(JFrame.NORMAL);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizar para aproveitar espaço
        // toFront();
        // requestFocus();
    }

    private JLabel createValueLabel() {
        JLabel val = new JLabel("R$ 0,00");
        return val;
    }

    private JPanel createTableProdutos() {
        JPanel p = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow][]"));
        p.setBackground(Color.WHITE);

        String[] cols = { "ID", "Item", "Qtd", "Valor Unit", "Total" };
        modelProdutos = new DefaultTableModel(cols, 0);
        tblProdutos = new JTable(modelProdutos);
        styleTable(tblProdutos);
        p.add(new JScrollPane(tblProdutos), "grow, wrap");

        // Painel de Totais
        JPanel totaisPanel = new JPanel(new MigLayout("fillx, insets 10", "[]20[]push[]", "[]"));
        totaisPanel.setBackground(new Color(249, 250, 251));
        totaisPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        lblQtdProdutos = new JLabel("Produtos Vendidos: 0");
        lblQtdProdutos.putClientProperty("FlatLaf.style", "font: bold 13");

        lblTotalProdutos = new JLabel("Total: R$ 0,00");
        lblTotalProdutos.putClientProperty("FlatLaf.style", "font: bold 14; foreground: #2563EB");

        totaisPanel.add(new JLabel("📊"));
        totaisPanel.add(lblQtdProdutos);
        totaisPanel.add(lblTotalProdutos);

        p.add(totaisPanel, "growx");
        return p;
    }

    private JPanel createTableLocacoes() {
        JPanel p = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow][][]")); // Mais uma linha para botão
        p.setBackground(Color.WHITE);

        String[] cols = { "Entrada", "Saída", "Quarto", "V. Quarto", "V. Consumo", "Desc", "Acres", "Total", "ID" };
        modelLocacoes = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Desabilita edição
            }
        };
        tblLocacoes = new JTable(modelLocacoes);
        styleTable(tblLocacoes);

        // Esconder coluna ID
        tblLocacoes.getColumnModel().getColumn(8).setMinWidth(0);
        tblLocacoes.getColumnModel().getColumn(8).setMaxWidth(0);
        tblLocacoes.getColumnModel().getColumn(8).setWidth(0);

        p.add(new JScrollPane(tblLocacoes), "grow, wrap");

        // Botão Ver Detalhes
        JButton btnDetalhes = new JButton("Ver Detalhes / Expandir");
        btnDetalhes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // btnDetalhes.setIcon(new
        // javax.swing.ImageIcon(getClass().getResource("/imagens/lupa.png"))); // Tente
        // achar um
        // icone ou remova
        // se nao tiver
        // Se nao tiver icone, remove o setIcon
        // btnDetalhes.setIcon(null);

        btnDetalhes.addActionListener(e -> {
            // Pegar lista atual do modelo
            java.util.List<Object[]> dados = new java.util.ArrayList<>();
            for (int i = 0; i < modelLocacoes.getRowCount(); i++) {
                Object[] row = new Object[modelLocacoes.getColumnCount()];
                for (int j = 0; j < modelLocacoes.getColumnCount(); j++) {
                    row[j] = modelLocacoes.getValueAt(i, j);
                }
                dados.add(row);
            }
            new DetalhesCaixaDialog(this, dados).setVisible(true);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnDetalhes);
        p.add(btnPanel, "growx, wrap");

        // Painel de Totais
        JPanel totaisPanel = new JPanel(new MigLayout("fillx, insets 10", "[]20[]20[]20[]push[]", "[]"));
        totaisPanel.setBackground(new Color(249, 250, 251));
        totaisPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        lblQtdLocacoes = new JLabel("Locações: 0");
        lblQtdLocacoes.putClientProperty("FlatLaf.style", "font: bold 13");

        lblTotalQuartos = new JLabel("Total Quartos: R$ 0,00");
        lblTotalQuartos.putClientProperty("FlatLaf.style", "font: bold 13");

        lblTotalConsumo = new JLabel("Total Consumo: R$ 0,00");
        lblTotalConsumo.putClientProperty("FlatLaf.style", "font: bold 13");

        lblTotalLocacoes = new JLabel("TOTAL: R$ 0,00");
        lblTotalLocacoes.putClientProperty("FlatLaf.style", "font: bold 14; foreground: #2563EB");

        totaisPanel.add(new JLabel("🏨"));
        totaisPanel.add(lblQtdLocacoes);
        totaisPanel.add(lblTotalQuartos);
        totaisPanel.add(lblTotalConsumo);
        totaisPanel.add(lblTotalLocacoes);

        p.add(totaisPanel, "growx");
        return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(240, 240, 240));
        t.getTableHeader().putClientProperty("FlatLaf.style",
                "font:bold; background:#F9FAFB; border: 0,0,1,0, #E5E7EB");

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        t.setDefaultRenderer(Object.class, centerRenderer);

        // Also center the headers if desired, but FlatLaf usually handles headers well.
        // For cells specifically:
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // --- LÓGICA (Backend) ---

    private void atualizarDados() {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();

        if (idCaixa == 0) {
            setupMode(false); // Modo Fechado
            limparDados();
        } else {
            setupMode(true); // Modo Aberto
            carregarInfo(idCaixa);
        }
    }

    private void setupMode(boolean aberto) {
        btnAbrir.setVisible(!aberto);
        btnFechar.setVisible(aberto);
        txtValorInicial.setEditable(!aberto);
        txtValorInicial.setFocusable(!aberto);

        lblStatusTitle.setText(aberto ? "Caixa Aberto" : "Caixa Fechado");
        lblStatusTitle.setForeground(aberto ? EstiloModerno.SUCCESS : EstiloModerno.DANGER);
    }

    private void limparDados() {
        lblUsuario.setText("Usuário: -");
        lblDataAbre.setText("Data: -");
        txtValorInicial.setText("0");
        // Zerar labels...
        valDinheiro.setText("R$ 0,00");
        valCartao.setText("R$ 0,00");
        valPix.setText("R$ 0,00");
        lblSaldoFinal.setText("R$ 0,00");
        if (lblAntecipadoOutro != null)
            lblAntecipadoOutro.setText("-");
        if (lblAntecipadoEste != null)
            lblAntecipadoEste.setText("-");
    }

    private void carregarInfo(int idCaixa) {
        fcaixa dao = new fcaixa();

        lblUsuario.setText("Usuário: " + dao.getUsuarioAbriu(idCaixa));
        lblDataAbre.setText("Abertura: " + dao.getDataAbriu(idCaixa));

        float saldoIni = dao.getValorAbriu(idCaixa);
        txtValorInicial.setText(String.valueOf(saldoIni));

        valores v = dao.getValores(idCaixa);
        valDinheiro.setText(df.format(v.entradaD));
        valCartao.setText(df.format(v.entradaC));
        valPix.setText(df.format(v.entradaP));
        valLocacoes.setText(df.format(v.entradaQuarto));
        valVendas.setText(df.format(v.entradaConsumo));

        List<Integer> ids = dao.getIdsLocacoes(idCaixa);
        float[] justif = dao.getTotaisJustificativas(ids);
        valDescontos.setText(df.format(justif[0]));
        valAcrescimos.setText(df.format(justif[1]));

        // Antecipados
        float antecipadoOutro = dao.getOutrosCaixas(idCaixa);
        float antecipadoEste = dao.getCaixaAtual(idCaixa);

        lblAntecipadoOutro.setText("Recebido Antecipado (Outro Caixa): " + df.format(antecipadoOutro));
        lblAntecipadoEste.setText("Neste Caixa (Antecipado): " + df.format(antecipadoEste));

        // Totais
        float entradaTotal = v.entradaConsumo + v.entradaQuarto + justif[1] - justif[0];

        // Saldo em Caixa = Apenas o que foi movimentado (sem considerar antecipados)
        // O ajuste dos antecipados será feito apenas no Resumo de Fechamento
        float saldoEmCaixa = entradaTotal + saldoIni;

        lblEntradaTotal.setText(df.format(entradaTotal));
        lblSaldoFinal.setText(df.format(saldoEmCaixa));

        carregarTabelas(idCaixa);
    }

    private void carregarTabelas(int idCaixa) {
        // Produtos
        vendaProdutos venda = new vendaProdutos();
        java.util.List<vendaProdutos> lista = venda.new gerenciaVenda().carregaLista();
        modelProdutos.setRowCount(0);

        int qtdProdutos = 0;
        float totalProdutos = 0f;

        for (vendaProdutos vp : lista) {
            String desc = new com.motelinteligente.dados.fprodutos().getDescicao(String.valueOf(vp.idProduto));
            float subtotal = vp.quantidade * vp.valorUnd;
            modelProdutos.addRow(new Object[] { vp.idProduto, desc, vp.quantidade, df.format(vp.valorUnd),
                    df.format(subtotal) });

            qtdProdutos += vp.quantidade;
            totalProdutos += subtotal;
        }

        // Atualizar Labels de Totais - Produtos
        if (lblQtdProdutos != null) {
            lblQtdProdutos.setText("Produtos Vendidos: " + qtdProdutos);
        }
        if (lblTotalProdutos != null) {
            lblTotalProdutos.setText("Total: " + df.format(totalProdutos));
        }

        // Locações
        fcaixa dao = new fcaixa();
        List<Object[]> locs = dao.getListaLocacoes(idCaixa);
        modelLocacoes.setRowCount(0);

        int qtdLocacoes = 0;
        float totalQuartos = 0f;
        float totalConsumo = 0f;

        for (Object[] row : locs) {
            modelLocacoes.addRow(row);
            qtdLocacoes++;

            // row[3] = V. Quarto, row[4] = V. Consumo (assumindo índices da definição)
            // Precisamos converter para float
            try {
                if (row.length > 4) {
                    totalQuartos += parseValue(row[3]);
                    totalConsumo += parseValue(row[4]);
                }
            } catch (Exception e) {
                // Ignorar erros de conversão
            }
        }

        // Atualizar Labels de Totais - Locações
        if (lblQtdLocacoes != null) {
            lblQtdLocacoes.setText("Locações: " + qtdLocacoes);
        }
        if (lblTotalQuartos != null) {
            lblTotalQuartos.setText("Total Quartos: " + df.format(totalQuartos));
        }
        if (lblTotalConsumo != null) {
            lblTotalConsumo.setText("Total Consumo: " + df.format(totalConsumo));
        }
        if (lblTotalLocacoes != null) {
            lblTotalLocacoes.setText("TOTAL: " + df.format(totalQuartos + totalConsumo));
        }
    }

    // Helper para converter valores das linhas da tabela
    private float parseValue(Object obj) {
        if (obj == null)
            return 0f;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).floatValue();
            }
            String str = obj.toString().replace("R$", "").replace(".", "").replace(",", ".").trim();
            return Float.parseFloat(str);
        } catch (Exception e) {
            return 0f;
        }
    }

    private void abrirCaixa() {
        try {
            float valor = Float.parseFloat(txtValorInicial.getText().replace(",", "."));
            if (new fcaixa().abrirCaixa(valor)) {
                configGlobal.getInstance().setCaixa(new fcaixa().getIdCaixa());
                atualizarDados();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Valor inválido");
        }
    }

    private void fecharCaixa() {
        // Carregar dados frescos para garantia
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        fcaixa dao = new fcaixa();
        valores v = dao.getValores(idCaixa);
        List<Integer> ids = dao.getIdsLocacoes(idCaixa);
        float[] justif = dao.getTotaisJustificativas(ids);

        float antecipadoOutro = dao.getOutrosCaixas(idCaixa);
        valores antecipadoDetalhado = dao.getAntecipadoDetalhado(idCaixa);
        float saldoIni = dao.getValorAbriu(idCaixa);
        float totalJustificativas = justif[1] - justif[0];

        // Abrir Modal de Conferência
        ResumoFechamentoDialog dialog = new ResumoFechamentoDialog(
                this, v, saldoIni, antecipadoOutro, antecipadoDetalhado, totalJustificativas);
        dialog.setVisible(true);

        if (dialog.isConfirmado()) {
            // Lógica final de fechamento
            // O valor final salvo no banco geralmente é o saldo final DO SISTEMA ou o que o
            // user conferiu?
            // O legado pegava o label. Vamos recalcular o total sistema, pois é isso que o
            // banco espera (snapshot do sistema).
            // Ou se o sistema espera o dinheiro em caixa...
            // "saldofecha" na tabela caixa. Geralmente é o valor físico final.

            float entradaNet = v.entradaConsumo + v.entradaQuarto + totalJustificativas;
            // O usuário confirmou que deseja salvar o TOTAL LANÇADO (Movimentação),
            // e o Saldo Final (Físico) é apenas para conferência visual.

            if (new fcaixa().fecharCaixa(entradaNet)) {
                JOptionPane.showMessageDialog(this,
                        "Caixa fechado com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                configGlobal.getInstance().setCaixa(0);
                configGlobal.getInstance().setFlagFechar(true); // Logoff
                dispose();
            }
        }
    }

    // Método auxiliar modificado para aceitar o label já criado
    private void addDetailRow(JPanel p, String text, JLabel lblValue, Color color) {
        p.add(new JLabel(text));
        lblValue.setForeground(color);
        lblValue.putClientProperty("FlatLaf.style", "font: bold 14");
        p.add(lblValue, "right, wrap"); // wrap garante que pule para a próxima linha
    }
}
