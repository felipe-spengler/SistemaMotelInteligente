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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.util.Timer;
import java.util.TimerTask;

public class NotificacaoAutomacao extends JDialog {

    public NotificacaoAutomacao(String mensagem) {
        setUndecorated(true);
        setAlwaysOnTop(true);
        setSize(350, 100);
        
        // Posiciona no canto inferior direito
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(scrSize.width - getWidth() - 20, scrSize.height - getHeight() - 60);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradê escuro elegante
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), 0, getHeight(), new Color(15, 23, 42));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        content.setOpaque(false);
        content.setLayout(null);
        content.setBorder(BorderFactory.createLineBorder(new Color(239, 68, 68, 80), 1)); // Borda vermelha sutil
        setContentPane(content);

        // Título do Alerta
        JLabel lblTitle = new JLabel("Alerta de Automação");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(239, 68, 68)); // Vermelho
        lblTitle.setBounds(20, 15, 250, 20);
        content.add(lblTitle);

        // Mensagem descritiva
        JLabel lblMsg = new JLabel("<html>" + mensagem.replace("\n", "<br>") + "</html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMsg.setForeground(Color.WHITE);
        lblMsg.setBounds(20, 40, 310, 45);
        lblMsg.setVerticalAlignment(SwingConstants.TOP);
        content.add(lblMsg);

        // Botão X para fechar manual no canto superior direito
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

        // Auto-fechar após 8 segundos
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                dispose();
            }
        }, 8000);

        setVisible(true);
    }
}
