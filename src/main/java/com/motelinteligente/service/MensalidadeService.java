package com.motelinteligente.service;

import com.motelinteligente.dados.ConexaoRemota;
import com.motelinteligente.telas.NotificacaoMensalidade;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MensalidadeService {

    private static final Logger logger = LoggerFactory.getLogger(MensalidadeService.class);
    private static final MensalidadeService INSTANCE = new MensalidadeService();

    public static MensalidadeService getInstance() {
        return INSTANCE;
    }

    /**
     * Obtém a propriedade SISTEMA configurada no application.properties do usuário
     */
    public String getSistemaProperty() {
        Properties props = new Properties();
        String userHome = System.getProperty("user.home");
        String path = userHome + File.separator + "Documents"
                + File.separator + "logs" + File.separator + "application.properties";

        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            String sistema = props.getProperty("SISTEMA", "SistemaDesconhecido");
            String sistemaLower = sistema.toLowerCase();
            return URLEncoder.encode(sistemaLower, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.error("Erro ao ler application.properties em {}", path, e);
            return "SistemaDesconhecido";
        }
    }

    /**
     * Verifica se há mensalidade em aberto ou próxima do vencimento e exibe notificação
     */
    public void verificarMensalidade() {
        LocalDate hoje = LocalDate.now();
        int diaDoMes = hoje.getDayOfMonth();

        // Só exibe avisos se o dia do mês for maior que 7
        if (diaDoMes <= 7) {
            return;
        }

        String sistemaNome = getSistemaProperty();

        try (Connection conn = ConexaoRemota.getConnection()) {
            String sql = "SELECT referente FROM mensalidade WHERE status = 'approved' ORDER BY referente DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

                LocalDate ultimoPagamento = null;
                if (rs.next()) {
                    ultimoPagamento = rs.getDate("referente").toLocalDate();
                }

                // 1. Verifica se a última mensalidade paga é a deste mês (ou já adiantada)
                if (ultimoPagamento != null && !ultimoPagamento.isBefore(hoje.withDayOfMonth(1))) {
                    return;
                }

                // URL de pagamento (mensalidade.php calcula juros/multas e reajuste automático)
                final String redirectUrl = "http://motelinteligente.com/api/mensalidade.php?sistema=" + sistemaNome;

                String mensagem;
                if (diaDoMes <= 10) {
                    mensagem = "O vencimento da Mensalidade está próximo (dia 10).\nDeseja pagar agora?";
                } else {
                    mensagem = "Sua mensalidade está em atraso.\nPor favor, regularize o pagamento para evitar interrupções.\nDeseja pagar agora?";
                }

                // Exibe a notificação no canto da tela
                SwingUtilities.invokeLater(() -> {
                    new NotificacaoMensalidade(mensagem, redirectUrl);
                });
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar mensalidade: {}", e.getMessage(), e);
        }
    }
}
