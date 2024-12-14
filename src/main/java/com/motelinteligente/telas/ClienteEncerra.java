/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motelinteligente.telas;


import com.motelinteligente.dados.configGlobal;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author MOTEL
 */
public class ClienteEncerra extends javax.swing.JFrame {

    /**
     * Creates new form ClienteEncerra
     */
    class CentralizarCelulasRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Configurando a centralização do texto
            if (cellComponent instanceof JLabel) {
                ((JLabel) cellComponent).setHorizontalAlignment(JLabel.CENTER);
            }

            return cellComponent;
        }
    }
    public ClienteEncerra() {
        initComponents();

        // Obtém a tela a mostrar da instância global
        String telaMostrar = configGlobal.getInstance().getTelaMostrar();

        // Verifica se a telaMostrar não é nula ou vazia
        if (telaMostrar != null && !telaMostrar.isEmpty()) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            // Encontra o dispositivo correspondente ao ID da tela
            for (GraphicsDevice screen : screens) {
                if (screen.getIDstring().equals(telaMostrar)) {
                    // Define a localização da janela com base na tela selecionada
                    this.setLocation(screen.getDefaultConfiguration().getBounds().x, 0);
                    break; // Para sair do loop após encontrar a tela correspondente
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nenhuma tela selecionada. Usando a tela padrão.");
        }

        // Maximiza a janela
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);

        // Configuração da tabela
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
        modelo.setNumRows(0);
        tabela.getColumn(tabela.getColumnName(0)).setPreferredWidth(55);
        tabela.getColumn(tabela.getColumnName(1)).setPreferredWidth(250);
        tabela.getColumn(tabela.getColumnName(2)).setPreferredWidth(120);
        tabela.getColumn(tabela.getColumnName(3)).setPreferredWidth(120);
    }

    public void setaDatas(String dataInicio, String dataFim, String tempoTotalLocado) {
        lblInicioLocacao.setText(formatarData(dataInicio));
        lblFimLocacao.setText(formatarData(dataFim));

        //setar tempo locado
        lblTempoLocado.setText(tempoTotalLocado);
    }
    public static String formatarData(String dataOriginal) {
        // Definindo o formato da string de data original
        SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

        try {
            // Fazendo o parsing da string de data original para um objeto Date
            Date data = formatoOriginal.parse(dataOriginal);

            // Definindo o formato desejado
            SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM HH:mm");

            // Formatando a data para o formato desejado
            return formatoDesejado.format(data);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Erro na função FORMATARDATA: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void setPessoas(int numeroPessoas) {
        lblPessoasQuarto.setText(String.valueOf(numeroPessoas));
    }

    public void setarValores(float valorQuarto, float valorAdicionalPeriodo) {
        lblHoraAdicional.setText(String.valueOf(valorAdicionalPeriodo) + "0");
        lblValorQuarto.setText(String.valueOf(valorQuarto) + "0");
    }

    public void setValorTotal(float valorTotal) {
        lblValorTotal.setText("R$" + String.valueOf(valorTotal) + "0");
    }

    public void setValorQuarto(float valorQuarto) {
        lblValorQuarto.setText("R$" + String.valueOf(valorQuarto) + "0");
    }

    public void adicionaTabela(String id, String quantidade, String texto, float valor, float valorSoma) {
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
        modelo.addRow(new Object[]{
            quantidade,
            texto,
            valor,
            valorSoma
        });
        tabela.setDefaultRenderer(Object.class, new CentralizarCelulasRenderer());

    }
    

    public void setConsumo(float valorConsumo) {
        SwingUtilities.invokeLater(() -> {
            lblValorConsumo.setText(String.valueOf("R$" + valorConsumo + "0"));
            lblValorConsumo2.setText(String.valueOf("R$" + valorConsumo + "0"));

        });
    }
    public void setTitulo(int numero) {
        SwingUtilities.invokeLater(() -> {
            lblTitulo.setText("Encerramento do Quarto " + numero);

        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelInfo = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblInicioLocacao = new javax.swing.JLabel();
        lblFimLocacao = new javax.swing.JLabel();
        lblValorQuarto = new javax.swing.JLabel();
        lblHoraAdicional = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblTempoLocado = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblValorConsumo2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        painelRecebimento = new javax.swing.JPanel();
        lblValorTotal = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblPessoasQuarto = new javax.swing.JLabel();
        painelProdutos = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();
        lblValorConsumo = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        painelInfo.setBackground(new java.awt.Color(204, 204, 204));
        painelInfo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel1.setText("Inicio da Locação:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel3.setText("Fim da Locação:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel5.setText("Valor Quarto:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel6.setText("Hora Adicional:");

        lblInicioLocacao.setBackground(new java.awt.Color(51, 51, 255));
        lblInicioLocacao.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lblInicioLocacao.setText("jLabel9");

        lblFimLocacao.setBackground(new java.awt.Color(51, 51, 255));
        lblFimLocacao.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lblFimLocacao.setText("jLabel9");

        lblValorQuarto.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblValorQuarto.setForeground(new java.awt.Color(51, 51, 255));
        lblValorQuarto.setText("jLabel9");

        lblHoraAdicional.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblHoraAdicional.setForeground(new java.awt.Color(51, 51, 255));
        lblHoraAdicional.setText("R$0.00");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel9.setText("Tempo Locado:");

        lblTempoLocado.setBackground(new java.awt.Color(51, 51, 255));
        lblTempoLocado.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lblTempoLocado.setText("jLabel9");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel10.setText("Pessoas no Quarto:");

        lblValorConsumo2.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblValorConsumo2.setForeground(new java.awt.Color(51, 51, 255));
        lblValorConsumo2.setText("R$0.00");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel4.setText("Valor do Consumo:");

        jPanel1.setBackground(new java.awt.Color(255, 241, 239));

        lblValorTotal.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblValorTotal.setForeground(new java.awt.Color(255, 51, 0));
        lblValorTotal.setText("0,00");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel12.setText("Valor Total:");

        javax.swing.GroupLayout painelRecebimentoLayout = new javax.swing.GroupLayout(painelRecebimento);
        painelRecebimento.setLayout(painelRecebimentoLayout);
        painelRecebimentoLayout.setHorizontalGroup(
            painelRecebimentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelRecebimentoLayout.createSequentialGroup()
                .addGroup(painelRecebimentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelRecebimentoLayout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addComponent(jLabel12))
                    .addGroup(painelRecebimentoLayout.createSequentialGroup()
                        .addGap(117, 117, 117)
                        .addComponent(lblValorTotal)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelRecebimentoLayout.setVerticalGroup(
            painelRecebimentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelRecebimentoLayout.createSequentialGroup()
                .addContainerGap(134, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblValorTotal)
                .addGap(102, 102, 102))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelRecebimento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(41, Short.MAX_VALUE)
                .addComponent(painelRecebimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        lblPessoasQuarto.setBackground(new java.awt.Color(51, 51, 255));
        lblPessoasQuarto.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lblPessoasQuarto.setText("2");

        javax.swing.GroupLayout painelInfoLayout = new javax.swing.GroupLayout(painelInfo);
        painelInfo.setLayout(painelInfoLayout);
        painelInfoLayout.setHorizontalGroup(
            painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(painelInfoLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4))
                        .addGap(32, 32, 32)
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblValorQuarto)
                            .addComponent(lblHoraAdicional)
                            .addComponent(lblValorConsumo2)))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addGap(46, 46, 46)
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblInicioLocacao)
                            .addComponent(lblFimLocacao)))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(63, 63, 63)
                        .addComponent(lblTempoLocado))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(70, 70, 70)
                        .addComponent(lblPessoasQuarto)))
                .addContainerGap(10, Short.MAX_VALUE))
        );
        painelInfoLayout.setVerticalGroup(
            painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelInfoLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblInicioLocacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblFimLocacao))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTempoLocado)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPessoasQuarto)
                    .addComponent(jLabel10))
                .addGap(37, 37, 37)
                .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addComponent(lblValorQuarto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHoraAdicional))
                    .addGroup(painelInfoLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(lblValorConsumo2))))
                .addGap(31, 31, 31)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelProdutos.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("Consumo:");

        tabela.setFont(new java.awt.Font("Segoe UI Historic", 0, 16)); // NOI18N
        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Quantidade", "Descrição", "Valor und", "Valor Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabela.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(tabela);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel11.setText("Valor do Consumo:");

        lblValorConsumo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblValorConsumo.setForeground(new java.awt.Color(0, 51, 255));
        lblValorConsumo.setText("R$0.00");

        javax.swing.GroupLayout painelProdutosLayout = new javax.swing.GroupLayout(painelProdutos);
        painelProdutos.setLayout(painelProdutosLayout);
        painelProdutosLayout.setHorizontalGroup(
            painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(28, 28, 28)
                .addComponent(lblValorConsumo)
                .addGap(177, 177, 177))
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(139, Short.MAX_VALUE))
        );
        painelProdutosLayout.setVerticalGroup(
            painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(lblValorConsumo))
                .addContainerGap(163, Short.MAX_VALUE))
        );

        lblTitulo.setFont(new java.awt.Font("Sitka Small", 1, 48)); // NOI18N
        lblTitulo.setText("Encerramento - Quarto x");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(painelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(painelProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 788, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(205, 205, 205))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(painelProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(ClienteEncerra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClienteEncerra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClienteEncerra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClienteEncerra.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClienteEncerra();
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFimLocacao;
    private javax.swing.JLabel lblHoraAdicional;
    private javax.swing.JLabel lblInicioLocacao;
    private javax.swing.JLabel lblPessoasQuarto;
    private javax.swing.JLabel lblTempoLocado;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblValorConsumo;
    private javax.swing.JLabel lblValorConsumo2;
    private javax.swing.JLabel lblValorQuarto;
    private javax.swing.JLabel lblValorTotal;
    private javax.swing.JPanel painelInfo;
    private javax.swing.JPanel painelProdutos;
    private javax.swing.JPanel painelRecebimento;
    private javax.swing.JTable tabela;
    // End of variables declaration//GEN-END:variables
}
