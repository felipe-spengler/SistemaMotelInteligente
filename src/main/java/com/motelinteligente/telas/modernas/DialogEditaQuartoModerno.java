package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DialogEditaQuartoModerno extends JDialog {

    private JTextField txtNumero;
    private JTextField txtTipo;
    private JTextField txtHoraAdicional; 
    private JTextField txtPessoaAdicional; 
    
    private JTable tabelaPeriodos;
    private DefaultTableModel modeloTabela;
    
    private int quartoId = 0;
    private boolean isUpdate = false;

    public DialogEditaQuartoModerno(Window parent, int numeroQuarto) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.quartoId = numeroQuarto;
        this.isUpdate = (numeroQuarto != 0);
        
        initUI();

        if (isUpdate) {
            carregarDadosQuarto();
        }
    }

    private void initUI() {
        setTitle(isUpdate ? "Editar Quarto " + quartoId : "Novo Quarto");
        setSize(650, 800);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);

        JPanel main = new JPanel(new MigLayout("fillx, insets 25, wrap 2", "[grow][grow]", "[]20[]5[]15[]5[]15[]5[]20[]15[]30[]"));
        main.setBackground(EstiloModerno.BG_BACKGROUND);

        main.add(EstiloModerno.criarTitulo(isUpdate ? "Editar Quarto" : "Cadastrar Quarto"), "span 2, center, wrap");

        // Número e Tipo
        main.add(EstiloModerno.criarLabel("Número"), "w 100!");
        main.add(EstiloModerno.criarLabel("Tipo / Descrição"), "growx");

        txtNumero = EstiloModerno.criarInput();
        txtNumero.setEditable(!isUpdate);
        if(isUpdate) txtNumero.setText(String.valueOf(quartoId));
        main.add(txtNumero, "w 100!, h 35!");

        txtTipo = EstiloModerno.criarInput();
        main.add(txtTipo, "growx, wrap");

        // Adicionais
        main.add(EstiloModerno.criarLabel("Hora Extra (R$)"));
        main.add(EstiloModerno.criarLabel("Pessoa Extra (R$)"));

        txtHoraAdicional = EstiloModerno.criarInput();
        txtHoraAdicional.setText("20.0");
        main.add(txtHoraAdicional, "growx");

        txtPessoaAdicional = EstiloModerno.criarInput();
        txtPessoaAdicional.setText("20.0");
        main.add(txtPessoaAdicional, "growx, wrap");

        // --- TABELA DE PERÍODOS DINÂMICOS ---
        main.add(EstiloModerno.criarLabel("Configuração de Períodos Dinâmicos"), "span 2, gaptop 15, wrap");
        
        String[] colunas = {"Ordem", "Descrição", "Minutos", "Valor (R$)", "Pernoite?"};
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
        tabelaPeriodos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Ajustar larguras das colunas
        tabelaPeriodos.getColumnModel().getColumn(0).setPreferredWidth(45); 
        tabelaPeriodos.getColumnModel().getColumn(0).setMaxWidth(50);
        tabelaPeriodos.getColumnModel().getColumn(2).setPreferredWidth(70); 
        tabelaPeriodos.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabelaPeriodos.getColumnModel().getColumn(4).setPreferredWidth(70); 
        
        JScrollPane scrollPeriodos = new JScrollPane(tabelaPeriodos);
        main.add(scrollPeriodos, "span 2, grow, h 250:400:600, wrap");

        JPanel pnlBotoesTabela = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBotoesTabela.setOpaque(false);
        JButton btnAdd = EstiloModerno.criarBotaoSecundario("+ Adicionar", null);
        JButton btnDefault = EstiloModerno.criarBotaoSecundario("Gerar Padrão", null);
        JButton btnRem = EstiloModerno.criarBotaoPerigo("Remover", null);
        
        btnAdd.addActionListener(e -> {
            int ordem = modeloTabela.getRowCount() + 1;
            modeloTabela.addRow(new Object[]{ordem, "2 Horas", 120, 0.0f, false});
        });
        
        btnDefault.addActionListener(e -> {
            if (modeloTabela.getRowCount() > 0) {
                int opt = JOptionPane.showConfirmDialog(this, "Isso irá limpar os períodos atuais e gerar o padrão (2h e Pernoite). Continuar?", "Gerar Padrão", JOptionPane.YES_NO_OPTION);
                if (opt != JOptionPane.YES_OPTION) return;
            }
            modeloTabela.setRowCount(0);
            modeloTabela.addRow(new Object[]{1, "2 Horas", 120, 0.0f, false});
            modeloTabela.addRow(new Object[]{2, "Pernoite", 720, 0.0f, true});
        });
        
        btnRem.addActionListener(e -> {
            int row = tabelaPeriodos.getSelectedRow();
            if(row != -1) {
                modeloTabela.removeRow(row);
                // Reordenar
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    modeloTabela.setValueAt(i + 1, i, 0);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um período na tabela para remover.");
            }
        });
        
        pnlBotoesTabela.add(btnAdd);
        pnlBotoesTabela.add(btnDefault);
        pnlBotoesTabela.add(btnRem);
        main.add(pnlBotoesTabela, "span 2, wrap");

        // Ações
        JButton btnSalvar = EstiloModerno.criarBotaoPrincipal("Salvar Dados", null);
        btnSalvar.addActionListener(e -> salvar());
        
        JButton btnCancelar = EstiloModerno.criarBotaoSecundario("Cancelar", null);
        btnCancelar.addActionListener(e -> dispose());

        main.add(btnSalvar, "growx, h 45!");
        main.add(btnCancelar, "growx, h 45!");

        add(main);
    }

    private void carregarDadosQuarto() {
        fquartos dao = new fquartos();
        // Pegar vquartos
        for(vquartos v : dao.mostrar()) {
            if(v.getNumeroquarto() == quartoId) {
                txtTipo.setText(v.getTipoquarto());
                txtPessoaAdicional.setText(String.valueOf(v.getAddPessoa()));
                break;
            }
        }
        txtHoraAdicional.setText(String.valueOf(dao.getAdicional(quartoId)));

        // Carregar períodos na tabela
        modeloTabela.setRowCount(0);
        List<PeriodoQuarto> periodos = dao.getPeriodos(quartoId);
        if (periodos != null && !periodos.isEmpty()) {
            for (PeriodoQuarto pq : periodos) {
                modeloTabela.addRow(new Object[]{
                    pq.getOrdem(),
                    pq.getDescricao(),
                    pq.getTempoMinutos(),
                    pq.getValor(),
                    pq.isPernoite()
                });
            }
        }
    }

    private void salvar() {
        String numStr = txtNumero.getText().trim();
        String tipo = txtTipo.getText().trim();

        if (numStr.isEmpty() || tipo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Número e Tipo são obrigatórios!");
            return;
        }

        try {
            int num = Integer.parseInt(numStr);
            float vAdd = Float.parseFloat(txtHoraAdicional.getText().replace(",", "."));
            float vPes = Float.parseFloat(txtPessoaAdicional.getText().replace(",", "."));
            
            // Pega valores da tabela para manter retrocompatibilidade com a tabela 'quartos' (usa o primeiro período)
            float vPer = 0;
            float vPern = 0;
            String tempo = "02:00";
            
            if (modeloTabela.getRowCount() > 0) {
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    boolean isp = (Boolean) modeloTabela.getValueAt(i, 4);
                    if (isp && vPern == 0) vPern = (Float) modeloTabela.getValueAt(i, 3);
                    if (!isp && vPer == 0) {
                        vPer = (Float) modeloTabela.getValueAt(i, 3);
                        int mins = (Integer) modeloTabela.getValueAt(i, 2);
                        tempo = String.format("%02d:%02d", mins / 60, mins % 60);
                    }
                }
            }

            fquartos dao = new fquartos();
            vquartos model = new vquartos(0, tipo, num, vPer, vPern, vPes);

            boolean ok;
            if (isUpdate) {
                ok = dao.fazOUp(model, vAdd, tempo);
            } else {
                if (dao.verExiste(num)) {
                    JOptionPane.showMessageDialog(this, "Quarto já existe!");
                    return;
                }
                ok = dao.insercao(model, vAdd, tempo);
            }

            if (ok) {
                salvarPeriodosBanco(num); // Salva os períodos dinâmicos após o quarto
                JOptionPane.showMessageDialog(this, "Salvo com sucesso!");
                configGlobal.getInstance().setMudanca(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao salvar.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void salvarPeriodosBanco(int numeroQuarto) {
        if (tabelaPeriodos.isEditing()) {
            tabelaPeriodos.getCellEditor().stopCellEditing();
        }

        try (Connection link = new fazconexao().conectar()) {
            link.setAutoCommit(false);
            
            // Limpa antigos
            try (PreparedStatement del = link.prepareStatement("DELETE FROM periodos_quarto WHERE numeroquarto = ?")) {
                del.setInt(1, numeroQuarto);
                del.executeUpdate();
            }

            // Insere novos da tabela
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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "ERRO CRÍTICO ao salvar períodos: " + ex.getMessage());
            System.err.println("Erro ao salvar períodos dinâmicos: " + ex.getMessage());
        }
    }
}
