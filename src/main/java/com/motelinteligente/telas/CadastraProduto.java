package com.motelinteligente.telas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vprodutos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

/**
 * Diálogo de Cadastro/Edição de Produto
 * Refatorado com MigLayout.
 */
public class CadastraProduto extends JDialog {

    private JTextField txtId;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JTextField txtEstoque;
    private boolean atualizar = false;

    public CadastraProduto(Frame parent, int idPassado) {
        super(parent, true); // Modal
        initUI();

        // Listener para atualizar a tela pai ao fechar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (parent instanceof Produto) {
                    ((Produto) parent).atualizarTela();
                }
            }
        });

        if (idPassado != 0) {
            carregarDados(idPassado);
        }
    }

    private void initUI() {
        setTitle("Cadastro de Produto");
        setSize(500, 350);
        setLocationRelativeTo(getParent());

        setLayout(new MigLayout("fillx, insets 20", "[right][grow, fill]", "[]10[]10[]10[]30[]"));

        JLabel lblTitle = new JLabel("Dados do Produto");
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        add(lblTitle, "span, center, wrap");

        add(new JLabel("ID (1-999):"));
        txtId = new JTextField();
        add(txtId, "wrap");

        add(new JLabel("Descrição:"));
        txtDescricao = new JTextField();
        add(txtDescricao, "wrap");

        add(new JLabel("Valor (R$):"));
        txtValor = new JTextField();
        add(txtValor, "wrap");

        add(new JLabel("Estoque:"));
        txtEstoque = new JTextField();
        txtEstoque.setText("-"); // Valor padrão
        txtEstoque.setToolTipText("Use '-' para produtos sem controle de estoque");
        add(txtEstoque, "wrap");

        add(new JLabel("<html><small>Dica: Para produtos sem controle de estoque, coloque ' - '</small></html>"),
                "skip, wrap");

        // Botões
        JPanel buttonPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]"));

        JButton btVoltar = new JButton("Voltar");
        btVoltar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_sair.png")));
        btVoltar.addActionListener(e -> dispose());

        JButton btSalvar = new JButton("Salvar");
        btSalvar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_bot_salvar.png")));
        btSalvar.putClientProperty(FlatClientProperties.STYLE, "semicolon;font:bold");
        btSalvar.addActionListener(e -> salvarProduto());

        buttonPanel.add(btVoltar, "growx");
        buttonPanel.add(btSalvar, "growx");

        add(buttonPanel, "span, growx");
    }

    private void carregarDados(int id) {
        vprodutos produto = new fprodutos().getProduto(id);
        if (produto != null) {
            txtId.setText(String.valueOf(id));
            txtDescricao.setText(produto.getDescricao());
            txtValor.setText(String.valueOf(produto.getValor()));
            txtEstoque.setText(String.valueOf(produto.getEstoque()));
            txtDescricao.requestFocus();
            atualizar = true; // Flag para indicar edição
            txtId.setEditable(false); // Não muda ID na edição
        }
    }

    private void salvarProduto() {
        int idProduto = 0;
        String descricao = txtDescricao.getText();
        float valor = 0;
        String estoque = txtEstoque.getText();

        // Validações
        if (descricao == null || descricao.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a Descrição!");
            txtDescricao.requestFocus();
            return;
        }

        try {
            idProduto = Integer.parseInt(txtId.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID inválido! Digite um número.");
            txtId.requestFocus();
            return;
        }

        try {
            valor = Float.parseFloat(txtValor.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido!");
            txtValor.requestFocus();
            return;
        }

        // Verifica existência (se não for atualização)
        fprodutos dao = new fprodutos();
        if (!atualizar && dao.verExiste(idProduto)) {
            int resp = JOptionPane.showConfirmDialog(this, "Este ID já existe. Deseja sobrescrever?", "Aviso",
                    JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) {
                return;
            }
            // Se SIM, virou atualização/sobrescrita
        }

        // Persistência
        Date dataAtual = new Date();
        java.sql.Timestamp data = new java.sql.Timestamp(dataAtual.getTime());

        // No sistema legado, update é feito deletando e inserindo de novo
        // Mantendo compatibilidade com a lógica original
        dao.exclusao(idProduto); // Remove anterior se existir

        vprodutos novo = new vprodutos(idProduto, descricao, valor, estoque, data);

        if (dao.insercao(novo)) {
            JOptionPane.showMessageDialog(this, "Produto salvo com sucesso!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao salvar produto.");
        }
    }

    public static void main(String[] args) {
        try {
            FlatIntelliJLaf.setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new CadastraProduto(null, 0).setVisible(true);
    }
}
