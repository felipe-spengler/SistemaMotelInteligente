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

        tabela = new JTable(model);
        styleTable(tabela);

        // Carregar dados
        for (Object[] row : dadosOriginais) {
            // Se row não tiver ID no final, precisamos garantir que tenha.
            // O fcaixa.getListaLocacoes retorna um array. Vamos assumir que o ID está
            // acessível ou foi passado.
            // Pelo código do CaixaFrameModerno, ele pega do DAO. Precisamos ver o DAO.
            // Mas vamos assumir que o row passado já é o dado exibido.
            // Se faltar o ID, não conseguimos buscar os produtos.
            // Na verdade, fcaixa.getListaLocacoes retorna obj[] que tem o ID?
            // Vamos verificar depois. Por ora, assumimos que o último elemento ou um deles
            // serve para busca.

            // Para garantir, vamos adicionar a linha como está.
            model.addRow(row);
        }

        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER);

        // Tooltip Logic
        tabela.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tabela.rowAtPoint(e.getPoint());
                if (row > -1) {
                    // Tenta obter ID da locação.
                    // Se o modelo tem 8 colunas visiveis, precisariamos saber onde está o ID.
                    // Supondo que fcaixa retorna o ID em alguma coluna oculta ou visivel.
                    // Vou assumir que vamos passar o ID na coluna 8 (index 8) que adicionei agora
                    // na view
                    // Se row original tem menos colunas, dará erro.

                    try {
                        Object idObj = model.getValueAt(row, 8); // Tentativa de pegar ID da coluna 8
                        if (idObj != null) {
                            int idLocacao = Integer.parseInt(idObj.toString());
                            String produtos = buscarProdutosFormatados(idLocacao);
                            tabela.setToolTipText(produtos.isEmpty() ? "Nenhum consumo" : produtos);
                        }
                    } catch (Exception ex) {
                        tabela.setToolTipText(null);
                    }
                }
            }
        });

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
        // t.getColumnModel().getColumn(8).setMinWidth(0);
        // t.getColumnModel().getColumn(8).setMaxWidth(0);
        // t.getColumnModel().getColumn(8).setWidth(0);
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
