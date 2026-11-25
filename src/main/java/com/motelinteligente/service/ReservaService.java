package com.motelinteligente.service;

import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fquartos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);
    private static final ReservaService INSTANCE = new ReservaService();

    public static ReservaService getInstance() {
        return INSTANCE;
    }

    /**
     * Consulta reservas próximas (próximas 12 horas), atualiza status de quarto se necessário
     * e retorna o texto HTML formatado para o label da tela principal.
     */
    public String verificarReservasProximas(Consumer<Integer> callbackMudaStatusCache) {
        String sql = "SELECT * FROM reservas WHERE TIMESTAMP(data_entrada, horario_entrada) BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 12 HOUR)";

        try (Connection conn = new fazconexao().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            CacheDados cache = CacheDados.getInstancia();
            StringBuilder textoHTML = new StringBuilder("<html>");
            boolean temReservas = false;

            while (rs.next()) {
                temReservas = true;

                String numero = rs.getString("numero_quarto");
                String hora = rs.getString("horario_entrada");
                String data = rs.getString("data_entrada");
                String valorPago = rs.getString("valor_pago");
                String nome = rs.getString("observacao");

                int numQuartoInt = Integer.parseInt(numero);
                CarregaQuarto quarto = cache.getCacheQuarto().get(numQuartoInt);

                if (quarto != null) {
                    if (!quarto.getStatusQuarto().contains("ocupado") || quarto.getStatusQuarto().equals("limpeza")) {
                        if (callbackMudaStatusCache != null) {
                            callbackMudaStatusCache.accept(numQuartoInt);
                        }

                        // Atualiza no banco de dados em background
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                new fquartos().setStatus(numQuartoInt, "reservado");
                                return null;
                            }
                        }.execute();
                    }
                }

                String dataHora = hora + "/" + data;
                textoHTML.append("<div style='font-size: 14px;'>")
                        .append("<span style='color: blue;'>&#9679;</span> Reserva: Nº <b style='color: red;'>")
                        .append(numero)
                        .append("</b> - <b style='color: red;'>")
                        .append(dataHora)
                        .append("</b> - pago <b style='color: red;'>")
                        .append(valorPago)
                        .append("</b> - <span style='color: black;'>")
                        .append(nome)
                        .append("</span></div><br>");
            }

            if (!temReservas) {
                textoHTML.append("Nenhuma reserva nas proximas 24h.");
            }

            textoHTML.append("</html>");
            return textoHTML.toString();

        } catch (Exception e) {
            logger.error("Erro ao verificar reservas próximas no ReservaService: {}", e.getMessage(), e);
            return "<html>Nenhuma reserva nas proximas 24h.</html>";
        }
    }
}
