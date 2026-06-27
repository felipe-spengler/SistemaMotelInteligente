package com.motelinteligente.telas;

import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vprodutos;
import com.motelinteligente.telas.modernas.EstiloModerno;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class DialogBuscaProduto extends JDialog {

    private JTextField txtBusca;
    private JTable tabelaProdutos;
    private JSpinner spinnerQtd;
    private JButton btnInserir;
    private JButton btnCancelar;
    private DefaultTableModel tableModel;

    private List<vprodutos> todosProdutos;
    private List<vprodutos> produtosFiltrados;
    private int idProdutoSelecionado = -1;
    private int quantidadeSelecionada = 1;
    private boolean verificado = false;

    public DialogBuscaProduto(Window parent) {
        super(parent, "Buscar Produto", ModalityType.APPLICATION_MODAL);
        initComponents();
        carregarProdutos();
    }

    private void initComponents() {
        setSize(550, 450);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);
        setLayout(new MigLayout("fill, insets 15", "[grow]", "[][grow][]"));

        // Busca
        JPanel pnlBusca = new JPanel(new MigLayout("fillx, insets 0", "[]10[grow]", "[]"));
        pnlBusca.setOpaque(false);
        pnlBusca.add(EstiloModerno.criarLabel("Buscar:"));
        txtBusca = EstiloModerno.criarInput();
        txtBusca.putClientProperty("JTextField.placeholderText", "Digite a descrição do produto para buscar...");
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        
        txtBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (tabelaProdutos.getRowCount() > 0) {
                        tabelaProdutos.requestFocus();
                        tabelaProdutos.setRowSelectionInterval(0, 0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (tabelaProdutos.getRowCount() > 0) {
                        tabelaProdutos.setRowSelectionInterval(0, 0);
                        confirmarSelecao();
                    }
                }
            }
        });
        
        pnlBusca.add(txtBusca, "growx");
        add(pnlBusca, "growx, wrap");

        // Tabela
        tableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Descrição", "Valor Unitário", "Estoque"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaProdutos = new JTable(tableModel);
        tabelaProdutos.setRowHeight(25);
        tabelaProdutos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaProdutos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmarSelecao();
                }
            }
        });
        tabelaProdutos.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume(); // Previne ação padrão do Enter no JTable
                    confirmarSelecao();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabelaProdutos);
        add(scroll, "grow, wrap");

        // Controles de Inserção
        JPanel pnlControles = new JPanel(new MigLayout("fillx, insets 0", "[]10[100!]push[]10[]", "[]"));
        pnlControles.setOpaque(false);

        pnlControles.add(EstiloModerno.criarLabel("Qtd:"));
        spinnerQtd = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        pnlControles.add(spinnerQtd, "width 70!");

        btnCancelar = EstiloModerno.criarBotaoSecundario("Cancelar", null);
        btnCancelar.addActionListener(e -> dispose());
        pnlControles.add(btnCancelar);

        btnInserir = EstiloModerno.criarBotaoPrincipal("Inserir", null);
        btnInserir.addActionListener(e -> confirmarSelecao());
        pnlControles.add(btnInserir);

        add(pnlControles, "growx");
    }

    private void carregarProdutos() {
        todosProdutos = new fprodutos().mostrarProduto();
        produtosFiltrados = new ArrayList<>(todosProdutos);
        atualizarTabela();
    }

    private void filtrar() {
        String query = txtBusca.getText().toLowerCase().trim();
        produtosFiltrados.clear();
        for (vprodutos p : todosProdutos) {
            if (p.getDescricao().toLowerCase().contains(query)) {
                produtosFiltrados.add(p);
            }
        }
        atualizarTabela();
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);
        for (vprodutos p : produtosFiltrados) {
            tableModel.addRow(new Object[]{
                    p.getIdProduto(),
                    p.getDescricao(),
                    p.getValor(),
                    p.getEstoque()
            });
        }
    }

    private void confirmarSelecao() {
        int selectedRow = tabelaProdutos.getSelectedRow();
        if (selectedRow >= 0) {
            idProdutoSelecionado = (Integer) tableModel.getValueAt(selectedRow, 0);
            quantidadeSelecionada = (Integer) spinnerQtd.getValue();
            verificado = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um produto da tabela!");
        }
    }

    public boolean isConfirmado() {
        return verificado;
    }

    public int getIdProdutoSelecionado() {
        return idProdutoSelecionado;
    }

    public int getQuantidadeSelecionada() {
        return quantidadeSelecionada;
    }
}
