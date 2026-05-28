package com.motelinteligente.telas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import com.motelinteligente.dados.fazconexao;

public class ConfiguracaoFiscal extends JFrame {

    private JTextField txtToken;
    private JTextField txtInscMunicipal;
    private JTextField txtInscEstadual;
    private JPasswordField txtSenhaCertificado;
    private JComboBox<String> cbRegime;
    private JLabel lblCertificado;
    private File certificadoFile;
    private JCheckBox chkAtivo;

    public ConfiguracaoFiscal() {
        setTitle("Configurações Fiscais");
        setSize(450, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Módulo Fiscal Ativo?"));
        chkAtivo = new JCheckBox();
        panel.add(chkAtivo);

        panel.add(new JLabel("Regime Tributário:"));
        cbRegime = new JComboBox<>(new String[]{"Simples Nacional", "Lucro Presumido", "Lucro Real"});
        panel.add(cbRegime);

        panel.add(new JLabel("Token de Integração:"));
        txtToken = new JTextField();
        panel.add(txtToken);

        panel.add(new JLabel("Inscrição Municipal:"));
        txtInscMunicipal = new JTextField();
        panel.add(txtInscMunicipal);

        panel.add(new JLabel("Inscrição Estadual:"));
        txtInscEstadual = new JTextField();
        panel.add(txtInscEstadual);

        panel.add(new JLabel("Certificado Digital (A1):"));
        JButton btnCertificado = new JButton("Selecionar .PFX");
        btnCertificado.addActionListener(this::selecionarCertificado);
        panel.add(btnCertificado);

        panel.add(new JLabel("Arquivo:"));
        lblCertificado = new JLabel("Nenhum selecionado");
        panel.add(lblCertificado);

        panel.add(new JLabel("Senha do Certificado:"));
        txtSenhaCertificado = new JPasswordField();
        panel.add(txtSenhaCertificado);

        JButton btnSalvar = new JButton("Salvar Configurações");
        btnSalvar.addActionListener(e -> salvarConfiguracoes());
        
        add(panel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnSalvar);
        
        JLabel lblAviso = new JLabel("<html><i>Cadastre o NCM e CEST nas fichas de produtos do frigobar.</i></html>");
        lblAviso.setForeground(Color.RED);
        bottomPanel.add(lblAviso);
        
        add(bottomPanel, BorderLayout.SOUTH);

        carregarConfiguracoes();
    }

    private void selecionarCertificado(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Certificados A1 (*.pfx)", "pfx"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            certificadoFile = fileChooser.getSelectedFile();
            lblCertificado.setText(certificadoFile.getName());
        }
    }

    private void carregarConfiguracoes() {
        try (Connection link = new fazconexao().conectar()) {
            String sql = "SELECT * FROM configuracoes LIMIT 1";
            try (PreparedStatement stmt = link.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    chkAtivo.setSelected(rs.getBoolean("status_modulo_fiscal"));
                    txtToken.setText(rs.getString("api_key_fornecedor"));
                    txtInscMunicipal.setText(rs.getString("inscricao_municipal"));
                    txtInscEstadual.setText(rs.getString("inscricao_estadual"));
                    try {
                        txtSenhaCertificado.setText(rs.getString("senha_certificado"));
                    } catch (SQLException ex) {
                        // Coluna pode não existir ainda
                    }
                    try {
                        byte[] cert = rs.getBytes("certificado_digital_a1_pfx");
                        if (cert != null && cert.length > 0) {
                            lblCertificado.setText("Certificado já salvo no banco (.PFX)");
                        }
                    } catch (SQLException ex) {
                        // Coluna pode não existir ainda
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void salvarConfiguracoes() {
        try (Connection link = new fazconexao().conectar()) {
            // Garantir que a coluna senha_certificado existe antes de salvar
            try (PreparedStatement testStmt = link.prepareStatement("SELECT senha_certificado FROM configuracoes LIMIT 1")) {
                testStmt.executeQuery();
            } catch (SQLException ex) {
                // Se a coluna não existir, adiciona
                try (Statement alterStmt = link.createStatement()) {
                    alterStmt.executeUpdate("ALTER TABLE configuracoes ADD COLUMN senha_certificado VARCHAR(100) DEFAULT NULL");
                }
            }

            boolean alterouCertificado = (certificadoFile != null);
            String sql = "UPDATE configuracoes SET status_modulo_fiscal = ?, api_key_fornecedor = ?, inscricao_municipal = ?, inscricao_estadual = ?, senha_certificado = ?";
            if (alterouCertificado) {
                sql += ", certificado_digital_a1_pfx = ?";
            }
            
            try (PreparedStatement stmt = link.prepareStatement(sql)) {
                stmt.setBoolean(1, chkAtivo.isSelected());
                stmt.setString(2, txtToken.getText());
                stmt.setString(3, txtInscMunicipal.getText());
                stmt.setString(4, txtInscEstadual.getText());
                stmt.setString(5, new String(txtSenhaCertificado.getPassword()));
                
                if (alterouCertificado) {
                    byte[] certBytes = java.nio.file.Files.readAllBytes(certificadoFile.toPath());
                    stmt.setBytes(6, certBytes);
                }
                
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Configurações Fiscais salvas com sucesso!");
                dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar configurações: " + ex.getMessage());
        }
    }
}
