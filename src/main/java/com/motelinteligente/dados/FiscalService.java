package com.motelinteligente.dados;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.json.JSONArray;

@Service
public class FiscalService {
    
    private static final Logger logger = LoggerFactory.getLogger(FiscalService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private void verificarEstruturaBanco(Connection link) {
        String[] colunasLocado = {
            "nfse_status VARCHAR(20) DEFAULT 'Não Emitida'",
            "nfse_id_api VARCHAR(100) DEFAULT NULL",
            "nfse_xml_url VARCHAR(255) DEFAULT NULL",
            "nfse_pdf_url VARCHAR(255) DEFAULT NULL",
            "nfce_status VARCHAR(20) DEFAULT 'Não Emitida'",
            "nfce_id_api VARCHAR(100) DEFAULT NULL",
            "nfce_xml_url VARCHAR(255) DEFAULT NULL",
            "nfce_pdf_url VARCHAR(255) DEFAULT NULL"
        };
        
        for (String col : colunasLocado) {
            String nomeColuna = col.split(" ")[0];
            try (PreparedStatement testStmt = link.prepareStatement("SELECT " + nomeColuna + " FROM registralocado LIMIT 1")) {
                testStmt.executeQuery();
            } catch (SQLException ex) {
                try (java.sql.Statement alterStmt = link.createStatement()) {
                    alterStmt.executeUpdate("ALTER TABLE registralocado ADD COLUMN " + col);
                    logger.info("Adicionada coluna " + nomeColuna + " na tabela registralocado.");
                } catch (SQLException alterEx) {
                    logger.error("Erro ao adicionar coluna " + nomeColuna + ": ", alterEx);
                }
            }
        }
        
        String[] colunasConfig = {
            "fiscal_ambiente VARCHAR(20) DEFAULT 'homologacao'",
            "bling_client_id VARCHAR(255) DEFAULT NULL",
            "bling_client_secret VARCHAR(255) DEFAULT NULL",
            "bling_access_token TEXT DEFAULT NULL",
            "bling_refresh_token TEXT DEFAULT NULL",
            "bling_token_expires_at TIMESTAMP DEFAULT NULL"
        };

        for (String col : colunasConfig) {
            String nomeColuna = col.split(" ")[0];
            try (PreparedStatement testStmt = link.prepareStatement("SELECT " + nomeColuna + " FROM configuracoes LIMIT 1")) {
                testStmt.executeQuery();
            } catch (SQLException ex) {
                try (java.sql.Statement alterStmt = link.createStatement()) {
                    alterStmt.executeUpdate("ALTER TABLE configuracoes ADD COLUMN " + col);
                    logger.info("Adicionada coluna " + nomeColuna + " na tabela configuracoes.");
                } catch (SQLException alterEx) {
                    logger.error("Erro ao adicionar coluna " + nomeColuna + ": ", alterEx);
                }
            }
        }
    }

    public void emitirNota(int idLocacao) {
        try (Connection link = new fazconexao().conectar()) {
            verificarEstruturaBanco(link);
            
            boolean moduloAtivo = false;
            String sqlConfig = "SELECT status_modulo_fiscal FROM configuracoes LIMIT 1";
            try (PreparedStatement stmt = link.prepareStatement(sqlConfig); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    moduloAtivo = rs.getBoolean("status_modulo_fiscal");
                }
            }
            
            if (!moduloAtivo) {
                logger.info("Módulo fiscal Bling inativo. Cancelando emissão de nota.");
                return;
            }

            // Obtém e valida o Access Token do Bling
            String token = obterValidoAccessToken(link);
            if (token == null || token.isEmpty()) {
                logger.error("Não foi possível obter um Access Token válido para o Bling. Verifique as configurações.");
                return;
            }

            // Valida os dados fiscais dos produtos vendidos nesta locação
            List<String> produtosSemNCM = new ArrayList<>();
            String sqlValida = "SELECT p.descricao FROM registravendido rv INNER JOIN produtos p ON rv.idproduto = p.idproduto WHERE rv.idlocacao = ? AND (p.ncm IS NULL OR p.ncm = '' OR p.csosn IS NULL OR p.csosn = '')";
            try (PreparedStatement stmtValida = link.prepareStatement(sqlValida)) {
                stmtValida.setInt(1, idLocacao);
                try (ResultSet rsValida = stmtValida.executeQuery()) {
                    while (rsValida.next()) {
                        produtosSemNCM.add(rsValida.getString("descricao"));
                    }
                }
            }

            if (!produtosSemNCM.isEmpty()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Atenção! O Módulo Fiscal Bling está ativo, mas os seguintes itens vendidos possuem dados fiscais incompletos (NCM ou CSOSN ausentes):\n\n");
                for (String prod : produtosSemNCM) {
                    msg.append("- ").append(prod).append("\n");
                }
                msg.append("\nPara evitar rejeição da Nota, complete os dados fiscais no Cadastro de Produtos.\n");
                msg.append("Como deseja proceder?");

                String[] opcoes = {"Salvar como Pendente", "Tentar Emitir Mesmo Assim"};
                int escolha = JOptionPane.showOptionDialog(null, msg.toString(), "Dados Fiscais Incompletos",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[0]);

                if (escolha == 0 || escolha == JOptionPane.CLOSED_OPTION) {
                    String sqlUpdate = "UPDATE registralocado SET nfce_status = 'Dados Incompletos' WHERE idlocacao = ?";
                    try (PreparedStatement stmt = link.prepareStatement(sqlUpdate)) {
                        stmt.setInt(1, idLocacao);
                        stmt.executeUpdate();
                    }
                    return;
                }
            }

            // Pega os dados da locação
            float valorQuarto = 0;
            float valorConsumo = 0;
            String sqlLoc = "SELECT valorquarto, valorconsumo FROM registralocado WHERE idlocacao = ?";
            try (PreparedStatement stmt = link.prepareStatement(sqlLoc)) {
                stmt.setInt(1, idLocacao);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        valorQuarto = rs.getFloat("valorquarto");
                        valorConsumo = rs.getFloat("valorconsumo");
                    }
                }
            }

