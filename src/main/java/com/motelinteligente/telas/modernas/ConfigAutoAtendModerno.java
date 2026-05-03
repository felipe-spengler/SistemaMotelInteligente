package com.motelinteligente.telas.modernas;

import com.formdev.flatlaf.FlatClientProperties;
import com.motelinteligente.dados.fazconexao;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ConfigAutoAtendModerno extends JFrame {

    private JTabbedPane tabbedPane;
    private JTextField txtPath;
    private JRadioButton radioCliente;
    private JRadioButton radioSistema;
    private JRadioButton subAtiva;
    private JRadioButton subDesativa;
    private JTable tabelaTipoQuarto;
    private JLabel labelFundoAbertura;
    private JLabel labelDirecionamento;
    private JLabel labelCabecalho;

    public ConfigAutoAtendModerno() {
        initComponents();
        carregarDados();
    }

    private void initComponents() {
        setTitle("Configuração Auto Atendimento");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill", "[grow]", "[][grow]"));

        // Header
        JLabel lblHeader = new JLabel("Auto Atendimento");
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        add(lblHeader, "center, wrap 10");

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("Configurações Gerais", criarPanelConfiguracoes());
        tabbedPane.addTab("Imagens", criarPanelImagens());

        add(tabbedPane, "grow");
    }

    private JPanel criarPanelConfiguracoes() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 20", "[grow]", "[][][][grow]"));

        // Path / Audio
        JPanel pnlAudio = new JPanel(new MigLayout("fillx, insets 10", "[][grow][]", "[]"));
        pnlAudio.setBorder(BorderFactory.createTitledBorder("Mensagem de Áudio"));
        pnlAudio.add(new JLabel("Caminho/Mensagem:"));
        txtPath = new JTextField();
        txtPath.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                salvarCaminhoAudio(txtPath.getText());
            }
        });
        pnlAudio.add(txtPath, "growx");
        JButton btnProcurar = new JButton("Procurar");
        btnProcurar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                txtPath.setText(f.getAbsolutePath());
                salvarCaminhoAudio(f.getAbsolutePath());
            }
        });
        pnlAudio.add(btnProcurar, "wrap");
        panel.add(pnlAudio, "growx, wrap 20");

        // Quarto Selection Logic
        JPanel pnlQuarto = new JPanel(new MigLayout("fillx, insets 10", "[]", "[]10[]5[]"));
        pnlQuarto.setBorder(BorderFactory.createTitledBorder("Seleção de Quarto"));
        pnlQuarto.add(new JLabel("Definir o número do Quarto após selecionar Tipo:"), "wrap");

        ButtonGroup grupoQuarto = new ButtonGroup();
        radioCliente = new JRadioButton("Cliente seleciona o número");
        radioSistema = new JRadioButton("Sistema define automaticamente");
        grupoQuarto.add(radioCliente);
        grupoQuarto.add(radioSistema);

        // Listeners for DB updates - logic was empty in original code, so adding
        // placeholders
        radioCliente.addActionListener(e -> salvarConfiguracaoQuarto(true));
        radioSistema.addActionListener(e -> salvarConfiguracaoQuarto(false));

        pnlQuarto.add(radioCliente, "wrap");
        pnlQuarto.add(radioSistema, "wrap");
        panel.add(pnlQuarto, "growx, wrap 20");

        // Sub Screen Logic
        JPanel pnlSub = new JPanel(new MigLayout("fillx, insets 10", "[]", "[]10[]5[]"));
        pnlSub.setBorder(BorderFactory.createTitledBorder("Subtela de Confirmação (Período/Pernoite)"));

        ButtonGroup grupoSub = new ButtonGroup();
        subAtiva = new JRadioButton("Ativado");
        subDesativa = new JRadioButton("Desativado");
        grupoSub.add(subAtiva);
        grupoSub.add(subDesativa);

        subAtiva.addActionListener(e -> salvarConfiguracaoSubtela(true));
        subDesativa.addActionListener(e -> salvarConfiguracaoSubtela(false));

        pnlSub.add(subAtiva, "wrap");
        pnlSub.add(subDesativa, "wrap");
        panel.add(pnlSub, "growx");

        return panel;
    }

    private JPanel criarPanelImagens() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow][grow][grow]", "[grow][]"));

        // 1. Fundo Abertura
        JPanel pnl1 = criarPainelImagemIndividual("Fundo Tela ENTRAR", "fundoAbertura", l -> labelFundoAbertura = l,
                this::carregarFundoAbertura);
        panel.add(pnl1, "grow");

        // 2. Fundo Direcionamento
        JPanel pnl2 = criarPainelImagemIndividual("Fundo Tela INDICAR QUARTO", "direcionamento",
                l -> labelDirecionamento = l, this::carregarDirecionamento);
        panel.add(pnl2, "grow");

        // 3. Cabeçalho
        JPanel pnl3 = criarPainelImagemIndividual("Cabeçalho", "cabecalho", l -> labelCabecalho = l,
                this::carregarCabecalho);
        panel.add(pnl3, "grow, wrap 20");

        // Tabela tipos de quarto
        JPanel pnlTabela = new JPanel(new MigLayout("fill", "[grow]20[200!]", "[grow]"));
        pnlTabela.setBorder(BorderFactory.createTitledBorder("Imagens por Tipo de Quarto"));

        tabelaTipoQuarto = new JTable(new DefaultTableModel(new Object[] { "Tipo Quarto" }, 0));
        tabelaTipoQuarto.setRowHeight(30);
        JScrollPane scroll = new JScrollPane(tabelaTipoQuarto);
        pnlTabela.add(scroll, "grow");

        // Actions for Table
        JPanel pnlActions = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]10[]20[][]"));

        JButton btnAlterar = new JButton("Alterar Imagem");
        btnAlterar.addActionListener(e -> alterarImagemTabela());
        pnlActions.add(btnAlterar, "growx, wrap");

        JButton btnVer = new JButton("Ver Imagem");
        btnVer.addActionListener(e -> verImagemTabela());
        pnlActions.add(btnVer, "growx, wrap");

        pnlActions.add(new JLabel(
                "<html><div style='width:10px;height:10px;background-color:green;display:inline-block'></div> Com Imagem</html>"),
                "wrap");
        pnlActions.add(new JLabel(
                "<html><div style='width:10px;height:10px;background-color:red;display:inline-block'></div> Sem Imagem</html>"),
                "wrap");

        pnlTabela.add(pnlActions, "top");

        panel.add(pnlTabela, "span 3, grow");

        return panel;
    }

    private JPanel criarPainelImagemIndividual(String titulo, String key,
            java.util.function.Consumer<JLabel> labelSetter, Runnable actionLoad) {
        JPanel p = new JPanel(new MigLayout("fill, insets 10", "[center]", "[center]10[]"));
        p.setBorder(BorderFactory.createEtchedBorder());

        JLabel lblImg = new JLabel("Carregando...");
        lblImg.setPreferredSize(new Dimension(200, 200));
        lblImg.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        labelSetter.accept(lblImg); // Export reference

        p.add(lblImg, "grow, wrap");

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        p.add(lblTitle, "wrap");

        JButton btnMudar = new JButton("Mudar");
        btnMudar.addActionListener(e -> {
            funcaoAlteraImagem(key);
            actionLoad.run();
        });
        p.add(btnMudar);

        return p;
    }

    private void carregarDados() {
        carregarFundoAbertura();
        carregarDirecionamento();
        carregarCabecalho();
        carregarTabela();

        // Load from configGlobal
        com.motelinteligente.dados.configGlobal config = com.motelinteligente.dados.configGlobal.getInstance();
        config.carregarConfiguracoesAdicionais(); // Refresh from DB

        txtPath.setText(config.getCaminhoAudio());

        if (config.isClienteSeleciona()) {
            radioCliente.setSelected(true);
        } else {
            radioSistema.setSelected(true);
        }

        if (config.isSubtelaAtiva()) {
            subAtiva.setSelected(true);
        } else {
            subDesativa.setSelected(true);
        }
    }

    // --- Logic copied and adapted from ConfigAutoAtend.java ---

    private void salvarConfiguracaoQuarto(boolean clienteSeleciona) {
        com.motelinteligente.dados.configGlobal config = com.motelinteligente.dados.configGlobal.getInstance();
        config.setClienteSeleciona(clienteSeleciona);

        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement("UPDATE configuracoes SET clienteSeleciona = ?")) {
            stmt.setBoolean(1, clienteSeleciona);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar configuração: " + e.getMessage());
        }
    }

    private void salvarConfiguracaoSubtela(boolean ativa) {
        com.motelinteligente.dados.configGlobal config = com.motelinteligente.dados.configGlobal.getInstance();
        config.setSubtelaAtiva(ativa);

        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement("UPDATE configuracoes SET subtelaAtiva = ?")) {
            stmt.setBoolean(1, ativa);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar configuração: " + e.getMessage());
        }
    }

    private void salvarCaminhoAudio(String path) {
        com.motelinteligente.dados.configGlobal config = com.motelinteligente.dados.configGlobal.getInstance();
        config.setCaminhoAudio(path);

        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement("UPDATE configuracoes SET caminhoAudio = ?")) {
            stmt.setString(1, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // JOptionPane.showMessageDialog(this, "Erro ao salvar audio: " +
            // e.getMessage());
        }
    }

    private void carregarFundoAbertura() {
        carregarImagemLabel(labelFundoAbertura, "fundoAbertura", 296, 296);
    }

    private void carregarDirecionamento() {
        carregarImagemLabel(labelDirecionamento, "direcionamento", 296, 296);
    }

    private void carregarCabecalho() {
        carregarImagemLabel(labelCabecalho, "cabecalho", 292, 292);
    }

    private void carregarImagemLabel(JLabel label, String key, int w, int h) {
        if (temImagem(key) != 0) {
            ImageIcon icon = obterImagemDoBanco(key);
            if (icon != null) {
                label.setText("");
                label.setIcon(redimensionarIcon(icon, w, h));
            }
        } else {
            label.setText("Nenhuma Imagem");
            label.setIcon(null);
        }
    }

    public void carregarTabela() {
        DefaultTableModel modelo = (DefaultTableModel) tabelaTipoQuarto.getModel();
        modelo.setRowCount(0);

        try (Connection link = new fazconexao().conectar();
                PreparedStatement statement = link
                        .prepareStatement("select tipoquarto from quartos order by numeroquarto");
                ResultSet resultado = statement.executeQuery()) {

            Set<String> tiposAdicionados = new HashSet<>();

            // Custom Renderer
            tabelaTipoQuarto.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    String tipo = (String) value;
                    if (temImagem(tipo) != 0) {
                        c.setBackground(new Color(200, 255, 200)); // Light Green
                    } else {
                        c.setBackground(new Color(255, 200, 200)); // Light Red
                    }
                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                    }
                    return c;
                }
            });

            while (resultado.next()) {
                String tipo = resultado.getString("tipoquarto");
                if (!tiposAdicionados.contains(tipo)) {
                    modelo.addRow(new String[] { tipo });
                    tiposAdicionados.add(tipo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void alterarImagemTabela() {
        int row = tabelaTipoQuarto.getSelectedRow();
        if (row != -1) {
            String tipo = (String) tabelaTipoQuarto.getValueAt(row, 0);
            funcaoAlteraImagem(tipo);
            carregarTabela();
        }
    }

    private void verImagemTabela() {
        int row = tabelaTipoQuarto.getSelectedRow();
        if (row != -1) {
            String tipo = (String) tabelaTipoQuarto.getValueAt(row, 0);
            exibirImagem(tipo);
        }
    }

    // --- DB Helpers ---

    private void funcaoAlteraImagem(String nomeArquivo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione uma imagem");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try {
                byte[] bytes = Files.readAllBytes(arquivo.toPath());

                try (Connection conn = new fazconexao().conectar()) {
                    // Remove existing
                    try (PreparedStatement delStmt = conn
                            .prepareStatement("DELETE FROM imagens WHERE nome_da_imagem = ?")) {
                        delStmt.setString(1, nomeArquivo);
                        delStmt.executeUpdate();
                    }
                    // Insert new
                    try (PreparedStatement insStmt = conn.prepareStatement(
                            "INSERT INTO imagens (nome_da_imagem, imagem, data_de_armazenamento) VALUES (?, ?, ?)")) {
                        insStmt.setString(1, nomeArquivo);
                        insStmt.setBytes(2, bytes);
                        insStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                        insStmt.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(this, "Imagem atualizada!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            }
        }
    }

    private int temImagem(String nome) {
        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT COUNT(*) AS total FROM imagens WHERE nome_da_imagem = ?")) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private ImageIcon obterImagemDoBanco(String nome) {
        try (Connection conn = new fazconexao().conectar();
                PreparedStatement stmt = conn.prepareStatement("SELECT imagem FROM imagens WHERE nome_da_imagem = ?")) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] bytes = rs.getBytes("imagem");
                    if (bytes != null)
                        return new ImageIcon(bytes);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void exibirImagem(String tipo) {
        ImageIcon icon = obterImagemDoBanco(tipo);
        if (icon != null) {
            JDialog d = new JDialog(this, "Imagem: " + tipo, true);
            d.add(new JScrollPane(new JLabel(icon)));
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Nenhuma imagem encontrada.");
        }
    }

    private ImageIcon redimensionarIcon(ImageIcon originalIcon, int largura, int altura) {
        if (originalIcon == null)
            return null;
        Image img = originalIcon.getImage();
        BufferedImage buffered = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffered.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        BufferedImage resized = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resized.createGraphics();
        g2.drawImage(buffered, 0, 0, largura, altura, null);
        g2.dispose();

        return new ImageIcon(resized);
    }
}
