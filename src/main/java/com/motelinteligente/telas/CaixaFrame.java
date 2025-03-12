/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motelinteligente.telas;

import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fcaixa;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.valores;
import com.motelinteligente.dados.vendaProdutos;
import java.security.Timestamp;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author MOTEL
 */
//atualizadooo
public class CaixaFrame extends javax.swing.JFrame {

    private boolean sair = false;
    float valorDescontos = 0, valorAcrescimos = 0;

    /**
     * Creates new form CaixaFrame
     */
    public CaixaFrame() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtCodigoCaixa3 = new javax.swing.JTextField();
        txtCodigoCaixa9 = new javax.swing.JTextField();
        painelBotoes = new javax.swing.JPanel();
        btVoltar = new javax.swing.JButton();
        painelDadosCaixa = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btFechar = new javax.swing.JButton();
        btAbrir = new javax.swing.JButton();
        txtCodigoCaixa = new javax.swing.JTextField();
        txtDataAbre = new javax.swing.JTextField();
        txtUserAbre = new javax.swing.JTextField();
        txtTroco = new javax.swing.JTextField();
        painelMostrar = new javax.swing.JPanel();
        tabPanel = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtLocacoes = new javax.swing.JTextField();
        txtSaldoInicial = new javax.swing.JTextField();
        txtCartao = new javax.swing.JTextField();
        txtPix = new javax.swing.JTextField();
        txtDinheiro = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtVendas = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtEntradaTotal = new javax.swing.JTextField();
        txtCaixaTotal = new javax.swing.JTextField();
        txtAcrescimos = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtDescontos = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtAntecipadoAgora = new javax.swing.JLabel();
        txtAntecipadoCaixa = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaProdutos = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        lblquantidadevendida = new javax.swing.JLabel();
        lbltotalprodutosvendidos = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaLocacoes = new javax.swing.JTable();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        lbltQuarto = new javax.swing.JLabel();
        lbltConsumo = new javax.swing.JLabel();
        lblContador = new javax.swing.JLabel();

