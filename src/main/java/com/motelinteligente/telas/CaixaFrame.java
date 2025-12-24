package com.motelinteligente.telas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fcaixa;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.valores;
import com.motelinteligente.dados.vendaProdutos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.List;

/**
 * CaixaFrame Refatorado
 * Interface limpa usando MigLayout e lógica backend delegada para fcaixa.
 */
public class CaixaFrame extends JFrame {

    // Componentes de Controle
    private JTextField txtIdCaixa;
    private JTextField txtUsuarioAbre;
    private JTextField txtDataAbre;

    // Componentes Financeiros
    private JTextField txtTroco; // Saldo Inicial Editável (se for abrir) ou Apenas Leitura
    private JTextField txtSaldoInicial;
    private JTextField txtLocacoes;
    private JTextField txtVendas;
    private JTextField txtDinheiro;
    private JTextField txtCartao;
    private JTextField txtPix;
    private JTextField txtDescontos;
    private JTextField txtAcrescimos;
    private JTextField txtEntradaTotal;
    private JTextField txtCaixaTotal;

    // Labels informativos
    private JLabel lblAntecipadoAgora;
    private JLabel lblAntecipadoCaixa;

    // Tabelas e Labels das Abas
    private JTable tblProdutos;
    private DefaultTableModel modelProdutos;
    private JLabel lblTotalProdVendidos;
    private JLabel lblQtdProdVendidos;

    private JTable tblLocacoes;
    private DefaultTableModel modelLocacoes;
    private JLabel lblTotalQuartos;
    private JLabel lblTotalConsumo;
    private JLabel lblContadorLocacoes;

    // Botões
    private JButton btAbrir;
    private JButton btFechar;
    private JButton btVoltar;

