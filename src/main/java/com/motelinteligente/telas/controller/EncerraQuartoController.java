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

            // Adicional por Período ou Pernoite
            if (partes.length > 1) {
                if (partes[1].equals("pernoite")) {
                    valorQuarto = ocupado.getValorPernoite();
                    int numeroAdicionais = subtrairHora(numeroDoQuarto, horarioQuarto, "pernoite");
                    valorAdicionalPeriodo = (float) numeroAdicionais * ocupado.getValorAdicional();
                } else if (partes[1].equals("periodo")) {
                    valorQuarto = ocupado.getValorPeriodo();
                    int numeroAdicionais = subtrairHora(numeroDoQuarto, horarioQuarto, ocupado.getTempoPeriodo());
                    valorAdicionalPeriodo = (float) numeroAdicionais * ocupado.getValorAdicional();
                }
            } else {
                // Caso fallback se status não tiver hifen (ex "ocupado")
                // Assumindo periodo por padrão ou logica antiga
                valorQuarto = ocupado.getValorPeriodo();
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
        int add = 0, minPernoite = 729;

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
            int totalPeriodo = (horasPeriodo * 60) + minutosPeriodo + 9;

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

        // 2. Salvar Cartão JA FOI FEITO NA VIEW (EncerraQuarto.java)

        // 3. Salvar Justificativa Se Houver
        // (A view deve chamar salvaJustifica separadamente ou passamos o texto aqui?
        // Pela lógica original, salvava antes. Vamos manter separado por ora ou assumir
        // que já foi feito)

        // 4. Salvar Locação (Fechar)
        float valorTotalQuarto = valorQuarto + valorAdicionalPeriodo + valorAdicionalPessoa;
        // Atualiza dataFim para agora
        Timestamp tsFim = new Timestamp(System.currentTimeMillis());
        Timestamp tsInicio = (dataInicio != null && !dataInicio.equals("N/A")) ? Timestamp.valueOf(dataInicio) : tsFim;

        quartodao.salvaLocacao(idLocacao, tsInicio, tsFim, valorTotalQuarto, valorConsumo, valD, valP, valC);

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

    public void registrarDesistencia(String motivo) {
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
            }
        } catch (Exception e) {
            logger.error("Erro ao registrar desistência", e);
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
}
