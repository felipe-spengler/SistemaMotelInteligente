package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class EstiloModerno {

    // Paleta de Cores (Tailwind inspirado)
    public static final Color BG_BACKGROUND = new Color(243, 244, 246); // gray-100
    public static final Color BG_CARD = Color.WHITE;

    public static final Color TEXT_PRIMARY = new Color(17, 24, 39); // gray-900
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128); // gray-500

    public static final Color PRIMARY = new Color(37, 99, 235); // blue-600
    public static final Color SUCCESS = new Color(16, 185, 129); // emerald-500
    public static final Color DANGER = new Color(220, 38, 38); // red-600
    public static final Color WARNING = new Color(217, 119, 6); // amber-600

    // Fontes
    public static final String FONT_TITLE = "font: bold +6"; // Título grande
    public static final String FONT_SUBTITLE = "font: bold +2";
    public static final String FONT_BODY = "font: 14";

    public static void aplicarEstiloFrame(JFrame frame) {
        frame.getContentPane().setBackground(BG_BACKGROUND);
        // Remove barra de título padrão se quiser algo customizado (opcional)
    }

    public static void aplicarEstiloDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(BG_BACKGROUND);
    }

    // Componentes Estilizados
    public static JPanel criarCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        // Sombra suave simulada com borda e client property do FlatLaf
        p.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12;" +
                        "border: 1,1,1,1, #E5E7EB;" + // Borda cinza clara
                        "background: #FFFFFF");
        return p;
    }

    public static JButton criarBotaoPrincipal(String texto, Icon icone) {
        JButton btn = new JButton(texto);
        if (icone != null)
            btn.setIcon(icone);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "background: #2563EB; foreground: #FFFFFF; " +
                        "borderWidth: 0; focusWidth: 0; arc: 8; " +
                        "font: bold 14; margin: 8,16,8,16");
        return btn;
    }

    public static JButton criarBotaoSecundario(String texto, Icon icone) {
        JButton btn = new JButton(texto);
        if (icone != null)
            btn.setIcon(icone);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #374151; " +
                        "borderColor: #D1D5DB; borderWidth: 1; focusWidth: 0; arc: 8; " +
                        "font: 14; margin: 8,16,8,16");
        return btn;
    }

    public static JButton criarBotaoPerigo(String texto, Icon icone) {
        JButton btn = new JButton(texto);
        if (icone != null)
            btn.setIcon(icone);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "background: #DC2626; foreground: #FFFFFF; " +
                        "borderWidth: 0; focusWidth: 0; arc: 8; " +
                        "font: bold 14; margin: 8,16,8,16");
        return btn;
    }

    public static JLabel criarTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.putClientProperty(FlatClientProperties.STYLE, FONT_TITLE);
        return lbl;
    }

    public static JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(new Color(55, 65, 81)); // gray-700
        lbl.putClientProperty(FlatClientProperties.STYLE, "font: semibold 13");
        return lbl;
    }

    public static JTextField criarInput() {
        JTextField txt = new JTextField();
        txt.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; borderColor: #D1D5DB; focusWidth: 2; focusColor: #3B82F6; margin: 6,10,6,10");
        return txt;
    }
}
