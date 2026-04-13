package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vprodutos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import javax.swing.SwingUtilities;
import java.awt.Cursor;

public class CadastraProdutoModerno extends JDialog {

    private JTextField txtId;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JTextField txtEstoque;
    private JCheckBox chkControlarEstoque;
    private JTextField txtCategoria;
    private JTextField txtImagem;
    private JTextArea txtDetalhes;
    private boolean atualizar = false;

    public CadastraProdutoModerno(Window parent, int idPassado) { // Aceita Window (JFrame ou JDialog)
        super(parent, ModalityType.APPLICATION_MODAL);
        initUI();

        if (idPassado != 0) {
            carregarDados(idPassado);
        }
    }

    private void initUI() {
        setTitle(atualizar ? "Editar Produto" : "Novo Produto");
        setSize(500, 650);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);

        JPanel pnlPrincipal = new JPanel(
                new MigLayout("fillx, insets 30, wrap 1", "[grow, fill]", "[]15[]5[]10[]5[]10[]5[]10[]5[]10[]5[]10[]5[]10[]5[]10[]5[grow]15[]10[]20[]"));
        pnlPrincipal.setBackground(EstiloModerno.BG_BACKGROUND); // Garante fundo correto

        pnlPrincipal.add(EstiloModerno.criarTitulo("Detalhes do Produto"), "center");

        // Campos
        pnlPrincipal.add(EstiloModerno.criarLabel("Código (ID)"));
        txtId = EstiloModerno.criarInput();
        pnlPrincipal.add(txtId);

        pnlPrincipal.add(EstiloModerno.criarLabel("Descrição / Nome"));
        txtDescricao = EstiloModerno.criarInput();
        pnlPrincipal.add(txtDescricao);

        pnlPrincipal.add(EstiloModerno.criarLabel("Valor Unitário (R$)"));
        txtValor = EstiloModerno.criarInput();
        pnlPrincipal.add(txtValor);

        pnlPrincipal.add(EstiloModerno.criarLabel("Categoria"));
        txtCategoria = EstiloModerno.criarInput();
        txtCategoria.setText("Diversos");
        pnlPrincipal.add(txtCategoria);

        pnlPrincipal.add(EstiloModerno.criarLabel("Imagem (URL ou Upload)"));
        JPanel imgPanel = new JPanel(new MigLayout("insets 0", "[grow]10[]", "[]"));
        imgPanel.setOpaque(false);
        txtImagem = EstiloModerno.criarInput();
        imgPanel.add(txtImagem, "growx");

        JButton btnUpload = EstiloModerno.criarBotaoPrincipal("Subir Foto", null);
        btnUpload.addActionListener(e -> selecionarFazerUpload());
        imgPanel.add(btnUpload);
        pnlPrincipal.add(imgPanel);

        pnlPrincipal.add(EstiloModerno.criarLabel("Descrição Detalhada / Detalhes"));
        txtDetalhes = new JTextArea(4, 20);
        txtDetalhes.setLineWrap(true);
        txtDetalhes.setWrapStyleWord(true);
        JScrollPane scrollDetalhes = new JScrollPane(txtDetalhes);
        pnlPrincipal.add(scrollDetalhes, "growx, height 80!");

        JPanel stockPanel = new JPanel(new MigLayout("insets 0", "[]10[grow]", "[]"));
        stockPanel.setOpaque(false);

        chkControlarEstoque = new JCheckBox("Controlar Estoque?");
        stockPanel.add(chkControlarEstoque);

        txtEstoque = EstiloModerno.criarInput();
        txtEstoque.setText("-");
        txtEstoque.setEnabled(false);
        stockPanel.add(txtEstoque, "growx");

        pnlPrincipal.add(EstiloModerno.criarLabel("Estoque Disponível"));
        pnlPrincipal.add(stockPanel, "growx");

        // Logic for Checkbox
        chkControlarEstoque.addActionListener(e -> {
            if (chkControlarEstoque.isSelected()) {
                txtEstoque.setEnabled(true);
                txtEstoque.setText("0");
                txtEstoque.requestFocus();
            } else {
                txtEstoque.setEnabled(false);
                txtEstoque.setText("-");
            }
        });

