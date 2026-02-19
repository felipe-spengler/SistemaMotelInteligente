package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import com.motelinteligente.dados.configGlobal;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClienteEncerraModerno extends JFrame {

    // Componentes UI
    private JLabel lblTitulo;

    // Info Labels
    private JLabel lblInicioLocacao;
    private JLabel lblFimLocacao;
    private JLabel lblTempoLocado;
    private JLabel lblPessoasQuarto;

    // Value Labels
    private JLabel lblValorQuarto;
    private JLabel lblHoraAdicional;
    private JLabel lblValorDesconto;
    private JLabel lblValorAcrescimo;
    private JLabel lblValorConsumo;
    private JLabel lblValorConsumoTotal; // Redundante mas mantido pro setConsumo
    private JLabel lblValorTotal;

    // Tabela
    private JTable tabela;
    private DefaultTableModel tableModel;

    public ClienteEncerraModerno() {
        initUI();
        configuraTela();
    }

    private void initUI() {
        setTitle("Encerramento de Conta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Será fechado pelo EncerraQuarto
        EstiloModerno.aplicarEstiloFrame(this);

        // Layout Principal: 2 Colunas (Info Esquerda | Produtos Direita)
        JPanel main = new JPanel(new MigLayout("fill, insets 30", "[40%]30[60%]", "[top]"));
        main.setOpaque(false);

        // === COLUNA ESQUERDA: INFO & TOTAIS ===
        JPanel infoPanel = EstiloModerno.criarCard();
        infoPanel.setLayout(new MigLayout("fillx, wrap 1, insets 25", "[grow]", "[]20[]20[]push[]"));

        // 1. Cabeçalho
        lblTitulo = EstiloModerno.criarTitulo("Quarto -");
        lblTitulo.putClientProperty("FlatLaf.style", "font: bold 28");
        infoPanel.add(lblTitulo, "center");
        infoPanel.add(new JSeparator(), "growx");

        // 2. Datas e Tempos
        JPanel datePanel = new JPanel(new MigLayout("fillx, insets 0", "[][grow]", "[]10[]"));
        datePanel.setOpaque(false);

        lblInicioLocacao = valorLabel("---");
        lblFimLocacao = valorLabel("---");
        lblTempoLocado = valorLabel("---");
        lblPessoasQuarto = valorLabel("2");

        addInfoRow(datePanel, "Início:", lblInicioLocacao);
        addInfoRow(datePanel, "Fim:", lblFimLocacao);
        addInfoRow(datePanel, "Tempo Total:", lblTempoLocado);
        addInfoRow(datePanel, "Pessoas:", lblPessoasQuarto);

        infoPanel.add(datePanel, "growx");

        // 3. Valores Detalhado (Card interno para destaque)
        JPanel valuesPanel = new JPanel(new MigLayout("fillx, insets 15", "[][grow, right]", "[]10[]10[]"));
        valuesPanel.setBackground(new Color(243, 244, 246)); // Cinza claro
        valuesPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        lblValorQuarto = moneyLabel("R$ 0,00");
        lblHoraAdicional = moneyLabel("R$ 0,00");
        lblValorDesconto = moneyLabel("R$ 0,00");
        lblValorAcrescimo = moneyLabel("R$ 0,00");
        lblValorConsumo = moneyLabel("R$ 0,00"); // Esse é usado no detalhamento

        lblValorDesconto.setForeground(new Color(22, 163, 74)); // Verde
        lblValorAcrescimo.setForeground(EstiloModerno.DANGER); // Vermelho

        addValRow(valuesPanel, "Valor Quarto:", lblValorQuarto);
        addValRow(valuesPanel, "Hora Adicional:", lblHoraAdicional);
        addValRow(valuesPanel, "Desconto:", lblValorDesconto);
        addValRow(valuesPanel, "Acréscimo:", lblValorAcrescimo);
        addValRow(valuesPanel, "Consumo:", lblValorConsumo);

        infoPanel.add(valuesPanel, "growx");

        // 4. TOTALÃO
        JPanel totalPanel = new JPanel(new MigLayout("fillx, insets 20", "[center]", "[]5[]"));
        totalPanel.setBackground(new Color(220, 252, 231)); // Fundo verde beeem claro
        totalPanel.setBorder(BorderFactory.createLineBorder(new Color(22, 163, 74), 2)); // Borda verde
        totalPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        JLabel lblTotalTitle = new JLabel("TOTAL A PAGAR");
        lblTotalTitle.setForeground(new Color(21, 128, 61));
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        lblValorTotal = new JLabel("R$ 0,00");
        lblValorTotal.setForeground(new Color(21, 128, 61)); // Verde forte
        lblValorTotal.putClientProperty("FlatLaf.style", "font: bold 48");

        totalPanel.add(lblTotalTitle, "wrap");
        totalPanel.add(lblValorTotal);

        infoPanel.add(totalPanel, "growx, bottom");

        main.add(infoPanel, "growy"); // Ocupa altura toda

        // === COLUNA DIREITA: PRODUTOS ===
        JPanel productsCard = EstiloModerno.criarCard();
        // Layout: Topo (Título), Centro (Tabela - grow), Baixo (Footer)
        // insets 0 para o footer encostar embaixo
        productsCard.setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[grow]0[]"));

        // Header com padding
        JPanel headerProd = new JPanel(new MigLayout("fillx, insets 20 20 10 20"));
        headerProd.setOpaque(false);
        JLabel lblProdTitle = EstiloModerno.criarTitulo("Consumo Detalhado");
        headerProd.add(lblProdTitle);
        productsCard.add(headerProd, "wrap, growx");

        // Tabela
        String[] cols = { "Qtd", "Descrição", "Unitário", "Total" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(tableModel);
        configuraTabela(tabela);

        // ScrollPane sem borda para fluir
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        productsCard.add(scroll, "grow, wrap");

        // Rodapé Tabela - Barra inferior distinta
        JPanel footerProd = new JPanel(new MigLayout("fillx, insets 15 20 15 20", "push[]10[]"));
        footerProd.setBackground(new Color(243, 244, 246)); // Cinza claro background
        footerProd.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235))); // Borda superior

        JLabel lblTotalConsumoTitle = new JLabel("Total Consumo:");
        lblTotalConsumoTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        lblValorConsumoTotal = new JLabel("R$ 0,00");
        lblValorConsumoTotal.setForeground(EstiloModerno.PRIMARY);
        lblValorConsumoTotal.putClientProperty("FlatLaf.style", "font: bold 24");

        footerProd.add(lblTotalConsumoTitle);
        footerProd.add(lblValorConsumoTotal);

        productsCard.add(footerProd, "growx");

        main.add(productsCard, "grow");

        add(main);
    }

    private void configuraTela() {
        // Configuração de display em monitor secundário se houver
        String telaMostrar = configGlobal.getInstance().getTelaMostrar();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        boolean telaEncontrada = false;

        if (telaMostrar != null && !telaMostrar.isEmpty()) {
            for (GraphicsDevice screen : screens) {
                if (screen.getIDstring().equals(telaMostrar)) {
                    Rectangle bounds = screen.getDefaultConfiguration().getBounds();
                    this.setBounds(bounds);
                    telaEncontrada = true;
                    break;
                }
            }
        }

        // Se não encontrou a tela configurada, mas há mais de 1 tela, tenta jogar na
        // segunda (index 1)
        if (!telaEncontrada && screens.length > 1) {
            Rectangle bounds = screens[1].getDefaultConfiguration().getBounds();
            this.setBounds(bounds);
        }

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
        this.toFront();
        this.requestFocus();
    }

    private void configuraTabela(JTable t) {
        t.setRowHeight(50); // Linhas maiores
        t.setFont(new Font("Segoe UI", Font.PLAIN, 22)); // Fonte maior
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 22)); // Header maior
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(229, 231, 235));
        t.setFillsViewportHeight(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        t.setDefaultRenderer(Object.class, centerRenderer);

        // Larguras (proporção visual)
        t.getColumnModel().getColumn(0).setPreferredWidth(80); // Qtd
        t.getColumnModel().getColumn(1).setPreferredWidth(400); // Desc
        t.getColumnModel().getColumn(2).setPreferredWidth(120); // Unit
        t.getColumnModel().getColumn(3).setPreferredWidth(120); // Total
    }

    // === MÉTODOS PÚBLICOS (Compatibilidade com ClienteEncerra) ===

    public void setTitulo(int numero) {
        SwingUtilities.invokeLater(() -> lblTitulo.setText("Encerramento - Quarto " + numero));
    }

    public void setaDatas(String dataInicio, String dataFim, String tempoTotalLocado) {
        SwingUtilities.invokeLater(() -> {
            lblInicioLocacao.setText(formatarData(dataInicio));
            lblFimLocacao.setText(formatarData(dataFim));
            lblTempoLocado.setText(tempoTotalLocado);
        });
    }

    public void setPessoas(int numeroPessoas) {
        SwingUtilities.invokeLater(() -> lblPessoasQuarto.setText(String.valueOf(numeroPessoas)));
    }

    public void setarValores(float valorQuarto, float valorAdicionalPeriodo) {
        SwingUtilities.invokeLater(() -> {
            lblValorQuarto.setText("R$ " + String.format("%.2f", valorQuarto));
            lblHoraAdicional.setText("R$ " + String.format("%.2f", valorAdicionalPeriodo));
        });
    }

    // NOVOS METODOS
    public void setDesconto(float valor) {
        SwingUtilities.invokeLater(() -> lblValorDesconto.setText("R$ " + String.format("%.2f", valor)));
    }

    public void setAcrescimo(float valor) {
        SwingUtilities.invokeLater(() -> lblValorAcrescimo.setText("R$ " + String.format("%.2f", valor)));
    }

    public void setValorTotal(float valorTotal) {
        SwingUtilities.invokeLater(() -> lblValorTotal.setText("R$ " + String.format("%.2f", valorTotal)));
    }

    // Método redundante no original, mas mantido. Atualiza o mesmo lblValorQuarto
    public void setValorQuarto(float valorQuarto) {
        SwingUtilities.invokeLater(() -> lblValorQuarto.setText("R$ " + String.format("%.2f", valorQuarto)));
    }

    public void setConsumo(float valorConsumo) {
        SwingUtilities.invokeLater(() -> {
            String txt = "R$ " + String.format("%.2f", valorConsumo);
            lblValorConsumo.setText(txt);
            lblValorConsumoTotal.setText(txt);
        });
    }

    public void adicionaTabela(String id, String quantidade, String texto, float valor, float valorSoma) {
        // Nota: O parametro 'id' original não é usado na tabela visual, apenas Qtd,
        // Texto, Valor, Soma
        SwingUtilities.invokeLater(() -> {
            tableModel.addRow(new Object[] {
                    quantidade,
                    texto,
                    String.format("%.2f", valor),
                    String.format("%.2f", valorSoma)
            });
        });
    }

    public void removerRow(int selected) {
        SwingUtilities.invokeLater(() -> {
            if (selected >= 0 && selected < tableModel.getRowCount()) {
                tableModel.removeRow(selected);
            }
        });
    }

    // === HELPER METHODS ===

    private void addInfoRow(JPanel p, String label, JLabel value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        l.setForeground(EstiloModerno.TEXT_SECONDARY);
        p.add(l);
        p.add(value, "wrap");
    }

    private void addValRow(JPanel p, String label, JLabel value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        p.add(l);
        p.add(value, "wrap");
    }

    private JLabel valorLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(EstiloModerno.TEXT_PRIMARY);
        return l;
    }

    private JLabel moneyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(EstiloModerno.TEXT_PRIMARY);
        return l;
    }

    public static String formatarData(String dataOriginal) {
        // Mesma lógica do original
        try {
            // Se for nulo ou vazio retorna
            if (dataOriginal == null)
                return "";

            // Tenta tratar timestamp com .S ou sem
            SimpleDateFormat formatoOriginal;
            if (dataOriginal.contains(".")) {
                formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            } else {
                formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }

            Date data = formatoOriginal.parse(dataOriginal);
            SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM HH:mm");
            return formatoDesejado.format(data);
        } catch (ParseException e) {
            System.err.println("Erro ao formatar data: " + e.getMessage());
            return dataOriginal; // Retorna original se falhar
        }
    }
}
