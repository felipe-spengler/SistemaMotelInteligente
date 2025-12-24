package com.motelinteligente.telas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.ffuncionario;
import com.motelinteligente.dados.vfuncionario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

/**
 * Tela de Cadastro de Funcionários
 * Refatorada para usar MigLayout e remover dependência do editor visual do
 * NetBeans.
 */
public class TelaCadFuncionario extends JFrame {

    private JTextField txtNome;
    private JTextField txtLogin;
    private JPasswordField txtSenha1;
    private JPasswordField txtSenha2;
    private JComboBox<String> comboCargo;
    private JTable tabelaFunc;
    private DefaultTableModel tableModel;

    private boolean isEditMode = false;
    private String loginOriginal = ""; // Para controlar atualização do login

    public TelaCadFuncionario() {
        initUI();
        carregaTabela();
    }

    private void initUI() {
        setTitle("Cadastro de Funcionários");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);

        // Layout Principal: Duas colunas (350px fixo para form, resto para tabela)
        setLayout(new MigLayout("fill, insets 20", "[350!, fill][grow, fill]", "[grow]"));

        // --- PAINEL ESQUERDO (Formulário) ---
        JPanel formPanel = new JPanel(new MigLayout("wrap 2, fillx, insets 20", "[][grow, fill]", "[]15[]"));
        formPanel.setBorder(BorderFactory.createTitledBorder("Dados do Funcionário"));
        formPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 15"); // Bordas arredondadas no painel

        // Campos
        formPanel.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        formPanel.add(txtNome);

        formPanel.add(new JLabel("Login:"));
        txtLogin = new JTextField();
        formPanel.add(txtLogin);

        formPanel.add(new JLabel("Senha:"));
        txtSenha1 = new JPasswordField();
        formPanel.add(txtSenha1);

        formPanel.add(new JLabel("Confirmar Senha:"));
        txtSenha2 = new JPasswordField();
        formPanel.add(txtSenha2);

        formPanel.add(new JLabel("Cargo:"));
        comboCargo = new JComboBox<>(new String[] { "admin", "gerente", "comum" });
        formPanel.add(comboCargo);

        // Botões do Formulário
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_bot_salvar.png")));
        btnSalvar.putClientProperty(FlatClientProperties.STYLE, "semicolon;font:bold");
        btnSalvar.addActionListener(e -> salvarFuncionario());

        JButton btnCancelar = new JButton("Limpar");
        btnCancelar.addActionListener(e -> limparCampos());

        formPanel.add(btnSalvar, "span, split 2, growx, gaptop 20");
        formPanel.add(btnCancelar, "growx, gaptop 20");

        add(formPanel, "top"); // Adiciona na coluna 0

        // --- PAINEL DIREITO (Tabela) ---
        JPanel tablePanel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow][]"));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Lista de Funcionários"));

        String[] cols = { "Nome", "Cargo", "Login" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaFunc = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(tabelaFunc);
        tablePanel.add(scroll, "grow, wrap");

        // Botões de Ação da Tabela
        JButton btnEditar = new JButton("Editar Selecionado");
        btnEditar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_backup.png")));
        btnEditar.addActionListener(e -> carregarParaEdicao());

        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png")));
        btnExcluir.addActionListener(e -> excluirFuncionario());

        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.setIcon(new ImageIcon(getClass().getResource("/imagens/icon_sair.png")));
        btnVoltar.addActionListener(e -> dispose());

        tablePanel.add(btnEditar, "split 3, growx");
        tablePanel.add(btnExcluir, "growx");
        tablePanel.add(btnVoltar, "growx");

        add(tablePanel, "grow"); // Adiciona na coluna 1
    }

    private void carregaTabela() {
        ffuncionario func = new ffuncionario();
        tableModel.setRowCount(0);

        for (vfuncionario q : func.mostrar()) {
            tableModel.addRow(new Object[] {
                    q.getNomefuncionario(),
                    q.getCargofuncionario(),
                    q.getLoginfuncionario()
            });
        }
    }

    private void salvarFuncionario() {
        String nome = txtNome.getText().trim();
        String login = txtLogin.getText().trim();
        String senha1 = new String(txtSenha1.getPassword());
        String senha2 = new String(txtSenha2.getPassword());
        String cargo = (String) comboCargo.getSelectedItem();

        // Validações Básicas
        if (nome.isEmpty() || login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha Nome e Login!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!senha1.equals(senha2)) {
            JOptionPane.showMessageDialog(this, "As senhas não conferem!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ffuncionario funcDao = new ffuncionario();

        if (isEditMode) {
            // Lógica de Atualização
            // Nota: SQL antigo -> UPDATE ... WHERE login = ...?
            // O sistema original atualiza baseado no Login antigo (loginOriginal)
            String sqlUpdate = "UPDATE funcionario SET nomefuncionario='" + nome + "', cargofuncionario='" + cargo
                    + "', loginfuncionario='" + login + "'";

            // Só atualiza senha se foi digitada
            if (!senha1.isEmpty()) {
                sqlUpdate += ", senhafuncionario='" + senha1 + "'";
            }

            sqlUpdate += " WHERE loginfuncionario='" + loginOriginal + "'";

            if (funcDao.fazUpdate(sqlUpdate)) {
                JOptionPane.showMessageDialog(this, "Funcionário atualizado com sucesso!");
                limparCampos();
                carregaTabela();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar funcionário.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else {
            // Lógica de Inserção
            if (senha1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "A senha é obrigatória para novos cadastros.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            vfuncionario novo = new vfuncionario(1, nome, cargo, login, senha1);
            if (funcDao.insercao(novo)) {
                JOptionPane.showMessageDialog(this, "Funcionário cadastrado com sucesso!");
                limparCampos();
                carregaTabela();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar. Verifique se o login já existe.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarParaEdicao() {
        int row = tabelaFunc.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um funcionário na lista para editar.");
            return;
        }

        String nome = (String) tabelaFunc.getValueAt(row, 0);
        String cargo = (String) tabelaFunc.getValueAt(row, 1);
        String login = (String) tabelaFunc.getValueAt(row, 2);

        txtNome.setText(nome);
        txtLogin.setText(login);
        comboCargo.setSelectedItem(cargo);
        txtSenha1.setText(""); // Não trazemos a senha por segurança
        txtSenha2.setText("");

        loginOriginal = login;
        isEditMode = true;

        // Destaque visual simples para indicar edição
        txtNome.requestFocus();
        setTitle("Cadastro de Funcionários - Editando: " + nome);
    }

    private void excluirFuncionario() {
        int row = tabelaFunc.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um funcionário para excluir.");
            return;
        }

        String nome = (String) tabelaFunc.getValueAt(row, 0);
        String cargo = (String) tabelaFunc.getValueAt(row, 1);
        String login = (String) tabelaFunc.getValueAt(row, 2);

        ffuncionario funcDao = new ffuncionario();
        int id = funcDao.getIdFuncionario(nome, cargo, login);

        if (id != -1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o funcionário '" + nome + "'?",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (funcDao.excluirFuncionario(id)) {
                    JOptionPane.showMessageDialog(this, "Funcionário excluído!");
                    carregaTabela();
                    limparCampos(); // Caso estivesse editando o que excluiu
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir no banco de dados.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Funcionário não encontrado no banco (ID -1).");
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
        setTitle("Cadastro de Funcionários");
        tabelaFunc.clearSelection();
    }

    /**
     * Método Main para testes isolados
     */
    public static void main(String args[]) {
        try {
            FlatIntelliJLaf.setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(() -> {
            new TelaCadFuncionario().setVisible(true);
        });
    }
}
