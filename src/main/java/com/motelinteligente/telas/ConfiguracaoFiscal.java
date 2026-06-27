package com.motelinteligente.telas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.CarregarVariaveis;
import com.motelinteligente.dados.ConexaoRemota;

public class ConfiguracaoFiscal extends JFrame {

    private JTextField txtClientId;
    private JTextField txtClientSecret;
    private JTextField txtInscMunicipal;
    private JTextField txtInscEstadual;
    private JComboBox<String> cbAmbiente;
    private JCheckBox chkAtivo;
    private JButton btnAutorizar;
    private JLabel lblStatusConexao;

    public ConfiguracaoFiscal() {
        setTitle("Configurações Fiscais - Bling API v3");
        setSize(480, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Módulo Fiscal Ativo?"));
        chkAtivo = new JCheckBox();
        panel.add(chkAtivo);

        panel.add(new JLabel("Ambiente de Emissão:"));
        cbAmbiente = new JComboBox<>(new String[]{"Homologação (Sandbox)", "Produção"});
        panel.add(cbAmbiente);

        panel.add(new JLabel("Bling Client ID:"));
        txtClientId = new JTextField();
        panel.add(txtClientId);

        panel.add(new JLabel("Bling Client Secret:"));
        txtClientSecret = new JTextField();
        panel.add(txtClientSecret);

        panel.add(new JLabel("Inscrição Municipal:"));
        txtInscMunicipal = new JTextField();
        panel.add(txtInscMunicipal);

        panel.add(new JLabel("Inscrição Estadual:"));
        txtInscEstadual = new JTextField();
        panel.add(txtInscEstadual);

        panel.add(new JLabel("Como Integrar?"));
        JButton btnAjuda = new JButton("❓ Passo a Passo");
        btnAjuda.addActionListener(this::exibirAjuda);
        panel.add(btnAjuda);

        panel.add(new JLabel("Integração OAuth 2.0:"));
        btnAutorizar = new JButton("🔗 Autorizar no Bling");
        btnAutorizar.setBackground(new Color(0, 123, 255));
        btnAutorizar.setForeground(Color.WHITE);
        btnAutorizar.addActionListener(this::autorizarBling);
        panel.add(btnAutorizar);

        panel.add(new JLabel("Status da Integração:"));
        lblStatusConexao = new JLabel("Verificando...");
        panel.add(lblStatusConexao);

        JButton btnSalvar = new JButton("Salvar Configurações");
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 12));
        btnSalvar.setBackground(new Color(40, 167, 69));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.addActionListener(e -> salvarConfiguracoes());

