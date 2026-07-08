package com.motelinteligente.telas.controller;

import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.dados.Antecipado;
import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.CacheDados.DadosVendidos;
import com.motelinteligente.dados.CarregaQuarto;
import com.motelinteligente.dados.DadosOcupados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import com.motelinteligente.dados.fprodutos;
import com.motelinteligente.dados.fquartos;
import com.motelinteligente.dados.PeriodoQuarto;
import com.motelinteligente.dados.playSound;
import com.motelinteligente.dados.vendaProdutos;

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

public class EncerraQuartoController {

    private static final Logger logger = LoggerFactory.getLogger(EncerraQuartoController.class);

    private int numeroDoQuarto;
    private int idLocacao;
    private String dataInicio;
    private String dataFim;
    private String tempoTotalLocado;

    // Valores monetários
    private float valorQuarto = 0;
    private float valorConsumo = 0;
    private float valorAdicionalPeriodo = 0;
    private float valorAdicionalPessoa = 0;
    private float valorDesconto = 0;
    private float valorAcrescimo = 0;
    private float valorRecebidoAntecipado = 0; // Valor já recebido anteriormente (ex: entrada)
    private float valorRecebidoAgora = 0; // Valor digitado no campo de recebimento manual
    private String periodoFinalStr = "Periodo Padrão"; // Armazena a descrição do periodo final cobrado

    // Valores de pagamento processados no JOP
    private float valD = 0; // Dinheiro
    private float valP = 0; // Pix
    private float valC = 0; // Cartão (Crédito + Débito)

    public EncerraQuartoController(int numeroDoQuarto) {
        this.numeroDoQuarto = numeroDoQuarto;
        inicializarDadosLocacao();
    }

