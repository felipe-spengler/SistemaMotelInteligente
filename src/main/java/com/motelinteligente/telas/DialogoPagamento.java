package com.motelinteligente.telas;

import com.motelinteligente.dados.Antecipado;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DialogoPagamento extends JDialog {

    private final float valorDivida;
    private final float valoreRecebidoOriginal;
    private float valoreRecebido;
    private final List<Antecipado> antecipados;

    // Valores acumulados de antecipados (não mudam)
    private float antecipadoDin = 0;
    private float antecipadoPix = 0;
    private float antecipadoCredito = 0;
    private float antecipadoDebito = 0;

    // Valores correntes (antecipado + digitado)
    private float recebidoDin = 0;
    private float recebidoPix = 0;
    private float recebidoCredito = 0;
    private float recebidoDebito = 0;

    // Retornos do diálogo
    private boolean sucesso = false;
    private float valD = 0;
    private float valC = 0;
    private float valP = 0;
    private float valCredito = 0;
    private float valDebito = 0;
    private float valorRecebidoAgora = 0;

    private String tipoPagamentoSelecionado = "";

    // Componentes de interface
    private final JTextField txtValorInput;
    private final JTextArea txtAreaRecebidos;
    private final JLabel lblTotalDivida;
    private final JLabel lblTotalAntecipado;
    private final JLabel lblRestante;

    private final JButton btnCredito;
    private final JButton btnDebito;
    private final JButton btnDinheiro;
    private final JButton btnPix;

    private final JButton btnAcertouDin;
    private final JButton btnAcertouDeb;
    private final JButton btnAcertouCred;
    private final JButton btnAcertouPix;

    private final JButton btnZerar;
    private final JButton btnSalvar;

    public DialogoPagamento(Window parent, float valorDivida, float valoreRecebido, List<Antecipado> antecipados) {
        super(parent, "Registrar Recebimento e Fechamento", ModalityType.APPLICATION_MODAL);
        this.valorDivida = valorDivida;
        this.valoreRecebidoOriginal = valoreRecebido;
        this.valoreRecebido = valoreRecebido;
        this.antecipados = antecipados;

        // Processa antecipados
        if (antecipados != null) {
            for (Antecipado a : antecipados) {
                String tipo = a.getTipo();
                float val = a.getValor();
                switch (tipo.toLowerCase()) {
                    case "credito":
                        antecipadoCredito += val;
                        break;
                    case "debito":
                        antecipadoDebito += val;
                        break;
                    case "dinheiro":
                        antecipadoDin += val;
                        break;
                    case "pix":
                        antecipadoPix += val;
                        break;
                }
            }
        }
        
        // Inicializa valores correntes com antecipados
        recebidoDin = antecipadoDin;
        recebidoPix = antecipadoPix;
        recebidoCredito = antecipadoCredito;
        recebidoDebito = antecipadoDebito;

        // Configurações do Dialogo
        setSize(800, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 246, 248));
        setLayout(new BorderLayout());

        // Painel Superior de Totais (Moderno com estilo Card)
        JPanel pnlHeader = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlHeader.setBackground(new Color(245, 246, 248));
        pnlHeader.setBorder(new EmptyBorder(15, 15, 15, 15));

        lblTotalDivida = criarCardTotal("TOTAL DA DÍVIDA", valorDivida, new Color(13, 110, 253));
        lblTotalAntecipado = criarCardTotal("JÁ PAGO (ANTECIPADO)", valoreRecebido, new Color(108, 117, 125));
        
        float restante = valorDivida - valoreRecebido;
        lblRestante = criarCardTotal("SALDO A PAGAR", restante < 0 ? 0 : restante, new Color(220, 53, 69));

        pnlHeader.add(lblTotalDivida);
        pnlHeader.add(lblTotalAntecipado);
        pnlHeader.add(lblRestante);
        add(pnlHeader, BorderLayout.NORTH);

        // Painel Central Principal
        JPanel pnlCenter = new JPanel(null);
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(220, 224, 230)));
        add(pnlCenter, BorderLayout.CENTER);

        // Seção esquerda: Seleção de Meio e Input de Valor
        JLabel lblMeio = new JLabel("1. Selecione o Tipo de Pagamento:");
        lblMeio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMeio.setForeground(Color.DARK_GRAY);
        lblMeio.setBounds(25, 15, 300, 25);
        pnlCenter.add(lblMeio);

        btnCredito = criarBotaoMetodo("Crédito (C)");
        btnDebito = criarBotaoMetodo("Débito (D)");
        btnDinheiro = criarBotaoMetodo("Dinheiro (O)");
        btnPix = criarBotaoMetodo("Pix (P)");

        btnCredito.setBounds(25, 45, 150, 45);
        btnDebito.setBounds(185, 45, 150, 45);
        btnDinheiro.setBounds(25, 100, 150, 45);
        btnPix.setBounds(185, 100, 150, 45);

        pnlCenter.add(btnCredito);
        pnlCenter.add(btnDebito);
        pnlCenter.add(btnDinheiro);
        pnlCenter.add(btnPix);

        JLabel lblValor = new JLabel("2. Digite o Valor Recebido (R$):");
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValor.setForeground(Color.DARK_GRAY);
        lblValor.setBounds(25, 165, 300, 25);
        pnlCenter.add(lblValor);

        txtValorInput = new JTextField();
        txtValorInput.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtValorInput.setHorizontalAlignment(JTextField.RIGHT);
        txtValorInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 204, 210), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtValorInput.setBounds(25, 195, 210, 45);
        pnlCenter.add(txtValorInput);

        JButton btnConfirmarValor = new JButton("Adicionar");
        btnConfirmarValor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirmarValor.setBackground(new Color(25, 135, 84));
        btnConfirmarValor.setForeground(Color.WHITE);
        btnConfirmarValor.setFocusPainted(false);
        btnConfirmarValor.setBounds(245, 195, 90, 45);
        pnlCenter.add(btnConfirmarValor);

        // Seção direita: Botões Rápidos ("Acertou no...")
        JLabel lblRapido = new JLabel("Ações Rápidas (Pagar Saldo Restante com):");
        lblRapido.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRapido.setForeground(Color.DARK_GRAY);
        lblRapido.setBounds(395, 15, 350, 25);
        pnlCenter.add(lblRapido);

        btnAcertouDin = criarBotaoRapido("Dinheiro (F6)", new Color(248, 249, 250));
        btnAcertouDeb = criarBotaoRapido("Débito (F7)", new Color(248, 249, 250));
        btnAcertouCred = criarBotaoRapido("Crédito (F8)", new Color(248, 249, 250));
        btnAcertouPix = criarBotaoRapido("Pix (F9)", new Color(248, 249, 250));

        btnAcertouDin.setBounds(395, 45, 170, 40);
        btnAcertouDeb.setBounds(580, 45, 170, 40);
        btnAcertouCred.setBounds(395, 95, 170, 40);
        btnAcertouPix.setBounds(580, 95, 170, 40);

        pnlCenter.add(btnAcertouDin);
        pnlCenter.add(btnAcertouDeb);
        pnlCenter.add(btnAcertouCred);
        pnlCenter.add(btnAcertouPix);

        // Seção Inferior: Histórico/Status do Recebimento
        JLabel lblStatus = new JLabel("Detalhamento dos Valores Lançados:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(Color.DARK_GRAY);
        lblStatus.setBounds(395, 155, 300, 25);
        pnlCenter.add(lblStatus);

        txtAreaRecebidos = new JTextArea();
        txtAreaRecebidos.setEditable(false);
        txtAreaRecebidos.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtAreaRecebidos.setBackground(new Color(250, 251, 252));
        txtAreaRecebidos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtAreaRecebidos);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1));
        scroll.setBounds(395, 185, 355, 150);
        pnlCenter.add(scroll);

        // Painel Inferior de Ação Geral (Salvar / Cancelar / Zerar)
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFooter.setBackground(new Color(245, 246, 248));
        
        btnZerar = new JButton("Zerar / Voltar ao Antecipado (Z)");
        btnZerar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnZerar.setBackground(new Color(220, 53, 69));
        btnZerar.setForeground(Color.WHITE);
        btnZerar.setFocusPainted(false);
        btnZerar.setPreferredSize(new Dimension(240, 40));

        btnSalvar = new JButton("Confirmar e Salvar Fechamento (S)");
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSalvar.setBackground(new Color(13, 110, 253));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setPreferredSize(new Dimension(280, 40));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancelar.setBackground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(100, 40));

        pnlFooter.add(btnZerar);
        pnlFooter.add(btnSalvar);
        pnlFooter.add(btnCancelar);
        add(pnlFooter, BorderLayout.SOUTH);

        // --- Lógica e Handlers ---
        
        // Listeners para Botões de Método
        ActionListener selectMethodListener = e -> {
            JButton btn = (JButton) e.getSource();
            String label = btn.getText();
            if (label.contains("(C)")) tipoPagamentoSelecionado = "C";
            else if (label.contains("(D)")) tipoPagamentoSelecionado = "D";
            else if (label.contains("(O)")) tipoPagamentoSelecionado = "O";
            else if (label.contains("(P)")) tipoPagamentoSelecionado = "P";

            resetarBotoesMetodo();
            btn.setBackground(new Color(108, 117, 125));
            btn.setForeground(Color.WHITE);
            txtValorInput.requestFocus();
        };

        btnCredito.addActionListener(selectMethodListener);
        btnDebito.addActionListener(selectMethodListener);
        btnDinheiro.addActionListener(selectMethodListener);
        btnPix.addActionListener(selectMethodListener);

        // Adicionar valor digitado
        ActionListener addValorAction = e -> {
            String txt = txtValorInput.getText().trim().replace(",", ".");
            if (tipoPagamentoSelecionado.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione um método de pagamento primeiro!", "Erro", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                float val = Float.parseFloat(txt);
                if (val <= 0) throw new NumberFormatException();

                switch (tipoPagamentoSelecionado) {
                    case "C": recebidoCredito += val; break;
                    case "D": recebidoDebito += val; break;
                    case "O": recebidoDin += val; break;
                    case "P": recebidoPix += val; break;
                }
                txtValorInput.setText("");
                resetarBotoesMetodo();
                tipoPagamentoSelecionado = "";
                atualizarAreaRecebidos();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Insira um valor numérico válido maior que 0.", "Erro", JOptionPane.WARNING_MESSAGE);
            }
        };

        btnConfirmarValor.addActionListener(addValorAction);
        txtValorInput.addActionListener(addValorAction);

        // Teclado apenas números e pontos
        txtValorInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != ',' && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });

        // Ações Rápidas (Acertou Restante) - Correção: Preserva Antecipados!
        btnAcertouDin.addActionListener(e -> acertarRestanteNoMetodo("dinheiro"));
        btnAcertouDeb.addActionListener(e -> acertarRestanteNoMetodo("debito"));
        btnAcertouCred.addActionListener(e -> acertarRestanteNoMetodo("credito"));
        btnAcertouPix.addActionListener(e -> acertarRestanteNoMetodo("pix"));

        // Zerar para o padrão (Mantendo antecipados originais)
        btnZerar.addActionListener(e -> {
            recebidoDin = antecipadoDin;
            recebidoPix = antecipadoPix;
            recebidoCredito = antecipadoCredito;
            recebidoDebito = antecipadoDebito;
            resetarBotoesMetodo();
            tipoPagamentoSelecionado = "";
            txtValorInput.setText("");
            atualizarAreaRecebidos();
        });

        // Salvar Fechamento
        btnSalvar.addActionListener(e -> {
            float totalRecebido = recebidoDin + recebidoPix + recebidoCredito + recebidoDebito;
            if (Math.abs(totalRecebido - valorDivida) < 0.01f) {
                valD = recebidoDin;
                valC = recebidoCredito + recebidoDebito;
                valP = recebidoPix;
                valCredito = recebidoCredito;
                valDebito = recebidoDebito;
                valorRecebidoAgora = totalRecebido - valoreRecebidoOriginal;
                
                sucesso = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    String.format("Valores não batem!\nTotal Divida: R$ %.2f\nTotal Lançado: R$ %.2f\n\nLance o valor exato restante para salvar.", valorDivida, totalRecebido),
                    "Divergência de Valores", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dispose());

        // Atalhos de teclado (C, D, O, P, S, Z, F6-F9)
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("C"), "credito");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "debito");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("O"), "dinheiro");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("P"), "pix");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "salvar");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Z"), "zerar");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F6"), "acertouDin");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F7"), "acertouDeb");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F8"), "acertouCred");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F9"), "acertouPix");

        root.getActionMap().put("credito", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnCredito.doClick(); } });
        root.getActionMap().put("debito", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnDebito.doClick(); } });
        root.getActionMap().put("dinheiro", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnDinheiro.doClick(); } });
        root.getActionMap().put("pix", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnPix.doClick(); } });
        root.getActionMap().put("salvar", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnSalvar.doClick(); } });
        root.getActionMap().put("zerar", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnZerar.doClick(); } });
        root.getActionMap().put("acertouDin", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnAcertouDin.doClick(); } });
        root.getActionMap().put("acertouDeb", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnAcertouDeb.doClick(); } });
        root.getActionMap().put("acertouCred", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnAcertouCred.doClick(); } });
        root.getActionMap().put("acertouPix", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { btnAcertouPix.doClick(); } });

        // Inicializa a exibição de recebidos
        atualizarAreaRecebidos();
    }

    private void acertarRestanteNoMetodo(String metodo) {
        // Restaura para os antecipados originais para recalcular do zero
        recebidoDin = antecipadoDin;
        recebidoPix = antecipadoPix;
        recebidoCredito = antecipadoCredito;
        recebidoDebito = antecipadoDebito;

        float restante = valorDivida - valoreRecebidoOriginal;
        if (restante < 0) restante = 0;

        switch (metodo) {
            case "dinheiro": recebidoDin += restante; break;
            case "pix": recebidoPix += restante; break;
            case "credito": recebidoCredito += restante; break;
            case "debito": recebidoDebito += restante; break;
        }

        resetarBotoesMetodo();
        tipoPagamentoSelecionado = "";
        atualizarAreaRecebidos();
    }

    private void atualizarAreaRecebidos() {
        txtAreaRecebidos.setText("");
        
        float totalGeral = recebidoCredito + recebidoDebito + recebidoDin + recebidoPix;
        float restante = valorDivida - totalGeral;
        if (restante < 0) restante = 0;
        
        lblRestante.setText(String.format("R$ %,.2f", restante));

        txtAreaRecebidos.append("--- VALORES DIGITADOS / PAGOS ---\n");
        if (recebidoDin > 0) {
            float dig = recebidoDin - antecipadoDin;
            txtAreaRecebidos.append(String.format("• Dinheiro: R$ %,.2f (Antecipado: R$ %,.2f | Pago Agora: R$ %,.2f)\n", recebidoDin, antecipadoDin, dig));
        }
        if (recebidoPix > 0) {
            float dig = recebidoPix - antecipadoPix;
            txtAreaRecebidos.append(String.format("• Pix:      R$ %,.2f (Antecipado: R$ %,.2f | Pago Agora: R$ %,.2f)\n", recebidoPix, antecipadoPix, dig));
        }
        if (recebidoCredito > 0) {
            float dig = recebidoCredito - antecipadoCredito;
            txtAreaRecebidos.append(String.format("• Crédito:  R$ %,.2f (Antecipado: R$ %,.2f | Pago Agora: R$ %,.2f)\n", recebidoCredito, antecipadoCredito, dig));
        }
        if (recebidoDebito > 0) {
            float dig = recebidoDebito - antecipadoDebito;
            txtAreaRecebidos.append(String.format("• Débito:   R$ %,.2f (Antecipado: R$ %,.2f | Pago Agora: R$ %,.2f)\n", recebidoDebito, antecipadoDebito, dig));
        }
        txtAreaRecebidos.append("\n---------------------------------\n");
        txtAreaRecebidos.append(String.format("TOTAL ACUMULADO: R$ %,.2f / R$ %,.2f", totalGeral, valorDivida));
    }

    private void resetarBotoesMetodo() {
        JButton[] btns = {btnCredito, btnDebito, btnDinheiro, btnPix};
        for (JButton b : btns) {
            b.setBackground(new Color(240, 242, 245));
            b.setForeground(Color.DARK_GRAY);
        }
    }

    private JLabel criarCardTotal(String titulo, float valor, Color corDestaque) {
        JLabel card = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(230, 233, 238));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(new EmptyBorder(10, 15, 10, 15));
        card.setPreferredSize(new Dimension(230, 80));

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(140, 145, 155));
        
        JLabel lblValue = new JLabel(String.format("R$ %,.2f", valor));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(corDestaque);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private JButton criarBotaoMetodo(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(240, 242, 245));
        btn.setForeground(Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(218, 222, 229), 1));
        return btn;
    }

    private JButton criarBotaoRapido(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(new Color(51, 65, 85));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 222, 229), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return btn;
    }

    // Getters públicos para expor os dados após fechamento do JDialog
    public boolean isSucesso() { return sucesso; }
    public float getValD() { return valD; }
    public float getValC() { return valC; }
    public float getValP() { return valP; }
    public float getValCredito() { return valCredito; }
    public float getValDebito() { return valDebito; }
    public float getValorRecebidoAgora() { return valorRecebidoAgora; }
}
