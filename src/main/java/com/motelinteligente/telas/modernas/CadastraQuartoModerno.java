package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.vquartos;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class CadastraQuartoModerno extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(CadastraQuartoModerno.class);

    // Campos
    private JTextField txtNumero;
    private JTextField txtTipo;
    private JTextField txtValorPeriodo;
    private JTextField txtValorPernoite;
    private JTextField txtHoraAdicional; // Valor por hora extra
    private JTextField txtPessoaAdicional; // Valor por pessoa extra
    private JTextField txtPeriodoHoras;
    private JTextField txtPeriodoMin;

    private JTable tabela;
    private DefaultTableModel tableModel;

    public CadastraQuartoModerno() {
        initUI();
        carregaTabela();
    }

    private void initUI() {
        setTitle("Gestão de Quartos");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        EstiloModerno.aplicarEstiloFrame(this);

        // Layout Principal: Duas colunas (Formulário e Tabela)
        JPanel main = new JPanel(new MigLayout("fill, insets 25", "[400!, fill]30[grow, fill]", "[grow]"));
        main.setBackground(new Color(243, 244, 246)); // Ensure background color
        main.setOpaque(true);

        // === CARD FORMULÁRIO ===
        JPanel cardForm = EstiloModerno.criarCard();
        cardForm.setLayout(
                new MigLayout("wrap 2, fillx, insets 20", "[grow][grow]", "[]15[]5[]15[]5[]15[]5[]15[]5[]15[]5[]20[]"));

        cardForm.add(EstiloModerno.criarTitulo("Cadastro de Quarto"), "span 2, center, wrap");

        // Linha 1: Número e Tipo
        cardForm.add(EstiloModerno.criarLabel("Número do Quarto"), "span 2, split 2, w 100!");
        cardForm.add(EstiloModerno.criarLabel("Tipo / Descrição")); // Label visual apenas para alinhar mentalmente

        txtNumero = EstiloModerno.criarInput();
        txtNumero.putClientProperty("FlatLaf.style", "font: bold +2");
        cardForm.add(txtNumero, "w 100!, h 35!");

        txtTipo = EstiloModerno.criarInput();
        cardForm.add(txtTipo, "growx, wrap");

        // Linha 2: Valores Principais
        cardForm.add(EstiloModerno.criarLabel("Valor Período (R$)"));
        cardForm.add(EstiloModerno.criarLabel("Valor Pernoite (R$)"));

        txtValorPeriodo = EstiloModerno.criarInput();
        cardForm.add(txtValorPeriodo, "w 150!, h 35!");

        txtValorPernoite = EstiloModerno.criarInput();
        cardForm.add(txtValorPernoite, "w 150!, h 35!");

        // Linha 3: Adicionais
        cardForm.add(EstiloModerno.criarLabel("Valor Hora Adic. (R$)"));
        cardForm.add(EstiloModerno.criarLabel("Valor Pessoa Extra (R$)"));

        txtHoraAdicional = EstiloModerno.criarInput();
        txtHoraAdicional.setText("20.0"); // Default value
        cardForm.add(txtHoraAdicional);

        txtPessoaAdicional = EstiloModerno.criarInput();
        txtPessoaAdicional.setText("20.0"); // Default value
        cardForm.add(txtPessoaAdicional);

        // Linha 4: Duração do Período
        cardForm.add(EstiloModerno.criarLabel("Duração do Período"), "span 2, wrap");

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

        cardForm.add(pnlTempo, "span 2, wrap");

        // Botões
        JButton btnSalvar = EstiloModerno.criarBotaoPrincipal("Salvar Quarto", null);
        btnSalvar.addActionListener(e -> salvarQuarto());

        JButton btnLimpar = EstiloModerno.criarBotaoSecundario("Limpar / Novo", null);
        btnLimpar.addActionListener(e -> limparCampos());

        cardForm.add(btnSalvar, "growx");
        cardForm.add(btnLimpar, "growx");

        main.add(cardForm, "top"); // Coluna 1

        // === CARD LISTAGEM ===
        JPanel cardTable = EstiloModerno.criarCard();
        cardTable.setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        JLabel lblList = EstiloModerno.criarTitulo("Quartos Cadastrados");
        cardTable.add(lblList, "wrap");

        String[] cols = { "Nº", "Tipo", "V. Período", "V. Pernoite", "V. Hora Adic.", "V. 3ª Pes" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setRowHeight(35);
        tabela.getTableHeader().putClientProperty("FlatLaf.style",
                "font:bold; background:#F3F4F6; border: 0,0,1,0, #E5E7EB");
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(new Color(240, 240, 240));

        cardTable.add(new JScrollPane(tabela), "grow, wrap");

        // Centralizar conteúdo das células
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Ações da Tabela
        JPanel actions = new JPanel(new MigLayout("insets 0, fillx", "push[][]"));
        actions.setOpaque(false);

        JButton btnEditar = EstiloModerno.criarBotaoSecundario("Carregar Selecionado",
                new ImageIcon(getClass().getResource("/imagens/icon_backup.png")));
        btnEditar.addActionListener(e -> carregarParaEdicao());

        JButton btnExcluir = EstiloModerno.criarBotaoPerigo("Excluir",
                new ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png")));
        btnExcluir.addActionListener(e -> excluirQuarto());

        actions.add(btnEditar);
        actions.add(btnExcluir);
        cardTable.add(actions, "growx");

        main.add(cardTable, "grow"); // Coluna 2
        add(main);
    }

    // --- LÓGICA DE NEGÓCIO (Migrada de CadastraQuarto.java) ---

    private void salvarQuarto() {
        // Validação básica
        String numStr = txtNumero.getText().trim();
        String tipo = txtTipo.getText().trim();

        if (numStr.isEmpty() || tipo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Número e Tipo são obrigatórios!");
            return;
        }

        int numeroQuarto;
        float valPeriodo, valPernoite, valHoraAdd, valPessoaAdd;
        int horas, minutos;

        try {
            numeroQuarto = Integer.parseInt(numStr);
            valPeriodo = Float.parseFloat(txtValorPeriodo.getText().replace(",", "."));
            valPernoite = Float.parseFloat(txtValorPernoite.getText().replace(",", "."));
            valHoraAdd = Float.parseFloat(txtHoraAdicional.getText().replace(",", "."));
            valPessoaAdd = Float.parseFloat(txtPessoaAdicional.getText().replace(",", "."));
            horas = Integer.parseInt(txtPeriodoHoras.getText());
            minutos = txtPeriodoMin.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtPeriodoMin.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Verifique os campos numéricos!");
            return;
        }

        String periodoStr = horas + ":" + minutos;
        boolean isUpdate = false;

        // Verifica existência logic
        fquartos dao = new fquartos();
        if (dao.verExiste(numeroQuarto)) {
            String status = dao.getStatus(numeroQuarto);
            if ("ocupado".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Quarto " + numeroQuarto + " está OCUPADO. Não pode editar agora.");
                return;
            }
            // Se já existe e está livre, pergunta se quer atualizar
            if (JOptionPane.showConfirmDialog(this, "Quarto já existe. Atualizar dados?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                isUpdate = true;
            } else {
                return;
            }
        }

        vquartos quartoModel = new vquartos(0, tipo, numeroQuarto, valPeriodo, valPernoite, valPessoaAdd);

        if (isUpdate) {
            if (dao.fazOUp(quartoModel, valHoraAdd, periodoStr)) {
                JOptionPane.showMessageDialog(this, "Quarto atualizado com sucesso!");
                atualizarCache(numeroQuarto, valPeriodo, valPernoite, valHoraAdd, periodoStr); // Atualiza cache se
                                                                                               // necessário
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar.");
            }
        } else {
            if (dao.insercao(quartoModel, valHoraAdd, periodoStr)) {
                JOptionPane.showMessageDialog(this, "Quarto cadastrado com sucesso!");
                inserirCache(numeroQuarto, tipo);
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar.");
            }
        }

        limparCampos();
        carregaTabela();
        configGlobal.getInstance().setMudanca(true);
    }

    private void atualizarCache(int numero, float vPeriodo, float vPernoite, float vAdicional, String tempoPeriodo) {
        CacheDados cache = CacheDados.getInstancia();
        Map<Integer, DadosOcupados> ocupados = cache.getCacheOcupado();
        if (ocupados.containsKey(numero)) {
            DadosOcupados d = ocupados.get(numero);
            d.setValorPeriodo(vPeriodo);
            d.setValorPernoite(vPernoite);
            d.setValorAdicional(vAdicional);
            d.setTempoPeriodo(tempoPeriodo);
        }
    }

    private void inserirCache(int numero, String tipo) {
        CacheDados cache = CacheDados.getInstancia();
        CarregaQuarto q = new CarregaQuarto(numero, tipo, "livre", String.valueOf(new Timestamp(new Date().getTime())));
        cache.getCacheQuarto().put(numero, q);
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

    private void carregarParaEdicao() {
        int row = tabela.getSelectedRow();
        if (row == -1)
            return;

        int num = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        // Busca dados completos do banco pois a tabela não tem tudo (ex: hora
        // adicional, periodo tempo)
        // Como o método fquartos.listarQuartos() retorna vquartos, talvez falte info.
        // Vamos usar o que tem na tela primeiro, e buscar o resto.

        // Simulação de "Get Full Data" - idealmente fquartos teria um getQuarto(num)
        // Vou usar lógica similar ao original que pega da tabela e tenta puxar o resto

        txtNumero.setText(String.valueOf(num));
        txtTipo.setText(tableModel.getValueAt(row, 1).toString());
        txtValorPeriodo.setText(tableModel.getValueAt(row, 2).toString());
        txtValorPernoite.setText(tableModel.getValueAt(row, 3).toString());
        txtHoraAdicional.setText(tableModel.getValueAt(row, 4).toString());
        txtPessoaAdicional.setText(tableModel.getValueAt(row, 5).toString());

        // Dados que faltam na tabela (tempo periodo)
        // Original usava fquartos().getPeriodo
        fquartos dao = new fquartos();
        String p = dao.getPeriodo(num); // Esperado "HH:mm"
        if (p != null && p.contains(":")) {
            String[] parts = p.split(":");
            txtPeriodoHoras.setText(parts[0]);
            txtPeriodoMin.setText(parts[1]);
        }
    }

    private void excluirQuarto() {
        int row = tabela.getSelectedRow();
        if (row == -1)
            return;

        int num = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "Tem certeza que deseja apagar o quarto " + num + "?", "Excluir",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Lógica de exclusão do legado: fquartos().exclusao(video) - mas espera,
            // vquartos tem ID?
            // O original usa vquartos().exclusao(vquartos modelo)
            // Precisamos instanciar um vquartos com o numero
            new fquartos().exclusao(num);

            // Remove do cache
            CacheDados.getInstancia().getCacheQuarto().remove(num);

            carregaTabela();
            limparCampos();
        }
    }

    private void limparCampos() {
        txtNumero.setText("");
        txtTipo.setText("");
        txtValorPeriodo.setText("");
        txtValorPernoite.setText("");
        txtHoraAdicional.setText("20.0");
        txtPessoaAdicional.setText("20.0");
        txtPeriodoHoras.setText("2");
        txtPeriodoMin.setText("00");
        tabela.clearSelection();
    }
}
