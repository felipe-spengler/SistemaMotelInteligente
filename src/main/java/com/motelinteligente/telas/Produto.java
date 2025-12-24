package com.motelinteligente.telas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vprodutos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Tela de Listagem de Produtos
 * Refatorada para usar MigLayout e remover dependência do editor visual.
 */
public class Produto extends JFrame {

    private JTable tabela;
    private DefaultTableModel tableModel;
    private JButton btNovo;
    private JButton btEditar;
    private JButton btApagar;
    private JButton btVoltar;

    public Produto() {
        initUI();
        mostraJTableProduto();

        // Aplica permissões
        configGlobal config = configGlobal.getInstance();
        if ("comum".equals(config.getCargoUsuario())) {
            btEditar.setEnabled(false);
            btApagar.setEnabled(false);
            btNovo.setEnabled(false);
        }
    }

    private void initUI() {
        setTitle("Gerenciamento de Produtos");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Layout Principal
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // Título
        JLabel lblTitulo = new JLabel("Lista de Produtos");
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        add(lblTitulo, "wrap");

        // Tabela
        String[] cols = { "ID", "Descrição", "Valor (R$)", "Estoque", "Última Compra" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, "grow, wrap");

        // Barra de Botões
        JPanel buttonPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][][][][]", "[]"));

        btVoltar = new JButton("Voltar");
        btVoltar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_sair.png")));
        btVoltar.addActionListener(e -> dispose());

        btApagar = new JButton("Apagar");
        btApagar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png")));
        btApagar.addActionListener(e -> apagarProduto());

        btEditar = new JButton("Editar");
        btEditar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_backup.png")));
        btEditar.addActionListener(e -> editarProduto());

        btNovo = new JButton("Novo Produto");
        btNovo.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_bot_salvar.png")));
        btNovo.putClientProperty(FlatClientProperties.STYLE, "semicolon;font:bold");
        btNovo.addActionListener(e -> novoProduto());

        // Adicionando ao painel de botões (Voltar na esquerda, Ações na direita)
        buttonPanel.add(btVoltar, "growx"); // check alignment
        buttonPanel.add(btApagar, "growx");
        buttonPanel.add(btEditar, "growx");
        buttonPanel.add(btNovo, "growx");

        add(buttonPanel, "growx");
    }

    private void mostraJTableProduto() {
        tableModel.setRowCount(0);
        fprodutos prod = new fprodutos();

        for (vprodutos q : prod.mostrarProduto()) {
            tableModel.addRow(new Object[] {
                    q.getIdProduto(),
                    q.getDescricao(),
                    q.getValor(),
                    q.getEstoque(),
                    q.getDataCompra()
            });
        }
    }

    private void novoProduto() {
        CadastraProduto dialog = new CadastraProduto(this, 0);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editarProduto() {
        int row = tabela.getSelectedRow();
        if (row != -1) {
            Object idObj = tabela.getValueAt(row, 0);
            int idProduto = Integer.parseInt(idObj.toString());
            new CadastraProduto(this, idProduto).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto para editar.");
        }
    }

    private void apagarProduto() {
        int row = tabela.getSelectedRow();
        if (row != -1) {
            Object idObj = tabela.getValueAt(row, 0);
            int idProduto = Integer.parseInt(idObj.toString());

            int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja apagar este produto?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (new fprodutos().exclusao(idProduto)) {
                    JOptionPane.showMessageDialog(this, "Produto excluído com sucesso.");
                    mostraJTableProduto();
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir produto.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto para apagar.");
        }
    }

    /**
     * Método público para atualizar a tela (chamado pelo dialog filho)
     */
    public void atualizarTela() {
        mostraJTableProduto();
    }

    public static void main(String args[]) {
        try {
            FlatIntelliJLaf.setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(() -> {
            new Produto().setVisible(true);
        });
    }
}
