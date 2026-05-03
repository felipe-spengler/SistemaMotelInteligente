package com.motelinteligente.telas.controller;

import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fpedidos;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.telas.Quadrado;
import com.motelinteligente.telas.TelaPrincipal;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.alarme.FAlarmes;
import com.motelinteligente.dados.PeriodoQuarto;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.arduino.ConectaArduino;

public class TelaPrincipalController {

    private static final Logger logger = LoggerFactory.getLogger(TelaPrincipalController.class);
    private final TelaPrincipal view;
    private final Map<Integer, Quadrado> mapaQuadrados;
    private long lastUpdate = 0;

    public TelaPrincipalController(TelaPrincipal view, Map<Integer, Quadrado> mapaQuadrados) {
        this.view = view;
        this.mapaQuadrados = mapaQuadrados;
    }

    public void atualizarQuartos(javax.swing.JComponent srPane) {
        // Removido debounce para garantir resposta imediata da UI conforme solicitado
        CacheDados cacheDados = CacheDados.getInstancia();
        int totalQuartos = cacheDados.getCacheQuarto().size();
        
        // Força rebuild se o container estiver vazio ou o número de quartos mudou
        boolean precisaRebuild = mapaQuadrados.isEmpty() 
                || mapaQuadrados.size() != totalQuartos
                || srPane.getComponentCount() == 0;

        if (precisaRebuild) {
            srPane.removeAll();
            
            // Lógica dinâmica de colunas baseada no número de quartos
            int colunas = 3;
            if (totalQuartos > 24) {
                colunas = 5;
            } else if (totalQuartos > 10) {
                colunas = 4;
            }
            
            srPane.setLayout(new GridLayout(0, colunas, 0, 0)); 
            mapaQuadrados.clear();
            
            // Ajusta o tamanho máximo vertical do srPane para não esticar os quadrados demais
            // Calculamos o número de linhas e multiplicamos por 220px (limite sugerido)
            int linhas = (int) Math.ceil((double) totalQuartos / colunas);
            int alturaMaxima = linhas * 220; 
            srPane.setMaximumSize(new Dimension(Short.MAX_VALUE, alturaMaxima));
            srPane.setPreferredSize(new Dimension(srPane.getPreferredSize().width, alturaMaxima));
        }

        Color outroVerde = new Color(0, 200, 0);
        Color meuVerde = new Color(30, 255, 50);
        Color meuAmarelo = new Color(238, 243, 96);
        Color outroAmarelo = new Color(238, 213, 0);
        Color meuCinza = new Color(204, 204, 204);
        Color outroCinza = new Color(155, 155, 155);
        Color meuVermelho = new Color(255, 70, 72);
        Color outroVermelho = new Color(213, 0, 30);
        Color outroAzul = new Color(0, 21, 111);

        for (Map.Entry<Integer, CarregaQuarto> entry : cacheDados.getCacheQuarto().entrySet()) {
            CarregaQuarto q = entry.getValue();
            int num = q.getNumeroQuarto();
            String status = q.getStatusQuarto();

            Quadrado quadrado = mapaQuadrados.get(num);
            boolean novo = (quadrado == null);

            String labelExtra = null;
            Color corFundo = meuVerde;
            Color corBorda = outroVerde;
            boolean auto = false;

            if (status.equals("livre")) {
                corFundo = meuVerde;
                corBorda = outroVerde;
            } else if (status.equals("manutencao")) {
                labelExtra = "MANUTENÇÃO";
                corFundo = meuCinza;
                corBorda = outroCinza;
            } else if (status.equals("limpeza")) {
                labelExtra = "LIMPEZA";
                corFundo = meuAmarelo;
                corBorda = outroAmarelo;
            } else if (status.equals("reservado")) {
                labelExtra = "RESERVADO";
                corFundo = Color.cyan;
                corBorda = outroAzul;
            } else if (status.contains("-")) {
                labelExtra = calculaData(q.getHoraStatus());
                corBorda = outroVermelho;
                auto = cacheDados.getCacheOcupado().containsKey(num)
                        && cacheDados.getCacheOcupado().get(num).isAutoAtendimento();
                if (status.split("-")[1].equals("pernoite"))
                    corFundo = new Color(153, 51, 153);
                else
                    corFundo = meuVermelho;
            }

            if (novo) {
                quadrado = new Quadrado(num, q.getTipoQuarto(), labelExtra, corBorda, auto);
                quadrado.setQuartoClickListener(view);
                mapaQuadrados.put(num, quadrado);
                srPane.add(quadrado);
            } else if (quadrado != null) {
                quadrado.atualizar(labelExtra, corBorda, auto);
            }

            if (quadrado != null) {
                quadrado.setBackground(corFundo);
            }
        }

        if (precisaRebuild) {
            srPane.revalidate();
        }
        srPane.repaint();
    }

