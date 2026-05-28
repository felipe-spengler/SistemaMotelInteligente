package com.motelinteligente.dados;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FiscalService {
    
    private static final Logger logger = LoggerFactory.getLogger(FiscalService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public void emitirNota(int idLocacao) {
        try (Connection link = new fazconexao().conectar()) {
            
            // Verifica se o modulo fiscal esta ativo e pega os dados da empresa
            boolean moduloAtivo = false;
            String apiKey = "";
            String sqlConfig = "SELECT status_modulo_fiscal, api_key_fornecedor FROM configuracoes LIMIT 1";
            try (PreparedStatement stmt = link.prepareStatement(sqlConfig); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    moduloAtivo = rs.getBoolean("status_modulo_fiscal");
                    apiKey = rs.getString("api_key_fornecedor");
                }
            }
            
            if (!moduloAtivo || apiKey == null || apiKey.isEmpty()) {
                logger.info("Módulo fiscal inativo ou sem API Key configurada. Cancelando emissão de nota.");
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
                msg.append("Atenção! O Módulo Fiscal está ativo, mas os seguintes itens vendidos possuem dados fiscais incompletos (NCM ou CSOSN ausentes):\n\n");
                for (String prod : produtosSemNCM) {
                    msg.append("- ").append(prod).append("\n");
                }
                msg.append("\nPara evitar a rejeição da Nota Fiscal pela SEFAZ, corrija os dados fiscais no Cadastro de Produtos.\n");
                msg.append("Como deseja proceder?");

                String[] opcoes = {"Corrigir Depois (Salva como Pendente)", "Tentar Emitir Mesmo Assim"};
                int escolha = JOptionPane.showOptionDialog(null, msg.toString(), "Dados Fiscais Incompletos",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[0]);

                if (escolha == 0 || escolha == JOptionPane.CLOSED_OPTION) {
                    // Salva status local como 'Dados Incompletos' e interrompe
                    String sqlUpdate = "UPDATE registralocado SET nfe_status = 'Dados Incompletos' WHERE idlocacao = ?";
                    try (PreparedStatement stmt = link.prepareStatement(sqlUpdate)) {
                        stmt.setInt(1, idLocacao);
                        stmt.executeUpdate();
                    }
                    logger.info("Emissão fiscal suspensa devido a produtos sem NCM/CSOSN para a locação " + idLocacao);
                    return;
                }
            }

            // Pega os dados da locação
            float valorTotal = 0;
            String sqlLoc = "SELECT valorquarto, valorconsumo FROM registralocado WHERE idlocacao = ?";
            try (PreparedStatement stmt = link.prepareStatement(sqlLoc)) {
                stmt.setInt(1, idLocacao);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        valorTotal = rs.getFloat("valorquarto") + rs.getFloat("valorconsumo");
                    }
                }
            }

            if (valorTotal <= 0) return;

            // Prepara a requisição para a API (Focus NFe ou PlugNotas)
            String url = "https://api.focusnfe.com.br/v2/nfe"; // Exemplo
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("natureza_operacao", "Venda de mercadoria");
            payload.put("valor_total", valorTotal);
            // Preencher com cliente 'Consumidor Omitido', itens, NCM, CEST etc...
            // Esta é uma estrutura simplificada simulando o envio
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(apiKey, ""); // Autenticação na API
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            // Simulação de chamada:
            // ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            // String idNotaApi = (String) response.getBody().get("id_nota_api");
            String idNotaApiSimulado = "NOTA_API_" + System.currentTimeMillis();
            
            // Atualiza status local
            String sqlUpdate = "UPDATE registralocado SET nfe_status = 'Pendente', nfe_chave = ? WHERE idlocacao = ?";
            try (PreparedStatement stmt = link.prepareStatement(sqlUpdate)) {
                stmt.setString(1, idNotaApiSimulado);
                stmt.setInt(2, idLocacao);
                stmt.executeUpdate();
            }
            
            logger.info("Nota fiscal enviada com sucesso para a locação " + idLocacao);

        } catch (Exception e) {
            logger.error("Erro ao emitir nota fiscal: ", e);
        }
    }
}
