/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motelinteligente.telas;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.sql.Connection;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author johnc
 */
public class ConfiguracoesAdicionais extends javax.swing.JFrame {

    private String telaMostrar; // Variável global para armazenar a tela selecionada
    private boolean isInitializing = false;
    /**
     * Creates new form ConfiguracoesAdicionais
     */
    private class NumOnly extends PlainDocument {

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str != null) {
                // Verifique se a string inserida contém apenas números (0 a 9)
                for (char c : str.toCharArray()) {
                    if (!Character.isDigit(c)) {
                        return; // Não insira caracteres não numéricos
                    }
                }
                // Se a string contém apenas números, insira-a normalmente
                super.insertString(offs, str, a);
            }
        }
    }

    public ConfiguracoesAdicionais() {
        
        initComponents();
        configGlobal config = configGlobal.getInstance();
        if (config.getLogoffecharcaixa()) {
            checkLogoff.setSelected(true);
        }
        if (config.getControlaEstoque()) {
            checkEstoque.setSelected(true);
        }
        if (config.isFlagMesmoUserCaixa()) {
            fechaCaixaUser.setSelected(true);
        }

        txtLimiteDesconto.setDocument(new NumOnly());
        txtLimiteDesconto.setText(String.valueOf(config.getLimiteDesconto()));
        isInitializing = true;
        carregarTelas();
        isInitializing = false;

    }

    private void carregarTelas() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        // Limpa as opções anteriores
        jComboBoxTelas.removeAllItems();

        // Adiciona as telas disponíveis ao JComboBox
        for (GraphicsDevice device : devices) {
            String nomeTela = device.getIDstring(); // Nome da tela
            jComboBoxTelas.addItem(nomeTela);
        }

        // Seleciona a última tela por padrão
        if (devices.length > 0) {
            jComboBoxTelas.setSelectedIndex(devices.length - 1);
            telaMostrar = devices[devices.length - 1].getIDstring(); // Atualiza a variável global
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        checkLogoff = new javax.swing.JCheckBox();
        checkEstoque = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fechaCaixaUser = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        txtLimiteDesconto = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxTelas = new javax.swing.JComboBox<>();

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("%");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        checkLogoff.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        checkLogoff.setText("Obrigar Login ao Fechar Caixa");
        checkLogoff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkLogoffActionPerformed(evt);
            }
        });

        checkEstoque.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        checkEstoque.setText("Controlar Estoque");
        checkEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkEstoqueActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setBackground(new java.awt.Color(204, 204, 204));
        jLabel1.setFont(new java.awt.Font("Sitka Display", 1, 24)); // NOI18N
        jLabel1.setText("Configurações Adicionais");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(294, 294, 294)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(350, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        fechaCaixaUser.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        fechaCaixaUser.setText("Fechamento caixa usuário abriu");
        fechaCaixaUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fechaCaixaUserActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Limita Desconto Colaborador: ");

        txtLimiteDesconto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtLimiteDesconto.setText("0");
        txtLimiteDesconto.setMaximumSize(new java.awt.Dimension(64, 26));
        txtLimiteDesconto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLimiteDescontoActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("%");

        jLabel4.setText("*  Não se aplica a usuários admim e gerente");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("*");

        jComboBoxTelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxTelas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxTelasItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(42, 42, 42)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(checkEstoque)
                                .addComponent(fechaCaixaUser)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(checkLogoff)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtLimiteDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel3)
                                    .addGap(106, 106, 106)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jComboBoxTelas, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkLogoff)
                    .addComponent(jLabel2)
                    .addComponent(txtLimiteDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkEstoque)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fechaCaixaUser)
                .addGap(39, 39, 39)
                .addComponent(jComboBoxTelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkLogoffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkLogoffActionPerformed
        configGlobal config = configGlobal.getInstance();
        if (checkLogoff.isSelected()) {
            funcaoSet("logoffcaixa", true);
            config.setLogoffecharcaixa(true);
        } else {
            funcaoSet("logoffcaixa", false);
            config.setLogoffecharcaixa(false);
        }

    }//GEN-LAST:event_checkLogoffActionPerformed

    private void checkEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkEstoqueActionPerformed
        configGlobal config = configGlobal.getInstance();
        if (checkEstoque.isSelected()) {
            funcaoSet("estoque", true);
            config.setControlaEstoque(true);
        } else {
            funcaoSet("estoque", false);
            config.setControlaEstoque(false);
        }
    }//GEN-LAST:event_checkEstoqueActionPerformed

    private void fechaCaixaUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fechaCaixaUserActionPerformed
        configGlobal config = configGlobal.getInstance();
        if (fechaCaixaUser.isSelected()) {
            funcaoSet("fechaUserCaixa", true);
            config.setFlagMesmoUserCaixa(true);
        } else {
            funcaoSet("fechaUserCaixa", false);
            config.setFlagMesmoUserCaixa(false);
        }
    }//GEN-LAST:event_fechaCaixaUserActionPerformed

    private void txtLimiteDescontoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLimiteDescontoActionPerformed
        String consultaSQL = "UPDATE configuracoes SET limitadesconto = ?";
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            // Atribuir o valor booleano ao parâmetro da consulta
            statement.setInt(1, Integer.valueOf(txtLimiteDesconto.getText()));

            int n = statement.executeUpdate();
            if (n != 0) {
                configGlobal config = configGlobal.getInstance();
                config.setLimiteDesconto(Integer.valueOf(txtLimiteDesconto.getText()));
                JOptionPane.showMessageDialog(null, "Limite de Desconto Modificado com Sucesso!");
                link.close();
                statement.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showConfirmDialog(null, e);
            }
        }

    }//GEN-LAST:event_txtLimiteDescontoActionPerformed

    private void jComboBoxTelasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTelasItemStateChanged
        if (isInitializing) {
        return; // Ignora o evento se estiver na fase de inicialização
    }

    // Atualiza a variável global com a tela selecionada
    telaMostrar = (String) jComboBoxTelas.getSelectedItem();
    JOptionPane.showMessageDialog(this, "Tela selecionada: " + telaMostrar);

    // Muda a tela no sistema e no banco de dados também
    configGlobal config = configGlobal.getInstance();
    config.setTelaMostrar(telaMostrar);
    setarTela(telaMostrar);
    }//GEN-LAST:event_jComboBoxTelasItemStateChanged
    public void funcaoSet(String campo, boolean flag) {
        String consultaSQL = "UPDATE configuracoes SET " + campo + " = ?";
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            // Atribuir o valor booleano ao parâmetro da consulta
            statement.setBoolean(1, flag);

            int n = statement.executeUpdate();
            if (n != 0) {
                link.close();
                statement.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showConfirmDialog(null, e);
            }
        }
    }

    public void setarTela(String campo) {
        String consultaSQL = "UPDATE configuracoes SET telaMostrar = ?"; // Usando '?' como placeholder
        Connection link = null;
        PreparedStatement statement = null; // Declarar fora do try para o uso no finally
        try {
            link = new fazconexao().conectar();
            statement = link.prepareStatement(consultaSQL);

            // Define o valor do placeholder
            statement.setString(1, campo);

            // Executa a atualização
            int n = statement.executeUpdate();
            if (n != 0) {
                // Sucesso na atualização
                JOptionPane.showMessageDialog(null, "Tela atualizada com sucesso.");
            }
        } catch (SQLException e) {
            // Exibe a mensagem de erro
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close(); // Fecha o PreparedStatement
                }
                if (link != null && !link.isClosed()) {
                    link.close(); // Fecha a conexão
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

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
            java.util.logging.Logger.getLogger(ConfiguracoesAdicionais.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ConfiguracoesAdicionais.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ConfiguracoesAdicionais.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ConfiguracoesAdicionais.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ConfiguracoesAdicionais().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkEstoque;
    private javax.swing.JCheckBox checkLogoff;
    private javax.swing.JCheckBox fechaCaixaUser;
    private javax.swing.JComboBox<String> jComboBoxTelas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtLimiteDesconto;
    // End of variables declaration//GEN-END:variables
}
