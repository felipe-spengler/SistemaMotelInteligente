package com.motelinteligente.telas.modernas;

import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.dados.Antecipado;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vendaProdutos;
import com.motelinteligente.telas.TelaPrincipal;
import com.motelinteligente.telas.controller.EncerraQuartoController;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncerraQuartoModerno extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(EncerraQuartoModerno.class);
    private final EncerraQuartoController controller;
    private final int numeroQuarto;
    private final TelaPrincipal principal;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    private ClienteEncerraModerno outraTela = new ClienteEncerraModerno();
    private JFrame secondaryFrameWifi;
    private boolean isWifiOpen = false;
    private boolean isUpdating = false; // Flag para evitar loop recursivo nos campos % e R$

    // UI Components
    private JLabel lblQuartoNum, lblStatus, lblTempo, lblTotalGrande;
    private JTable tabelaConsumo;
    private DefaultTableModel modelConsumo;
    private JTextField txtIdProd, txtQtdProd, txtDesconto, txtDescontoPorc, txtAcrescimo, txtAcrescimoPorc, txtRecebido, txtJustificativa, txtPessoas;
    private JLabel lblSubtotal, lblDescExibido, lblAcrExibido, lblAntecipadoExibido, lblSaldoPendente, lblTroco;
    private JButton btnSalvar, btnVoltar, btnDesistencia, btInserir, btnConferencia, btnDebito, btnWifi, btnApagar;

    private Color colorBg = new Color(15, 23, 42);
    private Color colorCard = new Color(30, 41, 59);
    private Color colorAccent = new Color(37, 99, 235);
    private Color colorText = new Color(241, 245, 249);
    private Color colorSuccess = new Color(34, 197, 94);
    private Color colorDanger = new Color(239, 68, 68);
    private Color colorWarning = new Color(234, 179, 8);

    public EncerraQuartoModerno(TelaPrincipal principal, int numeroQuarto) {
        this.principal = principal;
        this.numeroQuarto = numeroQuarto;
        this.controller = new EncerraQuartoController(numeroQuarto);

        setupFrame();
        initLayout();
        setupEvents();
        loadData();
        
        outraTela.setTitulo(numeroQuarto);
        outraTela.setaDatas(controller.getDataInicio(), controller.getDataFim(), controller.getTempoTotalLocado());
        
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Encerramento Moderno - Quarto " + numeroQuarto);
        setSize(1250, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(colorBg);
        setLayout(new BorderLayout(10, 10));
    }

    private void initLayout() {
        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(colorBg);
        header.setBorder(new EmptyBorder(20, 25, 10, 25));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setOpaque(false);
        
        lblQuartoNum = new JLabel(String.valueOf(numeroQuarto));
        lblQuartoNum.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblQuartoNum.setForeground(colorAccent);
        leftHeader.add(lblQuartoNum);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("ENCERRAMENTO DE QUARTO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(colorText);
        lblStatus = new JLabel("Status: Operacional");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStatus.setForeground(new Color(148, 163, 184));
        titlePanel.add(title);
        titlePanel.add(lblStatus);
        leftHeader.add(titlePanel);
        header.add(leftHeader, BorderLayout.WEST);

        JPanel rightHeader = new JPanel(new GridLayout(2, 1));
        rightHeader.setOpaque(false);
        lblTempo = new JLabel("Tempo: 00:00");
        lblTempo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTempo.setForeground(colorText);
        lblTempo.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotalGrande = new JLabel("R$ 0,00");
        lblTotalGrande.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTotalGrande.setForeground(colorSuccess);
        lblTotalGrande.setHorizontalAlignment(SwingConstants.RIGHT);
        rightHeader.add(lblTempo);
        rightHeader.add(lblTotalGrande);
        header.add(rightHeader, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(0, 25, 10, 25));

        // LEFT: Consumption Table
        mainPanel.add(createConsumptionPanel(), BorderLayout.CENTER);

        // RIGHT: Billing and Payment (2 Cards)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(450, 0));
        
        rightPanel.add(createBillingPanel());
        rightPanel.add(createPaymentPanel());
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);

        // --- FOOTER ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        footer.setBackground(colorBg);
        footer.setBorder(new EmptyBorder(0, 25, 10, 25));

        btnVoltar = createModernButton("VOLTAR (ESC)", new Color(71, 85, 105));
        btnConferencia = createModernButton("CONFERÊNCIA (F2)", colorWarning);
        btnDebito = createModernButton("DÉBITO (F4)", colorAccent);
        btnDesistencia = createModernButton("DESISTÊNCIA (F6)", colorDanger);
        btnWifi = createModernButton("WI-FI", new Color(124, 58, 237));
        btnSalvar = createModernButton("SALVAR (F9)", colorSuccess);

        footer.add(btnVoltar);
        footer.add(btnConferencia);
        footer.add(btnDebito);
        footer.add(btnWifi);
        footer.add(btnDesistencia);
        footer.add(btnSalvar);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createConsumptionPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setOpaque(false);

        JPanel inputPanel = createCard();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        
        txtIdProd = createModernField("Cód", 60);
        txtQtdProd = createModernField("Qtd", 50);
        txtQtdProd.setText("1");
        btInserir = createModernButton("INSERIR", colorAccent);
        btInserir.setPreferredSize(new Dimension(100, 35));
        
        inputPanel.add(new JLabel("PRODUTO:"));
        inputPanel.add(txtIdProd);
        inputPanel.add(new JLabel("QTD:"));
        inputPanel.add(txtQtdProd);
        inputPanel.add(btInserir);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());
        
        String[] cols = {"ID", "PRODUTO", "QTD", "VALOR UN.", "TOTAL"};
        modelConsumo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaConsumo = new JTable(modelConsumo);
        styleTable(tabelaConsumo);
        
        JScrollPane sp = new JScrollPane(tabelaConsumo);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        tableCard.add(sp, BorderLayout.CENTER);

        btnApagar = createModernButton("APAGAR ITEM SELECIONADO", colorDanger);
        btnApagar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tableCard.add(btnApagar, BorderLayout.SOUTH);

        p.add(inputPanel, BorderLayout.NORTH);
        p.add(tableCard, BorderLayout.CENTER);
        return p;
    }

    private JPanel createBillingPanel() {
        JPanel p = createCard();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        p.add(createSummaryRow("Início:", new JLabel(controller.getDataInicio())));
        
        JPanel rowPessoas = createSummaryRow("Pessoas no Quarto:", txtPessoas = createModernField("2", 40));
        p.add(rowPessoas);
        
        p.add(createSummaryRow("Subtotal Quarto:", lblSubtotal = new JLabel("R$ 0,00")));
        p.add(Box.createVerticalStrut(10));

        // Descontos e Acréscimos
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 5));
        grid.setOpaque(false);
        
        grid.add(new JLabel("DESCONTO (R$)"));
        grid.add(new JLabel("DESCONTO (%)"));
        txtDesconto = createModernField("0.00", 180);
        txtDescontoPorc = createModernField("0", 180);
        grid.add(txtDesconto);
        grid.add(txtDescontoPorc);
        
        p.add(grid);
        p.add(Box.createVerticalStrut(10));

        JPanel grid2 = new JPanel(new GridLayout(2, 2, 10, 5));
        grid2.setOpaque(false);
        grid2.add(new JLabel("ACRÉSCIMO (R$)"));
        grid2.add(new JLabel("ACRÉSCIMO (%)"));
        txtAcrescimo = createModernField("0.00", 180);
        txtAcrescimoPorc = createModernField("0", 180);
        grid2.add(txtAcrescimo);
        grid2.add(txtAcrescimoPorc);
        p.add(grid2);

        p.add(Box.createVerticalStrut(10));
        p.add(new JLabel("JUSTIFICATIVA:"));
        txtJustificativa = createModernField("Motivo", 400);
        p.add(txtJustificativa);

        return p;
    }

    private JPanel createPaymentPanel() {
        JPanel p = createCard();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        p.add(createSummaryRow("VALOR ANTECIPADO:", lblAntecipadoExibido = new JLabel("R$ 0,00")));
        p.add(createSummaryRow("SALDO PENDENTE:", lblSaldoPendente = new JLabel("R$ 0,00")));
        lblSaldoPendente.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSaldoPendente.setForeground(colorDanger);
        
        p.add(Box.createVerticalStrut(20));
        p.add(new JLabel("VALOR RECEBIDO (TROCO):"));
        txtRecebido = createModernField("0.00", 400);
        txtRecebido.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(txtRecebido);
        
        p.add(Box.createVerticalStrut(15));
        lblTroco = new JLabel("TROCO: R$ 0,00");
        lblTroco.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTroco.setForeground(colorSuccess);
        lblTroco.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTroco);

        return p;
    }

    private JPanel createSummaryRow(String label, Component valueComp) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        row.add(lbl, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.EAST);
        return row;
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(colorCard);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JTextField createModernField(String placeholder, int width) {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(width, 35));
        f.setPreferredSize(new Dimension(width, 35));
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    private JButton createModernButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 15, 10, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setBackground(colorCard);
        table.setForeground(colorText);
        table.setGridColor(new Color(51, 65, 85));
        table.setSelectionBackground(colorAccent);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
    }

    private void setupEvents() {
        btnVoltar.addActionListener(e -> dispose());
        
        ActionMap am = getRootPane().getActionMap();
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        am.put("ESC", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { dispose(); } });
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "F9");
        am.put("F9", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { salvar(); } });
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "F6");
        am.put("F6", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { desistir(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");
        am.put("F2", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { conferir(); } });

        // Listeners para atualizar totais e lógica de %
        txtDesconto.getDocument().addDocumentListener(new SimpleDL(() -> syncPercentage(txtDesconto, txtDescontoPorc, true)));
        txtDescontoPorc.getDocument().addDocumentListener(new SimpleDL(() -> syncPercentage(txtDesconto, txtDescontoPorc, false)));
        txtAcrescimo.getDocument().addDocumentListener(new SimpleDL(() -> syncPercentage(txtAcrescimo, txtAcrescimoPorc, true)));
        txtAcrescimoPorc.getDocument().addDocumentListener(new SimpleDL(() -> syncPercentage(txtAcrescimo, txtAcrescimoPorc, false)));
        txtRecebido.getDocument().addDocumentListener(new SimpleDL(this::atualizarTotais));
        txtPessoas.addActionListener(e -> recalcularPessoas());
        
        btInserir.addActionListener(e -> inserirProduto());
        btnApagar.addActionListener(e -> apagarItem());
        btnSalvar.addActionListener(e -> salvar());
        btnDesistencia.addActionListener(e -> desistir());
        btnWifi.addActionListener(e -> alternarWifi());
        btnConferencia.addActionListener(e -> conferir());
    }

    private void syncPercentage(JTextField valField, JTextField porcField, boolean valToPorc) {
        if (isUpdating) return;
        isUpdating = true;
        try {
            float subtotal = controller.getValorQuartoBase() + controller.getValorAdicionalPeriodo() + controller.getValorAdicionalPessoa();
            if (valToPorc) {
                float val = Float.parseFloat(valField.getText().isEmpty() ? "0" : valField.getText().replace(",", "."));
                float p = (val / subtotal) * 100;
                porcField.setText(String.format("%.1f", p).replace(",", "."));
            } else {
                float p = Float.parseFloat(porcField.getText().isEmpty() ? "0" : porcField.getText().replace(",", "."));
                float val = (p / 100) * subtotal;
                valField.setText(String.format("%.2f", val).replace(",", "."));
            }
        } catch (Exception e) {}
        isUpdating = false;
        atualizarTotais();
    }

    private void recalcularPessoas() {
        try {
            int q = Integer.parseInt(txtPessoas.getText());
            controller.calculaAdicionalPessoa(q);
            loadData(); // Recarrega labels
        } catch (Exception e) {}
    }

    private void loadData() {
        lblTempo.setText("Tempo: " + controller.getTempoTotalLocado());
        float base = controller.getValorQuartoBase() + controller.getValorAdicionalPeriodo() + controller.getValorAdicionalPessoa();
        lblSubtotal.setText(currencyFormat.format(base));
        lblAntecipadoExibido.setText(currencyFormat.format(controller.getValorRecebidoAntecipado()));
        atualizarTotais();
    }

    private void inserirProduto() {
        try {
            int id = Integer.parseInt(txtIdProd.getText());
            int qtd = Integer.parseInt(txtQtdProd.getText());
            fprodutos fprod = new fprodutos();
            float preco = fprod.getValorProduto(id);
            String nome = fprod.getDescicao(String.valueOf(id));
            if (nome != null && !nome.equals("<>")) {
                adicionarNaTabela(new vendaProdutos(id, qtd, preco, preco * qtd));
                txtIdProd.setText("");
                txtIdProd.requestFocus();
            }
        } catch (Exception e) {}
    }

    private void adicionarNaTabela(vendaProdutos vp) {
        modelConsumo.addRow(new Object[]{ vp.idProduto, new fprodutos().getDescicao(String.valueOf(vp.idProduto)), vp.quantidade, currencyFormat.format(vp.valorUnd), currencyFormat.format(vp.valorTotal) });
        atualizarTotais();
    }

    private void apagarItem() {
        int row = tabelaConsumo.getSelectedRow();
        if (row != -1) { modelConsumo.removeRow(row); atualizarTotais(); }
    }

    private void atualizarTotais() {
        float consumo = 0;
        for (int i = 0; i < modelConsumo.getRowCount(); i++) {
            String valStr = modelConsumo.getValueAt(i, 4).toString().replaceAll("[^0-9,]", "").replace(",", ".");
            consumo += Float.parseFloat(valStr);
        }
        controller.setValorConsumo(consumo);
        
        try {
            controller.setValorDesconto(Float.parseFloat(txtDesconto.getText().replace(",", ".").isEmpty() ? "0" : txtDesconto.getText()));
            controller.setValorAcrescimo(Float.parseFloat(txtAcrescimo.getText().replace(",", ".").isEmpty() ? "0" : txtAcrescimo.getText()));
        } catch (Exception e) {}

        float total = controller.calcularDividaTotal();
        float aReceber = controller.calcularValorAReceber();
        
        lblTotalGrande.setText(currencyFormat.format(total));
        lblSaldoPendente.setText(currencyFormat.format(aReceber));
        
        try {
            float recebido = Float.parseFloat(txtRecebido.getText().isEmpty() ? "0" : txtRecebido.getText().replace(",", "."));
            float troco = recebido - aReceber;
            lblTroco.setText("TROCO: " + currencyFormat.format(Math.max(0, troco)));
            if (troco < 0) lblTroco.setForeground(colorDanger); else lblTroco.setForeground(colorSuccess);
        } catch (Exception e) {}

        outraTela.setaDatas(controller.getDataInicio(), controller.getDataFim(), controller.getTempoTotalLocado());
        outraTela.setarValores(controller.getValorQuartoBase() + controller.getValorAdicionalPessoa(), controller.getValorAdicionalPeriodo());
        outraTela.setDesconto(controller.getValorDesconto());
        outraTela.setAcrescimo(controller.getValorAcrescimo());
        outraTela.setConsumo(consumo);
        outraTela.setValorTotal(aReceber);
    }

    private void conferir() { new ConectaArduino(999); } // Usando comando padrão de conferência

    private void desistir() {
        if (controller.podeFazerDesistencia()) {
            String m = JOptionPane.showInputDialog(this, "Motivo da Desistência:");
            if (m != null) { controller.registrarDesistencia(m); dispose(); }
        }
    }

    private void alternarWifi() {
        if (!isWifiOpen) {
            secondaryFrameWifi = new JFrame("Wi-Fi");
            secondaryFrameWifi.setSize(400, 400);
            secondaryFrameWifi.add(new JLabel(new ImageIcon(getClass().getResource("/imagens/conexaoWifi.jpg"))));
            GraphicsDevice[] sc = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (sc.length > 1) { Rectangle b = sc[1].getDefaultConfiguration().getBounds(); secondaryFrameWifi.setLocation(b.x, b.y); secondaryFrameWifi.setExtendedState(JFrame.MAXIMIZED_BOTH); }
            secondaryFrameWifi.setVisible(true); isWifiOpen = true;
        } else { if (secondaryFrameWifi != null) secondaryFrameWifi.dispose(); isWifiOpen = false; }
    }

    private void salvar() {
        float pendente = controller.calcularValorAReceber();
        String[] options = {"DINHEIRO", "PIX", "CARTÃO", "CANCELAR"};
        int x = JOptionPane.showOptionDialog(this, "Total: " + currencyFormat.format(pendente) + "\nSelecione o método:", "Pagamento", 0, 1, null, options, options[0]);
        if (x >= 3 || x == -1) return;
        float d=0, p=0, c=0;
        if (x == 0) d = pendente; if (x == 1) p = pendente; if (x == 2) c = pendente;

        List<vendaProdutos> pr = new ArrayList<>();
        for(int i=0; i<modelConsumo.getRowCount(); i++) {
            pr.add(new vendaProdutos((int)modelConsumo.getValueAt(i,0), Integer.parseInt(modelConsumo.getValueAt(i,2).toString()), Float.parseFloat(modelConsumo.getValueAt(i,3).toString().replaceAll("[^0-9,]", "").replace(",", ".")), Float.parseFloat(modelConsumo.getValueAt(i,4).toString().replaceAll("[^0-9,]", "").replace(",", "."))));
        }
        if (!txtJustificativa.getText().isEmpty()) controller.salvarJustificativa("Manual", controller.getValorDesconto() > 0 ? -controller.getValorDesconto() : controller.getValorAcrescimo(), txtJustificativa.getText());
        controller.salvarLocacao(pr, d, p, c, 0); configGlobal.getInstance().setMudanca(true); dispose();
    }

    @Override public void dispose() { if (secondaryFrameWifi != null) secondaryFrameWifi.dispose(); outraTela.dispose(); super.dispose(); }

    private class SimpleDL implements DocumentListener {
        private Runnable r;
        public SimpleDL(Runnable r) { this.r = r; }
        @Override public void insertUpdate(DocumentEvent e) { r.run(); }
        @Override public void removeUpdate(DocumentEvent e) { r.run(); }
        @Override public void changedUpdate(DocumentEvent e) { r.run(); }
    }
}
