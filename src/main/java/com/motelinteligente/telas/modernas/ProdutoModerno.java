package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vprodutos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ProdutoModerno extends JFrame {

    private JTable tabela;
    private DefaultTableModel tableModel;
    private JButton btNovo, btEditar, btApagar;

    public ProdutoModerno() {
        initUI();
        carregaTabela();
        aplicarPermissoes();
    }

    private void initUI() {
        setTitle("Catálogo de Produtos");
        setSize(900, 600);
        setLocationRelativeTo(null);
        EstiloModerno.aplicarEstiloFrame(this);

        JPanel main = new JPanel(new MigLayout("fill, insets 30", "[grow]", "[]20[grow][]"));
        main.setOpaque(false);

        // Header Flexível
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[]push[]"));
        header.setOpaque(false);

        JLabel lblTitulo = EstiloModerno.criarTitulo("Produtos & Serviços");
        header.add(lblTitulo);

        btNovo = EstiloModerno.criarBotaoPrincipal("Novo Produto",
                new ImageIcon(getClass().getResource("/imagens/icon_bot_salvar.png")));
        btNovo.addActionListener(e -> novoProduto());
        header.add(btNovo);

        main.add(header, "growx, wrap");

        // Tabela em Card
        JPanel cardTable = EstiloModerno.criarCard(); // Fundo branco arredondado
        cardTable.setLayout(new BorderLayout()); // Usa todo espaço do card

        String[] cols = { "ID", "Produto / Serviço", "Valor (R$)", "Estoque", "Última Compra" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(35);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(new Color(240, 240, 240));
        tabela.getTableHeader().putClientProperty("FlatLaf.style",
                "font:bold; background:#F9FAFB; border: 0,0,1,0, #E5E7EB");

        // Center the "Valor (R$)" column (index 2)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tabela.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        cardTable.add(scroll, BorderLayout.CENTER);

        main.add(cardTable, "grow, wrap");

        // Footer Actions
        JPanel footer = new JPanel(new MigLayout("insets 0, fillx", "push[][]"));
        footer.setOpaque(false);

        btEditar = EstiloModerno.criarBotaoSecundario("Editar Selecionado",
                new ImageIcon(getClass().getResource("/imagens/icon_backup.png")));
        btEditar.addActionListener(e -> editarProduto());

        btApagar = EstiloModerno.criarBotaoPerigo("Remover",
                new ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png")));
        btApagar.addActionListener(e -> apagarProduto());

        footer.add(btEditar);
        footer.add(btApagar);

        main.add(footer, "growx");

        add(main);
    }

    public void atualizarTela() {
        carregaTabela();
    }

    private void carregaTabela() {
        tableModel.setRowCount(0);
        for (vprodutos p : new fprodutos().mostrarProduto()) {
            tableModel.addRow(new Object[] {
                    p.getIdProduto(),
                    p.getDescricao(),
                    p.getValor(),
                    p.getEstoque(),
                    p.getDataCompra()
            });
        }
    }

    private void novoProduto() {
        new CadastraProdutoModerno(this, 0).setVisible(true);
    }

    private void editarProduto() {
        int row = tabela.getSelectedRow();
        if (row != -1) {
            int id = Integer.parseInt(tabela.getValueAt(row, 0).toString());
            new CadastraProdutoModerno(this, id).setVisible(true);
        }
    }

    private void apagarProduto() {
        int row = tabela.getSelectedRow();
        if (row != -1) {
            int id = Integer.parseInt(tabela.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Apagar este produto?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                new fprodutos().exclusao(id);
                carregaTabela();
            }
        }
    }

    private void aplicarPermissoes() {
        if ("comum".equals(configGlobal.getInstance().getCargoUsuario())) {
            btNovo.setEnabled(false);
            btEditar.setEnabled(false);
            btApagar.setEnabled(false);
        }
    }
}
