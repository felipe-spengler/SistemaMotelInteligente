/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motelinteligente.telas;

import com.motelinteligente.dados.ffuncionario;
import com.motelinteligente.dados.vfuncionario;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author MOTEL
 */
public class TelaCadFuncionario extends javax.swing.JFrame {

    boolean botaoAtualizarClick = false;
    String mudando = "";

    /**
     * Creates new form TelaCadFuncionario
     */
    public TelaCadFuncionario() {
        initComponents();
        carregaTabela();

    }

    public void carregaTabela() {
        ffuncionario func = new ffuncionario();
        DefaultTableModel modelo = (DefaultTableModel) tabelaFunc.getModel();
        modelo.setNumRows(0);

        for (vfuncionario q : func.mostrar()) {

            modelo.addRow(new Object[]{
                q.getNomefuncionario(),
                q.getCargofuncionario(),
                q.getLoginfuncionario()
            });
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        bt_novo1 = new javax.swing.JButton();
        bt_atualizar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaFunc = new javax.swing.JTable();
        bt_voltar = new javax.swing.JButton();
        bt_salvar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nome_func = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        senha1func = new javax.swing.JPasswordField();
        senha2func = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        cargo_func = new javax.swing.JComboBox<>();
        login_func = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Lista de Funcionários");

        bt_novo1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png"))); // NOI18N
        bt_novo1.setText("Apagar");
        bt_novo1.setMaximumSize(new java.awt.Dimension(87, 27));
        bt_novo1.setMinimumSize(new java.awt.Dimension(87, 27));
        bt_novo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_novo1ActionPerformed(evt);
            }
        });

        bt_atualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_backup.png"))); // NOI18N
        bt_atualizar.setText("Atualizar");
        bt_atualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_atualizarActionPerformed(evt);
            }
        });

        tabelaFunc.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nome", "Cargo", "Login"
            }
        ));
        jScrollPane1.setViewportView(tabelaFunc);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(jLabel6))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(bt_atualizar)
                                .addGap(18, 18, 18)
                                .addComponent(bt_novo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_atualizar)
                    .addComponent(bt_novo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        bt_voltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_sair.png"))); // NOI18N
        bt_voltar.setText("Voltar");
        bt_voltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_voltarActionPerformed(evt);
            }
        });

        bt_salvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_salvar.png"))); // NOI18N
        bt_salvar.setText("Salvar");
        bt_salvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_salvarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Cadastro de Funcionário");

        jLabel3.setText("Digite uma senha:");

        jLabel2.setText("Nome:");

        jLabel7.setText("login");

        jLabel8.setText("Digite a senha novamente:");

        jLabel5.setText("Cargo");

        cargo_func.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "gerente", "comum", " " }));
        cargo_func.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cargo_funcActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel4)
                                            .addGap(307, 307, 307))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(jLabel8)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(senha2func))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel3)
                                            .addGap(57, 57, 57)
                                            .addComponent(senha1func)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel7))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(nome_func)
                                            .addComponent(login_func))))
                                .addGap(60, 60, 60))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bt_voltar)
                        .addGap(26, 26, 26)
                        .addComponent(bt_salvar)
                        .addGap(105, 105, 105))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(cargo_func, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nome_func, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(login_func, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(senha1func, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(senha2func, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(cargo_func, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bt_salvar)
                            .addComponent(bt_voltar)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bt_novo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_novo1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bt_novo1ActionPerformed

    private void bt_atualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_atualizarActionPerformed
        // TODO add your handling code here:
        ffuncionario funcionario = new ffuncionario();
        int linhaSelecionada = tabelaFunc.getSelectedRow();
        if (linhaSelecionada != -1) {
            Object nome = tabelaFunc.getValueAt(linhaSelecionada, 0); // 0 é o índice da coluna do ID
            Object cargo = tabelaFunc.getValueAt(linhaSelecionada, 1); // 0 é o índice da coluna do ID
            Object login = tabelaFunc.getValueAt(linhaSelecionada, 2); // 0 é o índice da coluna do ID

            if (nome != null && cargo != null && login != null) {
                String nomeFuncionario = nome.toString();
                String cargoFuncionario = cargo.toString();
                String loginFuncionario = login.toString();

                nome_func.setText(nomeFuncionario);
                login_func.setText(loginFuncionario);
                if (cargo_func.getItemCount() > 0) {
                    for (int i = 0; i < cargo_func.getItemCount(); i++) {
                        String cargoatual = cargo_func.getItemAt(i); // Obtenha o cargo como String
                        if (cargoatual.equals(cargoFuncionario)) {
                            // O cargo do funcionário está presente no JComboBox
                            // Faça algo aqui, se necessário
                            cargo_func.setSelectedItem(cargoatual);
                            break; // Se encontrou, pode sair do loop
                        }
                    }
                }

                botaoAtualizarClick = true;
                mudando = loginFuncionario;
            }

        } else {
            JOptionPane.showMessageDialog(null, "Nenhum quarto selecionado!");
        }

        carregaTabela();
    }//GEN-LAST:event_bt_atualizarActionPerformed

    private void bt_voltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_voltarActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_bt_voltarActionPerformed

    private void bt_salvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_salvarActionPerformed
        // TODO add your handling code here:
        String nome;
        String login;
        String senha1, senha2;
        String cargo;
        try {
            nome = nome_func.getText();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Revise as inforções do Nome!");
            nome_func.grabFocus();// foca o campo
            nome_func.setText(""); //limpa o campo
        }
        try {
            login = login_func.getText();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Revise as inforções do Login");
            login_func.grabFocus();// foca o campo
            login_func.setText(""); //limpa o campo
        }
        try {
            cargo = (String) cargo_func.getSelectedItem();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Revise as inforções do Cargo");
            cargo_func.grabFocus();// foca o campo
        }
        try {
            senha1 = senha1func.getText();
            senha2 = senha2func.getText();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Revise as senhas! ");
            senha1func.grabFocus();// foca o campo
            senha1func.setText(""); //limpa o campo
            senha2func.grabFocus();// foca o campo
            senha2func.setText(""); //limpa o campo
        }
        nome = nome_func.getText();
        login = login_func.getText();
        senha1 = senha1func.getText();
        senha2 = senha2func.getText();
        cargo = (String) cargo_func.getSelectedItem();

        // verificar as senhas digitadas
        if (senha1.equals(senha2) == true) {
            if (botaoAtualizarClick && mudando.equals(login)) {
                String sqlUpdate = "UPDATE funcionario SET nomefuncionario='" + nome + "', cargofuncionario='" + cargo +
                   "', loginfuncionario='" + login + "', senhafuncionario='" + senha1 + "' WHERE loginfuncionario='" + login + "'";
                if(new ffuncionario().fazUpdate(sqlUpdate)){
                    JOptionPane.showMessageDialog(null, "Alterado com sucesso");
                }else{
                    JOptionPane.showMessageDialog(null, "Erro ao atualizar");
                }
            } else {
                vfuncionario novo;
                novo = new vfuncionario(1, nome, cargo, login, senha1);
                if (new ffuncionario().insercao(novo) == true) {
                    JOptionPane.showMessageDialog(null, "Cadastrado com sucesso");
                    nome_func.setText(""); //limpa o campo
                    login_func.setText("");
                    senha1func.setText(""); //limpa o campo
                    senha2func.setText("");
                } else {
                    JOptionPane.showConfirmDialog(null, "Algo de errado");
                }
            }

        } else {
            JOptionPane.showMessageDialog(null, "As senhas informadas não conferem! ");

        }
        carregaTabela();

    }//GEN-LAST:event_bt_salvarActionPerformed

    private void cargo_funcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargo_funcActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cargo_funcActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaCadFuncionario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaCadFuncionario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaCadFuncionario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaCadFuncionario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaCadFuncionario().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bt_atualizar;
    private javax.swing.JButton bt_novo1;
    private javax.swing.JButton bt_salvar;
    private javax.swing.JButton bt_voltar;
    private javax.swing.JComboBox<String> cargo_func;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField login_func;
    private javax.swing.JTextField nome_func;
    private javax.swing.JPasswordField senha1func;
    private javax.swing.JPasswordField senha2func;
    private javax.swing.JTable tabelaFunc;
    // End of variables declaration//GEN-END:variables
}
