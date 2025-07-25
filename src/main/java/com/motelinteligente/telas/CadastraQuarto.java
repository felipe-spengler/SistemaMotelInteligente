/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motelinteligente.telas;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.vquartos;
import java.awt.Color;
import java.awt.Font;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author MOTEL
 */
public class CadastraQuarto extends javax.swing.JFrame {

    private String tp_quarto;
    private int numero_quarto;
    private float valor_periodo;
    private float valor_pernoite;
    private float hora_adicional;
    private String periodo;

    /**
     * Creates new form CadastraQuarto
     */
    class numOnly extends PlainDocument {

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
    
    public CadastraQuarto() {
        FlatIntelliJLaf.setup(); // aplica o tema FlatLaf
        periodo = null;
        hora_adicional = 0;
        tp_quarto = null;
        numero_quarto = -1;
        valor_periodo = -1;
        valor_pernoite = -1;
        initComponents();
        num_quarto.setDocument(new numOnly());
        periodoHoras.setDocument(new numOnly());
        periodoMin.setDocument(new numOnly());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bt_voltar = new javax.swing.JButton();
        bt_salvar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        bt_foto = new javax.swing.JButton();
        tipo_quarto = new javax.swing.JTextField();
        num_quarto = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        pernoite = new javax.swing.JTextField();
        val_quarto = new javax.swing.JTextField();
        bt_atualizar = new javax.swing.JButton();
        bt_apagar = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        horaAdicional = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        periodoMin = new javax.swing.JTextField();
        periodoHoras = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(245, 245, 245));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        bt_voltar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        bt_voltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_sair.png"))); // NOI18N
        bt_voltar.setText("Voltar");
        bt_voltar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bt_voltar.setPreferredSize(new java.awt.Dimension(120, 31));
        bt_voltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_voltarActionPerformed(evt);
            }
        });

        bt_salvar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        bt_salvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_salvar.png"))); // NOI18N
        bt_salvar.setText("Salvar");
        bt_salvar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bt_salvar.setPreferredSize(new java.awt.Dimension(120, 31));
        bt_salvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_salvarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setText("Cadastro de Novo Quarto");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Valor Período");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Valor Pernoite:");

        bt_foto.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        bt_foto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/add_foto.png"))); // NOI18N
        bt_foto.setText("Add Foto do Quarto");
        bt_foto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bt_foto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_fotoActionPerformed(evt);
            }
        });

        tipo_quarto.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        num_quarto.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        num_quarto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                num_quartoActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Tipo do Quarto:");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Número");

        pernoite.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        pernoite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pernoiteActionPerformed(evt);
            }
        });

        val_quarto.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        val_quarto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                val_quartoActionPerformed(evt);
            }
        });

        bt_atualizar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        bt_atualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_backup.png"))); // NOI18N
        bt_atualizar.setText("Atualizar");
        bt_atualizar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bt_atualizar.setMaximumSize(new java.awt.Dimension(120, 30));
        bt_atualizar.setMinimumSize(new java.awt.Dimension(120, 30));
        bt_atualizar.setPreferredSize(new java.awt.Dimension(120, 31));
        bt_atualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_atualizarActionPerformed(evt);
            }
        });

        bt_apagar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        bt_apagar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png"))); // NOI18N
        bt_apagar.setText("Apagar");
        bt_apagar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bt_apagar.setMaximumSize(new java.awt.Dimension(87, 27));
        bt_apagar.setMinimumSize(new java.awt.Dimension(87, 27));
        bt_apagar.setPreferredSize(new java.awt.Dimension(120, 31));
        bt_apagar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_apagarActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Lista de Quartos");

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Número", "Tipo", "Valor Período", "Pernoite"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tabela);
        if (tabela.getColumnModel().getColumnCount() > 0) {
            tabela.getColumnModel().getColumn(0).setResizable(false);
            tabela.getColumnModel().getColumn(0).setPreferredWidth(8);
            tabela.getColumnModel().getColumn(1).setResizable(false);
            tabela.getColumnModel().getColumn(1).setPreferredWidth(30);
            tabela.getColumnModel().getColumn(2).setResizable(false);
            tabela.getColumnModel().getColumn(2).setPreferredWidth(10);
            tabela.getColumnModel().getColumn(3).setResizable(false);
            tabela.getColumnModel().getColumn(3).setPreferredWidth(10);
        }

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("Periodo Locação");

        horaAdicional.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        horaAdicional.setText("20");
        horaAdicional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horaAdicionalActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Valor Hora Adicional");

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText("h");

        periodoMin.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        periodoMin.setText("30");
        periodoMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                periodoMinActionPerformed(evt);
            }
        });

        periodoHoras.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        periodoHoras.setText("2");
        periodoHoras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                periodoHorasActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText("min");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1))
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bt_voltar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bt_salvar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bt_atualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bt_apagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(val_quarto, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(num_quarto, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tipo_quarto, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pernoite, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(bt_foto)
                        .addGap(71, 71, 71)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(horaAdicional, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(periodoHoras, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel9))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(periodoMin, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel10)))))))
                .addContainerGap(144, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(num_quarto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(tipo_quarto, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(val_quarto, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(pernoite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8)
                        .addComponent(horaAdicional, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bt_foto)
                        .addComponent(jLabel7))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(periodoHoras))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(periodoMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_voltar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bt_salvar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bt_atualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bt_apagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bt_voltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_voltarActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_bt_voltarActionPerformed

    private void bt_salvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_salvarActionPerformed
        // TODO add your handling code here:
        int cont = 0;
        boolean excluir = false;
        boolean continuar = true;
        if (tipo_quarto.getText() != null) {
            cont++;
            tp_quarto = tipo_quarto.getText();
        } else {
            JOptionPane.showMessageDialog(null, "Revise as inforções do Tipo de Quarto!");
        }
        try {
            numero_quarto = Integer.parseInt(num_quarto.getText());
            cont++;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Revise as inforções do Numero do Quarto");
            num_quarto.grabFocus();// foca o campo
            num_quarto.setText(""); //limpa o campo
        }
        // verificar se ja tem esse quarto cadastrado
        if (new fquartos().verExiste(numero_quarto) == true) {
            if (new fquartos().getStatus(numero_quarto).equals("ocupado")) {
                JOptionPane.showMessageDialog(null, "Quarto já cadastrado e OCUPADO. Não é possível modificar no momento!");
                continuar = false;
                System.out.println("é false");
            } else {
                int escolha = JOptionPane.showConfirmDialog(null, "Esse quarto ja está cadastrado. Deseja sobreescrever??");
                if (escolha == JOptionPane.YES_OPTION) {
                    continuar = true;
                    excluir = true;
                } else {
                    System.out.println("é false");
                    continuar = false;

                }
            }
            tipo_quarto.grabFocus();// foca o campo
            num_quarto.setText(""); //limpa o campo

        }

        if (continuar == true) {

            try {
                hora_adicional = Float.parseFloat(horaAdicional.getText());
                cont++;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Revise as inforções de Adicional");
                horaAdicional.grabFocus();// foca o campo
                horaAdicional.setText(""); //limpa o campo
            }
            try {
                int horas = Integer.parseInt(periodoHoras.getText());
                int min = Integer.parseInt(periodoMin.getText());
                periodo =  horas +":" + min;
                cont++;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Revise as inforções de Adicional");
                horaAdicional.grabFocus();// foca o campo
                horaAdicional.setText(""); //limpa o campo
            }
            try {
                valor_periodo = Float.parseFloat(val_quarto.getText());
                cont++;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Revise as inforções do Valor do Quarto");
                val_quarto.grabFocus();// foca o campo
                val_quarto.setText(""); //limpa o campo
            }
            try {
                valor_pernoite = Float.parseFloat(pernoite.getText());
                cont++;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Revise as inforções do Valor do Pernoite");
                pernoite.grabFocus();// foca o campo
                pernoite.setText(""); //limpa o campo
            }

            if (cont == 6) {
                vquartos novo = new vquartos(0, tp_quarto, numero_quarto, valor_periodo, valor_pernoite);

                if (excluir == true){
                    
                        //se cair aqui o quarto ja existe
                        if(new fquartos().fazOUp(novo, hora_adicional, periodo)){
                            JOptionPane.showMessageDialog(null, "Atualizado com Sucesso");
                        }else{
                            JOptionPane.showMessageDialog(null, "Erro ao fazer UPDATE quarto");
                        }
                        //verifica se está na cacheOcupados pra alterar la tbm
                        CacheDados cache = CacheDados.getInstancia();
                        Map<Integer, DadosOcupados> dadosOcupados = cache.getCacheOcupado();
                        if (dadosOcupados.containsKey(numero_quarto)) {
                            DadosOcupados quartoOcupado = dadosOcupados.get(numero_quarto);
                            //verifica que dados mudou
                            if(quartoOcupado.getValorAdicional() != hora_adicional){
                                quartoOcupado.setValorAdicional(hora_adicional);
                            }
                            if(quartoOcupado.getValorPeriodo()!= valor_periodo){
                                quartoOcupado.setValorPeriodo(valor_periodo);
                            }
                            if(quartoOcupado.getValorPernoite()!= valor_pernoite){
                                quartoOcupado.setValorPernoite(valor_pernoite);
                            }
                            if(!quartoOcupado.getTempoPeriodo().equals(periodo)){
                                quartoOcupado.setTempoPeriodo(periodo);
                            }
                            dadosOcupados.put(numero_quarto, quartoOcupado);
                        }else{
                            JOptionPane.showMessageDialog(null, "Quarto não está na Cache Ocupado!");
                        }
                }else{
                    if (new fquartos().insercao(novo, hora_adicional, periodo) == true) {
                        JOptionPane.showMessageDialog(null, "Cadastrado com sucesso");
                        Date dataAtual = new Date();
                        Timestamp timestamp = new Timestamp(dataAtual.getTime());

                        CacheDados dados = CacheDados.getInstancia();
                        CarregaQuarto quarto = new CarregaQuarto(numero_quarto, tp_quarto, "livre", String.valueOf(timestamp));
                        dados.getCacheQuarto().put(numero_quarto, quarto);
                    }
                    num_quarto.setText(""); //limpa o campo
                    val_quarto.setText("");
                    pernoite.setText(""); //limpa o campo
                    tipo_quarto.setText("");
                    
                    
                    num_quarto.setEditable(true);
                }
            }

        }
        mostraJTable();

        configGlobal config = configGlobal.getInstance();
        config.setMudanca(true);
    }//GEN-LAST:event_bt_salvarActionPerformed

    private void num_quartoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_num_quartoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_num_quartoActionPerformed

    private void pernoiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pernoiteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pernoiteActionPerformed

    private void val_quartoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_val_quartoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_val_quartoActionPerformed

    private void bt_atualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_atualizarActionPerformed
        // TODO add your handling code here:
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada != -1) {
            Object numero = tabela.getValueAt(linhaSelecionada, 0); // 0 é o índice da coluna do ID
            Object tipo = tabela.getValueAt(linhaSelecionada, 1); // 0 é o índice da coluna do ID
            Object valor = tabela.getValueAt(linhaSelecionada, 2); // 0 é o índice da coluna do ID
            Object pernoite = tabela.getValueAt(linhaSelecionada, 3); // 0 é o índice da coluna do ID

            if (numero != null && tipo != null && valor != null && pernoite != null) {
                String num_vai = numero.toString();
                String tipo_vai = tipo.toString();
                String valor_vai = valor.toString();
                String pernoite_vai = pernoite.toString();

                tipo_quarto.setText(tipo_vai);
                num_quarto.setText(num_vai);
                val_quarto.setText(valor_vai);
                this.pernoite.setText(pernoite_vai);
                String periodo = new fquartos().getPeriodo(Integer.parseInt(num_vai));
                float valorHoraAdd = new fquartos().getAdicional(Integer.parseInt(num_vai));
                String[] partesDividir = periodo.split(":");
                String horasDiv = partesDividir[0];
                String minutosDiv = partesDividir[1];
                periodoHoras.setText(horasDiv);
                periodoMin.setText(minutosDiv);
                num_quarto.setEditable(false);
                horaAdicional.setText(String.valueOf(valorHoraAdd));

                //new fquartos().exclusao(Integer.parseInt(num_vai));
            }

        } else {
            JOptionPane.showMessageDialog(null, "Nenhum quarto selecionado!");
        }

        mostraJTable();
    }//GEN-LAST:event_bt_atualizarActionPerformed

    private void bt_apagarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_apagarActionPerformed
        // TODO add your handling code here:
        CacheDados cache = CacheDados.getInstancia();

        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada != -1) {
            Object idSelecionado = tabela.getValueAt(linhaSelecionada, 0); // 0 é o índice da coluna do ID

            if (idSelecionado != null) {
                int id = Integer.parseInt(idSelecionado.toString()); // Converte para o tipo apropriado (int)
                CarregaQuarto quarto = cache.getCacheQuarto().get(id);
                String status = quarto.getStatusQuarto();
                if (status.equals("ocupado")) {
                    JOptionPane.showMessageDialog(null, "Quarto Ocupado. Não é possível excluir!");
                } else {
                    new fquartos().exclusao(id);
                    //tira o quarto da cache
                    cache.getCacheQuarto().remove(id);
                }

            }

        } else {
            JOptionPane.showMessageDialog(null, "Nenhum quarto selecionado!");
        }
        //apagar da cache o quarto também
        mostraJTable();
        configGlobal config = configGlobal.getInstance();
        config.setMudanca(true);

    }//GEN-LAST:event_bt_apagarActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        mostraJTable();

    }//GEN-LAST:event_formWindowOpened

    private void bt_fotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_fotoActionPerformed
        JOptionPane.showMessageDialog(null, "Essa função srá habilitada em breve");
    }//GEN-LAST:event_bt_fotoActionPerformed

    private void horaAdicionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horaAdicionalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_horaAdicionalActionPerformed

    private void periodoMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_periodoMinActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_periodoMinActionPerformed

    private void periodoHorasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_periodoHorasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_periodoHorasActionPerformed

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
            java.util.logging.Logger.getLogger(CadastraQuarto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CadastraQuarto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CadastraQuarto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CadastraQuarto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CadastraQuarto().setVisible(true);

            }
        });

    }

    private void mostraJTable() {

        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
        modelo.setNumRows(0);
        fquartos quartosdao = new fquartos();

        for (vquartos q : quartosdao.mostrar()) {

            modelo.addRow(new Object[]{
                q.getNumeroquarto(),
                q.getTipoquarto(),
                q.getValorquarto(),
                q.getPernoitequarto()
            });
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bt_apagar;
    private javax.swing.JButton bt_atualizar;
    private javax.swing.JButton bt_foto;
    private javax.swing.JButton bt_salvar;
    private javax.swing.JButton bt_voltar;
    private javax.swing.JTextField horaAdicional;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField num_quarto;
    private javax.swing.JTextField periodoHoras;
    private javax.swing.JTextField periodoMin;
    private javax.swing.JTextField pernoite;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField tipo_quarto;
    private javax.swing.JTextField val_quarto;
    // End of variables declaration//GEN-END:variables
}
