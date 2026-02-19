package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class GerarQrCodeDialog extends JDialog {

    private JTextField txtWifiRede;
    private JTextField txtWifiSenha;
    private JComboBox<String> cmbSeguranca;
    private JButton btnGerar;
    private JButton btnCancelar;

    public GerarQrCodeDialog(Window parent) {
        super(parent, "Gerador de QR Code Wi-Fi", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(400, 500);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);

        JPanel mainPanel = new JPanel(
                new MigLayout("fillx, insets 30, wrap 1", "[grow, fill]", "[]20[]5[]15[]5[]15[]5[]30[]"));
        mainPanel.setBackground(Color.WHITE);

        // Título e Ícone
        JLabel lblIcon = new JLabel("📶");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        mainPanel.add(lblIcon, "center");

        JLabel lblTitle = new JLabel("Configurar Wi-Fi");
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        mainPanel.add(lblTitle, "center, gapbottom 20");

        // Nome da Rede
        mainPanel.add(EstiloModerno.criarLabel("Nome da Rede (SSID)"));
        txtWifiRede = EstiloModerno.criarInput();
        txtWifiRede.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Motel Wifi Cliente");
        mainPanel.add(txtWifiRede);

        // Segurança
        mainPanel.add(EstiloModerno.criarLabel("Tipo de Segurança"));
        String[] segurancaOptions = { "WPA/WPA2 (Padrão)", "WEP (Antigo)", "Aberta (Sem Senha)" };
        cmbSeguranca = new JComboBox<>(segurancaOptions);
        cmbSeguranca.putClientProperty("FlatLaf.style", "font: 14");
        mainPanel.add(cmbSeguranca);

        // Senha
        mainPanel.add(EstiloModerno.criarLabel("Senha da Rede"));
        txtWifiSenha = EstiloModerno.criarInput(); // Poderia ser JPasswordField, mas muitas vezes querem ver o wifi
        mainPanel.add(txtWifiSenha);

        // Listener para desabilitar senha se for aberta
        cmbSeguranca.addActionListener(e -> {
            boolean isAberta = cmbSeguranca.getSelectedIndex() == 2;
            txtWifiSenha.setEnabled(!isAberta);
            if (isAberta)
                txtWifiSenha.setText("");
        });

        // Botões
        JPanel btnPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]10[grow]", "[]"));
        btnPanel.setOpaque(false);

        btnCancelar = EstiloModerno.criarBotaoSecundario("Cancelar", null);
        btnCancelar.addActionListener(e -> dispose());

        btnGerar = EstiloModerno.criarBotaoPrincipal("Gerar QR Code", null);
        btnGerar.addActionListener(e -> gerar());

        btnPanel.add(btnCancelar, "growx");
        btnPanel.add(btnGerar, "growx");

        mainPanel.add(btnPanel, "growx, pushy, bottom");

        add(mainPanel);
    }

    private void gerar() {
        String rede = txtWifiRede.getText().trim();
        String senha = txtWifiSenha.getText().trim();
        int segIndex = cmbSeguranca.getSelectedIndex();

        // Tradução do Tipo
        String securityType = "WPA";
        if (segIndex == 1)
            securityType = "WEP";
        if (segIndex == 2)
            securityType = "nopass";

        if (rede.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha o Nome da Rede!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!securityType.equals("nopass") && senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha a Senha!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            gerarSalvarQrCode(rede, senha, securityType);
            JOptionPane.showMessageDialog(this, "QR Code gerado e salvo com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar QR Code: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void gerarSalvarQrCode(String ssid, String password, String securityType) throws Exception {
        // Lógica idêntica ao que estava no ConfiguracoesModerno
        String wifiData;
        if ("nopass".equals(securityType)) {
            wifiData = "WIFI:S:" + ssid + ";T:nopass;;";
        } else {
            wifiData = "WIFI:S:" + ssid + ";T:" + securityType + ";P:" + password + ";;";
        }

        int qrSize = 400;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(wifiData, BarcodeFormat.QR_CODE, qrSize, qrSize);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        int totalHeight = qrSize + 150;
        BufferedImage combined = new BufferedImage(qrSize, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combined.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, qrSize, totalHeight);

        g.drawImage(qrImage, 0, 0, null);

        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        String textRede = "Rede: " + ssid;
        int xRede = (qrSize - g.getFontMetrics().stringWidth(textRede)) / 2;
        g.drawString(textRede, xRede, qrSize + 40);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        String textSenha;
        if ("nopass".equals(securityType)) {
            textSenha = "Rede Aberta (Sem Senha)";
            g.setColor(new Color(0, 100, 0));
        } else {
            textSenha = "Senha: " + password;
        }

        int xSenha = (qrSize - g.getFontMetrics().stringWidth(textSenha)) / 2;
        g.drawString(textSenha, xSenha, qrSize + 80);

        g.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        g.setColor(Color.GRAY);
        String rodape = "Aponte a câmera do celular";
        int xRodape = (qrSize - g.getFontMetrics().stringWidth(rodape)) / 2;
        g.drawString(rodape, xRodape, qrSize + 120);

        g.dispose();

        String userHome = System.getProperty("user.home");
        File outputFile = new File(userHome, "Documents/logs/wifi_custom.jpg");
        outputFile.getParentFile().mkdirs();
        ImageIO.write(combined, "jpg", outputFile);
    }
}
