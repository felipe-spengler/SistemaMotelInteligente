package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vprodutos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

public class CadastraProdutoModerno extends JDialog {

    private JTextField txtId;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JTextField txtEstoque;
    private JCheckBox chkControlarEstoque;
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
        setSize(450, 500);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);

        JPanel mainPanel = new JPanel(
                new MigLayout("fillx, insets 30, wrap 1", "[grow, fill]", "[]20[]5[]15[]5[]15[]5[]15[]5[]30[]"));
        mainPanel.setBackground(EstiloModerno.BG_BACKGROUND); // Garante fundo correto

        mainPanel.add(EstiloModerno.criarTitulo("Detalhes do Produto"), "center");

        // Campos
        mainPanel.add(EstiloModerno.criarLabel("Código (ID)"));
        txtId = EstiloModerno.criarInput();
        mainPanel.add(txtId);

        mainPanel.add(EstiloModerno.criarLabel("Descrição / Nome"));
        txtDescricao = EstiloModerno.criarInput();
        mainPanel.add(txtDescricao);

        mainPanel.add(EstiloModerno.criarLabel("Valor Unitário (R$)"));
        txtValor = EstiloModerno.criarInput();
        mainPanel.add(txtValor);

        JPanel stockPanel = new JPanel(new MigLayout("insets 0", "[]10[grow]", "[]"));
        stockPanel.setOpaque(false);

        chkControlarEstoque = new JCheckBox("Controlar Estoque?");
        stockPanel.add(chkControlarEstoque);

        txtEstoque = EstiloModerno.criarInput();
        txtEstoque.setText("-");
        txtEstoque.setEnabled(false);
        stockPanel.add(txtEstoque, "growx");

        mainPanel.add(EstiloModerno.criarLabel("Estoque Disponível"));
        mainPanel.add(stockPanel, "growx");

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

        mainPanel.add(btnPanel, "growx, pushy, bottom");

        add(mainPanel);
    }

    private void carregarDados(int id) {
        vprodutos produto = new fprodutos().getProduto(id);
        if (produto != null) {
            txtId.setText(String.valueOf(id));
            txtDescricao.setText(produto.getDescricao());
            txtValor.setText(String.valueOf(produto.getValor()));
            txtEstoque.setText(String.valueOf(produto.getEstoque()));

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

        vprodutos novo = new vprodutos(idProduto, descricao, valor, estoque, data);
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
}
