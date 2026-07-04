package com.motelinteligente.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class fquartos {

    private static final Logger logger = LoggerFactory.getLogger(fquartos.class);
    private final fazconexao conexao = new fazconexao();

    public List<CarregaQuarto> uploadQuartos() {
        List<CarregaQuarto> quartos = new ArrayList<>();
        String sql = "SELECT q.numeroquarto, s.atualquarto AS status, s.horastatus AS data, q.tipoquarto AS tipo FROM quartos q JOIN status s ON q.numeroquarto = s.numeroquarto ORDER BY q.numeroquarto";
        try (Connection link = conexao.conectar(); PreparedStatement stmt = link.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CarregaQuarto q = new CarregaQuarto();
                q.setNumeroQuarto(rs.getInt("numeroquarto"));
                q.setTipoQuarto(rs.getString("tipo"));
                q.setStatusQuarto(rs.getString("status"));
                q.setHoraStatus(rs.getTimestamp("data").toString());
                quartos.add(q);
            }
        } catch (SQLException e) { logger.error("Erro uploadQuartos", e); }
        return quartos;
    }

    public List<vquartos> mostrar() {
        List<vquartos> list = new ArrayList<>();
        try (Connection link = conexao.conectar(); PreparedStatement stmt = link.prepareStatement("SELECT * FROM quartos ORDER BY numeroquarto"); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                vquartos q = new vquartos();
                q.setTipoquarto(rs.getString("tipoquarto"));
                q.setNumeroquarto(rs.getInt("numeroquarto"));
                q.setValorquarto(rs.getFloat("valorquarto"));
                q.setPernoitequarto(rs.getFloat("pernoitequarto"));
                q.setAddPessoa(rs.getFloat("addPessoa"));
                list.add(q);
            }
        } catch (SQLException e) { logger.error("Erro mostrar", e); }
        return list;
    }

    public boolean setStatus(int numero, String status) {
        return setStatus(numero, status, new Timestamp(new Date().getTime()));
    }

    public boolean setStatus(int numero, String status, Timestamp horario) {
        try (Connection link = conexao.conectar(); PreparedStatement stmt = link.prepareStatement("UPDATE status SET atualquarto = ?, horastatus = ? WHERE numeroquarto = ?")) {
            stmt.setString(1, status); stmt.setTimestamp(2, horario); stmt.setInt(3, numero);
            boolean ok = stmt.executeUpdate() > 0;
            if (ok) {
                if (status.equalsIgnoreCase("manutencao") || status.toLowerCase().contains("ocupado")) {
                    com.motelinteligente.arduino.ConectaArduino.enviarComandoLuz(numero, true);
                } else if (status.equalsIgnoreCase("livre") || status.equalsIgnoreCase("limpeza")) {
                    com.motelinteligente.arduino.ConectaArduino.enviarComandoLuz(numero, false);
                }
            }
            return ok;
        } catch (SQLException e) { logger.error("Erro setStatus", e); return false; }
    }

    public boolean alteraOcupado(int numero, String status) {
        try (Connection link = conexao.conectar(); PreparedStatement stmt = link.prepareStatement("UPDATE status SET atualquarto = ? WHERE numeroquarto = ?")) {
            stmt.setString(1, status); stmt.setInt(2, numero);
            boolean ok = stmt.executeUpdate() > 0;
            if (ok) {
                if (status.equalsIgnoreCase("manutencao") || status.toLowerCase().contains("ocupado")) {
                    com.motelinteligente.arduino.ConectaArduino.enviarComandoLuz(numero, true);
                } else if (status.equalsIgnoreCase("livre") || status.equalsIgnoreCase("limpeza")) {
                    com.motelinteligente.arduino.ConectaArduino.enviarComandoLuz(numero, false);
                }
            }
            return ok;
        } catch (SQLException e) { return false; }
    }

    public boolean insercao(vquartos dados, float adicional, String periodo) {
        try (Connection link = conexao.conectar()) {
            link.setAutoCommit(false);
            try (PreparedStatement psQ = link.prepareStatement("INSERT INTO quartos (tipoquarto, numeroquarto, valorquarto, pernoitequarto, addPessoa) VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement psS = link.prepareStatement("INSERT INTO status (numeroquarto, atualquarto, horastatus, periodo, adicional) VALUES (?, ?, ?, ?, ?)")) {
                psQ.setString(1, dados.getTipoquarto()); psQ.setInt(2, dados.getNumeroquarto());
                psQ.setFloat(3, dados.getValorquarto()); psQ.setFloat(4, dados.getPernoitequarto()); psQ.setFloat(5, dados.getAddPessoa());
                psQ.executeUpdate();
                psS.setInt(1, dados.getNumeroquarto()); psS.setString(2, "livre"); psS.setTimestamp(3, new Timestamp(new Date().getTime()));
                psS.setString(4, periodo); psS.setFloat(5, adicional);
                psS.executeUpdate();
                
                try (PreparedStatement psP = link.prepareStatement("INSERT INTO periodos_quarto (numeroquarto, ordem, descricao, tempo_minutos, valor, is_pernoite) VALUES (?, ?, ?, ?, ?, ?)")) {
                    psP.setInt(1, dados.getNumeroquarto()); psP.setInt(2, 1); psP.setString(3, "2 Horas"); psP.setInt(4, 120); psP.setFloat(5, dados.getValorquarto()); psP.setInt(6, 0); psP.executeUpdate();
                    psP.setInt(1, dados.getNumeroquarto()); psP.setInt(2, 2); psP.setString(3, "Pernoite"); psP.setInt(4, 720); psP.setFloat(5, dados.getPernoitequarto()); psP.setInt(6, 1); psP.executeUpdate();
                }
                link.commit(); return true;
            } catch (SQLException e) { link.rollback(); throw e; }
        } catch (SQLException e) { logger.error("Erro insercao", e); return false; }
    }

    public boolean fazOUp(vquartos dados, float hora_adicional, String periodo) {
        try (Connection link = conexao.conectar()) {
            link.setAutoCommit(false);
            try (PreparedStatement psQ = link.prepareStatement("UPDATE quartos SET tipoquarto = ?, valorquarto = ?, pernoitequarto = ?, addPessoa = ? WHERE numeroquarto = ?");
                 PreparedStatement psS = link.prepareStatement("UPDATE status SET adicional = ?, periodo = ? WHERE numeroquarto = ?")) {
                psQ.setString(1, dados.getTipoquarto()); psQ.setFloat(2, dados.getValorquarto()); psQ.setFloat(3, dados.getPernoitequarto()); psQ.setFloat(4, dados.getAddPessoa()); psQ.setInt(5, dados.getNumeroquarto());
                psQ.executeUpdate();
                psS.setFloat(1, hora_adicional); psS.setString(2, periodo); psS.setInt(3, dados.getNumeroquarto());
                psS.executeUpdate();
                link.commit(); return true;
            } catch (SQLException e) { link.rollback(); throw e; }
        } catch (SQLException e) { logger.error("Erro fazOUp", e); return false; }
    }

    public boolean excluir(int num) {
        try (Connection link = conexao.conectar()) {
            link.setAutoCommit(false);
            try (PreparedStatement psP = link.prepareStatement("DELETE FROM periodos_quarto WHERE numeroquarto = ?");
                 PreparedStatement psS = link.prepareStatement("DELETE FROM status WHERE numeroquarto = ?");
                 PreparedStatement psQ = link.prepareStatement("DELETE FROM quartos WHERE numeroquarto = ?")) {
                psP.setInt(1, num); psP.executeUpdate();
                psS.setInt(1, num); psS.executeUpdate();
                psQ.setInt(1, num); int affected = psQ.executeUpdate();
                link.commit(); return affected > 0;
            } catch (SQLException e) { link.rollback(); throw e; }
        } catch (SQLException e) { return false; }
    }

    public int numeroQuartos() {
        try (Connection link = conexao.conectar(); PreparedStatement stmt = link.prepareStatement("SELECT COUNT(*) FROM quartos"); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {}
        return 0;
    }

    public int getIdLocacao(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement stmt = link.prepareStatement("SELECT idlocacao FROM registralocado WHERE numquarto = ? AND horafim IS NULL")) {
            stmt.setInt(1, num);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) {}
        return 0;
    }

    public PeriodoQuarto getPeriodoAtual(int num) {
        try (Connection link = conexao.conectar()) {
            String pLoc = null;
            try (PreparedStatement ps = link.prepareStatement("SELECT periodo_locado FROM registralocado WHERE numquarto = ? AND horafim IS NULL")) {
                ps.setInt(1, num);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) pLoc = rs.getString(1); }
            }
            if (pLoc != null && !pLoc.isEmpty()) {
                try (PreparedStatement ps = link.prepareStatement("SELECT * FROM periodos_quarto WHERE numeroquarto = ? AND descricao = ?")) {
                    ps.setInt(1, num); ps.setString(2, pLoc);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) return new PeriodoQuarto(rs.getInt("id"), rs.getInt("numeroquarto"), rs.getString("descricao"), rs.getInt("tempo_minutos"), rs.getFloat("valor"), rs.getBoolean("is_pernoite"), rs.getInt("ordem"));
                    }
                }
            }
            boolean isPern = getStatus(num).contains("pernoite");
            try (PreparedStatement ps = link.prepareStatement("SELECT * FROM periodos_quarto WHERE numeroquarto = ? AND is_pernoite = ? ORDER BY ordem LIMIT 1")) {
                ps.setInt(1, num); ps.setInt(2, isPern ? 1 : 0);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new PeriodoQuarto(rs.getInt("id"), rs.getInt("numeroquarto"), rs.getString("descricao"), rs.getInt("tempo_minutos"), rs.getFloat("valor"), rs.getBoolean("is_pernoite"), rs.getInt("ordem"));
                }
            }
        } catch (SQLException e) {}
        return null;
    }

    public float getAddPessoa(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT addPessoa FROM quartos WHERE numeroquarto = ?")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getFloat(1); }
        } catch (SQLException e) {}
        return 0;
    }

    public float getAdicional(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT adicional FROM status WHERE numeroquarto = ?")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getFloat(1); }
        } catch (SQLException e) {}
        return 0;
    }

    public String getPeriodo(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT periodo FROM status WHERE numeroquarto = ?")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (SQLException e) {}
        return null;
    }

    public String getDataInicio(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT horastatus FROM status WHERE numeroquarto = ?")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getTimestamp(1).toString(); }
        } catch (SQLException e) {}
        return null;
    }

    public String getData(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT horastatus FROM status WHERE numeroquarto = ?")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long diff = System.currentTimeMillis() - rs.getTimestamp(1).getTime();
                    long h = diff / (3600000); long m = (diff % 3600000) / 60000;
                    return String.format("%02d:%02d", h, m);
                }
            }
        } catch (SQLException e) {}
        return "00:00";
    }

    public String getTipo(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT tipoquarto FROM quartos WHERE numeroquarto = ?")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (SQLException e) {}
        return "";
    }

    public boolean registraLocacao(int num) { return registraLocacao(num, null); }

    public boolean registraLocacao(int num, String p) {
        try (Connection link = conexao.conectar(); PreparedStatement stmt = link.prepareStatement("INSERT INTO registralocado (numquarto, horainicio, numpessoas, periodo_locado) VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            Timestamp ts = new Timestamp(new Date().getTime());
            stmt.setInt(1, num); stmt.setTimestamp(2, ts); stmt.setInt(3, 2); stmt.setString(4, p);
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        CacheDados.getInstancia().getCacheOcupado().put(num, new DadosOcupados(ts, id, getValorQuarto(num, "periodo"), getValorQuarto(num, "pernoite"), 2, getAdicional(num), getPeriodo(num)));
                    }
                }
                return true;
            }
        } catch (SQLException e) {}
        return false;
    }

    public void atualizaPessoas(int num, int p) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("UPDATE registralocado SET numpessoas = ? WHERE numquarto = ? AND horafim IS NULL")) {
            ps.setInt(1, p); ps.setInt(2, num); ps.executeUpdate();
        } catch (SQLException e) {}
    }

    public int getPessoas(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT numpessoas FROM registralocado WHERE numquarto = ? AND horafim IS NULL")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) {}
        return 2;
    }

    public void salvaLocacao(int id, Timestamp ini, Timestamp fim, float vQ, float vC, float vD, float vP, float vCart, String p) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("UPDATE registralocado SET horafim=?, horainicio=?, valorquarto=?, valorconsumo=?, pagodinheiro=?, pagopix=?, pagocartao=?, idcaixaatual=?, periodo_locado=? WHERE idlocacao=?")) {
            ps.setTimestamp(1, fim); ps.setTimestamp(2, ini); ps.setFloat(3, vQ); ps.setFloat(4, vC);
            ps.setFloat(5, vD); ps.setFloat(6, vP); ps.setFloat(7, vCart);
            ps.setInt(8, configGlobal.getInstance().getCaixa()); ps.setString(9, p); ps.setInt(10, id);
            ps.executeUpdate();
        } catch (SQLException e) {}
    }

    public boolean salvaProduto(int idL, int idP, int q, float vU, float vT) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("INSERT INTO registravendido (idlocacao, idproduto, quantidade, valorunidade, valortotal, idcaixaatual) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, idL); ps.setInt(2, idP); ps.setInt(3, q); ps.setFloat(4, vU); ps.setFloat(5, vT);
            ps.setInt(6, configGlobal.getInstance().getCaixa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Object[]> getRelatorioAutoAtendimentoTipos(String dI, String dF) {
        List<Object[]> list = new ArrayList<>();
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT COALESCE(periodo_locado, 'MANUAL') as t, COUNT(*) as c FROM registralocado WHERE horafim IS NOT NULL AND DATE(horainicio) BETWEEN ? AND ? GROUP BY periodo_locado")) {
            ps.setString(1, dI); ps.setString(2, dF);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new Object[]{rs.getString(1), rs.getInt(2)}); }
        } catch (SQLException e) {}
        return list;
    }

    public List<Object[]> getRelatorioAutoAtendimentoHorarios(String dI, String dF) {
        List<Object[]> list = new ArrayList<>();
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT HOUR(horainicio) as h, COUNT(*) as tg, SUM(CASE WHEN periodo_locado IS NOT NULL AND periodo_locado != '' THEN 1 ELSE 0 END) as ta FROM registralocado WHERE horafim IS NOT NULL AND DATE(horainicio) BETWEEN ? AND ? GROUP BY HOUR(horainicio) ORDER BY h")) {
            ps.setString(1, dI); ps.setString(2, dF);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new Object[]{rs.getInt(1), rs.getInt(2), rs.getInt(3)}); }
        } catch (SQLException e) {}
        return list;
    }

    public Object[] getRelatorioUpsells(String dI, String dF) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT COUNT(*) as q, SUM(valor) as t FROM justificativa WHERE justificativa = 'Desconto aplicado via Autoatendimento' AND idlocacao IN (SELECT idlocacao FROM registralocado WHERE DATE(horainicio) BETWEEN ? AND ?)")) {
            ps.setString(1, dI); ps.setString(2, dF);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new Object[]{rs.getInt(1), rs.getFloat(2)}; }
        } catch (SQLException e) {}
        return new Object[]{0, 0.0f};
    }

    public boolean verExiste(int n) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT COUNT(*) FROM quartos WHERE numeroquarto = ?")) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) { return true; }
    }

    public boolean setPeriodoLocado(int id, String p) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("UPDATE registralocado SET periodo_locado = ? WHERE idlocacao = ?")) {
            ps.setString(1, p); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean salvaAntecipado(int id, String t, float v, int idC) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("INSERT INTO antecipado (idlocacao, tipo, valor, hora, idcaixaatual) VALUES (?, ?, ?, NOW(), ?)")) {
            ps.setInt(1, id); ps.setString(2, t); ps.setFloat(3, v); ps.setInt(4, idC);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<PeriodoQuarto> getPeriodos(int num) {
        List<PeriodoQuarto> list = new ArrayList<>();
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT * FROM periodos_quarto WHERE numeroquarto = ? ORDER BY ordem")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new PeriodoQuarto(rs.getInt("id"), rs.getInt("numeroquarto"), rs.getString("descricao"), rs.getInt("tempo_minutos"), rs.getFloat("valor"), rs.getBoolean("is_pernoite"), rs.getInt("ordem")));
            }
        } catch (SQLException e) {}
        return list;
    }

    public String getStatus(int num) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT atualquarto FROM status WHERE numeroquarto = ?")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (SQLException e) {}
        return "desconhecido";
    }

    public float getValorQuarto(int num, String t) {
        try (Connection link = conexao.conectar()) {
            if ("pernoite".equals(t)) {
                try (PreparedStatement ps = link.prepareStatement("SELECT valor FROM periodos_quarto WHERE numeroquarto = ? AND is_pernoite = 1 ORDER BY ordem LIMIT 1")) {
                    ps.setInt(1, num);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getFloat(1); }
                }
            } else {
                String pLoc = null;
                try (PreparedStatement ps = link.prepareStatement("SELECT periodo_locado FROM registralocado WHERE numquarto = ? AND horafim IS NULL")) {
                    ps.setInt(1, num);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) pLoc = rs.getString(1); }
                }
                if (pLoc != null) {
                    try (PreparedStatement ps = link.prepareStatement("SELECT valor FROM periodos_quarto WHERE numeroquarto = ? AND descricao = ?")) {
                        ps.setInt(1, num); ps.setString(2, pLoc);
                        try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getFloat(1); }
                    }
                }
                try (PreparedStatement ps = link.prepareStatement("SELECT valor FROM periodos_quarto WHERE numeroquarto = ? AND is_pernoite = 0 ORDER BY valor ASC LIMIT 1")) {
                    ps.setInt(1, num);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getFloat(1); }
                }
            }
            String col = "pernoite".equals(t) ? "pernoitequarto" : "valorquarto";
            try (PreparedStatement ps = link.prepareStatement("SELECT " + col + " FROM quartos WHERE numeroquarto = ?")) {
                ps.setInt(1, num);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getFloat(1); }
            }
        } catch (SQLException e) {}
        return 0;
    }

    public boolean adicionaRegistro(int num, String t) {
        String tbl = switch (t) {
            case "manutencao" -> "registramanutencao";
            case "limpeza" -> "registralimpeza";
            case "reservado" -> "registrareserva";
            default -> "registramanutencao";
        };
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("INSERT INTO " + tbl + " (numquarto, horaentrada) VALUES (?, ?)")) {
            ps.setInt(1, num); ps.setTimestamp(2, new Timestamp(new Date().getTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public int getIdCaixa(int idL) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT idcaixaatual FROM registralocado WHERE idlocacao = ?")) {
            ps.setInt(1, idL);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) {}
        return 0;
    }

    public boolean alteraRegistro(int num, String t) {
        String tbl = null;
        if (t.contains("-")) tbl = "registralocado";
        else if (t.equals("manutencao")) tbl = "registramanutencao";
        else if (t.equals("limpeza")) tbl = "registralimpeza";
        else if (t.equals("reservado")) tbl = "registrareserva";
        if (tbl == null) return false;
        String col = "registralocado".equals(tbl) ? "horainicio" : "horaentrada";
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT " + col + " FROM " + tbl + " WHERE numquarto = ? AND tempoTotal IS NULL")) {
            ps.setInt(1, num);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ini = rs.getTimestamp(1);
                    long diff = System.currentTimeMillis() - ini.getTime();
                    String fmt = String.format("%02d:%02d", diff/3600000, (diff%3600000)/60000);
                    try (PreparedStatement up = link.prepareStatement("UPDATE " + tbl + " SET tempoTotal = ?, " + col + " = ? WHERE numquarto = ? AND tempoTotal IS NULL")) {
                        up.setString(1, fmt); up.setTimestamp(2, ini); up.setInt(3, num);
                        return up.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {}
        return false;
    }

    public String getPeriodoLocado(int idL) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT periodo_locado FROM registralocado WHERE idlocacao = ?")) {
            ps.setInt(1, idL);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (SQLException e) {}
        return null;
    }

    public Timestamp getHoraInicio(int idL) {
        try (Connection link = conexao.conectar(); PreparedStatement ps = link.prepareStatement("SELECT horainicio FROM registralocado WHERE idlocacao = ? AND horafim IS NULL")) {
            ps.setInt(1, idL);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getTimestamp(1); }
        } catch (SQLException e) {}
        return null;
    }
}
