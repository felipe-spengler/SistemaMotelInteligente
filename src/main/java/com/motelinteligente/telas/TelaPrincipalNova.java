package com.motelinteligente.telas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * TelaPrincipalNova - Frontend Limpo (Réplica visual da original)
 * Apenas UI - Lógica será no Controller
 */
public class TelaPrincipalNova extends JFrame {

    // ========== COMPONENTES PÚBLICOS (mesmos da original) ==========

    // Painéis principais
    public JDesktopPane srPane; // Painel dos quadrados de quartos
    public JPanel painelSecundario; // Painel colorido lateral
    public JPanel painelBotton; // Painel inferior
    public JTabbedPane alteradorPaineis; // Abas (Dados, Prevendidos)

    // Labels
    public JLabel lblUsuario;
    public JLabel lblCargo;
    public JLabel labelData;
    public JLabel labelHora;
    public JLabel lblAlarmeAtivo;
    public JLabel labelReservas;

    // Labels do painel secundário
    public JLabel num; // Número do quarto
    public JLabel labelEntrada;
    public JLabel labelQuantidadePessoas;
    public JLabel labelValor;
    public JLabel labelAdicional;
    public JLabel labelTotal;

    // Campos de input
    public JTextField txtPessoas;
    public JTextField txtDescontoNegociado;
    public JTextField txtAntecipadoDinheiro;

    // Tabelas
    public JTable tabela1; // Tabela de produtos
    public DefaultTableModel modeloTabela1;

    // Botões principais
    public JButton botaoIniciar;
    public JButton botaoEncerrar;
    public JButton botaoStatus;
    public JButton botaoTroca;
    public JButton botaoAdicionar;
    public JButton botaoRemover;
    public JButton botaoDescontoNegociado;
    public JButton botaoAplicarAntecipado;

    // Menu Bar
    public JMenuBar jMenuBar1;
    public JMenu btCadastros, btProdutos, btCaixa, btRelatorios;
    public JMenu btFerramentas, btAlarmes, menuReservas, menuSistema, btMenuSair;

    public TelaPrincipalNova() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Motel Inteligente - Sistema de Gerenciamento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu bar
        criarMenuBar();

        // Conteúdo principal
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Lado esquerdo (Quartos + Reservas)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(criarPainelQuartos(), BorderLayout.CENTER);
        leftPanel.add(criarPainelReservas(), BorderLayout.SOUTH);

        // Centro (Painel colorido)
        painelSecundario = criarPainelSecundario();

        // Direita (Tabbed pane com dados e produtos)
        alteradorPaineis = criarTabbedPane();

        // Adicionar ao layout principal
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(painelSecundario, BorderLayout.CENTER);
        mainPanel.add(alteradorPaineis, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(criarPainelInferior(), BorderLayout.SOUTH);

        setSize(1400, 900);
        setLocationRelativeTo(null);
    }

    private void criarMenuBar() {
        jMenuBar1 = new JMenuBar();

        btCadastros = new JMenu("Cadastros");
        btProdutos = new JMenu("Produtos");
        btCaixa = new JMenu("Caixa");
        btRelatorios = new JMenu("Relatórios");
        btFerramentas = new JMenu("Ferramentas");
        btAlarmes = new JMenu("Alarmes");
        menuReservas = new JMenu("Reservas");
        menuSistema = new JMenu("Sistema");
        btMenuSair = new JMenu("Sair");

        jMenuBar1.add(btCadastros);
        jMenuBar1.add(btProdutos);
        jMenuBar1.add(btCaixa);
        jMenuBar1.add(btRelatorios);
        jMenuBar1.add(btFerramentas);
        jMenuBar1.add(btAlarmes);
        jMenuBar1.add(menuReservas);
        jMenuBar1.add(menuSistema);
        jMenuBar1.add(btMenuSair);

        setJMenuBar(jMenuBar1);
    }

