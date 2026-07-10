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

public class ConfiguracoesModerno extends JFrame {

    private JCheckBox checkLogoff;
    private JCheckBox checkEstoque;
    private JCheckBox checkPedidos;
    private JCheckBox checkLuz;
    private JTextField txtLimiteDesconto;
    private JComboBox<String> jComboBoxTelas;
    private JRadioButton botaoRF;
    private JRadioButton botaoBotoeira;
    private JButton botaoCodigos;
    private ButtonGroup portoesGroup;
    private JCheckBox checkImpressora;
    private JComboBox<String> jComboBoxImpressoras;
    private boolean isInitializing = false;

    public ConfiguracoesModerno() {
        initComponents();
        carregarDados();
    }

    private void initComponents() {
        setTitle("Configurações Adicionais");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 750);
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

        checkPedidos = new JCheckBox("Ativar Pedidos Online");
        checkPedidos.addActionListener(e -> salvarCheckPedidos());
        pnlGeral.add(checkPedidos, "wrap");

        checkLuz = new JCheckBox("Ativar Módulo de Luzes (Automação)");
        checkLuz.addActionListener(e -> salvarCheckLuz());
        pnlGeral.add(checkLuz, "wrap");

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

        // Panel for Printer Settings
        JPanel pnlImpressora = new JPanel(new MigLayout("fillx, insets 0", "[]10[grow]", "[]10[]"));
        pnlImpressora.setBorder(BorderFactory.createTitledBorder("Impressora de Cupom"));

        checkImpressora = new JCheckBox("Ativar Impressões automáticas");
        checkImpressora.addActionListener(e -> salvarCheckImpressora());
        pnlImpressora.add(checkImpressora, "span 2, wrap");

        pnlImpressora.add(new JLabel("Selecionar Impressora:"));
        jComboBoxImpressoras = new JComboBox<>();
        jComboBoxImpressoras.addItemListener(e -> {
            if (!isInitializing && e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                salvarImpressoraSelecionada();
            }
        });
        pnlImpressora.add(jComboBoxImpressoras, "growx");

        add(pnlImpressora, "growx, wrap");

        // Panel for Customization
        JPanel pnlPersonaliza = new JPanel(new MigLayout("fillx, insets 0", "[grow]10[grow]", "[]"));
        pnlPersonaliza.setBorder(BorderFactory.createTitledBorder("Personalização Wi-Fi"));

        JButton btnUploadWifi = new JButton("Carregar Imagem Pronta");
        btnUploadWifi.addActionListener(e -> selecionarImagemWifi());

        JButton btnGerarWifi = new JButton("Gerar Novo QR Code");
        btnGerarWifi.addActionListener(e -> new GerarQrCodeDialog(this).setVisible(true));

        pnlPersonaliza.add(btnUploadWifi, "growx");
        pnlPersonaliza.add(btnGerarWifi, "growx");

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
        checkPedidos.setSelected(config.isPedidosOnlineAtivo());
        checkLuz.setSelected(config.isLuzAtiva());

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

        // Impressora
        checkImpressora.setSelected(config.isImpressoraAtiva());
        jComboBoxImpressoras.removeAllItems();
        java.util.List<String> imps = com.motelinteligente.dados.ImpressoraService.listarImpressoras();
        for (String imp : imps) {
            jComboBoxImpressoras.addItem(imp);
        }
        if (config.getImpressoraNome() != null) {
            jComboBoxImpressoras.setSelectedItem(config.getImpressoraNome());
        }

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

    private void salvarCheckPedidos() {
        boolean selected = checkPedidos.isSelected();
        configGlobal.getInstance().setPedidosOnlineAtivo(selected);
        funcaoSet("pedidos_online", selected);
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

    private void salvarPortoes(boolean isRF) {
        botaoCodigos.setVisible(isRF);
        configGlobal.getInstance().setPortoesRF(isRF);
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

    private void salvarCheckImpressora() {
        if (isInitializing) return;
        boolean selected = checkImpressora.isSelected();
        configGlobal.getInstance().setImpressoraAtiva(selected);
        funcaoSet("impressora_ativa", selected);
    }

    private void salvarImpressoraSelecionada() {
        if (isInitializing) return;
        String imp = (String) jComboBoxImpressoras.getSelectedItem();
        if (imp != null) {
            configGlobal.getInstance().setImpressoraNome(imp);
            try (Connection link = new fazconexao().conectar();
                    PreparedStatement stmt = link.prepareStatement("UPDATE configuracoes SET impressora_nome = ?")) {
                stmt.setString(1, imp);
                stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar impressora: " + e.getMessage());
            }
        }
    }

    private void salvarCheckLuz() {
        if (isInitializing) return;
        boolean selected = checkLuz.isSelected();
        configGlobal.getInstance().setLuzAtiva(selected);
        funcaoSet("luz_ativa", selected);
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
