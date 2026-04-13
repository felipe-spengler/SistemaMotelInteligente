package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.vquartos;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class DialogEditaQuartoModerno extends JDialog {

    private JTextField txtNumero;
    private JTextField txtTipo;
    private JTextField txtValorPeriodo;
    private JTextField txtValorPernoite;
    private JTextField txtHoraAdicional; 
    private JTextField txtPessoaAdicional; 
    private JTextField txtPeriodoHoras;
    private JTextField txtPeriodoMin;
    
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
        setSize(500, 650);
        setLocationRelativeTo(getParent());
        EstiloModerno.aplicarEstiloDialog(this);

        JPanel main = new JPanel(new MigLayout("fillx, insets 30, wrap 2", "[grow][grow]", "[]20[]5[]15[]5[]15[]5[]15[]5[]15[]5[]20[]30[]"));
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

        // Valores Principais
        main.add(EstiloModerno.criarLabel("Valor Período (R$)"));
        main.add(EstiloModerno.criarLabel("Valor Pernoite (R$)"));

        txtValorPeriodo = EstiloModerno.criarInput();
        main.add(txtValorPeriodo, "w 150!, h 35!");

        txtValorPernoite = EstiloModerno.criarInput();
        main.add(txtValorPernoite, "w 150!, h 35!");

        // Adicionais
        main.add(EstiloModerno.criarLabel("Hora Extra (R$)"));
        main.add(EstiloModerno.criarLabel("Pessoa Extra (R$)"));

        txtHoraAdicional = EstiloModerno.criarInput();
        txtHoraAdicional.setText("20.0");
        main.add(txtHoraAdicional, "growx");

        txtPessoaAdicional = EstiloModerno.criarInput();
        txtPessoaAdicional.setText("20.0");
        main.add(txtPessoaAdicional, "growx");

        // Duração
        main.add(EstiloModerno.criarLabel("Duração Padrão (Fallback)"), "span 2, wrap");
        JPanel pnlTempo = new JPanel(new MigLayout("insets 0", "[]5[]5[]5[]"));
        pnlTempo.setOpaque(false);
        txtPeriodoHoras = EstiloModerno.criarInput();
        txtPeriodoHoras.setText("2");
        txtPeriodoMin = EstiloModerno.criarInput();
        txtPeriodoMin.setText("00");
        pnlTempo.add(txtPeriodoHoras, "w 50!");
        pnlTempo.add(new JLabel("h"));
        pnlTempo.add(txtPeriodoMin, "w 50!");
        pnlTempo.add(new JLabel("min"));
        main.add(pnlTempo, "span 2, wrap");

        // Botão Novo Modelo
        JButton btnPeriodosDinamicos = EstiloModerno.criarBotaoSecundario("Configurar Períodos Dinâmicos", null);
        btnPeriodosDinamicos.setBackground(new Color(99, 102, 241));
        btnPeriodosDinamicos.setForeground(Color.WHITE);
        btnPeriodosDinamicos.addActionListener(e -> {
            if (isUpdate) {
                new DialogPeriodosModerno((JFrame) SwingUtilities.getWindowAncestor(this), quartoId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Salve o quarto primeiro antes de configurar períodos dinâmicos.");
            }
        });
        main.add(btnPeriodosDinamicos, "span 2, growx, h 40!, wrap");

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
                txtValorPeriodo.setText(String.valueOf(v.getValorquarto()));
                txtValorPernoite.setText(String.valueOf(v.getPernoitequarto()));
                txtPessoaAdicional.setText(String.valueOf(v.getAddPessoa()));
                break;
            }
        }
        txtHoraAdicional.setText(String.valueOf(dao.getAdicional(quartoId)));
        String p = dao.getPeriodo(quartoId);
        if (p != null && p.contains(":")) {
            String[] parts = p.split(":");
            txtPeriodoHoras.setText(parts[0]);
            txtPeriodoMin.setText(parts[1]);
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
            float vPer = Float.parseFloat(txtValorPeriodo.getText().replace(",", "."));
            float vPern = Float.parseFloat(txtValorPernoite.getText().replace(",", "."));
            float vAdd = Float.parseFloat(txtHoraAdicional.getText().replace(",", "."));
            float vPes = Float.parseFloat(txtPessoaAdicional.getText().replace(",", "."));
            String tempo = txtPeriodoHoras.getText() + ":" + txtPeriodoMin.getText();

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
}
