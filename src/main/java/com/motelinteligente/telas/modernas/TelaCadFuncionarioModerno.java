package com.motelinteligente.telas.modernas;

import com.motelinteligente.dados.ffuncionario;
import com.motelinteligente.dados.vfuncionario;
import com.motelinteligente.telas.modernas.EstiloModerno;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Tela de Cadastro de Funcionários - Versão Moderna (Tailwind Style)
 */
public class TelaCadFuncionarioModerno extends JFrame {

    private JTextField txtNome;
    private JTextField txtLogin;
    private JPasswordField txtSenha1;
    private JPasswordField txtSenha2;
    private JComboBox<String> comboCargo;
    private JTable tabelaFunc;
    private DefaultTableModel tableModel;

    private boolean isEditMode = false;
    private String loginOriginal = "";

    public TelaCadFuncionarioModerno() {
        initUI();
        carregaTabela();
    }

    private void initUI() {
        // Configuração Janela
        setTitle("Gestão de Equipe");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        EstiloModerno.aplicarEstiloFrame(this);

        // Layout Principal: Sidebar (Form) e Content (Table)
        // Usar um painel principal com padding
        JPanel mainContainer = new JPanel(new MigLayout("fill, insets 25", "[320!, fill]30[grow, fill]", "[grow]"));
        mainContainer.setOpaque(false); // Transparente para ver a cor do Frame

        // === CARD FORMULÁRIO ===
        JPanel cardForm = EstiloModerno.criarCard();
        cardForm.setLayout(
                new MigLayout("wrap 1, fillx, insets 25", "[grow, fill]", "[]20[]5[]15[]5[]15[]5[]15[]5[]15[]5[]30[]"));

        cardForm.add(EstiloModerno.criarTitulo("Novo Funcionário"), "center");

        // Campos
        cardForm.add(EstiloModerno.criarLabel("Nome Completo"));
        txtNome = EstiloModerno.criarInput();
        cardForm.add(txtNome);

        cardForm.add(EstiloModerno.criarLabel("Usuário de Sistema"));
        txtLogin = EstiloModerno.criarInput();
        cardForm.add(txtLogin);

        cardForm.add(EstiloModerno.criarLabel("Cargo"));
        comboCargo = new JComboBox<>(new String[] { "comum", "gerente", "admin" });
        comboCargo.setBackground(Color.WHITE);
        cardForm.add(comboCargo);

        cardForm.add(EstiloModerno.criarLabel("Senha"));
        txtSenha1 = new JPasswordField();
        txtSenha1.putClientProperty("FlatLaf.style", "arc:8; borderColor:#D1D5DB; margin:6,10,6,10");
        cardForm.add(txtSenha1);

        cardForm.add(EstiloModerno.criarLabel("Confirmar Senha"));
        txtSenha2 = new JPasswordField();
        txtSenha2.putClientProperty("FlatLaf.style", "arc:8; borderColor:#D1D5DB; margin:6,10,6,10");
        cardForm.add(txtSenha2);

        // Botões Form
        JButton btnSalvar = EstiloModerno.criarBotaoPrincipal("Salvar Funcionário", null);
        btnSalvar.addActionListener(e -> salvarFuncionario());
        cardForm.add(btnSalvar, "gaptop 10");

        JButton btnLimpar = EstiloModerno.criarBotaoSecundario("Cancelar", null);
        btnLimpar.addActionListener(e -> limparCampos());
        cardForm.add(btnLimpar);

        mainContainer.add(cardForm, "top"); // Adiciona na esquerda

        // === CARD TABELA ===
        JPanel cardTable = EstiloModerno.criarCard();
        cardTable.setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // Header Tabela
        JPanel headerTable = new JPanel(new MigLayout("insets 0", "[grow][]"));
        headerTable.setOpaque(false);
        JLabel lblList = EstiloModerno.criarTitulo("Colaboradores");
        lblList.putClientProperty("FlatLaf.style", "font: bold +2");
        headerTable.add(lblList);

        // Stats Label (ex: Total: 5) - Opcional
        cardTable.add(headerTable, "wrap, growx");

        // Tabela Customizada
        String[] cols = { "Nome", "Cargo", "Login" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaFunc = new JTable(tableModel);

        // Estilo Tabela
        tabelaFunc.setRowHeight(40);
        tabelaFunc.getTableHeader().putClientProperty("FlatLaf.style",
                "font:bold; background:#F3F4F6; border: 0,0,1,0, #E5E7EB");
        tabelaFunc.setShowVerticalLines(false);
        tabelaFunc.setShowHorizontalLines(true);
        tabelaFunc.setGridColor(new Color(229, 231, 235));
        tabelaFunc.setSelectionBackground(new Color(239, 246, 255)); // blue-50
        tabelaFunc.setSelectionForeground(EstiloModerno.TEXT_PRIMARY);

        JScrollPane scroll = new JScrollPane(tabelaFunc);
        scroll.setBorder(BorderFactory.createEmptyBorder()); // Remove borda scroll
        scroll.getViewport().setBackground(Color.WHITE);
        cardTable.add(scroll, "grow, wrap");

        // Ações Tabela
        JPanel actionsPanel = new JPanel(new MigLayout("insets 10, fillx", "push[][]"));
        actionsPanel.setOpaque(false);

        JButton btnEditar = EstiloModerno.criarBotaoSecundario("Editar",
                new ImageIcon(getClass().getResource("/imagens/icon_backup.png")));
        btnEditar.addActionListener(e -> carregarParaEdicao());

        JButton btnExcluir = EstiloModerno.criarBotaoPerigo("Excluir",
                new ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png")));
        btnExcluir.addActionListener(e -> excluirFuncionario());

        actionsPanel.add(btnEditar);
        actionsPanel.add(btnExcluir);

        cardTable.add(actionsPanel, "growx");

        mainContainer.add(cardTable, "grow");

        add(mainContainer);
    }

    // --- LÓGICA (Reutilizada) ---
    private void carregaTabela() {
        ffuncionario func = new ffuncionario();
        tableModel.setRowCount(0);
        for (vfuncionario q : func.mostrar()) {
            tableModel
                    .addRow(new Object[] { q.getNomefuncionario(), q.getCargofuncionario(), q.getLoginfuncionario() });
        }
    }

    private void salvarFuncionario() {
        String nome = txtNome.getText().trim();
        String login = txtLogin.getText().trim();
        String senha1 = new String(txtSenha1.getPassword());
        String senha2 = new String(txtSenha2.getPassword());
        String cargo = (String) comboCargo.getSelectedItem();

        if (nome.isEmpty() || login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha Nome e Login!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!senha1.equals(senha2)) {
            JOptionPane.showMessageDialog(this, "As senhas não conferem!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ffuncionario funcDao = new ffuncionario();

        if (isEditMode) {
            String sqlUpdate = "UPDATE funcionario SET nomefuncionario='" + nome + "', cargofuncionario='" + cargo
                    + "', loginfuncionario='" + login + "'";
            if (!senha1.isEmpty())
                sqlUpdate += ", senhafuncionario='" + senha1 + "'";
            sqlUpdate += " WHERE loginfuncionario='" + loginOriginal + "'";

            if (funcDao.fazUpdate(sqlUpdate)) {
                JOptionPane.showMessageDialog(this, "Atualizado com sucesso!");
                limparCampos();
                carregaTabela();
            }
        } else {
            if (senha1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Senha é obrigatória.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            vfuncionario novo = new vfuncionario(1, nome, cargo, login, senha1);
            if (funcDao.insercao(novo)) {
                JOptionPane.showMessageDialog(this, "Cadastrado com sucesso!");
                limparCampos();
                carregaTabela();
            }
        }
    }

    private void carregarParaEdicao() {
        int row = tabelaFunc.getSelectedRow();
        if (row == -1)
            return;

        String nome = (String) tabelaFunc.getValueAt(row, 0);
        String cargo = (String) tabelaFunc.getValueAt(row, 1);
        String login = (String) tabelaFunc.getValueAt(row, 2);

        txtNome.setText(nome);
        txtLogin.setText(login);
        comboCargo.setSelectedItem(cargo);
        txtSenha1.setText("");
        txtSenha2.setText("");

        loginOriginal = login;
        isEditMode = true;
        txtNome.requestFocus();
    }

    private void excluirFuncionario() {
        int row = tabelaFunc.getSelectedRow();
        if (row == -1)
            return;

        String nome = (String) tabelaFunc.getValueAt(row, 0);
        String cargo = (String) tabelaFunc.getValueAt(row, 1);
        String login = (String) tabelaFunc.getValueAt(row, 2);

        ffuncionario funcDao = new ffuncionario();
        int id = funcDao.getIdFuncionario(nome, cargo, login);

        if (id != -1) {
            if (JOptionPane.showConfirmDialog(this, "Excluir " + nome + "?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                funcDao.excluirFuncionario(id);
                carregaTabela();
                limparCampos();
            }
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtLogin.setText("");
        txtSenha1.setText("");
        txtSenha2.setText("");
        comboCargo.setSelectedIndex(0);
        isEditMode = false;
        loginOriginal = "";
        tabelaFunc.clearSelection();
    }

    // Main para teste isolado
    public static void main(String args[]) {
        try {
            com.formdev.flatlaf.FlatIntelliJLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
        } catch (Exception ex) {
        }
        java.awt.EventQueue.invokeLater(() -> new TelaCadFuncionarioModerno().setVisible(true));
    }
}
