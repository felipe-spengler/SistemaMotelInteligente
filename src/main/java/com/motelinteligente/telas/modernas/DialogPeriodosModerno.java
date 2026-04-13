package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.PeriodoQuarto;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fquartos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class DialogPeriodosModerno extends JDialog {

    private int numeroQuarto;
    private JTable tabelaPeriodos;
    private DefaultTableModel modeloTabela;

    public DialogPeriodosModerno(JFrame parent, int numeroQuarto) {
        super(parent, "Períodos Dinâmicos - Quarto " + numeroQuarto, true);
        this.numeroQuarto = numeroQuarto;
        initUI();
        carregarPeriodos();
    }

    private void initUI() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setLayout(new MigLayout("fill", "[grow]", "[][grow][]"));

        JLabel lblTitulo = new JLabel("Configurar Períodos para Quarto: " + numeroQuarto);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(lblTitulo, "wrap");

        // Tabela
        String[] colunas = {"Ordem", "Descrição (Ex: 1 Hora)", "Minutos Limite", "Valor (R$)", "Pernoite?"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if(columnIndex == 0 || columnIndex == 2) return Integer.class;
                if(columnIndex == 3) return Float.class;
                if(columnIndex == 4) return Boolean.class;
                return String.class;
            }
        };

        tabelaPeriodos = new JTable(modeloTabela);
        tabelaPeriodos.setRowHeight(30);
        add(new JScrollPane(tabelaPeriodos), "grow, wrap");

        // Botões de Ação da Tabela
        JPanel pnlAcoesTabela = new JPanel(new MigLayout("insets 0"));
        JButton btnAddLinha = new JButton("+ Adicionar Período");
        JButton btnRemoverLinha = new JButton("- Remover Selecionado");

        btnAddLinha.addActionListener(e -> {
            int ordemSugerida = modeloTabela.getRowCount() + 1;
            modeloTabela.addRow(new Object[]{ordemSugerida, "Novo Período", 60, 50.0f, false});
        });

        btnRemoverLinha.addActionListener(e -> {
            int row = tabelaPeriodos.getSelectedRow();
            if (row != -1) {
                modeloTabela.removeRow(row);
            }
        });

        pnlAcoesTabela.add(btnAddLinha);
        pnlAcoesTabela.add(btnRemoverLinha);
        add(pnlAcoesTabela, "wrap");

        // Botão Salvar
        JButton btnSalvar = new JButton("Salvar no Banco");
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalvar.setBackground(new Color(34, 197, 94));
        btnSalvar.setForeground(Color.WHITE);

        btnSalvar.addActionListener(e -> salvarPeriodosInternato());
        add(btnSalvar, "right");
    }

    private void carregarPeriodos() {
        modeloTabela.setRowCount(0);
        fquartos dao = new fquartos();
        List<PeriodoQuarto> periodos = dao.getPeriodos(numeroQuarto);
        if (periodos != null) {
            for (PeriodoQuarto p : periodos) {
                modeloTabela.addRow(new Object[]{
                        p.getOrdem(),
                        p.getDescricao(),
                        p.getTempoMinutos(),
                        p.getValor(),
                        p.isPernoite()
                });
            }
        }
    }

    private void salvarPeriodosInternato() {
        if (tabelaPeriodos.isEditing()) {
            tabelaPeriodos.getCellEditor().stopCellEditing();
        }

        try (Connection link = new fazconexao().conectar()) {
            link.setAutoCommit(false);
            
            // Delete old
            try (PreparedStatement del = link.prepareStatement("DELETE FROM periodos_quarto WHERE numeroquarto = ?")) {
                del.setInt(1, numeroQuarto);
                del.executeUpdate();
            }

            // Insert new
            String insertSql = "INSERT INTO periodos_quarto (numeroquarto, ordem, descricao, tempo_minutos, valor, is_pernoite) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ins = link.prepareStatement(insertSql)) {
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    ins.setInt(1, numeroQuarto);
                    ins.setInt(2, (Integer) modeloTabela.getValueAt(i, 0));
                    ins.setString(3, (String) modeloTabela.getValueAt(i, 1));
                    ins.setInt(4, (Integer) modeloTabela.getValueAt(i, 2));
                    ins.setFloat(5, (Float) modeloTabela.getValueAt(i, 3));
                    ins.setInt(6, (Boolean) modeloTabela.getValueAt(i, 4) ? 1 : 0);
                    ins.executeUpdate();
                }
            }

            link.commit();
            JOptionPane.showMessageDialog(this, "Períodos salvos com sucesso!");
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar períodos: " + ex.getMessage());
        }
    }
}
