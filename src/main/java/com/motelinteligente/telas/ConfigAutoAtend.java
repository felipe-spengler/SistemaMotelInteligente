/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motelinteligente.telas;


import com.motelinteligente.dados.fazconexao;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import java.sql.Statement;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import com.motelinteligente.telas.ConferenciaLocacoes.CustomCellRenderer;

/**
 *
 * @author johnc
 */
public class ConfigAutoAtend extends javax.swing.JFrame {

    /**
     * Creates new form ConfiguracoesAdicionais
     */
    public ConfigAutoAtend() {
        initComponents();
        carregarTabela();
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioCliente);
        buttonGroup.add(radioSistema);
        RadioButtonHandler radioClienteSistema = new RadioButtonHandler();
        radioCliente.addItemListener(radioClienteSistema);
        radioSistema.addItemListener(radioClienteSistema);
        

        subAtiva.addActionListener(e -> {
            if (subAtiva.isSelected()) {
                subDesativa.setSelected(false);
                //seta no banco de dados também

            }
        });

        subDesativa.addActionListener(e -> {
            if (subDesativa.isSelected()) {
                subAtiva.setSelected(false);
            }
        });
        //carregar o fundoAbertura
        carregarFundoAbertura();

        //carregar direcionamento
        carregarDirecionamento();