        txtCodigoCaixa3.setBackground(new java.awt.Color(218, 218, 216));
        txtCodigoCaixa3.setForeground(new java.awt.Color(0, 0, 255));
        txtCodigoCaixa3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtCodigoCaixa3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoCaixa3ActionPerformed(evt);
            }
        });

        txtCodigoCaixa9.setBackground(new java.awt.Color(218, 218, 216));
        txtCodigoCaixa9.setForeground(new java.awt.Color(0, 0, 255));
        txtCodigoCaixa9.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtCodigoCaixa9.setFocusable(false);
        txtCodigoCaixa9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoCaixa9ActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        btVoltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sair.png"))); // NOI18N
        btVoltar.setText("Voltar");
        btVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVoltarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout painelBotoesLayout = new javax.swing.GroupLayout(painelBotoes);
        painelBotoes.setLayout(painelBotoesLayout);
        painelBotoesLayout.setHorizontalGroup(
            painelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBotoesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btVoltar)
                .addContainerGap(743, Short.MAX_VALUE))
        );
        painelBotoesLayout.setVerticalGroup(
            painelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBotoesLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(btVoltar)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        painelDadosCaixa.setBackground(new java.awt.Color(204, 204, 204));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Código Caixa:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Usuário Abertura:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Valor Para Troco:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Data Abertura");

        btFechar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btFechar.setText("Fechar");
        btFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFecharActionPerformed(evt);
            }
        });

        btAbrir.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btAbrir.setText("Abrir");
        btAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAbrirActionPerformed(evt);
            }
        });

        txtCodigoCaixa.setBackground(new java.awt.Color(218, 218, 216));
        txtCodigoCaixa.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCodigoCaixa.setForeground(new java.awt.Color(0, 0, 255));
        txtCodigoCaixa.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtCodigoCaixa.setFocusable(false);
        txtCodigoCaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoCaixaActionPerformed(evt);
            }
        });

        txtDataAbre.setBackground(new java.awt.Color(218, 218, 216));
        txtDataAbre.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDataAbre.setForeground(new java.awt.Color(0, 0, 255));
        txtDataAbre.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtDataAbre.setFocusable(false);
        txtDataAbre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDataAbreActionPerformed(evt);
            }
        });

        txtUserAbre.setBackground(new java.awt.Color(218, 218, 216));
        txtUserAbre.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtUserAbre.setForeground(new java.awt.Color(0, 0, 255));
        txtUserAbre.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtUserAbre.setFocusable(false);
        txtUserAbre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUserAbreActionPerformed(evt);
            }
        });

        txtTroco.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTroco.setForeground(new java.awt.Color(0, 0, 255));
        txtTroco.setText("0");
        txtTroco.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtTroco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTrocoActionPerformed(evt);
            }
        });

        tabPanel.setBackground(new java.awt.Color(255, 255, 204));
        tabPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        tabPanel.setForeground(new java.awt.Color(0, 0, 255));
        tabPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tabPanel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Saldo Inicial:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Locações:");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Dinheiro:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Cartão:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Pix:");

        txtLocacoes.setBackground(new java.awt.Color(218, 218, 216));
        txtLocacoes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtLocacoes.setForeground(new java.awt.Color(0, 0, 255));
        txtLocacoes.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtLocacoes.setFocusable(false);
        txtLocacoes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLocacoesActionPerformed(evt);
            }
        });

        txtSaldoInicial.setBackground(new java.awt.Color(218, 218, 216));
        txtSaldoInicial.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSaldoInicial.setForeground(new java.awt.Color(0, 0, 255));
        txtSaldoInicial.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtSaldoInicial.setFocusable(false);
        txtSaldoInicial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSaldoInicialActionPerformed(evt);
            }
        });

        txtCartao.setBackground(new java.awt.Color(218, 218, 216));
        txtCartao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCartao.setForeground(new java.awt.Color(0, 0, 255));
        txtCartao.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtCartao.setFocusable(false);
        txtCartao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCartaoActionPerformed(evt);
            }
        });

        txtPix.setBackground(new java.awt.Color(218, 218, 216));
        txtPix.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPix.setForeground(new java.awt.Color(0, 0, 255));
        txtPix.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtPix.setFocusable(false);
        txtPix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPixActionPerformed(evt);
            }
        });

        txtDinheiro.setBackground(new java.awt.Color(218, 218, 216));
        txtDinheiro.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDinheiro.setForeground(new java.awt.Color(0, 0, 255));
        txtDinheiro.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtDinheiro.setFocusable(false);
        txtDinheiro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDinheiroActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Vendas:");

        txtVendas.setBackground(new java.awt.Color(218, 218, 216));
        txtVendas.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtVendas.setForeground(new java.awt.Color(0, 0, 255));
        txtVendas.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtVendas.setFocusable(false);
        txtVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtVendasActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Entrada Total:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setText("Total Caixa:");

        txtEntradaTotal.setBackground(new java.awt.Color(218, 218, 216));
        txtEntradaTotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txtEntradaTotal.setForeground(new java.awt.Color(0, 0, 255));
        txtEntradaTotal.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtEntradaTotal.setFocusable(false);
        txtEntradaTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEntradaTotalActionPerformed(evt);
            }
        });

        txtCaixaTotal.setBackground(new java.awt.Color(218, 218, 216));
        txtCaixaTotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txtCaixaTotal.setForeground(new java.awt.Color(0, 0, 255));
        txtCaixaTotal.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtCaixaTotal.setFocusable(false);
        txtCaixaTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCaixaTotalActionPerformed(evt);
            }
        });

        txtAcrescimos.setBackground(new java.awt.Color(218, 218, 216));
        txtAcrescimos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtAcrescimos.setForeground(new java.awt.Color(0, 0, 255));
        txtAcrescimos.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtAcrescimos.setFocusable(false);
        txtAcrescimos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAcrescimosActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setText("Valor Acrescimos");

        txtDescontos.setBackground(new java.awt.Color(218, 218, 216));
        txtDescontos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDescontos.setForeground(new java.awt.Color(0, 0, 255));
        txtDescontos.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        txtDescontos.setFocusable(false);
        txtDescontos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescontosActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Valor Descontos");

        txtAntecipadoAgora.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtAntecipadoAgora.setText(" ");

        txtAntecipadoCaixa.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtAntecipadoCaixa.setText(" ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel18)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtDescontos))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtEntradaTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel6)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel10))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtSaldoInicial, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                                            .addComponent(txtLocacoes)
                                            .addComponent(txtVendas))))
                                .addGap(31, 31, 31)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel8)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel7))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtDinheiro)
                                            .addComponent(txtCartao)
                                            .addComponent(txtPix, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel17)
                                        .addGap(27, 27, 27)
                                        .addComponent(txtAcrescimos, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 144, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(138, 138, 138)
                                        .addComponent(txtCaixaTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(92, 92, 92)
                                .addComponent(txtAntecipadoCaixa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtAntecipadoAgora, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(60, 60, 60))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)
                    .addComponent(txtSaldoInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDinheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(txtLocacoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCartao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtPix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(txtVendas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtDescontos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(txtAcrescimos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtEntradaTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAntecipadoAgora)
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtCaixaTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAntecipadoCaixa))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        tabPanel.addTab("Informações do Caixa", jPanel3);

        tabelaProdutos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabelaProdutos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "id", "Descrição", "Quantidade", "Valor Und", "Total"
            }
        ));
        tabelaProdutos.setFocusable(false);
        jScrollPane2.setViewportView(tabelaProdutos);
        if (tabelaProdutos.getColumnModel().getColumnCount() > 0) {
            tabelaProdutos.getColumnModel().getColumn(1).setResizable(false);
        }

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Valor Total:");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Produtos Vendidos");

        lblquantidadevendida.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblquantidadevendida.setText("jLabel15");

        lbltotalprodutosvendidos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbltotalprodutosvendidos.setText("jLabel16");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 811, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblquantidadevendida)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addGap(53, 53, 53)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbltotalprodutosvendidos)
                .addGap(46, 46, 46))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(lblquantidadevendida)
                    .addComponent(lbltotalprodutosvendidos))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        tabPanel.addTab("ProdutosVendidos", jPanel1);

        tabelaLocacoes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabelaLocacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Entrada", "Saída", "Numero Quarto", "Valor Quarto", "Valor Consumo", "Desconto", "Acrescimo", "Valor Total"
            }
        ));
        tabelaLocacoes.setFocusable(false);
        jScrollPane1.setViewportView(tabelaLocacoes);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("Total Quartos:");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Total Consumo:");

        lbltQuarto.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbltQuarto.setText("jLabel17");

        lbltConsumo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbltConsumo.setText("jLabel17");

        lblContador.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblContador.setText("jLabel17");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblContador)
                .addGap(52, 52, 52)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbltQuarto)
                .addGap(54, 54, 54)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbltConsumo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(lbltQuarto)
                    .addComponent(lbltConsumo)
                    .addComponent(lblContador))
                .addGap(0, 32, Short.MAX_VALUE))
        );

        tabPanel.addTab("Locações", jPanel2);

        javax.swing.GroupLayout painelMostrarLayout = new javax.swing.GroupLayout(painelMostrar);
        painelMostrar.setLayout(painelMostrarLayout);
        painelMostrarLayout.setHorizontalGroup(
            painelMostrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMostrarLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(tabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelMostrarLayout.setVerticalGroup(
            painelMostrarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMostrarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelDadosCaixaLayout = new javax.swing.GroupLayout(painelDadosCaixa);
        painelDadosCaixa.setLayout(painelDadosCaixaLayout);
        painelDadosCaixaLayout.setHorizontalGroup(
            painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelMostrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(painelDadosCaixaLayout.createSequentialGroup()
                .addGroup(painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelDadosCaixaLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTroco, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btFechar, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelDadosCaixaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelDadosCaixaLayout.createSequentialGroup()
                                .addComponent(txtCodigoCaixa, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDataAbre, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtUserAbre, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(291, Short.MAX_VALUE))
        );
        painelDadosCaixaLayout.setVerticalGroup(
            painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosCaixaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigoCaixa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtDataAbre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(10, 10, 10)
                .addGroup(painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUserAbre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(10, 10, 10)
                .addGroup(painelDadosCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtTroco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btAbrir)
                    .addComponent(btFechar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelMostrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDadosCaixa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelBotoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelBotoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelDadosCaixa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public boolean getSair() {
        return sair;
    }
    private void btVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVoltarActionPerformed
        dispose();
    }//GEN-LAST:event_btVoltarActionPerformed

    private void txtCodigoCaixa3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoCaixa3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoCaixa3ActionPerformed

    private void txtTrocoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTrocoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTrocoActionPerformed

    private void txtUserAbreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUserAbreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUserAbreActionPerformed

    private void txtDataAbreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataAbreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataAbreActionPerformed

    private void txtCodigoCaixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoCaixaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoCaixaActionPerformed

    private void txtCodigoCaixa9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoCaixa9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoCaixa9ActionPerformed

    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed
        try {
            if (txtSaldoInicial != null) {
                float valorIniciar = Float.valueOf(txtTroco.getText());
                new fcaixa().abrirCaixa(valorIniciar);
                int idCaixa = new fcaixa().getIdCaixa();
                System.out.println("o id caixa é " + idCaixa);
                configGlobal config = configGlobal.getInstance();
                config.setCaixa(idCaixa);
            } else {
                JOptionPane.showMessageDialog(null, "Digite o valor do Troco");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Digite um valor monetário!! CABEÇÃO");
        }
        //btAtualizar.doClick();
        carregaInfo();

    }//GEN-LAST:event_btAbrirActionPerformed
    private void carregaInfo() {
        configGlobal config = configGlobal.getInstance();
        int idCaixaAtual = config.getCaixa();
        String userAbriu;
        Timestamp dataAbriu;

        if (idCaixaAtual != 0) {
            btAbrir.setEnabled(false);
            btFechar.setEnabled(true);
        }

        txtCodigoCaixa.setText(String.valueOf(idCaixaAtual));
        txtUserAbre.setText(new fcaixa().getUsuarioAbriu(idCaixaAtual));
        txtDataAbre.setText(String.valueOf(new fcaixa().getDataAbriu(idCaixaAtual)));
        float saldoInicial = new fcaixa().getValorAbriu(idCaixaAtual);
        txtSaldoInicial.setText("R$" + String.valueOf(saldoInicial));
        txtTroco.setText("R$" + String.valueOf(saldoInicial));

        txtTroco.setFocusable(false);

        valores val = new fcaixa().getValores(idCaixaAtual);
        txtDinheiro.setText("R$" + String.valueOf(val.entradaD));
        txtCartao.setText("R$" + String.valueOf(val.entradaC));
        txtPix.setText("R$" + String.valueOf(val.entradaP));

        txtLocacoes.setText("R$" + String.valueOf(val.entradaQuarto));
        txtVendas.setText("R$" + String.valueOf(val.entradaConsumo));

        List<Integer> idsLocacao = new fcaixa().getIdsLocacoes(idCaixaAtual);

        //aqui carrega desconto e acrescimos
        carregaDescontoAcrescimo(idsLocacao);
        txtDescontos.setText("R$" + String.valueOf(valorDescontos));
        txtAcrescimos.setText("R$" + String.valueOf(valorAcrescimos));
        txtEntradaTotal.setText("R$" + String.valueOf(val.entradaConsumo + val.entradaQuarto + valorAcrescimos - valorDescontos));
        txtCaixaTotal.setText("R$" + String.valueOf(val.entradaConsumo + val.entradaQuarto + saldoInicial + valorAcrescimos - valorDescontos));

        carregaTabelaProdutosVendidos();
        carregaTabelaQuartosLocados();

        //aqui carrega valores recebidos antecipados agora
        String sql = "SELECT  a.valor, a.tipo FROM registralocado rl "
                + "JOIN antecipado a ON rl.idlocacao = a.idlocacao "
                + "WHERE rl.horafim IS NULL AND a.idcaixaatual = ?";
        float valorAntecipado = 0;
        Connection link = null;
        PreparedStatement statement = null;
        ResultSet resultado = null;
        
        try {
            link = new fazconexao().conectar();
            statement = link.prepareStatement(sql);
            statement.setInt(1, idCaixaAtual);
            resultado = statement.executeQuery();

            while (resultado.next()) {
                if (!resultado.getString("tipo").equals("desconto")) {
                    valorAntecipado += resultado.getFloat("valor");
                }
            }
            if (valorAntecipado > 0) {
                txtAntecipadoAgora.setText("Tem R$" + valorAntecipado + " no caixa de recebimento antecipado");
            } else {
                txtAntecipadoAgora.setText("");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao inserir/atualizar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (resultado != null) {
                    resultado.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (link != null) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        float valorCaixasPassados = 0;
//aqui verifica se tem valores desse caixa recebidos caixa passado
        String sql2 = "SELECT rl.idlocacao, a.valor, a.tipo FROM registralocado rl "
                + "JOIN antecipado a ON rl.idlocacao = a.idlocacao "
                + "WHERE rl.idcaixaatual = ? AND rl.horafim IS NOT NULL AND a.idcaixaatual != ?";

        try (Connection conexao = new fazconexao().conectar(); PreparedStatement statement2 = conexao.prepareStatement(sql2);) {

            statement2.setInt(1, idCaixaAtual);
            statement2.setInt(2, idCaixaAtual);

            try (ResultSet resultado2 = statement2.executeQuery()) {
                while (resultado2.next()) {
                    if (!resultado2.getString("tipo").equals("desconto")) {
                        valorCaixasPassados += resultado2.getFloat("valor");
                    }
                }
                if (valorCaixasPassados > 0) {
                    txtAntecipadoCaixa.setText("Recebeu R$" + valorCaixasPassados + " em outros caixas! ");
                } else {
                    txtAntecipadoCaixa.setText("");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao consultar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    private void carregaDescontoAcrescimo(List<Integer> idsLocacao) {

        if (idsLocacao == null || idsLocacao.size() == 0) {
            valorAcrescimos = 0;
            valorDescontos = 0;
            return;
        }

        // Constrói a cláusula IN dinamicamente
        StringBuilder idsLocacaoClause = new StringBuilder();
        for (int i = 0; i < idsLocacao.size(); i++) {
            idsLocacaoClause.append(idsLocacao.get(i));
            if (i < idsLocacao.size() - 1) {
                idsLocacaoClause.append(", ");
            }
        }
        String sqlDesconto = "SELECT SUM(valor) AS totalDesconto FROM justificativa WHERE tipo = 'desconto' AND idlocacao IN (" + idsLocacaoClause.toString() + ")";
        String sqlAcrescimo = "SELECT SUM(valor) AS totalAcrescimo FROM justificativa WHERE tipo = 'acrescimo' AND idlocacao IN (" + idsLocacaoClause.toString() + ")";

        Connection link = null;
        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();

            // Consulta para calcular total de descontos
            ResultSet resultadoDesconto = statement.executeQuery(sqlDesconto);
            if (resultadoDesconto.next()) {
                valorDescontos = resultadoDesconto.getFloat("totalDesconto");
            }
            resultadoDesconto.close();

            // Consulta para calcular total de acréscimos
            ResultSet resultadoAcrescimo = statement.executeQuery(sqlAcrescimo);
            if (resultadoAcrescimo.next()) {
                valorAcrescimos = resultadoAcrescimo.getFloat("totalAcrescimo");
            }
            resultadoAcrescimo.close();

            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao calcular descontos e acréscimos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void carregaTabelaProdutosVendidos() {
        vendaProdutos venda = new vendaProdutos();
        vendaProdutos.gerenciaVenda gerenciaVenda = venda.new gerenciaVenda();
        List<vendaProdutos> vendas = gerenciaVenda.carregaLista();
        int quantidadeVendida = 0;
        float valorVendido = 0;
        DefaultTableModel modelo = (DefaultTableModel) tabelaProdutos.getModel();
        modelo.setNumRows(0);
        //JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        for (vendaProdutos v : vendas) {
            String desc = new fprodutos().getDescicao(String.valueOf(v.idProduto));
            modelo.addRow(new Object[]{
                v.idProduto,
                desc,
                v.quantidade,
                v.valorUnd,
                v.quantidade * v.valorUnd
            });
            quantidadeVendida += v.quantidade;
            valorVendido += (v.quantidade * v.valorUnd);
        }
        lbltotalprodutosvendidos.setText("R$" + String.valueOf(valorVendido));
        lblquantidadevendida.setText(String.valueOf(quantidadeVendida));
    }

    public void carregaTabelaQuartosLocados() {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        int count = 0;
        String consultaSQL = "SELECT rl.*, j.valor, j.tipo "
                + "FROM registralocado rl "
                + "LEFT JOIN justificativa j ON rl.idlocacao = j.idlocacao "
                + "WHERE rl.idcaixaatual = " + idCaixa + " "
                + "ORDER BY rl.horainicio";
        Connection link = null;
        DefaultTableModel modelo = (DefaultTableModel) tabelaLocacoes.getModel();
        modelo.setNumRows(0);
        float valQ = 0, valC = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

        try {
            link = new fazconexao().conectar();
            Statement statement = link.createStatement();
            ResultSet resultado = statement.executeQuery(consultaSQL);
            while (resultado.next()) {
                int idlocacao = resultado.getInt("idlocacao");
                float valorQuarto = resultado.getFloat("valorquarto");
                float valorConsumo = resultado.getFloat("valorconsumo");
                valQ += valorQuarto;
                valC += valorConsumo;

                // Variáveis para os valores de acréscimo e desconto
                float acrescimo = 0;
                float desconto = 0;

                // Verifica se há um valor de justificativa e classifica como acréscimo ou desconto
                if (resultado.getString("tipo") != null) {
                    if (resultado.getString("tipo").equals("acrescimo")) {
                        acrescimo = resultado.getFloat("valor");
                    }
                    if (resultado.getString("tipo").equals("desconto")) {
                        desconto = resultado.getFloat("valor");
                    }
                }
                // Formata as datas
                String horaInicio = dateFormat.format(new Date(resultado.getTimestamp("horainicio").getTime()));
                String horaFim = dateFormat.format(new Date(resultado.getTimestamp("horafim").getTime()));
                // Adiciona a linha na tabela com os dados de acréscimo, desconto e total
                modelo.addRow(new Object[]{
                    horaInicio,
                    horaFim,
                    resultado.getInt("numquarto"),
                    valorQuarto,
                    valorConsumo,
                    desconto,
                    acrescimo,
                    valorQuarto + valorConsumo + acrescimo - desconto
                });
                count++;
            }
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, e);
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        TableColumn column = null;
        for (int i = 0; i < tabelaLocacoes.getColumnCount(); i++) {
            column = tabelaLocacoes.getColumnModel().getColumn(i);
            switch (i) {
                case 0: // Hora Início
                case 1: // Hora Fim
                    column.setPreferredWidth(100);
                    break;
                case 2: // Número do Quarto
                    column.setPreferredWidth(50);
                    break;
                case 3: // Valor Quarto
                case 4: // Valor Consumo
                case 7: // Total Quarto
                    column.setPreferredWidth(50);
                    break;
                case 5: // Desconto
                case 6: // Acréscimo
                    column.setPreferredWidth(40);
                    break;
            }
        }
        lblContador.setText(count + " locações");
        lbltConsumo.setText("R$" + String.valueOf(valC));
        lbltQuarto.setText("R$" + String.valueOf(valQ));
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        configGlobal config = configGlobal.getInstance();
        int idCaixaAtual = config.getCaixa();
        System.out.println(idCaixaAtual);
        if (idCaixaAtual == 0) {
            //nenhum caixa aberto
            btFechar.setEnabled(false);
            btAbrir.setEnabled(true);
        } else {
            btAbrir.setEnabled(false);
            btFechar.setEnabled(true);

            carregaInfo();

        }

    }//GEN-LAST:event_formWindowOpened

    private void btFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFecharActionPerformed
        // fechar o caixa
        float valCaixa = 0;
        boolean fechar = false;
        // pega o valot total do caixa
        try {
            int indiceDoR = txtEntradaTotal.getText().indexOf("R$");
            if (indiceDoR != -1) {
                String valorPegar = txtEntradaTotal.getText().substring(indiceDoR + 2);
                valorPegar = valorPegar.replace(",", ".");
                valCaixa = Float.parseFloat(valorPegar);
                fechar = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fechar) {
            if (new fcaixa().fecharCaixa(valCaixa)) {
                configGlobal config = configGlobal.getInstance();
                config.setCaixa(0);
                JOptionPane.showMessageDialog(null, "Caixa Fechado com Sucesso");
                if (config.getLogoffecharcaixa()) {
                    config.setFlagFechar(true);
                    this.dispose();
                } else {
                    this.dispose();
                    new CaixaFrame().setVisible(true);
                }
            }
        }


    }//GEN-LAST:event_btFecharActionPerformed

    private void txtDescontosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescontosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescontosActionPerformed

    private void txtAcrescimosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAcrescimosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAcrescimosActionPerformed

    private void txtCaixaTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCaixaTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCaixaTotalActionPerformed

    private void txtEntradaTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEntradaTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEntradaTotalActionPerformed

    private void txtVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtVendasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtVendasActionPerformed

    private void txtDinheiroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDinheiroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDinheiroActionPerformed

    private void txtPixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPixActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPixActionPerformed

    private void txtCartaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCartaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCartaoActionPerformed

    private void txtSaldoInicialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSaldoInicialActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSaldoInicialActionPerformed

    private void txtLocacoesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLocacoesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLocacoesActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CaixaFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAbrir;
    private javax.swing.JButton btFechar;
    private javax.swing.JButton btVoltar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblContador;
    private javax.swing.JLabel lblquantidadevendida;
    private javax.swing.JLabel lbltConsumo;
    private javax.swing.JLabel lbltQuarto;
    private javax.swing.JLabel lbltotalprodutosvendidos;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelDadosCaixa;
    private javax.swing.JPanel painelMostrar;
    private javax.swing.JTabbedPane tabPanel;
    private javax.swing.JTable tabelaLocacoes;
    private javax.swing.JTable tabelaProdutos;
    private javax.swing.JTextField txtAcrescimos;
    private javax.swing.JLabel txtAntecipadoAgora;
    private javax.swing.JLabel txtAntecipadoCaixa;
    private javax.swing.JTextField txtCaixaTotal;
    private javax.swing.JTextField txtCartao;
    private javax.swing.JTextField txtCodigoCaixa;
    private javax.swing.JTextField txtCodigoCaixa3;
    private javax.swing.JTextField txtCodigoCaixa9;
    private javax.swing.JTextField txtDataAbre;
    private javax.swing.JTextField txtDescontos;
    private javax.swing.JTextField txtDinheiro;
    private javax.swing.JTextField txtEntradaTotal;
    private javax.swing.JTextField txtLocacoes;
    private javax.swing.JTextField txtPix;
    private javax.swing.JTextField txtSaldoInicial;
    private javax.swing.JTextField txtTroco;
    private javax.swing.JTextField txtUserAbre;
    private javax.swing.JTextField txtVendas;
    // End of variables declaration//GEN-END:variables

}