        // Botões
        JPanel btnPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]10[grow]", "[]"));
        btnPanel.setOpaque(false);

        JButton btnVoltar = EstiloModerno.criarBotaoSecundario("Cancelar", null);
        btnVoltar.addActionListener(e -> dispose());

        JButton btnSalvar = EstiloModerno.criarBotaoPrincipal(atualizar ? "Salvar Alterações" : "Cadastrar Produto",
                null);
        btnSalvar.addActionListener(e -> salvarProduto());

        btnPanel.add(btnVoltar, "growx");
        btnPanel.add(btnSalvar, "growx");

        pnlPrincipal.add(btnPanel, "growx, pushy, bottom");

        add(pnlPrincipal);
    }

    private void carregarDados(int id) {
        vprodutos produto = new fprodutos().getProduto(id);
        if (produto != null) {
            txtId.setText(String.valueOf(id));
            txtDescricao.setText(produto.getDescricao());
            txtValor.setText(String.valueOf(produto.getValor()));
            txtEstoque.setText(String.valueOf(produto.getEstoque()));
            txtCategoria.setText(produto.getCategoria() != null ? produto.getCategoria() : "Diversos");
            txtImagem.setText(produto.getImagem());
            txtDetalhes.setText(produto.getDetalhes());

            if (!produto.getEstoque().equals("-")) {
                chkControlarEstoque.setSelected(true);
                txtEstoque.setEnabled(true);
            } else {
                chkControlarEstoque.setSelected(false);
                txtEstoque.setEnabled(false);
            }

            txtId.setEditable(false);
            txtId.putClientProperty("FlatLaf.style", "background: #F3F4F6"); // Disabled look

            atualizar = true;
            setTitle("Editar Produto");
        }
    }

    private void salvarProduto() {
        // Lógica de salvamento idêntica à original, mas num corpo bonito
        int idProduto = 0;
        String descricao = txtDescricao.getText();
        float valor = 0;
        String estoque = txtEstoque.getText();
        String categoria = txtCategoria.getText();
        String imagem = txtImagem.getText();
        String detalhes = txtDetalhes.getText();

        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Descrição obrigatória");
            return;
        }

        try {
            idProduto = Integer.parseInt(txtId.getText());
            valor = Float.parseFloat(txtValor.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Verifique os campos numéricos (ID e Valor).");
            return;
        }

        fprodutos dao = new fprodutos();
        if (!atualizar && dao.verExiste(idProduto)) {
            if (JOptionPane.showConfirmDialog(this, "ID já existe. Sobrescrever?", "Atenção",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
        }

        Date dataAtual = new Date();
        java.sql.Timestamp data = new java.sql.Timestamp(dataAtual.getTime());
        dao.exclusao(idProduto); // Remove anterior (logica legado)

        vprodutos novo = new vprodutos(idProduto, descricao, valor, estoque, data, categoria, imagem, detalhes);
        if (dao.insercao(novo)) {
            JOptionPane.showMessageDialog(this, "Salvo com sucesso!");
            dispose();
            // Notifica pai se for ProdutoModerno
            Window parent = getOwner();
            if (parent instanceof ProdutoModerno) {
                ((ProdutoModerno) parent).atualizarTela();
            }
        }
    }
    private void selecionarFazerUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Foto do Produto");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", "jpg", "jpeg", "png", "webp"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Pergunta a URL do site (pode ser automatizado depois)
            String siteUrl = JOptionPane.showInputDialog(this, "Confirme a URL do Site (ex: http://motel.com.br):", "Configuração de Upload", JOptionPane.QUESTION_MESSAGE);
            if (siteUrl == null || siteUrl.isEmpty()) return;
            if (!siteUrl.endsWith("/")) siteUrl += "/";

            final String finalUrl = siteUrl + "api/upload_foto.php";
            
            // Mostra loading
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            new Thread(() -> {
                try {
                    String response = uploadImage(finalUrl, selectedFile);
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        if (response != null && response.contains("\"success\":true")) {
                            // Extrai URL do JSON (simplificado)
                            String url = response.split("\"url\":\"")[1].split("\"")[0];
                            txtImagem.setText(url);
                            JOptionPane.showMessageDialog(this, "Foto enviada e otimizada com sucesso!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Erro no upload: " + response);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        JOptionPane.showMessageDialog(this, "Erro técnico: " + ex.getMessage());
                    });
                }
            }).start();
        }
    }

    private String uploadImage(String targetURL, File file) throws Exception {
        String boundary = "---" + System.currentTimeMillis() + "---";
        URL url = new URL(targetURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true)) {
            
            writer.println("--" + boundary);
            writer.println("Content-Disposition: form-data; name=\"foto\"; filename=\"" + file.getName() + "\"");
            writer.println("Content-Type: image/jpeg");
            writer.println();
            writer.flush();

            Files.copy(file.toPath(), out);
            out.flush();
            
            writer.println();
            writer.println("--" + boundary + "--");
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
        }
        return response.toString();
    }
}
