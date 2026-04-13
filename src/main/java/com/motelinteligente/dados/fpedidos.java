package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class fpedidos {
    private static final Logger logger = LoggerFactory.getLogger(fpedidos.class);

    public static class PedidoOnline {
        public int id;
        public int numeroQuarto;
        public String itens;
        public float valorTotal;
        public String status;
        public String hora;
    }

    public List<PedidoOnline> buscarNovosPedidos() {
        List<PedidoOnline> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedidos_online WHERE status = 'pendente' ORDER BY id ASC";

        try (Connection conn = new fazconexao().conectar();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                PedidoOnline p = new PedidoOnline();
                p.id = rs.getInt("id");
                p.numeroQuarto = rs.getInt("numeroquarto");
                p.itens = rs.getString("itens");
                p.valorTotal = rs.getFloat("valor_total");
                p.status = rs.getString("status");
                p.hora = rs.getString("hora_pedido");
                pedidos.add(p);
            }

        } catch (SQLException e) {
            logger.error("Erro ao buscar pedidos online: ", e);
        }
        return pedidos;
    }

    public boolean marcarComoVisto(int id) {
        String sql = "UPDATE pedidos_online SET status = 'visualizado' WHERE id = ?";
        try (Connection conn = new fazconexao().conectar();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao marcar pedido como visto: ", e);
            return false;
        }
    }
    
    public boolean finalizarPedido(int id) {
        String sql = "UPDATE pedidos_online SET status = 'entregue' WHERE id = ?";
        try (Connection conn = new fazconexao().conectar();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao finalizar pedido: ", e);
            return false;
        }
    }
}
