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
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null) {
                return ResponseEntity.badRequest().body("Payload inválido: objeto data ausente");
            }
            
            String idNotaApi = (String) data.get("id");
            String status = (String) data.get("status");
            String model = (String) data.get("model");
            String environmentType = (String) data.get("environmentType");

            if (idNotaApi != null && status != null && model != null) {
                boolean isService = "serviceInvoice".equalsIgnoreCase(model);
                
                String domain = "development".equalsIgnoreCase(environmentType) 
                    ? "https://sandbox-api.spedy.com.br" 
                    : "https://api.spedy.com.br";
                    
                String xmlUrl = domain + "/v1/" + (isService ? "service-invoices" : "consumer-invoices") + "/" + idNotaApi + "/xml";
                String pdfUrl = domain + "/v1/" + (isService ? "service-invoices" : "consumer-invoices") + "/" + idNotaApi + "/pdf";

                try (Connection link = new fazconexao().conectar()) {
                    String colStatus = isService ? "nfse_status" : "nfce_status";
                    String colId = isService ? "nfse_id_api" : "nfce_id_api";
                    String colXml = isService ? "nfse_xml_url" : "nfce_xml_url";
                    String colPdf = isService ? "nfse_pdf_url" : "nfce_pdf_url";
                    
                    String sql = "UPDATE registralocado SET " + colStatus + " = ?, " + colXml + " = ?, " + colPdf + " = ? WHERE " + colId + " = ?";
                    try (PreparedStatement stmt = link.prepareStatement(sql)) {
                        stmt.setString(1, status);
                        stmt.setString(2, xmlUrl);
                        stmt.setString(3, pdfUrl);
                        stmt.setString(4, idNotaApi);
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