    private void inicializarDadosLocacao() {
        fquartos quartodao = new fquartos();
        CacheDados cache = CacheDados.getInstancia();

        // Garante que temos o ID da Locação
        if (cache.getCacheOcupado().containsKey(numeroDoQuarto)) {
            idLocacao = cache.getCacheOcupado().get(numeroDoQuarto).getIdLoca();
        }

        if (idLocacao == 0) {
            idLocacao = quartodao.getIdLocacao(numeroDoQuarto);
        }

        // Dados de Data/Hora
        dataInicio = quartodao.getDataInicio(numeroDoQuarto);
        dataFim = String.valueOf(new Timestamp(System.currentTimeMillis()));
        tempoTotalLocado = quartodao.getData(numeroDoQuarto);

        // Cálculo inicial de valores do quarto (Pernoite/Período)
        DadosOcupados ocupado = cache.getCacheOcupado().get(numeroDoQuarto);
        CarregaQuarto quarto = cache.getCacheQuarto().get(numeroDoQuarto);

        if (ocupado != null && quarto != null) {
            String status = quarto.getStatusQuarto();
            String horarioQuarto = quarto.getHoraStatus();
            String[] partes = status.split("-");

            // Adicional por Pessoa
            valorAdicionalPessoa = calculaAdicionalPessoa(ocupado.getNumeroPessoas());

            List<PeriodoQuarto> periodos = quartodao.getPeriodos(numeroDoQuarto);

            if (periodos != null && !periodos.isEmpty()) {
                // NOVO MODELO (DINAMICO E CONTINUO)
                String diferenca = calculaData(horarioQuarto);
                String[] partesDif = diferenca.split(":");
                int horasPassadas = Integer.parseInt(partesDif[0]);
                int minutosPassados = Integer.parseInt(partesDif[1]);
                int totalMinutosPassados = (horasPassadas * 60) + minutosPassados;

                valorQuarto = periodos.get(0).getValor();
                PeriodoQuarto periodoEncontrado = null;

                // 1. Tentar localizar pelo período exato gravado na locação (Vindo do Totem)
                String periodoGravado = quartodao.getPeriodoLocado(idLocacao);
                if (periodoGravado != null && !periodoGravado.isEmpty()) {
                    for (PeriodoQuarto p : periodos) {
                        if (p.getDescricao().equalsIgnoreCase(periodoGravado)) {
                            
                            // SE o status manual for diferente do tipo do período do totem, 
                            // ignora o travamento do totem e permite o recálculo dinâmico.
                            boolean statusEhPernoite = status.contains("pernoite");
                            if (statusEhPernoite != p.isPernoite()) {
                                continue; 
                            }

                            valorQuarto = p.getValor();
                            periodoFinalStr = p.getDescricao();
                            valorAdicionalPeriodo = 0;
                            
                            // Caso ultrapasse o tempo do período escolhido, cobra adicional
                            if (totalMinutosPassados > p.getTempoMinutos() + 10) {
                                int add = 0;
                                int totalPeriodo = p.getTempoMinutos() + 10;
                                while (totalMinutosPassados > totalPeriodo) {
                                    totalPeriodo += 60;
                                    add++;
                                }
                                valorAdicionalPeriodo = (float) add * ocupado.getValorAdicional();
                            }
                            return; // Encontrou o preço exato e os tipos batem, pode parar.
                        }
                    }
                }

                // 2. Se não tem período gravado ou não achou o nome, usa a lógica de tempo (Upgrade/Recalculo)
                if (status.contains("pernoite")) {
                    for (PeriodoQuarto p : periodos) {
                        if (p.isPernoite()) {
                            periodoEncontrado = p;
                            break;
                        }
                    }
                } else {
                    // Lógica para cobrança por PERÍODO (Upgrade automático)
                    // Pega o menor período que cubra o tempo passado
                    for (PeriodoQuarto p : periodos) {
                        if (!p.isPernoite()) {
                            if (totalMinutosPassados <= p.getTempoMinutos() + 10) {
                                periodoEncontrado = p;
                                break;
                            }
                        }
                    }
                }

                // Se não achou nenhum período que caiba no tempo (ex: ficou 5h e o maior é 3h)
                // Pega o MAIOR período que não seja pernoite para usar como base das horas adicionais.
                if (periodoEncontrado == null && !periodos.isEmpty()) {
                    for (int i = periodos.size() - 1; i >= 0; i--) {
                        if (!periodos.get(i).isPernoite()) {
                            periodoEncontrado = periodos.get(i);
                            break;
                        }
                    }
                }
                
                // Se ainda for nulo (caso só existam pernoites na lista), pega o primeiro
                if (periodoEncontrado == null && !periodos.isEmpty()) {
                    periodoEncontrado = periodos.get(0);
                }

                valorQuarto = periodoEncontrado.getValor();
                periodoFinalStr = periodoEncontrado.getDescricao();
                valorAdicionalPeriodo = 0;

                // Horas adicionais (caso ultrapasse o pernoite ou ultimo maior periodo)
                if (totalMinutosPassados > periodoEncontrado.getTempoMinutos() + 10) {
                    int add = 0;
                    int totalPeriodo = periodoEncontrado.getTempoMinutos() + 10;
                    while (totalMinutosPassados > totalPeriodo) {
                        totalPeriodo += 60;
                        add++;
                    }
                    valorAdicionalPeriodo = (float) add * ocupado.getValorAdicional();
                }

            } else {
                // Caso não existam períodos configurados, assume-se que o sistema deve ser configurado
                logger.error("ERRO CRÍTICO: Quarto #" + numeroDoQuarto + " não possui períodos configurados na tabela periodos_quarto.");
                valorQuarto = 0;
                periodoFinalStr = "Erro: Sem Períodos";
            }
        }
    }

