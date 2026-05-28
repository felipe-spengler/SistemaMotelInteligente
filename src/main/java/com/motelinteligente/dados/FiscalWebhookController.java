package com.motelinteligente.dados;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;

@RestController
@RequestMapping("/api/fiscal")
public class FiscalWebhookController {

    @PostMapping("/webhook/nfe")
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Exemplo de payload vindo da Focus NFe / PlugNotas:
            // { "id_nota_api": "123", "status": "autorizado", "xml_url": "...", "pdf_url": "..." }
            
            String idNotaApi = (String) payload.get("id_nota_api");
            String status = (String) payload.get("status");
            String xmlUrl = (String) payload.get("xml_url");
            String pdfUrl = (String) payload.get("pdf_url");

            if (idNotaApi != null && status != null) {
                try (Connection link = new fazconexao().conectar()) {
                    String sql = "UPDATE registralocado SET nfe_status = ? WHERE nfe_chave = ?";
                    try (PreparedStatement stmt = link.prepareStatement(sql)) {
                        stmt.setString(1, status);
                        stmt.setString(2, idNotaApi);
                        stmt.executeUpdate();
                    }
                }
            }
            return ResponseEntity.ok("Webhook recebido com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao processar webhook: " + e.getMessage());
        }
    }
}
