package com.motelinteligente.telas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;

public class PopupFiscalAd extends JDialog {

    public PopupFiscalAd(JFrame parent) {
        super(parent, "🚀 Automatize a Emissão de Notas do seu Motel!", true);
        setSize(500, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Cansado de preencher recibos à mão ou correr riscos?");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitle);

        panel.add(Box.createVerticalStrut(15));

        JLabel lblText = new JLabel("<html><div style='text-align: center;'>"
                + "Ative agora o nosso Módulo Fiscal Automático. Sempre que uma suíte fechar "
                + "ou uma bebida for vendida, o sistema emite a nota fiscal na hora, "
                + "controla seu estoque e envia tudo direto para o seu contador.<br><br>"
                + "✅ Sem sistemas de terceiros: Tudo integrado no painel que você já usa.<br>"
                + "✅ Configuração rápida: Suporte para Certificado A1.</div></html>");
        lblText.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblText);

        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnContratar = new JButton("Quero Contratar / Falar com Suporte");
        btnContratar.setBackground(new Color(0, 153, 51));
        btnContratar.setForeground(Color.WHITE);
        btnContratar.addActionListener(this::abrirSuporte);

        JButton btnLembrar = new JButton("Lembrar mais tarde");
        btnLembrar.addActionListener(e -> dispose());

        btnPanel.add(btnContratar);
        btnPanel.add(btnLembrar);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void abrirSuporte(ActionEvent e) {
        try {
            Desktop.getDesktop().browse(new URI("https://wa.me/5599999999999?text=Quero%20ativar%20o%20modulo%20fiscal"));
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir navegador.");
        }
    }

    public static void verificarExibicao(JFrame parent, boolean statusModuloFiscal) {
        if (!statusModuloFiscal) {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            int dia = hoje.getDayOfMonth();
            // Verifica se é dia 05 ou 10 para exibir
            if (dia == 5 || dia == 10) {
                new PopupFiscalAd(parent).setVisible(true);
            }
        }
    }
}
