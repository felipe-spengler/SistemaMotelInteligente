package com.motelinteligente.telas;

import com.motelinteligente.dados.ConexaoRemota;
import com.motelinteligente.dados.configGlobal;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelaMensalidade extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(TelaMensalidade.class);
    private String sistemaNome;
    private JLabel lblStatus;
    private JLabel lblReferente;
    private JButton btnPagar;

    public TelaMensalidade() {
        this.sistemaNome = getSistemaProperty();
        initComponents();
        verificarStatus();
    }

    private String getSistemaProperty() {
        return com.motelinteligente.dados.CarregarVariaveis.getFilial();
    }

    private void initComponents() {
        setTitle("Sistema - Mensalidade");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel Principal com Gradiente
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), 0, getHeight(), new Color(15, 23, 42));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(null);
        add(mainPanel, BorderLayout.CENTER);

        JLabel title = new JLabel("Mensalidade do Sistema");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBounds(20, 20, 400, 40);
        mainPanel.add(title);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 1));
        card.setBounds(20, 80, 440, 200);
        card.setLayout(null);
        mainPanel.add(card);

        lblStatus = new JLabel("Verificando status...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblStatus.setForeground(Color.LIGHT_GRAY);
        lblStatus.setBounds(20, 30, 400, 30);
        card.add(lblStatus);

        lblReferente = new JLabel("");
        lblReferente.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblReferente.setForeground(Color.GRAY);
        lblReferente.setBounds(20, 60, 400, 30);
        card.add(lblReferente);

        btnPagar = new JButton("IR PARA PAGAMENTO");
        btnPagar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPagar.setBackground(new Color(37, 99, 235));
        btnPagar.setForeground(Color.WHITE);
        btnPagar.setFocusPainted(false);
        btnPagar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnPagar.setBounds(20, 130, 200, 40);
        btnPagar.setVisible(false);
        btnPagar.addActionListener(e -> abrirPagamento());
        card.add(btnPagar);

        JLabel footer = new JLabel("Em caso de dúvidas, entre em contato com o suporte.");
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.setForeground(new Color(148, 163, 184));
        footer.setBounds(20, 320, 400, 20);
        mainPanel.add(footer);
    }

    private void verificarStatus() {
        new Thread(() -> {
            try (Connection conn = ConexaoRemota.getConnection()) {
                String sql = "SELECT referente FROM mensalidade WHERE status = 'approved' ORDER BY referente DESC LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

                    LocalDate hoje = LocalDate.now();
                    LocalDate ultimoPagamento = null;
                    if (rs.next()) {
                        ultimoPagamento = rs.getDate("referente").toLocalDate();
                    }

                    LocalDate finalUltimo = ultimoPagamento;
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (finalUltimo != null && !finalUltimo.isBefore(hoje.withDayOfMonth(1))) {
                            lblStatus.setText("Status: PAGAMENTO EM DIA");
                            lblStatus.setForeground(new Color(74, 222, 128));
                            lblReferente.setText("Última referência paga: " + finalUltimo.getMonthValue() + "/" + finalUltimo.getYear());
                            btnPagar.setVisible(false);
                        } else {
                            lblStatus.setText("Status: PENDENTE / EM ABERTO");
                            lblStatus.setForeground(new Color(248, 113, 113));
                            if (finalUltimo != null) {
                                lblReferente.setText("Última referência paga: " + finalUltimo.getMonthValue() + "/" + finalUltimo.getYear());
                            } else {
                                lblReferente.setText("Nenhum pagamento registrado.");
                            }
                            btnPagar.setVisible(true);
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("Erro ao verificar status da mensalidade na tela", e);
                javax.swing.SwingUtilities.invokeLater(() -> lblStatus.setText("Erro ao conectar com servidor."));
            }
        }).start();
    }

    private void abrirPagamento() {
        String url = "http://motelinteligente.com/api/mensalidade.php?sistema=" + sistemaNome;
        TelaPrincipal.abrirURL(url);
    }
}
