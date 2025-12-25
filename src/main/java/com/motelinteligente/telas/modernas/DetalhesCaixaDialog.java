package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.vendaProdutos;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;

public class DetalhesCaixaDialog extends JDialog {

    private JTable tabela;
    private DefaultTableModel model;
    private final DecimalFormat df = new DecimalFormat("R$ #,##0.00");
    private final List<Object[]> dadosOriginais;

    public DetalhesCaixaDialog(JFrame parent, List<Object[]> dados) {
        super(parent, "Detalhamento Completo de Locações", true);
        this.dadosOriginais = dados;
        initUI();
    }

    private void initUI() {
        setSize(1200, 800);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Cabeçalho
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(new Color(245, 245, 245));
        JLabel title = new JLabel("Histórico Completo de Locações");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // Tabela
        String[] cols = { "Entrada", "Saída", "Quarto", "V. Quarto", "V. Consumo", "Desc", "Acres", "Total", "ID" };
        // ID escondido ou na última coluna útil para buscar produtos

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(model) {
            @Override
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);

                if (rowIndex >= 0) {
                    try {
                        Object idObj = model.getValueAt(rowIndex, 8); // Tentativa de pegar ID da coluna 8
                        if (idObj != null) {
                            int idLocacao = Integer.parseInt(idObj.toString());
                            String produtos = buscarProdutosFormatados(idLocacao);
                            tip = produtos.isEmpty() ? "Nenhum consumo" : produtos;
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                return tip;
            }
        };
        styleTable(tabela);

        // Carregar dados
        for (Object[] row : dadosOriginais) {
            model.addRow(row);
        }

        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER);

        // Botão Fechar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        footer.add(btnFechar);
        add(footer, BorderLayout.SOUTH);
    }

    private void styleTable(JTable t) {
        t.setRowHeight(35);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        t.setDefaultRenderer(Object.class, centerRenderer);

        // Hide ID column if preferred, but keep it in model
        // Hide ID column
        t.getColumnModel().getColumn(8).setMinWidth(0);
        t.getColumnModel().getColumn(8).setMaxWidth(0);
        t.getColumnModel().getColumn(8).setWidth(0);
    }

    private String buscarProdutosFormatados(int idLocacao) {
        // Lógica para buscar produtos da locação
        vendaProdutos dao = new vendaProdutos();
        // O método vendidoLocacao retorna List<vendaProdutos>
        List<vendaProdutos> lista = dao.new gerenciaVenda().vendidoLocacao(idLocacao);

        if (lista.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder("<html><p style='width: 200px;'><b>Consumo:</b><br>");
        fprodutos fprod = new fprodutos();

        for (vendaProdutos vp : lista) {
            String nome = fprod.getDescicao(String.valueOf(vp.idProduto));
            sb.append(vp.quantidade).append("x ").append(nome).append("<br>");
        }
        sb.append("</p></html>");
        return sb.toString();
    }
}
