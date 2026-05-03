package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.MotelInteligenteApplication;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.ffuncionario;
import com.motelinteligente.telas.TelaPrincipal;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class TelaLoginModerno extends JFrame {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnSair;

    public TelaLoginModerno() {
        initComponents();
        setIcone();
    }

    private void initComponents() {
        setTitle("Motel Intensy - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        // Painel principal com gradiente
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();

                // Gradiente moderno
                Color color1 = new Color(103, 58, 183); // Roxo
                Color color2 = new Color(81, 45, 168); // Roxo mais escuro
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new MigLayout("fill, insets 40", "[center]", "[grow][][]"));

        // Card central branco
        JPanel cardLogin = new JPanel();
        cardLogin.setBackground(Color.WHITE);
        cardLogin.setLayout(new MigLayout("fillx, insets 40", "[grow, fill]", "[]30[]10[]10[]10[]30[]10[]"));
        cardLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Logo
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/imagens/logo.png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(200, -1, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
            cardLogin.add(lblLogo, "center, wrap");
        } catch (Exception e) {
            JLabel lblTitulo = new JLabel("Motel Intensy");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblTitulo.setForeground(new Color(103, 58, 183));
            cardLogin.add(lblTitulo, "center, wrap");
        }

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Sistema de Gestão");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(120, 120, 120));
        cardLogin.add(lblSubtitulo, "center, wrap 30");

        // Campo Login
        JLabel lblLogin = new JLabel("Usuário");
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLogin.setForeground(new Color(60, 60, 60));
        cardLogin.add(lblLogin, "wrap");

        txtLogin = new JTextField();
        txtLogin.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Digite seu usuário");
        txtLogin.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; " +
                        "borderWidth: 2; " +
                        "focusWidth: 2; " +
                        "innerFocusWidth: 0; " +
                        "focusedBorderColor: #673AB7; " +
                        "font: 16");
        txtLogin.setPreferredSize(new Dimension(0, 55));
        cardLogin.add(txtLogin, "growx, wrap");

        // Campo Senha
        JLabel lblSenha = new JLabel("Senha");
        lblSenha.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSenha.setForeground(new Color(60, 60, 60));
        cardLogin.add(lblSenha, "wrap");

        txtSenha = new JPasswordField();
        txtSenha.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Digite sua senha");
        txtSenha.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; " +
                        "borderWidth: 2; " +
                        "focusWidth: 2; " +
                        "innerFocusWidth: 0; " +
                        "focusedBorderColor: #673AB7; " +
                        "showRevealButton: true; " +
                        "font: 16");
        txtSenha.setPreferredSize(new Dimension(0, 55));

        // Enter key listener
        txtSenha.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        });
        cardLogin.add(txtSenha, "growx, wrap");

        // Botão Entrar
        btnEntrar = new JButton("ENTRAR");
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setBackground(new Color(103, 58, 183));
        btnEntrar.setFocusPainted(false);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; " +
                        "borderWidth: 0; " +
                        "focusWidth: 0; " +
                        "innerFocusWidth: 0; " +
                        "background: #673AB7; " +
                        "hoverBackground: #512DA8; " +
                        "pressedBackground: #4527A0; " +
                        "font: bold $medium.font");
        btnEntrar.setPreferredSize(new Dimension(0, 55));
        btnEntrar.addActionListener(e -> realizarLogin());
        cardLogin.add(btnEntrar, "growx, wrap");

        // Botão Sair
        btnSair = new JButton("Sair");
        btnSair.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSair.setForeground(new Color(120, 120, 120));
        btnSair.setBorderPainted(false);
        btnSair.setContentAreaFilled(false);
        btnSair.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSair.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; " +
                        "borderWidth: 0; " +
                        "background: null");
        btnSair.addActionListener(e -> System.exit(0));
        cardLogin.add(btnSair, "center");

        // Definir tamanho fixo do card
        cardLogin.setMaximumSize(new Dimension(400, 600));
        cardLogin.setPreferredSize(new Dimension(400, 550));

        // Adiciona o card ao painel principal
        mainPanel.add(cardLogin, "w 400!, wrap");

        // Rodapé com versão
        JLabel lblVersao = new JLabel("v1.0.0 © 2025 Motel Intensy");
        lblVersao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersao.setForeground(new Color(255, 255, 255, 180));
        mainPanel.add(lblVersao, "center");

        add(mainPanel);

        // Foco no campo de login
        SwingUtilities.invokeLater(() -> txtLogin.requestFocusInWindow());
    }

    private void setIcone() {
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/imagens/iconeMotel.png")));
        } catch (Exception e) {
            // Ignora erro se ícone não existir
        }
    }

    private void realizarLogin() {
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, preencha todos os campos!",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Desabilita botão durante login
        btnEntrar.setEnabled(false);
        btnEntrar.setText("ENTRANDO...");

        // Verifica login em uma thread separada para não travar a UI
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                System.out.println("[LOGIN] Verificando credenciais...");
                return new ffuncionario().verificaLogin(login, senha);
            }

            @Override
            protected void done() {
                try {
                    String cargo = get();
                    System.out.println("[LOGIN] Cargo retornado: " + cargo);

                    if (cargo != null) {
                        System.out.println("[LOGIN] Login bem-sucedido! Carregando sistema...");

                        // Carregar dados
                        CacheDados cache = CacheDados.getInstancia();
                        configGlobal configuracoes = configGlobal.getInstance();

                        // Carrega informações globais
                        configuracoes.carregarInformacoes(cargo, login);
                        cache.carregarDadosQuarto();
                        System.out.println("[LOGIN] Dados carregados");

                        // Carrega Arduino se necessário
                        if (!configuracoes.isFlagArduino()) {
                            try {
                                cache.carregaArduino();
                                configuracoes.setFlagArduino(true);
                                System.out.println("[LOGIN] Arduino carregado");
                            } catch (Exception ex) {
                                System.err.println("[LOGIN] Erro ao carregar Arduino: " + ex.getMessage());
                            }
                        }

                        // Iniciar sistema Spring se necessário
                        if (!configuracoes.isFlagSistemaSpring()) {
                            new Thread(() -> {
                                System.out.println("[LOGIN] Iniciando Spring Boot...");
                                MotelInteligenteApplication.main(new String[] {});
                            }).start();
                            configuracoes.setFlagSistemaSpring(true);
                        }

                        // Abrir tela principal na EDT
                        SwingUtilities.invokeLater(() -> {
                            try {
                                System.out.println("[LOGIN] Abrindo tela principal...");
                                TelaPrincipal tela = new TelaPrincipal();
                                tela.setVisible(true);
                                System.out.println("[LOGIN] Tela principal aberta!");

                                // Fechar login
                                dispose();
                            } catch (Exception ex) {
                                System.err.println("[LOGIN] Erro ao abrir tela principal: " + ex.getMessage());
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(TelaLoginModerno.this,
                                        "Erro ao abrir tela principal: " + ex.getMessage(),
                                        "Erro",
                                        JOptionPane.ERROR_MESSAGE);
                                btnEntrar.setEnabled(true);
                                btnEntrar.setText("ENTRAR");
                            }
                        });
                    } else {
                        System.out.println("[LOGIN] Credenciais inválidas");
                        JOptionPane.showMessageDialog(TelaLoginModerno.this,
                                "Usuário ou senha incorretos!",
                                "Erro no Login",
                                JOptionPane.ERROR_MESSAGE);
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("ENTRAR");
                        txtSenha.setText("");
                        txtSenha.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    System.err.println("[LOGIN] Exceção durante login: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TelaLoginModerno.this,
                            "Erro ao realizar login: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("ENTRAR");
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        // Configuração do tema moderno
        try {
            FlatIntelliJLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Criar e exibir a tela
        SwingUtilities.invokeLater(() -> {
            TelaLoginModerno login = new TelaLoginModerno();
            login.setVisible(true);
        });
    }
}