    private JPanel criarPainelQuartos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 500));

        JLabel titulo = new JLabel("Quartos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(titulo, BorderLayout.NORTH);

        srPane = new JDesktopPane();
        srPane.setBackground(Color.WHITE);
        srPane.setLayout(new GridLayout(3, 3, 5, 5));

        JScrollPane scroll = new JScrollPane(srPane);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel criarPainelReservas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 150));
        panel.setBorder(BorderFactory.createTitledBorder("Reservas Próximas"));

        labelReservas = new JLabel("<html>Nenhuma reserva nas próximas 24h</html>");
        labelReservas.setVerticalAlignment(SwingConstants.TOP);

        JScrollPane scroll = new JScrollPane(labelReservas);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel criarPainelSecundario() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(149, 165, 166)); // Cor padrão cinza
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(350, 600));

        // Número do quarto (grande no topo)
        num = new JLabel("--", SwingConstants.CENTER);
        num.setFont(new Font("Arial", Font.BOLD, 72));
        num.setForeground(Color.WHITE);
        panel.add(num, BorderLayout.NORTH);

        // Informações centrais
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        infoPanel.setOpaque(false);

        infoPanel.add(criarLabel("Entrada:"));
        labelEntrada = criarLabel("--:--");
        infoPanel.add(labelEntrada);

        infoPanel.add(criarLabel("Pessoas:"));
        txtPessoas = new JTextField("2");
        txtPessoas.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(txtPessoas);

        infoPanel.add(criarLabel("Valor Quarto:"));
        labelValor = criarLabel("R$ 0,00");
        infoPanel.add(labelValor);

        infoPanel.add(criarLabel("Adicional:"));
        labelAdicional = criarLabel("R$ 0,00");
        infoPanel.add(labelAdicional);

        infoPanel.add(criarLabel("Consumo:"));
        JLabel labelConsumo = criarLabel("R$ 0,00");
        infoPanel.add(labelConsumo);

        infoPanel.add(criarLabel("Total:"));
        labelTotal = criarLabel("R$ 0,00");
        labelTotal.setFont(new Font("Arial", Font.BOLD, 18));
        infoPanel.add(labelTotal);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Botões na parte inferior
        JPanel botoesPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        botoesPanel.setOpaque(false);

        botaoIniciar = new JButton("INICIAR QUARTO");
        botaoIniciar.setFont(new Font("Arial", Font.BOLD, 14));
        botaoIniciar.setBackground(new Color(46, 204, 113));
        botaoIniciar.setForeground(Color.WHITE);
        botoesPanel.add(botaoIniciar);

        botaoEncerrar = new JButton("ENCERRAR QUARTO");
        botaoEncerrar.setFont(new Font("Arial", Font.BOLD, 14));
        botaoEncerrar.setBackground(new Color(231, 76, 60));
        botaoEncerrar.setForeground(Color.WHITE);
        botaoEncerrar.setVisible(false);
        botoesPanel.add(botaoEncerrar);

        botaoStatus = new JButton("ALTERAR STATUS");
        botaoStatus.setFont(new Font("Arial", Font.PLAIN, 13));
        botoesPanel.add(botaoStatus);

        botaoTroca = new JButton("TROCAR QUARTO");
        botaoTroca.setFont(new Font("Arial", Font.PLAIN, 13));
        botaoTroca.setVisible(false);
        botoesPanel.add(botaoTroca);

        panel.add(botoesPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTabbedPane criarTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(550, 600));

        // Aba 1: Dados
        tabbedPane.addTab("Dados", criarAbaDados());

        // Aba 2: Prevendidos
        tabbedPane.addTab("Prevendidos", criarAbaPrevendidos());

        return tabbedPane;
    }

    private JPanel criarAbaDados() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Campos
        JPanel camposPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        camposPanel.add(new JLabel("Desconto Negociado:"));
        txtDescontoNegociado = new JTextField("0");
        camposPanel.add(txtDescontoNegociado);

        camposPanel.add(new JLabel("Antecipado Dinheiro:"));
        txtAntecipadoDinheiro = new JTextField("0");
        camposPanel.add(txtAntecipadoDinheiro);

        panel.add(camposPanel, BorderLayout.NORTH);

        // Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        botaoDescontoNegociado = new JButton("Aplicar Desconto");
        botaoAplicarAntecipado = new JButton("Aplicar Antecipado");

        botoesPanel.add(botaoDescontoNegociado);
        botoesPanel.add(botaoAplicarAntecipado);

        panel.add(botoesPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel criarAbaPrevendidos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Tabela
        String[] colunas = { "Código", "Produto", "Qtd", "Valor" };
        modeloTabela1 = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela1 = new JTable(modeloTabela1);
        tabela1.setRowHeight(25);
        tabela1.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(tabela1);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        botaoAdicionar = new JButton("Adicionar Produto");
        botaoAdicionar.setBackground(new Color(46, 204, 113));
        botaoAdicionar.setForeground(Color.WHITE);

        botaoRemover = new JButton("Remover Produto");
        botaoRemover.setBackground(new Color(231, 76, 60));
        botaoRemover.setForeground(Color.WHITE);

        botoesPanel.add(botaoAdicionar);
        botoesPanel.add(botaoRemover);

        panel.add(botoesPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelInferior() {
        painelBotton = new JPanel(new BorderLayout());
        painelBotton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        painelBotton.setBackground(new Color(240, 240, 240));

        // Esquerda: Usuário
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setOpaque(false);

        lblUsuario = new JLabel("Usuário");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 13));

        lblCargo = new JLabel("• Cargo");
        lblCargo.setFont(new Font("Arial", Font.PLAIN, 12));

        userPanel.add(new JLabel("👤"));
        userPanel.add(lblUsuario);
        userPanel.add(lblCargo);

        // Centro: Alarme
        lblAlarmeAtivo = new JLabel("🔔 Alarmes Ativos");
        lblAlarmeAtivo.setFont(new Font("Arial", Font.BOLD, 12));
        lblAlarmeAtivo.setForeground(Color.RED);
        lblAlarmeAtivo.setVisible(false);

        // Direita: Data e Hora
        JPanel dataHoraPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dataHoraPanel.setOpaque(false);

        labelData = new JLabel("00/00/0000");
        labelData.setFont(new Font("Arial", Font.BOLD, 14));

        labelHora = new JLabel("00:00:00");
        labelHora.setFont(new Font("Arial", Font.BOLD, 16));
        labelHora.setForeground(new Color(41, 128, 185));

        dataHoraPanel.add(new JLabel("📅"));
        dataHoraPanel.add(labelData);
        dataHoraPanel.add(new JLabel("  🕒"));
        dataHoraPanel.add(labelHora);

        painelBotton.add(userPanel, BorderLayout.WEST);
        painelBotton.add(lblAlarmeAtivo, BorderLayout.CENTER);
        painelBotton.add(dataHoraPanel, BorderLayout.EAST);

        return painelBotton;
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    // Método para testar visualmente
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            TelaPrincipalNova frame = new TelaPrincipalNova();
            frame.lblUsuario.setText("Felipe");
            frame.lblCargo.setText("• Administrador");
            frame.labelData.setText("24/12/2025");
            frame.labelHora.setText("15:56:00");
            frame.setVisible(true);
        });
    }
}
