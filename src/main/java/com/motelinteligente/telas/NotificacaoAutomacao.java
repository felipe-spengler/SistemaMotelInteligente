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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.util.Timer;
import java.util.TimerTask;

public class NotificacaoAutomacao extends JDialog {

    private static boolean alternarCor = false;
    private static NotificacaoAutomacao notificacaoAtiva = null;
    private Timer autoCloseTimer;

    public NotificacaoAutomacao(String mensagem) {
        // Fecha a notificação anterior (se estiver aberta) para que a troca de cor fique imediata e visível
        synchronized (NotificacaoAutomacao.class) {
            if (notificacaoAtiva != null) {
                try {
                    if (notificacaoAtiva.autoCloseTimer != null) {
                        notificacaoAtiva.autoCloseTimer.cancel();
                    }
                    notificacaoAtiva.dispose();
                } catch (Exception ignored) {}
            }
            alternarCor = !alternarCor;
            notificacaoAtiva = this;
        }

        final boolean usarTemaA = alternarCor;

        // Tema A: Vermelho Rubi Forte | Tema B: Azul Royal Forte com destaque Amarelo Ouro
        final Color corTopo = usarTemaA ? new Color(185, 28, 28) : new Color(30, 64, 175);
        final Color corBase = usarTemaA ? new Color(105, 15, 15) : new Color(23, 37, 84);
        final Color corBorda = usarTemaA ? new Color(252, 165, 165) : new Color(250, 204, 21);
        final Color corTitulo = usarTemaA ? new Color(254, 240, 138) : new Color(250, 204, 21);

        setUndecorated(true);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setSize(370, 110);
        
        // Posiciona no canto inferior direito
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(scrSize.width - getWidth() - 20, scrSize.height - getHeight() - 60);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, corTopo, 0, getHeight(), corBase);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
        };
        content.setOpaque(false);
        content.setLayout(null);
        content.setBorder(BorderFactory.createLineBorder(corBorda, 2));
        setContentPane(content);

        String horaAtual = new SimpleDateFormat("HH:mm:ss").format(new Date());

        // Título do Alerta com horário para identificar cada tentativa
        JLabel lblTitle = new JLabel("⚠ Alerta de Automação  •  " + horaAtual);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(corTitulo);
        lblTitle.setBounds(18, 12, 315, 20);
        content.add(lblTitle);

        // Mensagem descritiva
        JLabel lblMsg = new JLabel("<html><div style='line-height: 1.25;'>" + mensagem.replace("\n", "<br>") + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMsg.setForeground(Color.WHITE);
        lblMsg.setBounds(18, 38, 330, 58);
        lblMsg.setVerticalAlignment(SwingConstants.TOP);
        content.add(lblMsg);

        // Botão X para fechar manual no canto superior direito
        JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setForeground(new Color(226, 232, 240));
        closeBtn.setBounds(342, 8, 22, 22);
        closeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (autoCloseTimer != null) {
                    autoCloseTimer.cancel();
                }
                dispose();
            }
        });
        content.add(closeBtn);

        // Auto-fechar após 8 segundos
        autoCloseTimer = new Timer(true);
        autoCloseTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dispose();
            }
        }, 8000);

        setVisible(true);
    }
}