        //carregar o cabeçalho
        carregarCabecalho();
    }

    private class RadioButtonHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (radioCliente.isSelected()) {
                // altera no banco
            }
            if (radioSistema.isSelected()) {
                 // altera no banco
            }
        }

    }

    public void carregarFundoAbertura() {
        if (temImagem("fundoAbertura") != 0) {
            ImageIcon fundoAbertura = obterImagemDoBanco("fundoAbertura");
            ImageIcon fundoOK = redimensionarIcon(fundoAbertura, 296, 296);
            label4.setText("");
            label4.setIcon(fundoOK);
        } else {
            label4.setText("Nenhuma Imagem");
        }
    }

    public void carregarDirecionamento() {
        if (temImagem("direcionamento") != 0) {
            ImageIcon direcionamento = obterImagemDoBanco("direcionamento");
            ImageIcon direcOK = redimensionarIcon(direcionamento, 296, 296);
            label2.setText("");
            label2.setIcon(direcOK);
        } else {
            label2.setText("Nenhuma Imagem");
        }
    }

    public void carregarCabecalho() {
        if (temImagem("cabecalho") != 0) {
            ImageIcon cabecalho = obterImagemDoBanco("cabecalho");
            ImageIcon cabeOK = redimensionarIcon(cabecalho, 292, 292);
            label3.setText("");
            label3.setIcon(cabeOK);
        } else {
            label3.setText("Nenhuma Imagem");
        }
    }

    public void carregarTabela() {
        Connection link = null;
        DefaultTableModel modelo = (DefaultTableModel) tabelaTipoQuarto.getModel();
        modelo.setNumRows(0);
        try {
            link = new fazconexao().conectar();
            String consultaSQL = "select tipoquarto from quartos order by numeroquarto";
            PreparedStatement statement = link.prepareStatement(consultaSQL);

            ResultSet resultado = statement.executeQuery();

            Set<String> tiposQuartoAdicionados = new HashSet<>(); // Conjunto para armazenar os tipos de quarto adicionados
            TableColumn column = tabelaTipoQuarto.getColumnModel().getColumn(0);
            column.setCellRenderer(new CustomCellRenderer());
            while (resultado.next()) {
                String tipoQuarto = resultado.getString("tipoquarto");

                if (!tiposQuartoAdicionados.contains(tipoQuarto)) {
                    modelo.addRow(new String[]{tipoQuarto});
                    tiposQuartoAdicionados.add(tipoQuarto); // Adiciona o tipo de quarto ao conjunto
                }
            }

            // Fechar os recursos
            resultado.close();
            statement.close();
            link.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    class CustomCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String tipoQuarto = (String) value; // Obtém o valor da célula

            if (temImagem(tipoQuarto) != 0) {
                cellComponent.setBackground(Color.GREEN);
            } else {
                cellComponent.setBackground(Color.RED);
            }

            return cellComponent;
        }
    }

    public int temImagem(String tipoQuarto) {
        String sql = "SELECT COUNT(*) AS total FROM imagens WHERE nome_da_imagem = ?";
        Connection link = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(sql);

            statement.setString(1, tipoQuarto);
            ResultSet resultado = statement.executeQuery();
            if (resultado.next()) {
                return resultado.getInt("total"); // Obtém o valor de contagem da coluna 'total'
            } else {
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0; // Retorna 0 caso não haja correspondências ou ocorra algum erro
    }

    private BufferedImage redimensionarImagem(BufferedImage originalImage, int largura, int altura) {
        if (originalImage == null) {
            return null;
        }
        try {
            double ratio = (double) largura / originalImage.getWidth();
            int newHeight = (int) (originalImage.getHeight() * ratio);

            BufferedImage resizedImage = new BufferedImage(largura, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, largura, newHeight, null);
            g.dispose();
            return resizedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ImageIcon redimensionarIcon(ImageIcon originalIcon, int largura, int altura) {
        if (originalIcon == null) {
            return null;
        }

        // Convertendo ImageIcon para BufferedImage
        Image originalImage = originalIcon.getImage();
        BufferedImage bufferedOriginal = new BufferedImage(originalImage.getWidth(null), originalImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedOriginal.createGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();

        // Redimensionando a imagem
        BufferedImage resizedImage = redimensionarImagem(bufferedOriginal, largura, altura);

        // Convertendo BufferedImage para ImageIcon
        if (resizedImage != null) {
            return new ImageIcon(resizedImage);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txtPath = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        radioCliente = new javax.swing.JRadioButton();
        radioSistema = new javax.swing.JRadioButton();
        jPanel12 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        subAtiva = new javax.swing.JRadioButton();
        subDesativa = new javax.swing.JRadioButton();
        painelImagens = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        painelImagem1 = new javax.swing.JPanel();
        label1 = new javax.swing.JLabel();
        label4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        mudar1 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        mudar2 = new javax.swing.JButton();
        painelDirec = new javax.swing.JPanel();
        label2 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        mudar3 = new javax.swing.JButton();
        painelImagem5 = new javax.swing.JPanel();
        label3 = new javax.swing.JLabel();
        btVer = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        btAlterar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaTipoQuarto = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setBackground(new java.awt.Color(204, 204, 204));
        jLabel1.setFont(new java.awt.Font("Sitka Display", 1, 24)); // NOI18N
        jLabel1.setText("Configurações Auto Atendimento");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(421, 421, 421)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        jTabbedPane1.setForeground(new java.awt.Color(0, 0, 255));
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N

        txtPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPathActionPerformed(evt);
            }
        });

        jButton1.setText("Procurar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Mensagem Auto Atendimento:");

        jLabel3.setText("________________________________________________________________________________________________________________________________________________________________");

        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel4.setText("Definir o número do Quarto após selecionar Tipo:");

        radioCliente.setText("Cliente Seleciona o  número desejado de Quarto");

        radioSistema.setText("Sistema Define qual o número do Quarto");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioCliente)
                    .addComponent(radioSistema))
                .addGap(0, 147, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioCliente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioSistema)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel11.setText("SubTela para cliente confirmar e escolher entre período e Pernoite");

        subAtiva.setText("Ativado");

        subDesativa.setText("Desativado");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subAtiva)
                    .addComponent(subDesativa)
                    .addComponent(jLabel11))
                .addGap(0, 10, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subAtiva)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subDesativa)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 803, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(6, 6, 6)
                                .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(491, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(443, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Configurações", jPanel3);

        painelImagens.setBackground(new java.awt.Color(255, 255, 255));

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        painelImagem1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        painelImagem1.setPreferredSize(new java.awt.Dimension(300, 300));

        label1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        label4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout painelImagem1Layout = new javax.swing.GroupLayout(painelImagem1);
        painelImagem1.setLayout(painelImagem1Layout);
        painelImagem1Layout.setHorizontalGroup(
            painelImagem1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
            .addGroup(painelImagem1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelImagem1Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(label4, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        painelImagem1Layout.setVerticalGroup(
            painelImagem1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
            .addGroup(painelImagem1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelImagem1Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(label4, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jLabel5.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Fundo Tela ENTRAR");

        mudar1.setText("MUDAR");
        mudar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mudar1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mudar1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(painelImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(mudar1))
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel6.setPreferredSize(new java.awt.Dimension(322, 365));

        jLabel8.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Fundo Tela INDICAR QUARTO");

        mudar2.setText("MUDAR");
        mudar2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mudar2ActionPerformed(evt);
            }
        });

        painelDirec.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        painelDirec.setPreferredSize(new java.awt.Dimension(300, 300));

        label2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout painelDirecLayout = new javax.swing.GroupLayout(painelDirec);
        painelDirec.setLayout(painelDirecLayout);
        painelDirecLayout.setHorizontalGroup(
            painelDirecLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
            .addGroup(painelDirecLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelDirecLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(label2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        painelDirecLayout.setVerticalGroup(
            painelDirecLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
            .addGroup(painelDirecLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelDirecLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(label2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelDirec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mudar2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDirec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(mudar2))
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel8.setPreferredSize(new java.awt.Dimension(322, 365));

        jLabel10.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Cabeçalho");

        mudar3.setText("MUDAR");
        mudar3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mudar3ActionPerformed(evt);
            }
        });

        painelImagem5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        painelImagem5.setPreferredSize(new java.awt.Dimension(300, 300));

        label3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout painelImagem5Layout = new javax.swing.GroupLayout(painelImagem5);
        painelImagem5.setLayout(painelImagem5Layout);
        painelImagem5Layout.setHorizontalGroup(
            painelImagem5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );
        painelImagem5Layout.setVerticalGroup(
            painelImagem5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label3, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mudar3))
                    .addComponent(painelImagem5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelImagem5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10)
                    .addComponent(mudar3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        btVer.setText("VER IMAGEM");
        btVer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVerActionPerformed(evt);
            }
        });

        jPanel9.setBackground(new java.awt.Color(0, 204, 51));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel9.setMaximumSize(new java.awt.Dimension(23, 22));
        jPanel9.setMinimumSize(new java.awt.Dimension(23, 22));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Tem Imagem Definida");

        jLabel7.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Red"));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Não possui imagem");

        jPanel10.setBackground(new java.awt.Color(255, 51, 0));
        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel10.setMaximumSize(new java.awt.Dimension(23, 22));
        jPanel10.setMinimumSize(new java.awt.Dimension(23, 22));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        btAlterar.setText("ALTERAR IMAGEM");
        btAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAlterarActionPerformed(evt);
            }
        });

        tabelaTipoQuarto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabelaTipoQuarto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Tipo Quarto"
            }
        ));
        jScrollPane1.setViewportView(tabelaTipoQuarto);

        javax.swing.GroupLayout painelImagensLayout = new javax.swing.GroupLayout(painelImagens);
        painelImagens.setLayout(painelImagensLayout);
        painelImagensLayout.setHorizontalGroup(
            painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelImagensLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelImagensLayout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelImagensLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelImagensLayout.createSequentialGroup()
                                .addGap(68, 68, 68)
                                .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelImagensLayout.createSequentialGroup()
                                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel7))
                                    .addGroup(painelImagensLayout.createSequentialGroup()
                                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel6))))
                            .addGroup(painelImagensLayout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btAlterar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btVer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                .addContainerGap(383, Short.MAX_VALUE))
        );
        painelImagensLayout.setVerticalGroup(
            painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelImagensLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(48, 48, 48)
                .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelImagensLayout.createSequentialGroup()
                        .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelImagensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btAlterar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btVer)
                        .addGap(67, 67, 67))
                    .addGroup(painelImagensLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(21, Short.MAX_VALUE))))
        );

        jTabbedPane1.addTab("Imagens", painelImagens);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTabbedPane1))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPathActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

    }//GEN-LAST:event_jButton1ActionPerformed

    private void mudar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mudar1ActionPerformed
        funcaoAlteraImagem("fundoAbertura");
        carregarFundoAbertura();

    }//GEN-LAST:event_mudar1ActionPerformed

    private void btVerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVerActionPerformed
        // botao para ver a imagem
        int selectedRow = tabelaTipoQuarto.getSelectedRow();
        if (selectedRow != -1) { // Verifica se uma linha está selecionada
            String tipoQuarto = (String) tabelaTipoQuarto.getValueAt(selectedRow, 0);

            // Lógica para obter e exibir a imagem com base no tipo de quarto
            exibirImagem(tipoQuarto);
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um tipo de quarto para visualizar a imagem.");
        }
    }//GEN-LAST:event_btVerActionPerformed
    public void funcaoAlteraImagem(String nomeArquivo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione uma imagem");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();

            try {
                Connection conexao = new fazconexao().conectar();
                String query = "SELECT id FROM imagens WHERE nome_da_imagem = ?";
                PreparedStatement statement = conexao.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, nomeArquivo);
                ResultSet resultadoQuery = statement.executeQuery();

                while (resultadoQuery.next()) {
                    // Se já existe uma imagem com esse nome, exclua-a do banco de dados
                    int idExistente = resultadoQuery.getInt("id");
                    excluirImagemDoBanco(idExistente);
                }

                // Insira a nova imagem no banco de dados
                byte[] imagemBytes = Files.readAllBytes(arquivoSelecionado.toPath());
                inserirImagemNoBanco(nomeArquivo, imagemBytes);

                // Limpeza de recursos
                resultadoQuery.close();
                statement.close();
                conexao.close();

                JOptionPane.showMessageDialog(this, "Imagem atualizada com sucesso.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ocorreu um erro ao atualizar a imagem.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nenhuma linha selecionaada!");
        }
    }
    private void btAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAlterarActionPerformed

        int selectedRow = tabelaTipoQuarto.getSelectedRow();
        if (selectedRow >= 0) {
            Object conteudoSelecionado = tabelaTipoQuarto.getValueAt(selectedRow, 0);
            String nomeArquivo = String.valueOf(conteudoSelecionado);
            funcaoAlteraImagem(nomeArquivo);

        }
        carregarTabela();
    }//GEN-LAST:event_btAlterarActionPerformed

    private void mudar2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mudar2ActionPerformed
        funcaoAlteraImagem("direcionamento");
        carregarDirecionamento();
    }//GEN-LAST:event_mudar2ActionPerformed

    private void mudar3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mudar3ActionPerformed
        funcaoAlteraImagem("cabecalho");
        carregarCabecalho();
    }//GEN-LAST:event_mudar3ActionPerformed
    private void inserirImagemNoBanco(String nomeArquivo, byte[] imagemBytes) throws SQLException {
        Connection conexao = new fazconexao().conectar();
        String query = "INSERT INTO imagens (nome_da_imagem, imagem, data_de_armazenamento) VALUES (?, ?, ?)";
        PreparedStatement statement = conexao.prepareStatement(query);
        LocalDateTime dataAtual = LocalDateTime.now();
        statement.setString(1, nomeArquivo);
        statement.setBytes(2, imagemBytes);
        statement.setTimestamp(3, Timestamp.valueOf(dataAtual));

        int result = statement.executeUpdate();
        int n = statement.executeUpdate();
        if (n != 0) {
            statement.close();
        } else {
            JOptionPane.showMessageDialog(null, "ERRO AO INSERIR IMG");
        }

        statement.close();
        conexao.close();
    }

    private void excluirImagemDoBanco(int idImagem) throws SQLException {
        Connection conexao = new fazconexao().conectar();
        String query = "DELETE FROM imagens WHERE id = ?";
        PreparedStatement statement = conexao.prepareStatement(query);

        statement.setInt(1, idImagem);
        statement.executeUpdate();

        statement.close();
        conexao.close();
    }

    private void exibirImagem(String tipoQuarto) {
        // Obtém a imagem do banco de dados com base no tipo de quarto
        ImageIcon imagem = obterImagemDoBanco(tipoQuarto);

        if (imagem != null) {
            JFrame frameImagem = new JFrame("Imagem do Quarto " + tipoQuarto);
            JLabel labelImagem = new JLabel(imagem);

            frameImagem.add(labelImagem);
            frameImagem.pack();
            frameImagem.setLocationRelativeTo(null);
            frameImagem.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            frameImagem.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Imagem não encontrada para o tipo de quarto " + tipoQuarto);
        }
    }

