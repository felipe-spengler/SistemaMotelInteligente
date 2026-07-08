package com.motelinteligente.telas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.util.Timer;
import java.util.TimerTask;

public class NotificacaoMensalidade extends JDialog {

    private String urlPagamento;

    public NotificacaoMensalidade(String mensagem, String url) {
        this.urlPagamento = url;
        setUndecorated(true);
        setAlwaysOnTop(true);
        setSize(350, 150);
        
        // Posiciona no canto inferior direito
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(scrSize.width - getWidth() - 20, scrSize.height - getHeight() - 60);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), 0, getHeight(), new Color(15, 23, 42));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        content.setOpaque(false);
        content.setLayout(null);
        content.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 1));
        setContentPane(content);

        JLabel lblTitle = new JLabel("Aviso de Mensalidade");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(248, 113, 113));
        lblTitle.setBounds(20, 15, 250, 25);
        content.add(lblTitle);

        JLabel lblMsg = new JLabel("<html>" + mensagem.replace("\n", "<br>") + "</html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMsg.setForeground(Color.WHITE);
        lblMsg.setBounds(20, 45, 310, 50);
        lblMsg.setVerticalAlignment(SwingConstants.TOP);
        content.add(lblMsg);

        JButton btnPagar = new JButton("PAGAR");
        btnPagar.setBounds(20, 105, 80, 30);
        btnPagar.setBackground(new Color(37, 99, 235));
        btnPagar.setForeground(Color.WHITE);
        btnPagar.setFocusPainted(false);
        btnPagar.setBorder(BorderFactory.createEmptyBorder());
        btnPagar.addActionListener(e -> {
            TelaPrincipal.abrirURL(urlPagamento);
            dispose();
        });
        content.add(btnPagar);

        JButton btnFechar = new JButton("DEPOIS");
        btnFechar.setBounds(110, 105, 80, 30);
        btnFechar.setBackground(new Color(71, 85, 105));
        btnFechar.setForeground(Color.WHITE);
        btnFechar.setFocusPainted(false);
        btnFechar.setBorder(BorderFactory.createEmptyBorder());
        btnFechar.addActionListener(e -> dispose());
        content.add(btnFechar);

        // Auto-fechar após 20 minutos
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                dispose();
            }
        }, 600000);

        // Permitir fechar clicando no canto
        JLabel closeBtn = new JLabel("X");
        closeBtn.setForeground(Color.GRAY);
        closeBtn.setBounds(330, 5, 20, 20);
        closeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        content.add(closeBtn);

        setVisible(true);
    }
}