    public void verificarAlarmes() {
        configGlobal config = configGlobal.getInstance();
        if (config.getAlarmesAtivos() > 0) {
            try (Connection conn = new fazconexao().conectar();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt
                            .executeQuery("SELECT id, hora_despertar, descricao FROM alarmes WHERE ativo = TRUE")) {

                Calendar agora = Calendar.getInstance();
                while (rs.next()) {
                    Timestamp horaDespertar = rs.getTimestamp("hora_despertar");
                    Calendar alarmeCalendar = Calendar.getInstance();
                    alarmeCalendar.setTime(horaDespertar);

                    if (agora.get(Calendar.YEAR) == alarmeCalendar.get(Calendar.YEAR) &&
                            agora.get(Calendar.MONTH) == alarmeCalendar.get(Calendar.MONTH) &&
                            agora.get(Calendar.DAY_OF_MONTH) == alarmeCalendar.get(Calendar.DAY_OF_MONTH) &&
                            agora.get(Calendar.HOUR_OF_DAY) == alarmeCalendar.get(Calendar.HOUR_OF_DAY) &&
                            agora.get(Calendar.MINUTE) == alarmeCalendar.get(Calendar.MINUTE)) {

                        view.showAlarmAlert(rs.getInt("id"), rs.getString("descricao"));
                    }
                }
            } catch (Exception e) {
                logger.error("Erro ao verificar alarmes", e);
            }
        }
    }

    public void verificarPedidosOnline() {
        fpedidos dao = new fpedidos();
        List<fpedidos.PedidoOnline> novos = dao.buscarNovosPedidos();
        if (!novos.isEmpty()) {
            for (fpedidos.PedidoOnline p : novos) {
                view.mostrarAlertaPedido(p);
            }
        }
    }

    public void mostrarAlertaPedidoExterno(int quarto, String itens, String hora) {
        fpedidos.PedidoOnline p = new fpedidos.PedidoOnline();
        p.id = -1;
        p.numeroQuarto = quarto;
        p.itens = itens;
        p.hora = hora;
        view.mostrarAlertaPedido(p);
    }

    public float calculaAdicionalPessoa(int numeroPessoas) {
        int add = (numeroPessoas - 2) * 30;
        return Math.max(0, add);
    }

    public int subtrairHora(int numeroQuarto, String horarioQuarto, String ondeEncaixa) {
        String diferenca = calculaData(horarioQuarto);
        String[] partes = diferenca.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        int totalMinutos = (horas * 60) + minutos;
        int add = 0, minPernoite = 730;

        if (ondeEncaixa.equals("pernoite")) {
            if (totalMinutos <= minPernoite)
                return 0;
            while (totalMinutos > minPernoite) {
                add++;
                minPernoite += 60;
            }
            return add;
        } else {
            CacheDados cache = CacheDados.getInstancia();
            DadosOcupados ocupado = cache.getCacheOcupado().get(numeroQuarto); // Corrigido para usar parâmetro
            if (ocupado == null)
                return 0;

            String periodoQuarto = ocupado.getTempoPeriodo();
            String[] partesPeriodo = periodoQuarto.split(":");
            int totalPeriodo = (Integer.parseInt(partesPeriodo[0]) * 60) + Integer.parseInt(partesPeriodo[1]) + 10;

            if (totalMinutos <= totalPeriodo)
                return 0;
            while (totalMinutos > totalPeriodo) {
                totalPeriodo += 60;
                add++;
            }
            return add;
        }
    }

    public String calculaData(String dataBanco) {
        try {
            Timestamp horaBanco = Timestamp.valueOf(dataBanco);
            long diferencaMillis = System.currentTimeMillis() - horaBanco.getTime();
            long segundos = diferencaMillis / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            return String.format("%02d:%02d", horas, minutos % 60);
        } catch (Exception e) {
            return "00:00";
        }
    }

    public String formatarData(String dataOriginal) {
        if (dataOriginal == null || dataOriginal.isEmpty())
            return "";
        SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try {
            Date data = formatoOriginal.parse(dataOriginal);
            SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM HH:mm");
            return formatoDesejado.format(data);
        } catch (ParseException e) {
            logger.error("Erro na função formatarData", e);
            return dataOriginal;
        }
    }