        add(panel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bottomPanel.add(btnSalvar, BorderLayout.CENTER);

        JLabel lblAviso = new JLabel("<html><div style='text-align: center; color: gray; margin-top: 5px;'>"
                + "<i>Cadastre o certificado digital A1 e tributos diretamente no painel do Bling.</i>"
                + "</div></html>");
        bottomPanel.add(lblAviso, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Garante que a estrutura do banco tem as novas colunas
        verificarColunasNoBanco();
        
        carregarConfiguracoes();
    }

    private void verificarColunasNoBanco() {
        try (Connection link = new fazconexao().conectar()) {
            String[] colunas = {
                "bling_client_id VARCHAR(255) DEFAULT NULL",
                "bling_client_secret VARCHAR(255) DEFAULT NULL",
                "bling_access_token TEXT DEFAULT NULL",
                "bling_refresh_token TEXT DEFAULT NULL",
                "bling_token_expires_at TIMESTAMP DEFAULT NULL"
            };
            for (String col : colunas) {
                String nomeColuna = col.split(" ")[0];
                try (PreparedStatement testStmt = link.prepareStatement("SELECT " + nomeColuna + " FROM configuracoes LIMIT 1")) {
                    testStmt.executeQuery();
                } catch (SQLException ex) {
                    try (Statement alterStmt = link.createStatement()) {
                        alterStmt.executeUpdate("ALTER TABLE configuracoes ADD COLUMN " + col);
                    } catch (SQLException alterEx) {
                        alterEx.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exibirAjuda(ActionEvent e) {
        String msg = "<html>"
                + "<body style='width: 350px; font-family: sans-serif; font-size: 11px;'>"
                + "<h3 style='color: #007bff; margin-bottom: 10px;'>Passo a Passo de Integração Bling</h3>"
                + "<b>1. Crie o Aplicativo no Bling do Motel:</b><br>"
                + "Acesse o Bling, vá em <i>Preferências (Engrenagem) &gt; Todas as Configurações &gt; Sistema &gt; Cadastro de Aplicativos</i>.<br><br>"
                + "<b>2. Preencha os dados:</b><br>"
                + "- Nome: <code>Motel Inteligente</code><br>"
                + "- URL de Redirecionamento: <br><code>https://www.motelinteligente.com/oauth_callback.php</code><br><br>"
                + "<b>3. Defina os Escopos (Permissões):</b><br>"
                + "Marque Leitura e Escrita para <b>Notas Fiscais</b> e <b>Notas Fiscais de Serviço</b>.<br><br>"
                + "<b>4. Salve e Copie:</b><br>"
                + "Copie o <b>Client ID</b> e <b>Client Secret</b> gerados e cole-os nesta tela.<br><br>"
                + "<b>5. Autorize:</b><br>"
                + "Clique no botão <b>'Autorizar no Bling'</b>, faça login na tela que abrir e confirme a autorização."
                + "</body>"
                + "</html>";
        JOptionPane.showMessageDialog(this, msg, "Guia de Integração Bling", JOptionPane.INFORMATION_MESSAGE);
    }

    private void autorizarBling(ActionEvent e) {
        String clientId = txtClientId.getText().trim();
        if (clientId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, insira o Bling Client ID antes de autorizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Salva primeiro para persistir as chaves necessárias
        salvarConfiguracoesSilenciosamente();

        try {
            String filial = CarregarVariaveis.getFilial();
            String url = "https://www.bling.com.br/Api/v3/oauth/authorize"
                    + "?response_type=code"
                    + "&client_id=" + clientId
                    + "&state=" + filial;
            
            Desktop.getDesktop().browse(new URI(url));
            JOptionPane.showMessageDialog(this, 
                "Uma aba do navegador foi aberta para você autorizar o aplicativo.\n" +
                "Após autorizar e ver a mensagem de sucesso, as configurações estarão prontas!",
                "Autorização Bling", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir o navegador: " + ex.getMessage());
        }
    }

    private void carregarConfiguracoes() {
        try (Connection link = new fazconexao().conectar()) {
            String sql = "SELECT * FROM configuracoes LIMIT 1";
            try (PreparedStatement stmt = link.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    chkAtivo.setSelected(rs.getBoolean("status_modulo_fiscal"));
                    txtClientId.setText(rs.getString("bling_client_id"));
                    txtClientSecret.setText(rs.getString("bling_client_secret"));
                    txtInscMunicipal.setText(rs.getString("inscricao_municipal"));
                    txtInscEstadual.setText(rs.getString("inscricao_estadual"));
                    
                    String amb = rs.getString("fiscal_ambiente");
                    if ("production".equalsIgnoreCase(amb)) {
                        cbAmbiente.setSelectedIndex(1);
                    } else {
                        cbAmbiente.setSelectedIndex(0);
                    }

                    String accessToken = rs.getString("bling_access_token");
                    java.sql.Timestamp expiresAt = rs.getTimestamp("bling_token_expires_at");

                    if (accessToken == null || accessToken.trim().isEmpty() || expiresAt == null || expiresAt.before(new java.util.Date())) {
                        try (Connection remoteLink = ConexaoRemota.getConnection()) {
                            if (remoteLink != null) {
                                String remoteSql = "SELECT bling_access_token, bling_refresh_token, bling_token_expires_at FROM configuracoes LIMIT 1";
                                try (PreparedStatement remoteStmt = remoteLink.prepareStatement(remoteSql); ResultSet remoteRs = remoteStmt.executeQuery()) {
                                    if (remoteRs.next()) {
                                        String rAccess = remoteRs.getString("bling_access_token");
                                        String rRefresh = remoteRs.getString("bling_refresh_token");
                                        java.sql.Timestamp rExpires = remoteRs.getTimestamp("bling_token_expires_at");
                                        if (rAccess != null && !rAccess.trim().isEmpty()) {
                                            String updateSql = "UPDATE configuracoes SET bling_access_token = ?, bling_refresh_token = ?, bling_token_expires_at = ?";
                                            try (PreparedStatement updateStmt = link.prepareStatement(updateSql)) {
                                                updateStmt.setString(1, rAccess);
                                                updateStmt.setString(2, rRefresh);
                                                updateStmt.setTimestamp(3, rExpires);
                                                updateStmt.executeUpdate();
                                            }
                                            accessToken = rAccess;
                                            expiresAt = rExpires;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Erro ao buscar tokens do banco remoto: " + e.getMessage());
                        }
                    }

                    if (accessToken != null && !accessToken.trim().isEmpty()) {
                        if (expiresAt != null && expiresAt.after(new java.util.Date())) {
                            lblStatusConexao.setText("<html><font color='green'>● Conectado (Válido)</font></html>");
                        } else {
                            lblStatusConexao.setText("<html><font color='orange'>● Conectado (Expirado - Renovação Auto)</font></html>");
                        }
                    } else {
                        lblStatusConexao.setText("<html><font color='red'>● Não Integrado</font></html>");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            lblStatusConexao.setText("Erro ao carregar");
        }
    }

    private void salvarConfiguracoesSilenciosamente() {
        try (Connection link = new fazconexao().conectar()) {
            String sql = "UPDATE configuracoes SET status_modulo_fiscal = ?, bling_client_id = ?, bling_client_secret = ?, inscricao_municipal = ?, inscricao_estadual = ?, fiscal_ambiente = ?";
            try (PreparedStatement stmt = link.prepareStatement(sql)) {
                stmt.setBoolean(1, chkAtivo.isSelected());
                stmt.setString(2, txtClientId.getText().trim());
                stmt.setString(3, txtClientSecret.getText().trim());
                stmt.setString(4, txtInscMunicipal.getText().trim());
                stmt.setString(5, txtInscEstadual.getText().trim());
                stmt.setString(6, cbAmbiente.getSelectedIndex() == 1 ? "production" : "homologacao");
                stmt.executeUpdate();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void salvarConfiguracoes() {
        try (Connection link = new fazconexao().conectar()) {
            String sql = "UPDATE configuracoes SET status_modulo_fiscal = ?, bling_client_id = ?, bling_client_secret = ?, inscricao_municipal = ?, inscricao_estadual = ?, fiscal_ambiente = ?";
            try (PreparedStatement stmt = link.prepareStatement(sql)) {
                stmt.setBoolean(1, chkAtivo.isSelected());
                stmt.setString(2, txtClientId.getText().trim());
                stmt.setString(3, txtClientSecret.getText().trim());
                stmt.setString(4, txtInscMunicipal.getText().trim());
                stmt.setString(5, txtInscEstadual.getText().trim());
                stmt.setString(6, cbAmbiente.getSelectedIndex() == 1 ? "production" : "homologacao");
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Configurações salvas com sucesso!");
                dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar configurações: " + ex.getMessage());
        }
    }
}
