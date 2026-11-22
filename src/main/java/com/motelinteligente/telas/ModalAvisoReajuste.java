package com.motelinteligente.telas;

import com.motelinteligente.dados.ConexaoRemota;
import com.motelinteligente.dados.fazconexao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModalAvisoReajuste extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(ModalAvisoReajuste.class);
    private int mouseX, mouseY;
    private JLabel lblValores;
    private JLabel lblDetalhe;

    public ModalAvisoReajuste(JFrame parent) {
        super(parent, true); // Modal
        setUndecorated(true);
        setSize(520, 360);
        setLocationRelativeTo(null); // Centralizado no meio da tela

        initUI();
        carregarValoresEmBackground();
    }

    private void initUI() {
        // Painel Principal com borda moderna e gradiente Slate Dark
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246, 200), 2, true),
                BorderFactory.createEmptyBorder(15, 20, 20, 20)
        ));
        setContentPane(mainPanel);

        // Header arrastável
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        headerPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
            }
        });

        JLabel lblBadge = new JLabel("📢 COMUNICADO DE REAJUSTE DE MENSALIDADE");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBadge.setForeground(new Color(96, 165, 250)); // Azul claro
        headerPanel.add(lblBadge, BorderLayout.WEST);

        JLabel btnFecharX = new JLabel("✕");
        btnFecharX.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnFecharX.setForeground(new Color(148, 163, 184));
        btnFecharX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFecharX.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                btnFecharX.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnFecharX.setForeground(new Color(148, 163, 184));
            }
        });
        headerPanel.add(btnFecharX, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Centro: Conteúdo explicativo e Card de Valores
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 5, 10, 5));

        JLabel lblPrezado = new JLabel("Prezado(a) Parceiro(a),");
        lblPrezado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrezado.setForeground(Color.WHITE);
        lblPrezado.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(lblPrezado);

        contentPanel.add(Box.createVerticalStrut(8));

        JLabel lblTexto = new JLabel("<html><div style='line-height: 1.4; color: #cbd5e1;'>"
                + "Informamos que, a partir do vencimento de <b style='color: #60a5fa;'>Janeiro de 2027</b>, a mensalidade "
                + "do sistema passará a vigorar com o novo valor reajustado abaixo:</div></html>");
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTexto.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(lblTexto);

        contentPanel.add(Box.createVerticalStrut(14));

        // Card com o Comparativo de Valores
        JPanel cardValores = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 15));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        cardValores.setLayout(new BoxLayout(cardValores, BoxLayout.Y_AXIS));
        cardValores.setOpaque(false);
        cardValores.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        cardValores.setAlignmentX(LEFT_ALIGNMENT);

        lblValores = new JLabel("Consultando valores...");
        lblValores.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValores.setForeground(new Color(250, 204, 21)); // Amarelo/Dourado
        lblValores.setAlignmentX(LEFT_ALIGNMENT);
        cardValores.add(lblValores);

        cardValores.add(Box.createVerticalStrut(5));

        lblDetalhe = new JLabel("Novo valor a vigorar a partir de Janeiro de 2027.");
        lblDetalhe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetalhe.setForeground(new Color(148, 163, 184));
        lblDetalhe.setAlignmentX(LEFT_ALIGNMENT);
        cardValores.add(lblDetalhe);

        contentPanel.add(cardValores);

        contentPanel.add(Box.createVerticalStrut(12));

        JLabel lblObs = new JLabel("<html><div style='color: #94a3b8; font-size: 11px;'>"
                + "✓ Nenhuma ação é necessária agora: o novo valor será aplicado automaticamente na fatura de Jan/2027.<br>"
                + "✓ Pagamentos referentes ao ano de 2026 continuam com o valor atual vigente.</div></html>");
        lblObs.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblObs.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(lblObs);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer com botão Entendido
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        footerPanel.setOpaque(false);

        JButton btnEntendido = new JButton("ENTENDIDO");
        btnEntendido.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntendido.setBackground(new Color(37, 99, 235));
        btnEntendido.setForeground(Color.WHITE);
        btnEntendido.setFocusPainted(false);
        btnEntendido.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntendido.setPreferredSize(new Dimension(180, 40));
        btnEntendido.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnEntendido.addActionListener(e -> dispose());
        footerPanel.add(btnEntendido);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Teclas de atalho (ESC ou ENTER fecha o modal)
        KeyAdapter fecharKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                }
            }
        };
        btnEntendido.addKeyListener(fecharKey);
        addKeyListener(fecharKey);
    }

    private void carregarValoresEmBackground() {
        new Thread(() -> {
            double valorAtual = 0.0;

            // 1. Tenta buscar da tabela mensalidade remota
            try (Connection conn = ConexaoRemota.getConnection()) {
                String sql = "SELECT valor FROM mensalidade WHERE status = 'approved' ORDER BY referente ASC LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        valorAtual = rs.getDouble("valor");
                    }
                }
            } catch (Exception e) {
                logger.warn("Não foi possível consultar valor remoto da mensalidade: {}", e.getMessage());
            }

            // 2. Fallback caso a conexão remota não retorne: verifica quantidade de quartos no banco local
            if (valorAtual <= 0) {
                try (Connection connLocal = new fazconexao().conectar()) {
                    String sqlQuartos = "SELECT COUNT(*) AS total FROM quartos";
                    try (PreparedStatement stmtQ = connLocal.prepareStatement(sqlQuartos); ResultSet rsQ = stmtQ.executeQuery()) {
                        if (rsQ.next()) {
                            int totalQuartos = rsQ.getInt("total");
                            valorAtual = (totalQuartos <= 7) ? 250.00 : 350.00;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao consultar quartos locais para estimar mensalidade: {}", e.getMessage());
                    valorAtual = 250.00;
                }
            }

            double novoValor = Math.round(valorAtual * 1.14 * 100.0) / 100.0;
            final double vAtualFinal = valorAtual;
            final double vNovoFinal = novoValor;

            SwingUtilities.invokeLater(() -> {
                lblValores.setText(String.format("Valor Atual: R$ %.2f   ➔   Novo Valor: R$ %.2f", vAtualFinal, vNovoFinal));
                lblDetalhe.setText(String.format("A partir de Janeiro de 2027, o valor passará para R$ %.2f.", vNovoFinal));
            });
        }).start();
    }

    /**
     * Exibe o modal centralizado na tela.
     * Deve ser chamado sempre após o login.
     */
    public static void exibir(JFrame parent) {
        try {
            LocalDate hoje = LocalDate.now();
            // Exibe durante o período de aviso prévio (2026) e início de 2027 (janeiro de 2027)
            if (hoje.getYear() <= 2027 && (hoje.getYear() < 2027 || hoje.getMonthValue() <= 1)) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        ModalAvisoReajuste modal = new ModalAvisoReajuste(parent);
                        modal.setVisible(true);
                    } catch (Exception ex) {
                        logger.error("Erro ao exibir modal de reajuste", ex);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Erro na verificação de exibição do aviso de reajuste", e);
        }
    }
}