    public List<vendaProdutos> buscarPreVendidos() {
        List<vendaProdutos> lista = new ArrayList<>();
        String consultaProdutosSQL = "SELECT idproduto, quantidade FROM prevendidos WHERE idlocacao = ?";

        try (Connection link = new fazconexao().conectar();
                PreparedStatement statementProdutos = link.prepareStatement(consultaProdutosSQL)) {

            statementProdutos.setInt(1, idLocacao);

            try (ResultSet resultadoProdutos = statementProdutos.executeQuery()) {
                fprodutos fprod = new com.motelinteligente.dados.fprodutos();
                while (resultadoProdutos.next()) {
                    int id = resultadoProdutos.getInt("idproduto");
                    int quantidade = resultadoProdutos.getInt("quantidade");

                    String descricao = fprod.getDescicao(String.valueOf(id));
                    float valor = fprod.getValorProduto(id);
                    float total = valor * quantidade;

                    lista.add(new vendaProdutos(id, quantidade, valor, total));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar prevendidos: ", e);
        }
        return lista;
    }

    public List<Antecipado> buscarAntecipados() {
        List<Antecipado> lista = new ArrayList<>();
        valorRecebidoAntecipado = 0; // Reset
        valorDesconto = 0; // Reset inicial, pode ser alterado se houver desconto antecipado

        try (Connection link = new fazconexao().conectar();
                PreparedStatement ps = link
                        .prepareStatement("SELECT tipo, valor, hora FROM antecipado WHERE idlocacao = ?")) {

            ps.setInt(1, idLocacao);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    float valor = rs.getFloat("valor");
                    Timestamp hora = rs.getTimestamp("hora");

                    if (tipo.equals("desconto")) {
                        this.valorDesconto = valor;
                        // O View deve checar se este valor > 0 para preencher o campo desconto
                    } else {
                        valorRecebidoAntecipado += valor;
                        lista.add(new Antecipado(tipo, valor, hora));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar antecipados: ", e);
        }
        return lista;
    }

    // Lógica Matemática
    public float calculaAdicionalPessoa(int numeroPessoas) {
        float valAdd = new fquartos().getAddPessoa(numeroDoQuarto);
        if (valAdd == 0)
            valAdd = 1;

        float adicional = (numeroPessoas - 2) * valAdd;
        this.valorAdicionalPessoa = (adicional <= 0) ? 0 : adicional;
        return this.valorAdicionalPessoa;
    }

    public int subtrairHora(int numeroQuarto, String horarioQuarto, String ondeEncaixa) {
        String diferenca = calculaData(horarioQuarto);
        String[] partes = diferenca.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        int totalMinutos = (horas * 60) + minutos;
        int add = 0, minPernoite = 730; // 12h + 10min tolerance

        if (ondeEncaixa.equals("pernoite")) {
            if (totalMinutos <= minPernoite) {
                return 0;
            } else {
                while (totalMinutos > minPernoite) {
                    add++;
                    minPernoite += 60;
                }
                return add;
            }
        } else {
            CacheDados cache = CacheDados.getInstancia();
            DadosOcupados ocupado = cache.getCacheOcupado().get(numeroQuarto);
            // Proteção contra nulo
            if (ocupado == null)
                return 0;

            String periodoQuarto = ocupado.getTempoPeriodo();
            if (periodoQuarto == null)
                return 0;

            String[] partesPeriodo = periodoQuarto.split(":");
            int horasPeriodo = Integer.parseInt(partesPeriodo[0]);
            int minutosPeriodo = Integer.parseInt(partesPeriodo[1]);
            int totalPeriodo = (horasPeriodo * 60) + minutosPeriodo + 10;

            if (totalMinutos <= totalPeriodo) {
                return 0;
            } else {
                while (totalMinutos > totalPeriodo) {
                    totalPeriodo += 60;
                    add++;
                }
                return add;
            }
        }
    }

    public String calculaData(String dataBanco) {
        if (dataBanco == null)
            return "00:00";
        Timestamp horaBanco = Timestamp.valueOf(dataBanco);
        Long datetime = System.currentTimeMillis();
        Timestamp horaAtual = new Timestamp(datetime);

        long diferencaMillis = horaAtual.getTime() - horaBanco.getTime();
        long minutos = diferencaMillis / (60 * 1000) % 60;
        long horas = diferencaMillis / (60 * 60 * 1000);

        return String.format("%02d:%02d", horas, minutos);
    }

    // Gets e Sets para a View usar
    public float calcularDividaTotal() {
        // total = (quarto + consumo + acrescimos + adicionais) - desconto
        return (valorQuarto + valorConsumo + valorAcrescimo + valorAdicionalPeriodo + valorAdicionalPessoa)
                - valorDesconto;
    }

    public float calcularValorAReceber() {
        return calcularDividaTotal() - valorRecebidoAntecipado - valorRecebidoAgora;
    }

    public void setValorConsumo(float valorConsumo) {
        this.valorConsumo = valorConsumo;
    }

    public float getValorConsumo() {
        return valorConsumo;
    }

    public void setValorRecebidoAgora(float valor) {
        this.valorRecebidoAgora = valor;
    }

    public void setValorDesconto(float valor) {
        this.valorDesconto = valor;
    }

    public void setValorAcrescimo(float valor) {
        this.valorAcrescimo = valor;
    }

    // Getters
    public int getIdLocacao() {
        return idLocacao;
    }

    public String getDataInicio() {
        return dataInicio;
    }

    public String getDataFim() {
        return dataFim;
    }

    public String getTempoTotalLocado() {
        return tempoTotalLocado;
    }

    public float getValorQuartoBase() {
        return valorQuarto;
    }

    public float getValorAdicionalPeriodo() {
        return valorAdicionalPeriodo;
    }

    public float getValorAdicionalPessoa() {
        return valorAdicionalPessoa;
    }

    public float getValorDesconto() {
        return valorDesconto;
    }

    public float getValorAcrescimo() {
        return valorAcrescimo;
    }

    public float getValorRecebidoAntecipado() {
        return valorRecebidoAntecipado;
    }

    // Salvar e Finalizar
    public void salvarLocacao(List<vendaProdutos> produtosVendidos, float pagoDinheiro, float pagoPix,
            float pagoCredito, float pagoDebito) {
        this.valD = pagoDinheiro;
        this.valP = pagoPix;
        this.valC = pagoCredito + pagoDebito;

        // 1. Salvar Produtos
        fquartos quartodao = new fquartos();
        com.motelinteligente.dados.fprodutos prodDao = new com.motelinteligente.dados.fprodutos();

        for (vendaProdutos vp : produtosVendidos) {
            quartodao.salvaProduto(idLocacao, vp.idProduto, vp.quantidade, vp.valorUnd, vp.valorTotal);
            prodDao.diminuiEstoque(vp.idProduto, vp.quantidade);
        }

        // 2. Salvar Cartão
        if (pagoCredito > 0 || pagoDebito > 0) {
            salvaCartao(idLocacao, pagoCredito, pagoDebito);
        }

        // 3. Salvar Justificativa Se Houver
        // (A view deve chamar salvaJustifica separadamente ou passamos o texto aqui?
        // Pela lógica original, salvava antes. Vamos manter separado por ora ou assumir
        // que já foi feito)

        // 4. Salvar Locação (Fechar)
        float valorTotalQuarto = valorQuarto + valorAdicionalPeriodo + valorAdicionalPessoa;
        // Atualiza dataFim para agora
        Timestamp tsFim = new Timestamp(System.currentTimeMillis());
        Timestamp tsInicio = (dataInicio != null && !dataInicio.equals("N/A")) ? Timestamp.valueOf(dataInicio) : tsFim;

        quartodao.salvaLocacao(idLocacao, tsInicio, tsFim, valorTotalQuarto, valorConsumo, valD, valP, valC, periodoFinalStr);

        // 5. Sons e Hardware
        new playSound().playSound("som/agradecemosPreferencia.wav");
        new ConectaArduino(999);

        // 6. Limpar e Atualizar Sistema
        mudaStatusNaCache(numeroDoQuarto, "limpeza");
        CacheDados.getInstancia().getCacheOcupado().remove(numeroDoQuarto);

        quartodao.setStatus(numeroDoQuarto, "limpeza");
        quartodao.adicionaRegistro(numeroDoQuarto, "limpeza");
        configGlobal.getInstance().setMudanca(true);
    }

    public boolean registrarDesistencia(String motivo) {
        configGlobal config = configGlobal.getInstance();
        int idCaixa = config.getCaixa();
        fquartos quartodao = new fquartos();
        CacheDados cache = CacheDados.getInstancia();

        String horaSaida = String.valueOf(new Timestamp(System.currentTimeMillis()));
        // Se dataInicio for nula, usa agora
        String horaEntrada = (dataInicio != null) ? dataInicio : horaSaida;

        try (Connection link = new fazconexao().conectar();
                PreparedStatement statement = link.prepareStatement(
                        "INSERT INTO desistencia (numquarto, horainicio, horafim, motivo, idcaixaatual) VALUES (?, ?, ? , ? , ?)")) {

            statement.setInt(1, numeroDoQuarto);
            statement.setTimestamp(2, Timestamp.valueOf(horaEntrada));
            statement.setTimestamp(3, Timestamp.valueOf(horaSaida));
            statement.setString(4, motivo);
            statement.setInt(5, idCaixa);

            int n = statement.executeUpdate();
            if (n != 0) {
                excluiDaRegistraLocado();
                mudaStatusNaCache(numeroDoQuarto, "limpeza");
                cache.getCacheOcupado().remove(numeroDoQuarto);
                quartodao.setStatus(numeroDoQuarto, "limpeza");
                quartodao.adicionaRegistro(numeroDoQuarto, "limpeza");
                config.setMudanca(true);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Erro ao registrar desistência", e);
            return false;
        }
    }

    private void excluiDaRegistraLocado() {
        try (Connection link = new fazconexao().conectar();
                PreparedStatement statement = link.prepareStatement("DELETE FROM registralocado WHERE idlocacao = ?")) {
            statement.setInt(1, idLocacao);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao excluir registro locado", e);
        }
    }

    public boolean salvarJustificativa(String tipo, float valor, String texto) {
        try (Connection link = new fazconexao().conectar();
                PreparedStatement statement = link.prepareStatement(
                        "INSERT INTO justificativa (idlocacao, valor, tipo, justificativa) VALUES (?, ?, ?, ?)")) {

            statement.setInt(1, idLocacao);
            statement.setFloat(2, valor);
            statement.setString(3, tipo);
            statement.setString(4, texto);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Erro ao salvar justificativa", e);
            return false;
        }
    }

    private void mudaStatusNaCache(int quartoMudar, String statusColocar) {
        CacheDados dados = CacheDados.getInstancia();
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        if (quarto != null) {
            quarto.setStatusQuarto(statusColocar);
            quarto.setHoraStatus(String.valueOf(new Timestamp(System.currentTimeMillis())));
            dados.getCacheQuarto().put(quartoMudar, quarto);
        }
    }

    public void salvaCartao(int idLocacao, float recebidoCredito, float recebidoDebito) {
        String consultaSQL = "INSERT INTO valorcartao (idlocacao, valorcredito, valordebito) VALUES (?, ?, ?)";

        try (Connection link = new fazconexao().conectar();
                PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            statement.setInt(1, idLocacao);
            statement.setFloat(2, recebidoCredito);
            statement.setFloat(3, recebidoDebito);

            statement.executeUpdate();
        } catch (Exception e) {
            logger.error("Erro ao salvar o cartão: ", e);
        }
    }


    // Verifica permissão para desistência
    public boolean podeFazerDesistencia() {
        if (tempoTotalLocado == null)
            return false;
        String[] partes = tempoTotalLocado.split(":");
        if (partes.length < 2)
            return false;

        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);

        // Regra: < 10min de graça OU cargo gerente/admin
        if (horas == 0 && minutos <= 10)
            return true;

        String cargo = configGlobal.getInstance().getCargoUsuario();
        return cargo.equals("gerente") || cargo.equals("admin");
    }

    public boolean salvarPreVendidos(List<vendaProdutos> produtos) {
        try (Connection conn = new fazconexao().conectar()) {
            conn.setAutoCommit(false);
            try {
                // 1. Limpa os pré-vendidos anteriores
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM prevendidos WHERE idlocacao = ?")) {
                    del.setInt(1, idLocacao);
                    del.executeUpdate();
                }
                
                // 2. Insere a lista atual
                if (produtos != null && !produtos.isEmpty()) {
                    try (PreparedStatement ins = conn.prepareStatement("INSERT INTO prevendidos (idlocacao, idproduto, quantidade) VALUES (?, ?, ?)")) {
                        for (vendaProdutos vp : produtos) {
                            ins.setInt(1, idLocacao);
                            ins.setInt(2, vp.idProduto);
                            ins.setInt(3, vp.quantidade);
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                logger.error("Erro ao salvar prevendidos no fechamento: ", ex);
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro de conexao ao salvar prevendidos: ", e);
            return false;
        }
    }
}
