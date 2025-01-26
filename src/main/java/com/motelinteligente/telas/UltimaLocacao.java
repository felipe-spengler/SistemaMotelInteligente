/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motelinteligente.telas;

import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.vendaProdutos;
import com.motelinteligente.dados.vendaProdutos.gerenciaVenda;
import static com.motelinteligente.telas.EncerraQuarto.isInteger;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author MOTEL
 */
public class UltimaLocacao extends javax.swing.JFrame {

    private int idLocacao, idCaixa;
    private List<vendaProdutos> produtos;

    /**
     * Creates new form UltimaLocacao
     */
    public UltimaLocacao(boolean editable, int numeroQuarto) {
        initComponents();

        // Inicialização de variáveis
        int numPessoas = 2;
        float valorQuarto = 0, valorConsumo = 0, valorTotal = 0, acrescimo = 0, desconto = 0;
        float recebeuD = 0, recebeuP = 0, recebeuC = 0;
        Timestamp horaInicio = null, horaFim = null;
        String justificativa = null, tipoQuarto = null;
        String consultaSQL = "SELECT * FROM registralocado WHERE idlocacao = (SELECT MAX(idlocacao) FROM registralocado WHERE numquarto = ? AND horafim IS NOT NULL)";
        Connection link = null;

        // Configuração da tabela
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
        modelo.setNumRows(0);
        tabela.getColumn(tabela.getColumnName(0)).setPreferredWidth(55);
        tabela.getColumn(tabela.getColumnName(1)).setPreferredWidth(55);
        tabela.getColumn(tabela.getColumnName(2)).setPreferredWidth(250);
        tabela.getColumn(tabela.getColumnName(3)).setPreferredWidth(120);
        tabela.getColumn(tabela.getColumnName(4)).setPreferredWidth(120);

        // Campos que nunca mudam
        
        txtTipo.setEditable(false);
        txtNumero.setEditable(false);
        txtValorTotal.setEditable(false);
        txtInicio.setEditable(false);
        txtFim.setEditable(false);
        txtNumPessoas.setEditable(false);

        // Campos que variam conforme editable
        txtValorQuarto.setEditable(editable);
        txtConsumo.setEditable(editable);
        txtAcrescimo.setEditable(editable);
        txtJustificativa.setEditable(editable);
        txtDesconto.setEditable(editable);
        txtDinheiro.setEditable(editable);
        txtPix.setEditable(editable);
        txtCartao.setEditable(editable);

        bt_apagarProduto.setEnabled(editable);
        bt_inserirProduto.setEnabled(editable);
        bt_salvar.setEnabled(editable);

        PreparedStatement statement = null;
        ResultSet resultado = null;
        PreparedStatement statementJustificativa = null;
        ResultSet resultSetJustificativa = null;

        try {
            link = new fazconexao().conectar();
            fquartos quartodao = new fquartos();
            statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, numeroQuarto);
            resultado = statement.executeQuery();

            if (resultado.next()) {
                idLocacao = resultado.getInt("idlocacao");
                setTitle("Locação " + idLocacao);
                horaInicio = resultado.getTimestamp("horainicio");
                horaFim = resultado.getTimestamp("horafim");

                // Convertendo para LocalDateTime
                LocalDateTime inicio = horaInicio.toLocalDateTime();
                LocalDateTime fim = horaFim.toLocalDateTime();

                // Calculando a diferença
                Duration duracao = Duration.between(inicio, fim);

                // Exibindo a diferença em horas e minutos
                long horas = duracao.toHours();
                long minutos = duracao.toMinutes() % 60;

                jLabel2.setText("Permanencia:");
                txtTipo.setText(horas + ":" + minutos);

                tipoQuarto = quartodao.getTipo(numeroQuarto);
                numPessoas = resultado.getInt("numpessoas");
                valorQuarto = resultado.getFloat("valorquarto");
                valorConsumo = resultado.getFloat("valorconsumo");
                recebeuD = resultado.getFloat("pagodinheiro");
                recebeuP = resultado.getFloat("pagopix");
                recebeuC = resultado.getFloat("pagocartao");
                idCaixa = resultado.getInt("idcaixaatual");

                // Verifica a tabela justificativa
                String sqlJustificativa = "SELECT * FROM justificativa WHERE idlocacao = ?";
                statementJustificativa = link.prepareStatement(sqlJustificativa);
                statementJustificativa.setInt(1, idLocacao);
                resultSetJustificativa = statementJustificativa.executeQuery();

                if (resultSetJustificativa.next()) {
                    String tipo = resultSetJustificativa.getString("tipo");
                    if ("desconto".equals(tipo)) {
                        desconto = resultSetJustificativa.getFloat("valor");
                        acrescimo = 0;
                    } else {
                        acrescimo = resultSetJustificativa.getFloat("valor");
                        desconto = 0;
                    }
                    justificativa = resultSetJustificativa.getString("justificativa");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                if (resultSetJustificativa != null) {
                    resultSetJustificativa.close();
                }
                if (statementJustificativa != null) {
                    statementJustificativa.close();
                }
                if (resultado != null) {
                    resultado.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }

        // Seta os valores nos campos
        txtDinheiro.setText(String.valueOf(recebeuD));
        txtCartao.setText(String.valueOf(recebeuC));
        txtPix.setText(String.valueOf(recebeuP));
        txtAcrescimo.setText(String.valueOf(acrescimo));
        txtDesconto.setText(String.valueOf(desconto));
        txtJustificativa.setText(justificativa);
        txtNumero.setText(String.valueOf(numeroQuarto));
        txtNumPessoas.setText(String.valueOf(numPessoas));
        txtInicio.setText(String.valueOf(horaInicio));
        txtFim.setText(String.valueOf(horaFim));
        txtValorQuarto.setText(String.valueOf(valorQuarto));
        txtConsumo.setText(String.valueOf(valorConsumo));
        txtValorTotal.setText(String.valueOf(valorQuarto + valorConsumo - desconto + acrescimo));

        // Carregar a tabela com os produtos vendidos
        vendaProdutos venda = new vendaProdutos();
        vendaProdutos.gerenciaVenda gerenciaVenda = venda.new gerenciaVenda();
        produtos = gerenciaVenda.vendidoLocacao(idLocacao);
        float valorVendido = 0;
        modelo.setNumRows(0);
        for (vendaProdutos v : produtos) {
            String desc = new fprodutos().getDescicao(String.valueOf(v.idProduto));
            modelo.addRow(new Object[]{
                v.idProduto,
                v.quantidade,
                desc,
                v.valorUnd,
                v.quantidade * v.valorUnd
            });
            valorVendido += (v.quantidade * v.valorUnd);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtNumero = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtTipo = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtInicio = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtFim = new javax.swing.JTextField();
        txtConsumo = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtValorQuarto = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtDesconto = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtAcrescimo = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtNumPessoas = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtValorTotal = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtJustificativa = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        txtDinheiro = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        txtPix = new javax.swing.JTextField();
        txtCartao = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        bt_salvar = new javax.swing.JButton();
        bt_inserirProduto = new javax.swing.JButton();
        bt_voltar = new javax.swing.JButton();
        bt_apagarProduto = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel1.setText("Nº Quarto:");

        txtNumero.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        jLabel2.setText("Tipo Quarto");

        txtTipo.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        jLabel4.setText("Início:");

        txtInicio.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        jLabel5.setText("Fim:");

        txtFim.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        txtConsumo.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel8.setText("Valor Consumo:");

        txtValorQuarto.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel9.setText("Valor Quarto:");

        txtDesconto.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        txtDesconto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescontoActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        jLabel10.setText("Acréscimo:");

        txtAcrescimo.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        jLabel11.setText("Desconto:");

        txtNumPessoas.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        txtNumPessoas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNumPessoasActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        jLabel14.setText("Motivo:");

        txtValorTotal.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel15.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel15.setText("Valor Total:");

        txtJustificativa.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        tabela.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabela.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                    {null, null, null, null, null},
                    {null, null, null, null, null},
                    {null, null, null, null, null},
                    {null, null, null, null, null}
                },
                new String[]{
                    "id", "Quantidade", "Descrição", "Valor und", "Valor Total"
                }
        ) {
            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean[]{
                false, true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabela.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane1.setViewportView(tabela);
        if (tabela.getColumnModel().getColumnCount() > 0) {
            tabela.getColumnModel().getColumn(0).setResizable(false);
            tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        }

        jLabel16.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N
        jLabel16.setText("Nº Pessoas:");

        txtDinheiro.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel19.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel19.setText("dinheiro:");

        jLabel20.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel20.setText("pix:");

        txtPix.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        txtCartao.setFont(new java.awt.Font("Lucida Fax", 0, 14)); // NOI18N

        jLabel21.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel21.setText("cartão:");

        jLabel22.setFont(new java.awt.Font("Lucida Fax", 1, 14)); // NOI18N
        jLabel22.setText("RECEBIMENTO:");

        bt_salvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_salvar.png"))); // NOI18N
        bt_salvar.setText("Salvar");
        bt_salvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_salvarActionPerformed(evt);
            }
        });

        bt_inserirProduto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_editar.png"))); // NOI18N
        bt_inserirProduto.setText("Inserir Produto");
        bt_inserirProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_inserirProdutoActionPerformed(evt);
            }
        });

        bt_voltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_sair.png"))); // NOI18N
        bt_voltar.setText("Voltar");
        bt_voltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_voltarActionPerformed(evt);
            }
        });

        bt_apagarProduto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/icon_bot_excluir.png"))); // NOI18N
        bt_apagarProduto.setText("Apagar Produto");
        bt_apagarProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_apagarProdutoActionPerformed(evt);
            }
        });
        txtConsumo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorConsumoActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(86, 86, 86)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel1)
                                                        .addComponent(jLabel4))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(txtInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(91, 91, 91)
                                                                .addComponent(jLabel5)
                                                                .addGap(12, 12, 12)
                                                                .addComponent(txtFim, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(41, 41, 41)
                                                                .addComponent(jLabel2)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(366, 366, 366)
                                                .addComponent(jLabel22))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(93, 93, 93)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 652, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addComponent(jLabel19)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(txtDinheiro, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(jLabel20)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(txtPix, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jLabel21)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(txtCartao, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(bt_voltar, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(bt_salvar, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(bt_inserirProduto)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(bt_apagarProduto))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(78, 78, 78)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel10)
                                                                        .addComponent(jLabel16)
                                                                        .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING))
                                                                .addGap(20, 20, 20))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jLabel9)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(txtNumPessoas, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(46, 46, 46)
                                                                .addComponent(jLabel15)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(txtValorTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(txtValorQuarto)
                                                                        .addComponent(txtAcrescimo, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(jLabel8)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel11)
                                                                                .addGap(15, 15, 15)))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(txtConsumo)
                                                                        .addComponent(txtDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addComponent(txtJustificativa, javax.swing.GroupLayout.PREFERRED_SIZE, 579, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(78, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2)
                                        .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(txtInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel5)
                                                .addComponent(txtFim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtValorQuarto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel8)
                                        .addComponent(txtConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(txtDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(txtAcrescimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jLabel11))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(txtJustificativa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel14))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel15)
                                                        .addComponent(txtValorTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtNumPessoas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel16)))
                                        .addComponent(jLabel10))
                                .addGap(46, 46, 46)
                                .addComponent(jLabel22)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel19)
                                        .addComponent(txtDinheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel20)
                                        .addComponent(txtPix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel21)
                                        .addComponent(txtCartao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(bt_salvar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(bt_inserirProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(bt_apagarProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(bt_voltar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private boolean isNumeroFloat(String texto) {
        // Substituir vírgulas por pontos
        texto = texto.replace(',', '.');

        // Expressão regular para verificar se a string representa um número float válido
        String regex = "^[-+]?[0-9]*\\.?[0-9]+$";

        // Verificar se a string corresponde ao padrão
        return texto.matches(regex);
    }

    private void bt_salvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_salvarActionPerformed
        // Primeiro ver se os valores fecham!
        boolean valores = false, recebido = false, positivos = false;
        float valorAcrescimo, valorDesconto, valorQuarto, valorConsumo, valorTotal;
        float recC, recD, recP;
        recC = recD = recP = 0;
        valorAcrescimo = valorDesconto = valorQuarto = valorConsumo = valorTotal = 0;
        try {
            // Validar e converter os valores dos campos
            if (!(txtCartao.getText().isEmpty()) && isNumeroFloat(txtCartao.getText())) {
                recC = Float.parseFloat(txtCartao.getText().replace(',', '.'));
            }
            if (!(txtDinheiro.getText().isEmpty()) && isNumeroFloat(txtDinheiro.getText())) {
                recD = Float.parseFloat(txtDinheiro.getText().replace(',', '.'));
            }
            if (!(txtPix.getText().isEmpty()) && isNumeroFloat(txtPix.getText())) {
                recP = Float.parseFloat(txtPix.getText().replace(',', '.'));
            }
            if (!(txtDesconto.getText().isEmpty()) && isNumeroFloat(txtDesconto.getText())) {
                valorDesconto = Float.parseFloat(txtDesconto.getText().replace(',', '.'));
            }
            if (!(txtAcrescimo.getText().isEmpty()) && isNumeroFloat(txtAcrescimo.getText())) {
                valorAcrescimo = Float.parseFloat(txtAcrescimo.getText().replace(',', '.'));
            }
            if (!(txtConsumo.getText().isEmpty()) && isNumeroFloat(txtConsumo.getText())) {
                valorConsumo = Float.parseFloat(txtConsumo.getText().replace(',', '.'));
            }
            if (!(txtValorQuarto.getText().isEmpty()) && isNumeroFloat(txtValorQuarto.getText())) {
                valorQuarto = Float.parseFloat(txtValorQuarto.getText().replace(',', '.'));
            }
            if (!(txtValorTotal.getText().isEmpty()) && isNumeroFloat(txtValorTotal.getText())) {
                valorTotal = Float.parseFloat(txtValorTotal.getText().replace(',', '.'));
            }

            // Verificar se todos os valores são positivos
            if (recC >= 0 && recD >= 0 && recP >= 0 && valorDesconto >= 0 && valorAcrescimo >= 0
                    && valorConsumo >= 0 && valorQuarto >= 0 && valorTotal >= 0) {
                positivos = true;
            }

            // Verificar se os valores fecham
            if ((valorQuarto + valorConsumo + valorAcrescimo - valorDesconto) == valorTotal) {
                valores = true;
            } else {
                JOptionPane.showMessageDialog(null, "Algo errado! Valores inconsistentes!");
            }

            // Verificar se o valor recebido é igual ao valor da conta
            if ((recC + recD + recP) == valorTotal) {
                recebido = true;
            } else {
                JOptionPane.showMessageDialog(null, "Valor recebido é diferente do valor da conta!");
            }

            // Salvar os dados se todos os critérios forem atendidos
            if (valores && recebido && positivos) {
                if (valorDesconto >= 0 && valorAcrescimo == 0 || (valorAcrescimo >= 0 && valorDesconto == 0)) {
                    salvaDados();
                } else {
                    JOptionPane.showMessageDialog(null, "Somente Desconto ou Acréscimo! Arrume essa bagunça.");
                }

            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                JOptionPane.showMessageDialog(null, "Erro_SalvarAction: " + cause.getMessage());
                cause.printStackTrace();
            } else {
                JOptionPane.showMessageDialog(null, "Erro_SalvarAction desconhecido: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_bt_salvarActionPerformed

    private void salvaDados() {
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            String updateRegistralocadoSQL = "UPDATE registralocado SET pagodinheiro = ?, pagopix = ?, pagocartao = ?, horainicio = ?, horafim = ?, valorquarto = ?, valorconsumo = ? WHERE idlocacao = ?";
            PreparedStatement statementRegistralocado = link.prepareStatement(updateRegistralocadoSQL);
            statementRegistralocado.setFloat(1, Float.parseFloat(txtDinheiro.getText().replace(',', '.')));
            statementRegistralocado.setFloat(2, Float.parseFloat(txtPix.getText().replace(',', '.')));
            statementRegistralocado.setFloat(3, Float.parseFloat(txtCartao.getText().replace(',', '.')));
            statementRegistralocado.setTimestamp(4, Timestamp.valueOf(txtInicio.getText()));
            statementRegistralocado.setTimestamp(5, Timestamp.valueOf(txtFim.getText()));
            statementRegistralocado.setFloat(6, Float.parseFloat(txtValorQuarto.getText().replace(',', '.')));
            statementRegistralocado.setFloat(7, Float.parseFloat(txtConsumo.getText().replace(',', '.')));
            statementRegistralocado.setInt(8, idLocacao); // Substitua idLocacao pelo valor correto da sua lógica
            statementRegistralocado.executeUpdate();
            System.out.println("id da locacao é " + idLocacao);
            String tipo = "";
            float valorSetar = 0;
            if (Float.parseFloat(txtDesconto.getText().replace(',', '.')) > 0) {
                tipo = "desconto";
                System.out.println("caiu desconto");
                valorSetar = Float.parseFloat(txtDesconto.getText().replace(',', '.'));
            } else if (Float.parseFloat(txtAcrescimo.getText().replace(',', '.')) > 0) {
                tipo = "acrescimo";
                System.out.println("caiu acrescimo");
                valorSetar = Float.parseFloat(txtAcrescimo.getText().replace(',', '.'));
            }
            if (valorSetar > 0) {
                String updateJustificativaSQL = "UPDATE justificativa SET tipo = ?, valor = ?,  justificativa = ? WHERE idlocacao = ?";
                PreparedStatement statementJustificativa = link.prepareStatement(updateJustificativaSQL);
                statementJustificativa.setString(1, tipo);
                statementJustificativa.setFloat(2, valorSetar);
                statementJustificativa.setString(3, txtJustificativa.getText());
                statementJustificativa.setInt(4, idLocacao);
                int rowsUpdated = statementJustificativa.executeUpdate();
                if (rowsUpdated == 0) {
                    // Nenhuma linha foi atualizada, então a justificativa não existia, devemos inseri-la
                    String insertJustificativaSQL = "INSERT INTO justificativa (idlocacao, tipo, valor, justificativa) VALUES (?, ?, ?, ?)";
                    PreparedStatement statementInsertJustificativa = link.prepareStatement(insertJustificativaSQL);
                    statementInsertJustificativa.setInt(1, idLocacao); // Substitua idLocacao pelo valor correto da sua lógica
                    statementInsertJustificativa.setString(2, tipo);
                    statementInsertJustificativa.setFloat(3, Float.parseFloat(txtDesconto.getText().replace(',', '.')));
                    statementInsertJustificativa.setFloat(4, Float.parseFloat(txtAcrescimo.getText().replace(',', '.')));
                    statementInsertJustificativa.setString(5, txtJustificativa.getText());
                    statementInsertJustificativa.executeUpdate();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SalvaDados: " + e);
        } finally {
            if (link != null) {
                try {
                    link.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex);
                }
            }
        }

        salvaVendidos();
    }

    private void salvaVendidos() {
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            String deleteSQL = "DELETE FROM registravendido WHERE idlocacao = ?";
            PreparedStatement deleteStatement = link.prepareStatement(deleteSQL);
            deleteStatement.setInt(1, idLocacao); 
            int rowsAffected = deleteStatement.executeUpdate();
            link.close();
            deleteStatement.close();

            reinsereProdutos();
            JOptionPane.showMessageDialog(null, " Registro Alterado Com Sucesso");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SalvaVendidos:" + e);
            e.printStackTrace();
        } finally {
            // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
            if (link != null) {
                try {
                    link.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex);
                }
            }
        }
    }

    private void reinsereProdutos() {
        PreparedStatement insertStatement = null;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            String insertSQL = "INSERT INTO registravendido (idlocacao, idproduto, quantidade, valorunidade, valortotal, idcaixaatual) VALUES (?, ?, ?, ?, ?, ?)";
            insertStatement = link.prepareStatement(insertSQL);

            int totalInserted = 0; // Contador para o número de registros inseridos

            for (vendaProdutos produto : produtos) {
                insertStatement.setInt(1, idLocacao);
                insertStatement.setInt(2, produto.idProduto);
                insertStatement.setInt(3, produto.quantidade);
                insertStatement.setFloat(4, produto.valorUnd);
                insertStatement.setFloat(5, produto.valorTotal);
                insertStatement.setInt(6, idCaixa);
                int rowsAffected = insertStatement.executeUpdate();
                totalInserted += rowsAffected;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao inserir registros: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (insertStatement != null) {
                try {
                    insertStatement.close();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Erro ao fechar statement: " + e.getMessage());
                }
            }
            if (link != null) {
                try {
                    link.close();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Erro ao fechar conexão: " + e.getMessage());
                }
            }
        }
    }

    private void bt_inserirProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_inserirProdutoActionPerformed
        obterProduto();
    }//GEN-LAST:event_bt_inserirProdutoActionPerformed

    private void obterProduto() {
        // Criação do campo de texto para o ID do produto
        JTextField txtIdProduto = new JTextField();
        txtIdProduto.setColumns(10);

        // Criação do campo de texto para a quantidade
        JTextField txtQuantidade = new JTextField();
        txtQuantidade.setColumns(10);

        // Label para exibir a descrição do produto
        JLabel lblDescricaoProduto = new JLabel();
        lblDescricaoProduto.setPreferredSize(new Dimension(200, 20));

        // Adiciona um DocumentListener ao campo de texto para monitorar as mudanças no texto
        txtIdProduto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarDescricaoProduto();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isInteger(txtIdProduto.getText())) {
                    atualizarDescricaoProduto();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            // Método para atualizar a descrição do produto com base no ID do produto
            private void atualizarDescricaoProduto() {
                String descricao = new fprodutos().getDescicao(txtIdProduto.getText());
                lblDescricaoProduto.setText(descricao);
            }
        });

        // Cria um painel para organizar os componentes
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("ID do Produto:"));
        panel.add(txtIdProduto);
        panel.add(new JLabel("Quantidade:"));
        panel.add(txtQuantidade);
        panel.add(new JLabel("Descrição do Produto:"));
        panel.add(lblDescricaoProduto);

        int result = JOptionPane.showConfirmDialog(null, panel, "Obter Produto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        txtIdProduto.grabFocus();
        if (result == JOptionPane.OK_OPTION) {
            // Aqui você pode usar os valores inseridos pelo usuário, por exemplo:
            String idProdutoStr = txtIdProduto.getText();
            String quantidadeStr = txtQuantidade.getText();
            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();

            if (isInteger(idProdutoStr)) {
                fprodutos produtodao = new fprodutos();
                String texto = produtodao.getDescicao(idProdutoStr);
                if (texto != null) {
                    if (quantidadeStr != null) {
                        float valor = produtodao.getValorProduto(Integer.parseInt(idProdutoStr));
                        float valorSoma = valor * Integer.parseInt(quantidadeStr);
                        modelo.addRow(new Object[]{
                            idProdutoStr,
                            quantidadeStr,
                            texto,
                            valor,
                            valorSoma
                        });
                        vendaProdutos vendido = new vendaProdutos(Integer.valueOf(idProdutoStr), Integer.valueOf(quantidadeStr), valor, valorSoma);
                        produtos.add(vendido);
                        float valorConsumo = 0;
                        //atualiza valor consumo
                        if (!(txtConsumo.getText().isEmpty()) && isNumeroFloat(txtConsumo.getText())) {
                            valorConsumo = Float.parseFloat(txtConsumo.getText().replace(',', '.'));
                        }
                        valorConsumo += valorSoma;
                        txtConsumo.setText(String.valueOf(valorConsumo));

                        //atualiza valor total
                        atualizaTotal();
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Quantidade inválida!");

                    }
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Código inserido invalido!");
                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Digite um valor válido!");
            }
        }
    }

    public void atualizaTotal() {
        float valorDesconto = 0, valorAcrescimo = 0, valorConsumo = 0, valorQuarto = 0;
        if (!(txtDesconto.getText().isEmpty()) && isNumeroFloat(txtDesconto.getText())) {
            valorDesconto = Float.parseFloat(txtDesconto.getText().replace(',', '.'));
        }
        if (!(txtAcrescimo.getText().isEmpty()) && isNumeroFloat(txtAcrescimo.getText())) {
            valorAcrescimo = Float.parseFloat(txtAcrescimo.getText().replace(',', '.'));
        }
        if (!(txtConsumo.getText().isEmpty()) && isNumeroFloat(txtConsumo.getText())) {
            valorConsumo = Float.parseFloat(txtConsumo.getText().replace(',', '.'));
        }
        if (!(txtValorQuarto.getText().isEmpty()) && isNumeroFloat(txtValorQuarto.getText())) {
            valorQuarto = Float.parseFloat(txtValorQuarto.getText().replace(',', '.'));
        }
        float valorTotal = valorAcrescimo + valorQuarto + valorConsumo - valorDesconto;
        txtValorTotal.setText(String.valueOf(valorTotal));

    }

    private void bt_voltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_voltarActionPerformed
        dispose();
    }//GEN-LAST:event_bt_voltarActionPerformed

    private void bt_apagarProdutoActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();

        int selectedRow;
        selectedRow = tabela.getSelectedRow();
        if (selectedRow != -1) {
            int quantidade = (int) modelo.getValueAt(selectedRow, 1);
            int idProduto = (int) modelo.getValueAt(selectedRow, 0);
            float valorProduto = (float) modelo.getValueAt(selectedRow, 4);
            modelo.removeRow(selectedRow);

            float valorConsumo = 0;
            //atualiza valor consumo
            if (!(txtConsumo.getText().isEmpty()) && isNumeroFloat(txtConsumo.getText().replace(',', '.'))) {
                valorConsumo = Float.parseFloat(txtConsumo.getText().replace(',', '.'));
            }
            valorConsumo -= valorProduto;
            txtConsumo.setText(String.valueOf(valorConsumo));
            Iterator<vendaProdutos> iterator = produtos.iterator();
            while (iterator.hasNext()) {
                vendaProdutos produto = iterator.next();
                if (produto.idProduto == idProduto && produto.quantidade == quantidade && produto.valorTotal == valorProduto) {
                    iterator.remove();
                    System.out.println("retirou da lista");
                    break; // Assume que estamos removendo apenas uma ocorrência do produto
                }
            }
            //atualiza valor total
            atualizaTotal();

        } else {
            JOptionPane.showMessageDialog(null, "Nenhum produto selecionado!");
        }
    }

    public void realizaDesconto() {
        float valorQuarto = 0, valorConsumo = 0;
        float desconto = 0;
        if (!(txtValorQuarto.getText().isEmpty()) && isNumeroFloat(txtValorQuarto.getText().replace(',', '.'))) {
            valorQuarto = Float.parseFloat(txtValorQuarto.getText().replace(',', '.'));
        }
        if (!(txtConsumo.getText().isEmpty()) && isNumeroFloat(txtConsumo.getText().replace(',', '.'))) {
            valorConsumo = Float.parseFloat(txtConsumo.getText().replace(',', '.'));
        }
        if (!(txtDesconto.getText().isEmpty()) && isNumeroFloat(txtDesconto.getText().replace(',', '.'))) {
            desconto = Float.parseFloat(txtDesconto.getText().replace(',', '.'));
        }

        if (desconto > 0) {
            //seta valor total
            float valorTotal = valorConsumo + valorQuarto - desconto;
            txtValorTotal.setText(String.valueOf(valorTotal));
        }
    }

    private void txtDescontoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescontoActionPerformed
        //dar desconto
        realizaDesconto();

    }//GEN-LAST:event_txtDescontoActionPerformed

    public void realizaAcrescimo() {
        float valorQuarto = 0, valorConsumo = 0;
        float acrescimo = 0, desconto = 0;
        if (!(txtValorQuarto.getText().isEmpty()) && isNumeroFloat(txtValorQuarto.getText().replace(',', '.'))) {
            valorQuarto = Float.parseFloat(txtValorQuarto.getText().replace(',', '.'));
        }
        if (!(txtConsumo.getText().isEmpty()) && isNumeroFloat(txtConsumo.getText().replace(',', '.'))) {
            valorConsumo = Float.parseFloat(txtConsumo.getText().replace(',', '.'));
        }
        if (!(txtAcrescimo.getText().isEmpty()) && isNumeroFloat(txtAcrescimo.getText().replace(',', '.'))) {
            acrescimo = Float.parseFloat(txtAcrescimo.getText().replace(',', '.'));
        }
        if (!(txtDesconto.getText().isEmpty()) && isNumeroFloat(txtDesconto.getText().replace(',', '.'))) {
            desconto = Float.parseFloat(txtDesconto.getText().replace(',', '.'));
        }

        if (acrescimo > 0 && desconto < 1) {
            //seta valor total
            float valorTotal = valorConsumo + valorQuarto + acrescimo;
            txtValorTotal.setText(String.valueOf(valorTotal));
        } else {
            JOptionPane.showMessageDialog(null, "Somente Desconto ou Acréscimo! Arrume essa bagunça.");
        }
    }
    private void txtValorConsumoActionPerformed(java.awt.event.ActionEvent evt) {
        atualizaTotal();
    }
    private void txtNumPessoasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNumPessoasActionPerformed

    }//GEN-LAST:event_txtNumPessoasActionPerformed

    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bt_apagarProduto;
    private javax.swing.JButton bt_inserirProduto;
    private javax.swing.JButton bt_salvar;
    private javax.swing.JButton bt_voltar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtAcrescimo;
    private javax.swing.JTextField txtCartao;
    private javax.swing.JTextField txtConsumo;
    private javax.swing.JTextField txtDesconto;
    private javax.swing.JTextField txtDinheiro;
    private javax.swing.JTextField txtFim;
    private javax.swing.JTextField txtInicio;
    private javax.swing.JTextField txtJustificativa;
    private javax.swing.JTextField txtNumPessoas;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtPix;
    private javax.swing.JTextField txtTipo;
    private javax.swing.JTextField txtValorQuarto;
    private javax.swing.JTextField txtValorTotal;
    // End of variables declaration//GEN-END:variables
}
