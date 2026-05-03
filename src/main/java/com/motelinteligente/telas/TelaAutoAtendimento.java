package com.motelinteligente.telas;

import com.motelinteligente.dados.fquartos;
import com.formdev.flatlaf.FlatClientProperties;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class TelaAutoAtendimento extends javax.swing.JFrame {

    private JDateChooser dcInicio;
    private JDateChooser dcFim;
    private JLabel lblTotalGeral, lblTotalAuto, lblPercentual, lblUpsells;
    private JPanel cardUpsell;
    private ChartPanel graphHorarios;
    private PieChartPanel graphTipos;

    public TelaAutoAtendimento() {
        initComponents();
        configurarEstilo();
        carregarHoje();
    }

    private void configurarEstilo() {
        setTitle("Dashboard de Performance - Autoatendimento");
        setSize(1200, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void carregarHoje() {
        dcInicio.setDate(new Date());
        dcFim.setDate(new Date());
        atualizarRelatorio();
    }

    private void atualizarRelatorio() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String inicio = sdf.format(dcInicio.getDate());
        String fim = sdf.format(dcFim.getDate());
        fquartos dao = new fquartos();

        // 1. Dados e KPI
        List<Object[]> tipos = dao.getRelatorioAutoAtendimentoTipos(inicio, fim);
        int totalGeral = 0, totalAuto = 0;
        Map<String, Integer> mapTipos = new LinkedHashMap<>();

        for (Object[] linha : tipos) {
            String tipo = (String) linha[0];
            int total = (int) linha[1];
            totalGeral += total;
            if (!tipo.equals("MANUAL")) {
                totalAuto += total;
                mapTipos.put(tipo, total);
            }
        }

        lblTotalGeral.setText(String.valueOf(totalGeral));
        lblTotalAuto.setText(String.valueOf(totalAuto));
        if (totalGeral > 0) {
            float perc = ((float) totalAuto / totalGeral) * 100;
            lblPercentual.setText(String.format("%.1f%%", perc));
        } else {
            lblPercentual.setText("0%");
        }

        // 2. Gráfico de Pizza (Tipos)
        graphTipos.setData(mapTipos);

        // 3. Gráfico de Barras (Horários)
        List<Object[]> horarios = dao.getRelatorioAutoAtendimentoHorarios(inicio, fim);
        graphHorarios.setData(horarios);

        // 4. Upsells
        Object[] upsellData = dao.getRelatorioUpsells(inicio, fim);
        int qtdUpsell = (int) upsellData[0];
        float totalGasto = (float) upsellData[1];
        lblUpsells.setText(String.format("%d (R$ %.2f)", qtdUpsell, totalGasto));
        
        if (qtdUpsell > 0 && Math.abs((totalGasto / qtdUpsell) - 10.0f) > 0.1f) {
            cardUpsell.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #ef4444");
        } else {
            cardUpsell.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #7c3aed");
        }
        
        repaint();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(248, 250, 252));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Top Panel: Filtros Estilo Performance.php
        JPanel pnlFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        pnlFiltro.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #ffffff");
        
        pnlFiltro.add(new JLabel("Período de:"));
        dcInicio = new JDateChooser();
        dcInicio.setPreferredSize(new Dimension(150, 30));
        pnlFiltro.add(dcInicio);
        
        pnlFiltro.add(new JLabel("até:"));
        dcFim = new JDateChooser();
        dcFim.setPreferredSize(new Dimension(150, 30));
        pnlFiltro.add(dcFim);
        
        JButton btnFiltrar = new JButton("FILTRAR PERFORMANCE");
        btnFiltrar.putClientProperty(FlatClientProperties.STYLE, "background: #2563eb; foreground: #ffffff; arc: 10; font: bold");
        btnFiltrar.setPreferredSize(new Dimension(200, 35));
        btnFiltrar.addActionListener(e -> atualizarRelatorio());
        pnlFiltro.add(btnFiltrar);

        // Center: KPIs e Gráficos
        JPanel pnlCenter = new JPanel(new BorderLayout(25, 25));
        pnlCenter.setOpaque(false);
        
        // KPIs Cards
        JPanel pnlKpis = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlKpis.setOpaque(false);
        pnlKpis.add(criarCardKpi("TOTAL ENTRADAS", lblTotalGeral = new JLabel("0"), "#1e293b"));
        pnlKpis.add(criarCardKpi("USO TOTEM", lblTotalAuto = new JLabel("0"), "#3b82f6"));
        pnlKpis.add(criarCardKpi("% ADESÃO", lblPercentual = new JLabel("0%"), "#10b981"));
        cardUpsell = criarCardKpi("UPSELLS (DESC.)", lblUpsells = new JLabel("0"), "#7c3aed");
        pnlKpis.add(cardUpsell);

        // Gráficos Area
        JPanel pnlGraphs = new JPanel(new GridLayout(1, 2, 25, 0));
        pnlGraphs.setOpaque(false);

        // Pizza
        JPanel cardPizza = criarCardGraph("Distribuição de Escolhas (Totem)");
        graphTipos = new PieChartPanel();
        cardPizza.add(graphTipos, BorderLayout.CENTER);
        pnlGraphs.add(cardPizza);

        // Barras
        JPanel cardBarras = criarCardGraph("Picos de Uso por Horário (Adesão %)");
        graphHorarios = new ChartPanel();
        cardBarras.add(graphHorarios, BorderLayout.CENTER);
        pnlGraphs.add(cardBarras);

        pnlCenter.add(pnlKpis, BorderLayout.NORTH);
        pnlCenter.add(pnlGraphs, BorderLayout.CENTER);

        mainPanel.add(pnlFiltro, BorderLayout.NORTH);
        mainPanel.add(pnlCenter, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel criarCardKpi(String titulo, JLabel valor, String hex) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 25; background: " + hex);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel t = new JLabel(titulo);
        t.setForeground(new Color(255,255,255, 180));
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valor.setForeground(Color.WHITE);
        valor.setFont(new Font("Segoe UI", Font.BOLD, 32));
        card.add(t, BorderLayout.NORTH);
        card.add(valor, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarCardGraph(String titulo) {
        JPanel card = new JPanel(new BorderLayout());
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 25; background: #ffffff");
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
        card.add(t, BorderLayout.NORTH);
        return card;
    }

    // CLASSES DE DESENHO DE GRÁFICOS CUSTOM
    class PieChartPanel extends JPanel {
        private Map<String, Integer> data = new HashMap<>();
        private Color[] colors = {new Color(59, 130, 246), new Color(16, 185, 129), new Color(139, 92, 246), new Color(245, 158, 11)};
        public void setData(Map<String, Integer> d) { this.data = d; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int total = data.values().stream().mapToInt(i -> i).sum();
            double cur = 0, size = Math.min(getWidth(), getHeight()) * 0.7;
            int x = (int)(getWidth()-size)/2, y = (int)(getHeight()-size)/2;
            int i = 0;
            for (String key : data.keySet()) {
                double slice = (double)data.get(key)/total;
                g2.setColor(colors[i % colors.length]);
                g2.fill(new Arc2D.Double(x, y, size, size, cur * 360, slice * 360, Arc2D.PIE));
                // Texto
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(key + " (" + data.get(key) + ")", 10, 20 + (i * 20));
                cur += slice; i++;
            }
        }
    }

    class ChartPanel extends JPanel {
        private List<Object[]> data = new ArrayList<>();
        public void setData(List<Object[]> d) { this.data = d; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int maxTotal = data.stream().mapToInt(o -> (int)o[1]).max().orElse(1);
            int margin = 40, w = (getWidth() - 2*margin) / 24;
            for (Object[] d : data) {
                int h = (int)d[0], total = (int)d[1], auto = (int)d[2];
                int barH = (int)((getHeight()-2*margin) * ((double)total/maxTotal));
                int autoH = (int)((getHeight()-2*margin) * ((double)auto/maxTotal));
                int x = margin + (h * w);
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(x, getHeight()-margin-barH, w-5, barH, 5, 5);
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(x, getHeight()-margin-autoH, w-5, autoH, 5, 5);
                if (h % 3 == 0) g2.drawString(h+"h", x, getHeight()-margin+15);
            }
        }
    }

    public static void main(String args[]) {
        try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); } catch (Exception e) {}
        java.awt.EventQueue.invokeLater(() -> new TelaAutoAtendimento().setVisible(true));
    }
}