    // Formatadores
    private final DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    public CaixaFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Gerenciamento de Caixa");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        // Listener para focar e atualizar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                atualizarTela();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                atualizarTela();
            }
        });

        // Layout Principal
        setLayout(new MigLayout("fill, insets 15 20 15 20", "[grow]", "[][][grow]"));

        // ==========================================
        // TOPO: Botoes e Cabeçalho
        // ==========================================
        JPanel topPanel = new JPanel(new MigLayout("insets 0", "[]push[]", "[]"));
        btVoltar = new JButton("Voltar");
        btVoltar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_sair.png"))); // ajustei nome imag
        btVoltar.addActionListener(e -> dispose());
        topPanel.add(btVoltar);
        add(topPanel, "growx, wrap");

        // Painel de Dados do Caixa (ID, User, Data)
        JPanel headerInfo = new JPanel(new MigLayout("fillx, insets 10", "[][grow][][grow][][grow]", "[]"));
        headerInfo.setBorder(BorderFactory.createTitledBorder("Informações do Caixa"));

        headerInfo.add(new JLabel("ID Caixa:"));
        txtIdCaixa = createReadOnlyField();
        headerInfo.add(txtIdCaixa, "width 80!");

        headerInfo.add(new JLabel("Usuário:"));
        txtUsuarioAbre = createReadOnlyField();
        headerInfo.add(txtUsuarioAbre, "growx");

        headerInfo.add(new JLabel("Abertura:"));
        txtDataAbre = createReadOnlyField();
        headerInfo.add(txtDataAbre, "growx");

        add(headerInfo, "growx, wrap");

        // ==========================================
        // ÁREA DE AÇÃO (Abrir/Fechar)
        // ==========================================
        JPanel actionPanel = new JPanel(new MigLayout("insets 10, fillx", "[][120!]push[][]", "[]"));
        actionPanel.add(new JLabel("Valor Troco / Início (R$):"));
        txtTroco = new JTextField("0");
        txtTroco.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        actionPanel.add(txtTroco, "growx");

        btAbrir = new JButton("Abrir Caixa");
        btAbrir.addActionListener(e -> abrirCaixa());
        btAbrir.putClientProperty(FlatClientProperties.STYLE, "font:bold; background:#4b6eaf; foreground:white");

        btFechar = new JButton("Fechar Caixa");
        btFechar.addActionListener(e -> fecharCaixa());
        btFechar.putClientProperty(FlatClientProperties.STYLE, "font:bold; background:#e53935; foreground:white");

        actionPanel.add(btAbrir);
        actionPanel.add(btFechar);
        add(actionPanel, "growx, wrap");

        // ==========================================
        // ABAS (Info, Produtos, Locações)
        // ==========================================
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Resumo Financeiro", createTabFinanceiro());
        tabs.addTab("Produtos Vendidos", createTabProdutos());
        tabs.addTab("Locações Realizadas", createTabLocacoes());
        add(tabs, "grow, push");
    }

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField();
        tf.setEditable(false);
        tf.setFocusable(false);
        tf.setBackground(new Color(240, 240, 240));
        return tf;
    }

    private JPanel createTabFinanceiro() {
        JPanel p = new JPanel(
                new MigLayout("fillx, insets 20", "[][grow][gap 40][][grow]", "[]10[]10[]10[]10[]10[]20[]10[]"));

        // Coluna 1: Totais Gerais
        p.add(new JLabel("Saldo Inicial:"));
        txtSaldoInicial = createReadOnlyField();
        p.add(txtSaldoInicial, "growx");

        p.add(new JLabel("Dinheiro:"));
        txtDinheiro = createReadOnlyField();
        p.add(txtDinheiro, "growx, wrap");

        p.add(new JLabel("Tot. Locações:"));
        txtLocacoes = createReadOnlyField();
        p.add(txtLocacoes, "growx");

        p.add(new JLabel("Cartão:"));
        txtCartao = createReadOnlyField();
        p.add(txtCartao, "growx, wrap");

        p.add(new JLabel("Tot. Vendas:"));
        txtVendas = createReadOnlyField();
        p.add(txtVendas, "growx");

        p.add(new JLabel("Pix:"));
        txtPix = createReadOnlyField();
        p.add(txtPix, "growx, wrap");

        p.add(new JLabel("Descontos (-):"));
        txtDescontos = createReadOnlyField();
        txtDescontos.setForeground(Color.RED);
        p.add(txtDescontos, "growx");

        p.add(new JLabel("Acréscimos (+):"));
        txtAcrescimos = createReadOnlyField();
        txtAcrescimos.setForeground(new Color(0, 150, 0));
        p.add(txtAcrescimos, "growx, wrap");

        p.add(new JSeparator(), "span, growx, wrap, gaptop 10");

        // Totais Finais
        JLabel l1 = new JLabel("Entrada Total:");
        l1.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        p.add(l1);

        txtEntradaTotal = createReadOnlyField();
        txtEntradaTotal.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        p.add(txtEntradaTotal, "growx");

        JLabel l2 = new JLabel("TOTAL NO CAIXA:");
        l2.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        p.add(l2, "skip 1"); // Pula gap e label col 2

        txtCaixaTotal = createReadOnlyField();
        txtCaixaTotal.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#0000AA");
        p.add(txtCaixaTotal, "growx, wrap");

        lblAntecipadoAgora = new JLabel();
        lblAntecipadoAgora.setForeground(new Color(200, 100, 0));
        lblAntecipadoAgora.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        p.add(lblAntecipadoAgora, "span, center, wrap, gaptop 10");

        lblAntecipadoCaixa = new JLabel();
        lblAntecipadoCaixa.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        p.add(lblAntecipadoCaixa, "span, center");

        return p;
    }

    private JPanel createTabProdutos() {
        JPanel p = new JPanel(new MigLayout("fill", "[grow]", "[grow][]"));
        modelProdutos = new DefaultTableModel(new String[] { "ID", "Descrição", "Qtd", "Valor Unit", "Total" }, 0);
        tblProdutos = new JTable(modelProdutos);
        p.add(new JScrollPane(tblProdutos), "grow, wrap");

        JPanel footer = new JPanel(new MigLayout("insets 10"));
        lblQtdProdVendidos = new JLabel("Qtd: 0");
        lblTotalProdVendidos = new JLabel("Total R$: 0,00");
        lblTotalProdVendidos.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");

        footer.add(new JLabel("Resumo:"));
        footer.add(lblQtdProdVendidos, "gapleft 20");
        footer.add(lblTotalProdVendidos, "gapleft 40");
        p.add(footer, "growx");
        return p;
    }

    private JPanel createTabLocacoes() {
        JPanel p = new JPanel(new MigLayout("fill", "[grow]", "[grow][]"));
        modelLocacoes = new DefaultTableModel(new String[] {
                "Entrada", "Saída", "Quarto", "V. Quarto", "V. Consumo", "Desc.", "Acres.", "TOTAL"
        }, 0);
        tblLocacoes = new JTable(modelLocacoes);
        p.add(new JScrollPane(tblLocacoes), "grow, wrap");

        JPanel footer = new JPanel(new MigLayout("insets 10"));
        lblContadorLocacoes = new JLabel("0 locações");
        lblTotalQuartos = new JLabel("Quartos R$: 0,00");
        lblTotalConsumo = new JLabel("Consumo R$: 0,00");

        footer.add(lblContadorLocacoes);
        footer.add(lblTotalQuartos, "gapleft 30");
        footer.add(lblTotalConsumo, "gapleft 30");
        p.add(footer, "growx");
        return p;
    }

    // ==========================================
    // LÓGICA DE NEGÓCIO
    // ==========================================

    private void abrirCaixa() {
        try {
            float valor = Float.parseFloat(txtTroco.getText().replace("R$", "").replace(",", ".").trim());
            fcaixa dao = new fcaixa();
            if (dao.abrirCaixa(valor)) {
                int id = dao.getIdCaixa();
                configGlobal.getInstance().setCaixa(id);
                atualizarTela();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido para abertura!");
        }
    }

    private void fecharCaixa() {
        try {
            // Tenta pegar do campo de entrada total calculado
            String txt = txtEntradaTotal.getText().replace("R$", "").replace(".", "").replace(",", ".").trim();
            // O replace de ponto milhar deve ser cuidado. Se usar DecimalFormat, ideal é
            // parsear.
            // Simplificando: vamos pegar o valor bruto calculado na tela (que veio do
            // banco)
            // Melhor: recalcular agora para garantir
            // Mas como solicitado, vou seguir logica original que pega texto.
            // Vamos melhorar: a logica original pega do texto.
            float valorFinal = Float.parseFloat(txt);

            fcaixa dao = new fcaixa();
            if (dao.fecharCaixa(valorFinal)) {
                configGlobal config = configGlobal.getInstance();
                config.setCaixa(0);
                JOptionPane.showMessageDialog(this, "Caixa Fechado com Sucesso");

                if (config.getLogoffecharcaixa()) {
                    config.setFlagFechar(true);
                    dispose();
                } else {
                    dispose();
                    // Reabrir nova instancia limpa
                    new CaixaFrame().setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao processar valor de fechamento.");
        }
    }

    private void atualizarTela() {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();

        if (idCaixa == 0) {
            // Nenhum caixa aberto
            btAbrir.setEnabled(true);
            btFechar.setEnabled(false);
            txtTroco.setEditable(true);
            txtTroco.setFocusable(true);
            limparTela();
        } else {
            btAbrir.setEnabled(false);
            btFechar.setEnabled(true);
            txtTroco.setEditable(false);
            txtTroco.setFocusable(false);
            carregaInfo(idCaixa);
        }
    }

    private void limparTela() {
        txtIdCaixa.setText("");
        txtUsuarioAbre.setText("");
        txtDataAbre.setText("");
        txtSaldoInicial.setText("");
        // Limpar tabelas...
    }

    private void carregaInfo(int idCaixa) {
        fcaixa dao = new fcaixa();

        txtIdCaixa.setText(String.valueOf(idCaixa));
        txtUsuarioAbre.setText(dao.getUsuarioAbriu(idCaixa));
        txtDataAbre.setText(String.valueOf(dao.getDataAbriu(idCaixa)));

        float saldoIni = dao.getValorAbriu(idCaixa);
        txtSaldoInicial.setText(df.format(saldoIni));
        txtTroco.setText(String.valueOf(saldoIni));

        // Valores
        valores v = dao.getValores(idCaixa);
        txtDinheiro.setText(df.format(v.entradaD));
        txtCartao.setText(df.format(v.entradaC));
        txtPix.setText(df.format(v.entradaP));
        txtLocacoes.setText(df.format(v.entradaQuarto));
        txtVendas.setText(df.format(v.entradaConsumo));

        // Descontos e Acrescimos
        List<Integer> ids = dao.getIdsLocacoes(idCaixa);
        float[] justif = dao.getTotaisJustificativas(ids);
        float desconto = justif[0];
        float acrescimo = justif[1];

        txtDescontos.setText(df.format(desconto));
        txtAcrescimos.setText(df.format(acrescimo));

        // Totais
        float entradaTotal = v.entradaConsumo + v.entradaQuarto + acrescimo - desconto;
        float caixaTotal = entradaTotal + saldoIni;

        txtEntradaTotal.setText(df.format(entradaTotal));
        txtCaixaTotal.setText(df.format(caixaTotal));

        // Antecipados
        float antAgora = dao.getAntecipadoAtual(idCaixa);
        lblAntecipadoAgora.setText(antAgora > 0 ? "Tem " + df.format(antAgora) + " em antecipado neste caixa." : "");

        float antOutros = dao.getAntecipadoOutros(idCaixa);
        lblAntecipadoCaixa.setText(antOutros > 0 ? "Recebeu " + df.format(antOutros) + " em caixas passados!" : "");

        // Tabelas
        carregaTabelaProdutos();
        carregaTabelaLocacoes(idCaixa);
    }

    private void carregaTabelaProdutos() {
        vendaProdutos venda = new vendaProdutos();
        vendaProdutos.gerenciaVenda gerencia = venda.new gerenciaVenda();
        java.util.List<vendaProdutos> lista = gerencia.carregaLista();

        modelProdutos.setRowCount(0);
        int qtd = 0;
        float total = 0;

        for (vendaProdutos vp : lista) {
            String desc = new fprodutos().getDescicao(String.valueOf(vp.idProduto));
            float t = vp.quantidade * vp.valorUnd;
            modelProdutos
                    .addRow(new Object[] { vp.idProduto, desc, vp.quantidade, df.format(vp.valorUnd), df.format(t) });
            qtd += vp.quantidade;
            total += t;
        }

        lblQtdProdVendidos.setText("Qtd: " + qtd);
        lblTotalProdVendidos.setText("Total: " + df.format(total));
    }

    private void carregaTabelaLocacoes(int idCaixa) {
        fcaixa dao = new fcaixa();
        List<Object[]> lista = dao.getListaLocacoes(idCaixa);

        modelLocacoes.setRowCount(0);
        float tQuarto = 0;
        float tConsumo = 0;

        for (Object[] row : lista) {
            // [ini, fim, num, vQ, vC, desc, acres, tot]
            modelLocacoes.addRow(row);
            tQuarto += (float) row[3];
            tConsumo += (float) row[4];
        }

        lblContadorLocacoes.setText(lista.size() + " locações");
        lblTotalQuartos.setText("Quartos: " + df.format(tQuarto));
        lblTotalConsumo.setText("Consumo: " + df.format(tConsumo));
    }

    public static void main(String args[]) {
        try {
            FlatIntelliJLaf.setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(() -> {
            new CaixaFrame().setVisible(true);
        });
    }
}