            if (valorQuarto <= 0 && valorConsumo <= 0) return;

            // Emite notas no Bling
            if (valorQuarto > 0) {
                emitirNfseBling(link, idLocacao, valorQuarto, token);
            }
            if (valorConsumo > 0) {
                emitirNfceBling(link, idLocacao, valorConsumo, token);
            }

        } catch (Exception e) {
            logger.error("Erro ao emitir nota fiscal: ", e);
        }
    }

    private String obterValidoAccessToken(Connection link) {
        String accessToken = "";
        String refreshToken = "";
        java.sql.Timestamp expiresAt = null;
        String clientId = "";
        String clientSecret = "";

        String sql = "SELECT bling_client_id, bling_client_secret, bling_access_token, bling_refresh_token, bling_token_expires_at FROM configuracoes LIMIT 1";
        try (PreparedStatement stmt = link.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                clientId = rs.getString("bling_client_id");
                clientSecret = rs.getString("bling_client_secret");
                accessToken = rs.getString("bling_access_token");
                refreshToken = rs.getString("bling_refresh_token");
                expiresAt = rs.getTimestamp("bling_token_expires_at");
            }
        } catch (SQLException e) {
            logger.error("Erro ao ler tokens do Bling: ", e);
            return null;
        }

        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            logger.warn("Bling Client ID ou Secret não configurados.");
            return null;
        }

        // Se expirou ou vai expirar nos próximos 5 minutos, tenta renovar ou buscar do remoto
        if (expiresAt == null || expiresAt.before(new java.util.Date(System.currentTimeMillis() + 300_000))) {
            // Tenta buscar do remoto antes (caso tenha sido atualizado via oauth_callback)
            try (Connection remoteLink = ConexaoRemota.getConnection()) {
                if (remoteLink != null) {
                    String remoteSql = "SELECT bling_access_token, bling_refresh_token, bling_token_expires_at FROM configuracoes LIMIT 1";
                    try (PreparedStatement remoteStmt = remoteLink.prepareStatement(remoteSql); ResultSet remoteRs = remoteStmt.executeQuery()) {
                        if (remoteRs.next()) {
                            java.sql.Timestamp rExpires = remoteRs.getTimestamp("bling_token_expires_at");
                            if (rExpires != null && rExpires.after(new java.util.Date(System.currentTimeMillis() + 300_000))) {
                                String rAccess = remoteRs.getString("bling_access_token");
                                String rRefresh = remoteRs.getString("bling_refresh_token");
                                String updateSql = "UPDATE configuracoes SET bling_access_token = ?, bling_refresh_token = ?, bling_token_expires_at = ?";
                                try (PreparedStatement updateStmt = link.prepareStatement(updateSql)) {
                                    updateStmt.setString(1, rAccess);
                                    updateStmt.setString(2, rRefresh);
                                    updateStmt.setTimestamp(3, rExpires);
                                    updateStmt.executeUpdate();
                                }
                                logger.info("Token do Bling atualizado com sucesso a partir do banco remoto.");
                                return rAccess;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Erro ao buscar token do Bling no banco remoto: ", e);
            }

            if (refreshToken == null || refreshToken.isEmpty()) {
                logger.warn("Refresh token do Bling ausente. É necessário autorizar a integração novamente.");
                return null;
            }
            logger.info("Token do Bling expirado ou prestes a expirar. Renovando...");
            accessToken = renovarTokenBling(link, clientId, clientSecret, refreshToken);
        }

        return accessToken;
    }

    private String renovarTokenBling(Connection link, String clientId, String clientSecret, String refreshToken) {
        try {
            String url = "https://www.bling.com.br/Api/v3/oauth/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
            
            String body = "grant_type=refresh_token&refresh_token=" + refreshToken;
            
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject json = new JSONObject(response.getBody());
                String newAccess = json.getString("access_token");
                String newRefresh = json.getString("refresh_token");
                int expiresIn = json.getInt("expires_in");
                
                java.sql.Timestamp newExpiresAt = new java.sql.Timestamp(System.currentTimeMillis() + (expiresIn * 1000L));
                
                String sql = "UPDATE configuracoes SET bling_access_token = ?, bling_refresh_token = ?, bling_token_expires_at = ?";
                try (PreparedStatement stmt = link.prepareStatement(sql)) {
                    stmt.setString(1, newAccess);
                    stmt.setString(2, newRefresh);
                    stmt.setTimestamp(3, newExpiresAt);
                    stmt.executeUpdate();
                }
                logger.info("Token do Bling renovado com sucesso!");
                return newAccess;
            } else {
                logger.error("Erro ao renovar token do Bling. Resposta HTTP: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Exceção ao tentar renovar token do Bling: ", e);
        }
        return null;
    }

    private void emitirNfseBling(Connection link, int idLocacao, float valorQuarto, String token) {
        try {
            String url = "https://api.bling.com.br/Api/v3/nfse";
            
            JSONObject payload = new JSONObject();
            payload.put("dataEmissao", java.time.LocalDateTime.now().toString());
            payload.put("valorTotal", valorQuarto);
            payload.put("descricao", "Prestação de serviços de hospedagem em suíte - locação " + idLocacao);
            payload.put("codigoServico", "0901");
            payload.put("aliquota", 5.0);
            
            JSONObject tomador = new JSONObject();
            tomador.put("nome", "Consumidor Final");
            payload.put("tomador", tomador);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            logger.info("Enviando requisição de NFS-e para Bling para locação " + idLocacao);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject responseBody = new JSONObject(response.getBody());
                // Na v3 do Bling, a resposta do POST de criação retorna o id criado.
                String idNotaApi = String.valueOf(responseBody.getJSONObject("data").getLong("id"));
                
                // Emite/Envia para a Prefeitura
                enviarNfseSefaz(idNotaApi, token);

                String sqlUpdate = "UPDATE registralocado SET nfse_status = 'Emitida', nfse_id_api = ? WHERE idlocacao = ?";
                try (PreparedStatement stmt = link.prepareStatement(sqlUpdate)) {
                    stmt.setString(1, idNotaApi);
                    stmt.setInt(2, idLocacao);
                    stmt.executeUpdate();
                }
                logger.info("NFS-e criada e enviada com sucesso no Bling. ID: " + idNotaApi);
            }
        } catch (Exception e) {
            logger.error("Erro na emissão da NFS-e do Bling para locação " + idLocacao + ": ", e);
        }
    }

    private void emitirNfceBling(Connection link, int idLocacao, float valorConsumo, String token) {
        try {
            String url = "https://api.bling.com.br/Api/v3/nfe";
            
            JSONObject payload = new JSONObject();
            payload.put("tipo", 1); // Saída
            payload.put("naturezaOperacao", new JSONObject().put("descricao", "Venda de Consumidor"));
            
            JSONObject contato = new JSONObject();
            contato.put("nome", "Consumidor Final");
            payload.put("contato", contato);
            
            JSONArray itensArray = new JSONArray();
            String sqlItens = "SELECT rv.idproduto, rv.quantidade, rv.valorunidade, rv.valortotal, p.descricao, p.ncm, p.csosn " +
                              "FROM registravendido rv " +
                              "INNER JOIN produtos p ON rv.idproduto = p.idproduto " +
                              "WHERE rv.idlocacao = ?";
            
            try (PreparedStatement stmt = link.prepareStatement(sqlItens)) {
                stmt.setInt(1, idLocacao);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        JSONObject item = new JSONObject();
                        item.put("codigo", "PROD-" + rs.getInt("idproduto"));
                        item.put("descricao", rs.getString("descricao"));
                        item.put("quantidade", rs.getInt("quantidade"));
                        item.put("valor", rs.getFloat("valorunidade"));
                        item.put("unidade", "UN");
                        item.put("classificacaoFiscal", rs.getString("ncm"));
                        itensArray.put(item);
                    }
                }
            }
            payload.put("itens", itensArray);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            logger.info("Enviando requisição de NFC-e para Bling para locação " + idLocacao);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject responseBody = new JSONObject(response.getBody());
                String idNotaApi = String.valueOf(responseBody.getJSONObject("data").getLong("id"));
                
                // Envia para a SEFAZ
                enviarNfeSefaz(idNotaApi, token);

                String sqlUpdate = "UPDATE registralocado SET nfce_status = 'Emitida', nfce_id_api = ? WHERE idlocacao = ?";
                try (PreparedStatement stmt = link.prepareStatement(sqlUpdate)) {
                    stmt.setString(1, idNotaApi);
                    stmt.setInt(2, idLocacao);
                    stmt.executeUpdate();
                }
                logger.info("NFC-e criada e enviada com sucesso no Bling. ID: " + idNotaApi);
            }
        } catch (Exception e) {
            logger.error("Erro na emissão da NFC-e no Bling para locação " + idLocacao + ": ", e);
        }
    }

    private void enviarNfeSefaz(String idNota, String token) {
        try {
            String url = "https://api.bling.com.br/Api/v3/nfe/" + idNota + "/enviar";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            restTemplate.postForEntity(url, request, String.class);
            logger.info("Comando de transmissão da NF-e " + idNota + " enviado à Sefaz.");
        } catch (Exception e) {
            logger.error("Erro ao enviar NF-e " + idNota + " para Sefaz: ", e);
        }
    }

    private void enviarNfseSefaz(String idNota, String token) {
        try {
            String url = "https://api.bling.com.br/Api/v3/nfse/" + idNota + "/enviar";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            restTemplate.postForEntity(url, request, String.class);
            logger.info("Comando de transmissão da NFS-e " + idNota + " enviado à Prefeitura.");
        } catch (Exception e) {
            logger.error("Erro ao enviar NFS-e " + idNota + " para Prefeitura: ", e);
        }
    }
}