    public static void abrirURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                if (os.contains("win")) {
                    pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", url);
                } else {
                    pb = new ProcessBuilder("xdg-open", url);
                }
                pb.start();
            }
        } catch (Exception e) {
            logger.error("Erro ao abrir URL: {}", url, e);
        }
    }

    public boolean mudaStatusNaCache(int quartoMudar, String statusColocar, Timestamp hora) {
        CacheDados dados = CacheDados.getInstancia();
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        if (quarto == null)
            return false;

        Timestamp timestamp = (hora != null) ? hora : new Timestamp(System.currentTimeMillis());
        quarto.setStatusQuarto(statusColocar);
        quarto.setHoraStatus(String.valueOf(timestamp));

        dados.getCacheQuarto().put(quartoMudar, quarto);
        if (statusColocar.contains("ocupado")) {
            dados.carregarOcupado(quartoMudar);
        } else {
            dados.getCacheOcupado().remove(quartoMudar);
        }
        return true;
    }

    public boolean alteraOcupadoCache(int quartoMudar, String statusColocar) {
        CacheDados dados = CacheDados.getInstancia();
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        if (quarto == null)
            return false;
        quarto.setStatusQuarto(statusColocar);
        dados.getCacheQuarto().put(quartoMudar, quarto);
        return true;
    }

    public void executaIniciar(int quartoEmFoco) {
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "ocupado-periodo", null);
            view.mostraQuartos();
            if (new fquartos().registraLocacao(quartoEmFoco)) {
                if (!new fquartos().setStatus(quartoEmFoco, "ocupado-periodo")) {
                    JOptionPane.showMessageDialog(null, "Falha ao iniciar locação no banco!");
                } else {
                    view.focoQuarto();
                    new Thread(() -> {
                        try {
                            Thread.sleep(300);
                            new ConectaArduino(quartoEmFoco);
                            Thread.sleep(800);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            logger.error("Thread interrompida ao acionar Arduino no início da locação", ex);
                        }
                        new ConectaArduino(888);
                    }).start();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Falha ao iniciar locação!");
            }
        }
    }

    public void executarFinalizar(int quartoEmFoco) {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        if (idCaixa == 0) {
            new com.motelinteligente.telas.modernas.CaixaFrameModerno().setVisible(true);
            JOptionPane.showMessageDialog(null, "Precisa abrir o caixa!");
        } else {
            int confirmacao = JOptionPane.showConfirmDialog(null, "Deseja encerrar o quarto " + quartoEmFoco + "?",
                    "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                view.abrirEncerraQuarto(quartoEmFoco);
            }
        }
    }

    public void trocaQuarto(int idLocacao, int quartoEmFoco, int numeroNovoQuarto) {
        fquartos quarto = new fquartos();
        String horaStatus = quarto.getDataInicio(quartoEmFoco);
        Timestamp hStatus = null;
        try {
            if (horaStatus != null)
                hStatus = Timestamp.valueOf(horaStatus);
        } catch (Exception e) {
            logger.error("Erro ao converter horaStatus para Timestamp. horaStatus={}", horaStatus, e);
        }

        mudaStatusNaCache(quartoEmFoco, "limpeza", hStatus);
        mudaStatusNaCache(numeroNovoQuarto, "ocupado-periodo", hStatus);

        quarto.adicionaRegistro(quartoEmFoco, "limpeza");
        quarto.setStatus(quartoEmFoco, "limpeza", hStatus);
        quarto.setStatus(numeroNovoQuarto, "ocupado-periodo", hStatus);

        String SQL = "UPDATE registralocado set numquarto=" + numeroNovoQuarto + " where idlocacao = " + idLocacao;
        try (Connection link = new fazconexao().conectar()) {
            PreparedStatement statement = link.prepareStatement(SQL);
            int linhasAfetadas = statement.executeUpdate();
            if (linhasAfetadas == 1) {
                JOptionPane.showMessageDialog(null, "Troca de quarto realizada com sucesso!");
            }
        } catch (SQLException e) {
            logger.error("Erro ao trocar o quarto de {} para {}", quartoEmFoco, numeroNovoQuarto, e);
            JOptionPane.showMessageDialog(null, "Erro ao trocar o quarto: " + e.getMessage());
        }
        view.focoQuarto();
        view.mostraQuartos();
    }

    public void salvaAntecipado(int idLocacao, String tipo, float valor) {
        tipo = switch (tipo) {
            case "C" -> "credito";
            case "D" -> "debito";
            case "O" -> "dinheiro";
            case "P" -> "pix";
            default -> "desconto";
        };
        configGlobal config = configGlobal.getInstance();
        int idCaixaatual = config.getCaixa();

        try (Connection link = new fazconexao().conectar()) {
            String currentTime = new java.sql.Timestamp(System.currentTimeMillis()).toString();
            String updateSQL = "UPDATE antecipado SET valor = ?, hora = ?, idcaixaatual = ? WHERE idlocacao = ? AND tipo = ?";
            try (PreparedStatement updateStatement = link.prepareStatement(updateSQL)) {
                updateStatement.setFloat(1, valor);
                updateStatement.setString(2, currentTime);
                updateStatement.setInt(3, idCaixaatual);
                updateStatement.setInt(4, idLocacao);
                updateStatement.setString(5, tipo);

                int rowsUpdated = updateStatement.executeUpdate();
                if (rowsUpdated == 0) {
                    String insertSQL = "INSERT INTO antecipado (idlocacao, tipo, valor, hora , idcaixaatual) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStatement = link.prepareStatement(insertSQL)) {
                        insertStatement.setInt(1, idLocacao);
                        insertStatement.setString(2, tipo);
                        insertStatement.setFloat(3, valor);
                        insertStatement.setString(4, currentTime);
                        insertStatement.setInt(5, idCaixaatual);
                        insertStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao inserir/atualizar antecipado. locacao={}, tipo={}", idLocacao, tipo, e);
            JOptionPane.showMessageDialog(null, "Erro ao inserir/atualizar: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void atualizaAntecipado(int locacao, javax.swing.JTextField txtAntecipado,
            javax.swing.JTextField txtDescontoNegociado) {
        float jaRecebeu = 0;
        float valorDesconto = 0;
        try (Connection link = new fazconexao().conectar()) {
            String selectSQL = "SELECT tipo, valor FROM antecipado WHERE idlocacao = ?";
            try (PreparedStatement preparedStatement = link.prepareStatement(selectSQL)) {
                preparedStatement.setInt(1, locacao);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String tipo = resultSet.getString("tipo");
                        float valor = resultSet.getFloat("valor");
                        if (tipo.equals("desconto"))
                            valorDesconto += valor;
                        else
                            jaRecebeu += valor;
                    }
                }
            }
            txtAntecipado.setText(String.format("R$ %.2f", jaRecebeu));
            txtDescontoNegociado.setText(String.format("R$ %.2f", valorDesconto));
        } catch (SQLException e) {
            logger.error("Erro ao buscar dados antecipados para locacao {}", locacao, e);
            JOptionPane.showMessageDialog(null, "Erro ao buscar dados antecipados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public float carregarValorRecebido(String tipoPagamento, int idLocacao) {
        String recebido = switch (tipoPagamento) {
            case "C" -> "credito";
            case "D" -> "debito";
            case "O" -> "dinheiro";
            case "P" -> "pix";
            default -> null;
        };

        if (recebido == null)
            return 0;

        String consultaSQL = "SELECT valor FROM antecipado WHERE idlocacao = ? AND tipo = ?";
        try (Connection link = new fazconexao().conectar()) {
            try (PreparedStatement statement = link.prepareStatement(consultaSQL)) {
                statement.setInt(1, idLocacao);
                statement.setString(2, recebido);
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getFloat("valor");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao carregar valor recebido. tipo={}, idLocacao={}", tipoPagamento, idLocacao, e);
            return 0;
        }
        return 0;
    }

    public void colocarLivre(int quartoEmFoco) {
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "livre", null);
            view.mostraQuartos();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    String status = quarto.getStatus(quartoEmFoco);
                    quarto.setStatus(quartoEmFoco, "livre");
                    quarto.alteraRegistro(quartoEmFoco, status);
                    view.focoQuarto();
                    return null;
                }
            }.execute();
        }
    }

    public void colocarManutencao(int quartoEmFoco) {
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "manutencao", null);
            com.motelinteligente.dados.configGlobal.getInstance().setMudanca(true);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    String statusAntes = quarto.getStatus(quartoEmFoco);
                    quarto.setStatus(quartoEmFoco, "manutencao");
                    if (!"livre".equals(statusAntes)) {
                        quarto.alteraRegistro(quartoEmFoco, statusAntes);
                    }
                    quarto.adicionaRegistro(quartoEmFoco, "manutencao");
                    return null;
                }
            }.execute();
        }
    }

    public void colocarReserva(int quartoEmFoco) {
        if (quartoEmFoco != 0) {
            mudaStatusNaCache(quartoEmFoco, "reservado", null);
            com.motelinteligente.dados.configGlobal.getInstance().setMudanca(true);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    fquartos quarto = new fquartos();
                    String statusAntes = quarto.getStatus(quartoEmFoco);
                    quarto.setStatus(quartoEmFoco, "reservado");
                    if (!"livre".equals(statusAntes)) {
                        quarto.alteraRegistro(quartoEmFoco, statusAntes);
                    }
                    quarto.adicionaRegistro(quartoEmFoco, "reservado");
                    return null;
                }
            }.execute();
        }
    }

    public void mudarParaPernoite(int quartoEmFoco) {
        if (quartoEmFoco != 0) {
            alteraOcupadoCache(quartoEmFoco, "ocupado-pernoite");
            view.mostraQuartos();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new fquartos().alteraOcupado(quartoEmFoco, "ocupado-pernoite");
                    view.focoQuarto();
                    SwingUtilities.invokeLater(() -> view.mostraQuartos());
                    return null;
                }
            }.execute();
        }
    }

    public void mudarParaPeriodo(int quartoEmFoco) {
        if (quartoEmFoco != 0) {
            alteraOcupadoCache(quartoEmFoco, "ocupado-periodo");
            view.mostraQuartos();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new fquartos().alteraOcupado(quartoEmFoco, "ocupado-periodo");
                    view.focoQuarto();
                    SwingUtilities.invokeLater(() -> view.mostraQuartos());
                    return null;
                }
            }.execute();
        }
    }

    public Map<String, Object> calcularResumoFinanceiro(int quartoEmFoco, String horarioQuarto, int numPessoas) {
        Map<String, Object> resumo = new java.util.HashMap<>();
        CacheDados cache = CacheDados.getInstancia();
        DadosOcupados ocupado = cache.getCacheOcupado().get(quartoEmFoco);

        if (ocupado == null)
            return resumo;

        fquartos dao = new fquartos();
        PeriodoQuarto pAtual = dao.getPeriodoAtual(quartoEmFoco);

        float valorBase = 0;
        int tempoMinutos = 120;
        boolean isPernoite = false;

        if (pAtual != null) {
            valorBase = pAtual.getValor();
            tempoMinutos = pAtual.getTempoMinutos();
            isPernoite = pAtual.isPernoite();
        } else {
            valorBase = ocupado.getValorPeriodo();
        }

        float valorAdicionalPessoa = calculaAdicionalPessoa(numPessoas);
        float valorTotalQuarto = valorBase + valorAdicionalPessoa;

        int numeroAdicionais = 0;
        if (isPernoite) {
            numeroAdicionais = subtrairHora(quartoEmFoco, horarioQuarto, "pernoite");
        } else {
            String tempoStr = String.format("%02d:%02d", tempoMinutos / 60, tempoMinutos % 60);
            numeroAdicionais = subtrairHora(quartoEmFoco, horarioQuarto, tempoStr);
        }

        float valorExcedente = (float) numeroAdicionais * ocupado.getValorAdicional();

        resumo.put("valorBase", valorBase);
        resumo.put("valorTotalQuarto", valorTotalQuarto);
        resumo.put("valorExcedente", valorExcedente);
        resumo.put("entradaFormatada", formatarData(horarioQuarto));

        return resumo;
    }

    public List<Map<String, Object>> getProdutosConsumidos(int idLocacao) {
        List<Map<String, Object>> produtos = new ArrayList<>();
        try (Connection link = new fazconexao().conectar()) {
            String sql = "SELECT idproduto, quantidade FROM prevendidos WHERE idlocacao = ?";
            try (PreparedStatement stmt = link.prepareStatement(sql)) {
                stmt.setInt(1, idLocacao);
                try (ResultSet rs = stmt.executeQuery()) {
                    fprodutos produtoDao = new fprodutos();
                    while (rs.next()) {
                        Map<String, Object> item = new java.util.HashMap<>();
                        int id = rs.getInt("idproduto");
                        int qtd = rs.getInt("quantidade");
                        float valor = produtoDao.getValorProduto(id);

                        item.put("quantidade", qtd);
                        item.put("descricao", produtoDao.getDescicao(String.valueOf(id)));
                        item.put("valorUnitario", valor);
                        item.put("valorTotal", valor * qtd);
                        produtos.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar produtos consumidos para locacao {}", idLocacao, e);
        }
        return produtos;
    }

    public void removerAlarme(int idAlarme) {
        new FAlarmes().removeAlarmFromDatabase(idAlarme);
    }

    public void marcarPedidoVisto(int idPedido) {
        new fpedidos().marcarComoVisto(idPedido);
    }
}
