package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.telas.CodigosPortoes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.google.zxing.BarcodeFormat;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ConfiguracoesModerno extends JFrame {

    private JCheckBox checkLogoff;
    private JCheckBox checkEstoque;
    private JTextField txtLimiteDesconto;
    private JComboBox<String> jComboBoxTelas;
    private JRadioButton botaoRF;
    private JRadioButton botaoBotoeira;
    private JButton botaoCodigos;
    private ButtonGroup portoesGroup;
    private String telaMostrar;
    private boolean isInitializing = false;

    public ConfiguracoesModerno() {
        initComponents();
        carregarDados();
    }

    private void initComponents() {
        setTitle("Configurações Adicionais");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][][][][grow]"));

        // Header
        JLabel lblHeader = new JLabel("Configurações");
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        add(lblHeader, "center, wrap 20");

        // Panel for General Settings
        JPanel pnlGeral = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]10[]10[]"));
        pnlGeral.setBorder(BorderFactory.createTitledBorder("Geral"));

        checkLogoff = new JCheckBox("Obrigar Login ao Fechar Caixa");
        checkLogoff.addActionListener(e -> salvarCheckLogoff());
        pnlGeral.add(checkLogoff, "wrap");

        checkEstoque = new JCheckBox("Controlar Estoque");
        checkEstoque.addActionListener(e -> salvarCheckEstoque());
        pnlGeral.add(checkEstoque, "wrap");

        // Discount
        JPanel pnlDesconto = new JPanel(new MigLayout("insets 0", "[]10[]5[]"));
        pnlDesconto.add(new JLabel("Limita Desconto Colaborador:"));
        txtLimiteDesconto = new JTextField("0", 5);
        txtLimiteDesconto.setDocument(new NumOnly());
        txtLimiteDesconto.addActionListener(e -> salvarLimiteDesconto());
        pnlDesconto.add(txtLimiteDesconto);
        pnlDesconto.add(new JLabel("%"));
        pnlGeral.add(pnlDesconto, "wrap");

        JLabel lblObs = new JLabel("* Não se aplica a administradores e gerentes");
        lblObs.putClientProperty(FlatClientProperties.STYLE, "font: small; foreground: $Label.disabledForeground");
        pnlGeral.add(lblObs, "wrap");

        add(pnlGeral, "growx, wrap 20");

        // Panel for Screen Settings
        JPanel pnlTela = new JPanel(new MigLayout("fillx, insets 0", "[]10[grow]", "[]"));
        pnlTela.setBorder(BorderFactory.createTitledBorder("Tela Secundária"));
        pnlTela.add(new JLabel("Selecionar Tela:"));
        jComboBoxTelas = new JComboBox<>();
        jComboBoxTelas.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                salvarTelaSelecionada();
            }
        });
        pnlTela.add(jComboBoxTelas, "growx");

        add(pnlTela, "growx, wrap 20");

        // Panel for Gate Settings
        JPanel pnlPortoes = new JPanel(new MigLayout("fillx, insets 0", "[]20[]20[]", "[]10[]"));
        pnlPortoes.setBorder(BorderFactory.createTitledBorder("Abertura Portões"));

        portoesGroup = new ButtonGroup();
        botaoRF = new JRadioButton("Portões RF");
        botaoRF.addActionListener(e -> salvarPortoes(true));

        botaoBotoeira = new JRadioButton("Portões Botoeira");
        botaoBotoeira.addActionListener(e -> salvarPortoes(false));

        portoesGroup.add(botaoRF);
        portoesGroup.add(botaoBotoeira);

        pnlPortoes.add(botaoRF);
        pnlPortoes.add(botaoBotoeira, "wrap");

        botaoCodigos = new JButton("Códigos Portões");
        botaoCodigos.addActionListener(e -> new CodigosPortoes().setVisible(true));
        pnlPortoes.add(botaoCodigos, "span, growx"); // Span across columns

        add(pnlPortoes, "growx, wrap");

        // Panel for Customization
        // Layout principal com 2 colunas para separar Upload e Gerador
        JPanel pnlPersonaliza = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]"));
        pnlPersonaliza.setBorder(BorderFactory.createTitledBorder("Personalização Wi-Fi"));

        // === Sub-painel 1: Upload Manual ===
        JPanel pnlUpload = new JPanel(new MigLayout("insets 5", "[]10[]"));
        pnlUpload.setBorder(BorderFactory.createTitledBorder("Opção A: Upload de Imagem Pronta"));

        JButton btnUploadWifi = new JButton("Carregar Imagem");
        btnUploadWifi.addActionListener(e -> selecionarImagemWifi());
        pnlUpload.add(new JLabel("Selecionar arquivo .JPG/.PNG:"));
        pnlUpload.add(btnUploadWifi);

        // === Sub-painel 2: Gerador Automático ===
        JPanel pnlGerador = new JPanel(new MigLayout("fillx, insets 5", "[right]10[grow]", "[]10[]10[]10[]10[]"));
        pnlGerador.setBorder(BorderFactory.createTitledBorder("Opção B: Gerar Novo QR Code"));

        JTextField txtWifiRede = new JTextField(15);
        JTextField txtWifiSenha = new JTextField(15);

        // Combo de Segurança
        String[] segurancaOptions = { "WPA/WPA2 (Padrão)", "WEP (Antigo)", "Aberta (Sem Senha)" };
        JComboBox<String> cmbSeguranca = new JComboBox<>(segurancaOptions);

        // Listener para desabilitar senha se for Aberta
        cmbSeguranca.addActionListener(e -> {
            boolean isAberta = cmbSeguranca.getSelectedIndex() == 2;
            txtWifiSenha.setEnabled(!isAberta);
            if (isAberta)
                txtWifiSenha.setText("");
        });

        JButton btnGerarWifi = new JButton("Gerar e Salvar");

        btnGerarWifi.addActionListener(e -> {
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
                // Chama o método auxiliar ajustado
                gerarSalvarQrCode(rede, senha, securityType);
                JOptionPane.showMessageDialog(this, "QR Code gerado e salvo com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar QR Code: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        pnlGerador.add(new JLabel("Nome da Rede (SSID):"));
        pnlGerador.add(txtWifiRede, "wrap");

        pnlGerador.add(new JLabel("Segurança:"));
        pnlGerador.add(cmbSeguranca, "wrap");

        pnlGerador.add(new JLabel("Senha:"));
        pnlGerador.add(txtWifiSenha, "wrap");
        pnlGerador.add(btnGerarWifi, "span, center");

        // Adiciona os sub-paineis ao painel de personalização
        pnlPersonaliza.add(pnlUpload, "growx, wrap");
        pnlPersonaliza.add(pnlGerador, "growx");

        add(pnlPersonaliza, "growx, wrap");
    }

    private void selecionarImagemWifi() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione a imagem do QR Code/Wi-Fi");
        fileChooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Imagens (JPG, PNG)", "jpg", "png", "jpeg"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToCopy = fileChooser.getSelectedFile();
            try {
                String userHome = System.getProperty("user.home");
                java.nio.file.Path destPath = java.nio.file.Paths.get(userHome, "Documents", "logs", "wifi_custom.jpg");

                // Cria diretórios se não existirem
                java.nio.file.Files.createDirectories(destPath.getParent());

                // Copia substituindo existente
                java.nio.file.Files.copy(fileToCopy.toPath(), destPath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                JOptionPane.showMessageDialog(this, "Imagem do Wi-Fi atualizada com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar imagem: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void carregarDados() {
        isInitializing = true;
        configGlobal config = configGlobal.getInstance();

        // Checkboxes
        checkLogoff.setSelected(config.getLogoffecharcaixa());
        checkEstoque.setSelected(config.getControlaEstoque());

        // Discount
        txtLimiteDesconto.setText(String.valueOf(config.getLimiteDesconto()));

        // Gates
        if (config.getPortoesRF()) {
            botaoRF.setSelected(true);
            botaoCodigos.setVisible(true);
        } else {
            botaoBotoeira.setSelected(true);
            botaoCodigos.setVisible(false);
        }

        // Load Screens
        carregarTelas();

        isInitializing = false;
    }

    private void carregarTelas() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        jComboBoxTelas.removeAllItems();

        for (GraphicsDevice device : devices) {
            jComboBoxTelas.addItem(device.getIDstring());
        }

        if (devices.length > 0) {
            jComboBoxTelas.setSelectedIndex(devices.length - 1);
            telaMostrar = devices[devices.length - 1].getIDstring();
        }
    }

    private void salvarCheckLogoff() {
        boolean selected = checkLogoff.isSelected();
        configGlobal.getInstance().setLogoffecharcaixa(selected);
        funcaoSet("logoffcaixa", selected);
    }

    private void salvarCheckEstoque() {
        boolean selected = checkEstoque.isSelected();
        configGlobal.getInstance().setControlaEstoque(selected);
        funcaoSet("estoque", selected);
    }

    private void salvarLimiteDesconto() {
        try {
            int valor = Integer.parseInt(txtLimiteDesconto.getText());

            try (Connection link = new fazconexao().conectar();
                    PreparedStatement stmt = link.prepareStatement("UPDATE configuracoes SET limitadesconto = ?")) {

                stmt.setInt(1, valor);
                if (stmt.executeUpdate() != 0) {
                    configGlobal.getInstance().setLimiteDesconto(valor);
                    JOptionPane.showMessageDialog(this, "Limite de Desconto Modificado com Sucesso!");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro BD: " + e.getMessage());
        }
    }

    private void salvarTelaSelecionada() {
        if (isInitializing)
            return;

        String tela = (String) jComboBoxTelas.getSelectedItem();
        JOptionPane.showMessageDialog(this, "Tela selecionada: " + tela);

        configGlobal.getInstance().setTelaMostrar(tela);
        setarTela(tela);
    }

    private void gerarSalvarQrCode(String ssid, String password, String securityType) throws Exception {
        // 1. String padrão Wi-Fi: WIFI:S:MyNetwork;T:WPA;P:mypass;;
        // Se for aberta, é WIFI:S:MyNetwork;T:nopass;;
        String wifiData;
        if ("nopass".equals(securityType)) {
            wifiData = "WIFI:S:" + ssid + ";T:nopass;;";
        } else {
            wifiData = "WIFI:S:" + ssid + ";T:" + securityType + ";P:" + password + ";;";
        }

        // 2. Cria o BitMatrix do QR Code (400x400)
        int qrSize = 400;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(wifiData, BarcodeFormat.QR_CODE, qrSize, qrSize);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // 3. Cria imagem maior para caber o texto (400x550) - Fundo Branco
        int totalHeight = qrSize + 150;
        BufferedImage combined = new BufferedImage(qrSize, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combined.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, qrSize, totalHeight);

        // 4. Desenha QR Code
        g.drawImage(qrImage, 0, 0, null);

        // 5. Configura Texto
        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Rede
        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        String textRede = "Rede: " + ssid;
        int xRede = (qrSize - g.getFontMetrics().stringWidth(textRede)) / 2;
        g.drawString(textRede, xRede, qrSize + 40);

        // Senha (ou aviso de aberta)
        g.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        String textSenha;
        if ("nopass".equals(securityType)) {
            textSenha = "Rede Aberta (Sem Senha)";
            g.setColor(new Color(0, 100, 0)); // Verde escuro para destacar
        } else {
            textSenha = "Senha: " + password;
        }

        int xSenha = (qrSize - g.getFontMetrics().stringWidth(textSenha)) / 2;
        g.drawString(textSenha, xSenha, qrSize + 80);

        // Rodapé
        g.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        g.setColor(Color.GRAY);
        String rodape = "Aponte a câmera do celular";
        int xRodape = (qrSize - g.getFontMetrics().stringWidth(rodape)) / 2;
        g.drawString(rodape, xRodape, qrSize + 120);

        g.dispose();

        // 6. Salva
        String userHome = System.getProperty("user.home");
        File outputFile = new File(userHome, "Documents/logs/wifi_custom.jpg");
        outputFile.getParentFile().mkdirs();
        ImageIO.write(combined, "jpg", outputFile);
    }

    private void salvarPortoes(boolean isRF) {
        botaoCodigos.setVisible(isRF);
        funcaoSet("portoesrf", isRF);
        // We don't need to manually verify botoes state as ButtonGroup handles visual
        // toggle, but we set DB.
    }

    // Database Helper Methods
    private void funcaoSet(String campo, boolean flag) {
        try (Connection link = new fazconexao().conectar();
                PreparedStatement stmt = link.prepareStatement("UPDATE configuracoes SET " + campo + " = ?")) {
            stmt.setBoolean(1, flag);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar configuração: " + e.getMessage());
        }
    }

    private void setarTela(String campo) {
        try (Connection link = new fazconexao().conectar();
                PreparedStatement stmt = link.prepareStatement("UPDATE configuracoes SET telaMostrar = ?")) {
            stmt.setString(1, campo);
            stmt.executeUpdate();
            // JOptionPane.showMessageDialog(this, "Tela atualizada com sucesso."); // Maybe
            // too annoying? Original had it.
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar tela: " + e.getMessage());
        }
    }

    // Number Only Filter
    private static class NumOnly extends PlainDocument {
        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str != null && str.chars().allMatch(Character::isDigit)) {
                super.insertString(offs, str, a);
            }
        }
    }
}
