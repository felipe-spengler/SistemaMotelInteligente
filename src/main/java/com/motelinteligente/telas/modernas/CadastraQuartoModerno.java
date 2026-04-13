package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.vquartos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CadastraQuartoModerno extends JFrame {

    private JTable tabela;
    private DefaultTableModel tableModel;

    public CadastraQuartoModerno() {
        initUI();
        carregaTabela();
    }

    private void initUI() {
        setTitle("Gestão de Quartos");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        EstiloModerno.aplicarEstiloFrame(this);

        JPanel main = new JPanel(new MigLayout("fill, insets 25", "[grow]", "[]20[grow][]"));
        main.setBackground(new Color(243, 244, 246));
        main.setOpaque(true);

        // Header
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[]push[]"));
        header.setOpaque(false);
        header.add(EstiloModerno.criarTitulo("Quartos"), "split 2");
        header.add(new JLabel("Gerencie os quartos e valores do motel"), "gapleft 10");

        JButton btnNovo = EstiloModerno.criarBotaoPrincipal("Novo Quarto", null);
        btnNovo.addActionListener(e -> {
            new DialogEditaQuartoModerno(this, 0).setVisible(true);
            carregaTabela();
        });
        header.add(btnNovo, "right");
        main.add(header, "growx, wrap");

        // Listagem em Card
        JPanel cardTable = EstiloModerno.criarCard();
        cardTable.setLayout(new BorderLayout());

        String[] cols = { "Nº", "Tipo", "V. Período", "V. Pernoite", "V. Hora Adic.", "V. 3ª Pes" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setRowHeight(40);
        tabela.getTableHeader().putClientProperty("FlatLaf.style", "font:bold; background:#F3F4F6; border: 0,0,1,0, #E5E7EB");
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(new Color(240, 240, 240));

        // Center cell content
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        cardTable.add(scroll, BorderLayout.CENTER);
        main.add(cardTable, "grow, wrap");

        // Footer Actions
        JPanel actions = new JPanel(new MigLayout("insets 10 0 0 0, fillx", "push[][]"));
        actions.setOpaque(false);

        JButton btnEditar = EstiloModerno.criarBotaoSecundario("Editar Selecionado", 
                new ImageIcon(getClass().getResource("/imagens/icon_backup.png")));
        btnEditar.addActionListener(e -> {
            int row = tabela.getSelectedRow();
            if (row != -1) {
                int num = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                new DialogEditaQuartoModerno(this, num).setVisible(true);
                carregaTabela();
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um quarto na lista primeiro!");
            }
        });

        JButton btnExcluir = EstiloModerno.criarBotaoPerigo("Excluir", 
                new ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png")));
        btnExcluir.addActionListener(e -> excluirQuarto());

        actions.add(btnEditar);
        actions.add(btnExcluir);
        main.add(actions, "growx");

        add(main);
    }

    private void carregaTabela() {
        tableModel.setRowCount(0);
        fquartos dao = new fquartos();
        for (vquartos q : dao.mostrar()) {
            int numeroQuarto = q.getNumeroquarto();
            float valorHoraAdicional = dao.getAdicional(numeroQuarto);
            tableModel.addRow(new Object[] {
                    numeroQuarto,
                    q.getTipoquarto(),
                    q.getValorquarto(),
                    q.getPernoitequarto(),
                    valorHoraAdicional,
                    q.getAddPessoa()
            });
        }
    }

    private void excluirQuarto() {
        int row = tabela.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um quarto!");
            return;
        }

        int num = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "Tem certeza que deseja apagar o quarto " + num + "?", "Excluir",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new fquartos().exclusao(num);
            CacheDados.getInstancia().getCacheQuarto().remove(num);
            carregaTabela();
        }
    }
}