// Método fictício para obter a imagem do banco de dados com base no tipo de quarto
    private ImageIcon obterImagemDoBanco(String tipoQuarto) {
        ImageIcon imagem = null;

        Connection link = null;
        try {
            link = new fazconexao().conectar();
            String query = "SELECT * FROM imagens WHERE nome_da_imagem = ?";
            PreparedStatement statement = link.prepareStatement(query);
            statement.setString(1, tipoQuarto);
            ResultSet resultado = statement.executeQuery();

            if (resultado.next()) {
                // Supondo que a coluna 'imagem' seja um BLOB ou um formato suportado para uma imagem
                byte[] bytesImagem = resultado.getBytes("imagem");
                imagem = new ImageIcon(bytesImagem);
            }

            resultado.close();
            statement.close();
            link.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return imagem;
    }

    private void salvarImagemNoBanco(String nomeArquivo, BufferedImage imagem) {
        Connection conexao = null;
        try {
            conexao = new fazconexao().conectar(); // Obtém a conexão com o banco de dados

            // Converte a imagem para bytes
            File file = new File(nomeArquivo);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }

            byte[] imagemBytes = bos.toByteArray();

            // Insere a imagem na tabela
            String sql = "INSERT INTO imagens (nome, imagem) VALUES (?, ?)";
            PreparedStatement pstmt = conexao.prepareStatement(sql);
            pstmt.setString(1, nomeArquivo);
            pstmt.setBytes(2, imagemBytes);

            pstmt.executeUpdate();

            pstmt.close();
            conexao.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao salvar imagem no banco de dados: " + e.getMessage());
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
            java.util.logging.Logger.getLogger(ConfigAutoAtend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ConfigAutoAtend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ConfigAutoAtend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ConfigAutoAtend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ConfigAutoAtend().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAlterar;
    private javax.swing.JButton btVer;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JLabel label3;
    private javax.swing.JLabel label4;
    private javax.swing.JButton mudar1;
    private javax.swing.JButton mudar2;
    private javax.swing.JButton mudar3;
    private javax.swing.JPanel painelDirec;
    private javax.swing.JPanel painelImagem1;
    private javax.swing.JPanel painelImagem5;
    private javax.swing.JPanel painelImagens;
    private javax.swing.JRadioButton radioCliente;
    private javax.swing.JRadioButton radioSistema;
    private javax.swing.JRadioButton subAtiva;
    private javax.swing.JRadioButton subDesativa;
    private javax.swing.JTable tabelaTipoQuarto;
    private javax.swing.JTextField txtPath;
    // End of variables declaration//GEN-END:variables
}
