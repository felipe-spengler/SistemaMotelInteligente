/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import com.fazecast.jSerialComm.SerialPort;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 *
 * @author johnc
 */
public class CacheDados {

    private static CacheDados instancia;
    private Map<Integer, CarregaQuarto> cacheQuarto = new HashMap<>();
    private Map<Integer, DadosOcupados> cacheOcupado = new HashMap<>();
    public Map<Integer, List<DadosVendidos>> cacheProdutosVendidos = new HashMap<>();
    public Map<Integer, List<Negociados>> cacheNegociado = new HashMap<>();
    SerialPort arduinoPort;

    private CacheDados() {
        // Construtor privado para impedir instâncias diretas
    }

    public static synchronized CacheDados getInstancia() {
        if (instancia == null) {
            instancia = new CacheDados();
        }
        return instancia;
    }

    public SerialPort getArduino() {
        return arduinoPort;
    }

    public void carregaArduino() {
        arduinoPort = SerialPort.getCommPort("COM4");
        if (!arduinoPort.openPort()) {
            JOptionPane.showMessageDialog(null, "Falha ao abrir a porta COM4 - Conecte o arduino.");
            return;
        }

        // Configura os parâmetros de comunicação
        arduinoPort.setBaudRate(9600); // Taxa de transmissão
        arduinoPort.setNumDataBits(8); // Bits de dados
        arduinoPort.setNumStopBits(1); // Bits de parada
        arduinoPort.setParity(SerialPort.NO_PARITY); // Paridade
    }

    public void carregarOcupado(int numeroQuarto) {
    float valPeriodo = 0, valPernoite = 0, valAdicional = 0;
    int pessoas = 0;
    fquartos quartodao = new fquartos();
    valPernoite = quartodao.getValorQuarto(numeroQuarto, "pernoite");
    valPeriodo = quartodao.getValorQuarto(numeroQuarto, "periodo");
    valAdicional = quartodao.getAdicional(numeroQuarto);
    pessoas = quartodao.getPessoas(numeroQuarto);
    String tempo = quartodao.getPeriodo(numeroQuarto);
    System.out.println("Nos ocupados");
    int idLoca = new fquartos().getIdLocacao(numeroQuarto);
    DadosOcupados ocupado = new DadosOcupados(idLoca, valPeriodo, valPernoite, pessoas, valAdicional, tempo);
    cacheOcupado.put(numeroQuarto, ocupado);
    carregaProdutosNegociadosCache(idLoca);
}

    public void carregaProdutosNegociadosCache(int idLoca){
        
    // Consulta produtos prevendidos no banco de dados e adiciona à cache
    String consultaProdutosSQL = "SELECT idproduto, quantidade FROM prevendidos WHERE idlocacao = ?";
    try (Connection link = new fazconexao().conectar();
         PreparedStatement statementProdutos = link.prepareStatement(consultaProdutosSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
        statementProdutos.setInt(1, idLoca);
        try (ResultSet resultadoProdutos = statementProdutos.executeQuery()) {
            // Verifica se há produtos prevendidos para essa locação no banco de dados
            if (resultadoProdutos.next()) {
                // Cria uma lista para armazenar os produtos prevendidos
                List<DadosVendidos> produtosVendidos = new ArrayList<>();

                // Adiciona os produtos prevendidos do banco de dados à lista
                do {
                    int idProdutoBD = resultadoProdutos.getInt("idproduto");
                    int quantidadeBD = resultadoProdutos.getInt("quantidade");
                    produtosVendidos.add(new DadosVendidos(idProdutoBD, quantidadeBD));
                } while (resultadoProdutos.next());

                // Adiciona a lista de produtos prevendidos à cache
                cacheProdutosVendidos.put(idLoca, produtosVendidos);

            } 
        }
    } catch (SQLException e) {
        // Tratamento de erro
        e.printStackTrace();
    }

    // Consulta negociações antecipadas no banco de dados e adiciona à cacheNegociados
    String consultaNegociacoesSQL = "SELECT * FROM antecipado WHERE idlocacao = ?";
    try (Connection link = new fazconexao().conectar();
         PreparedStatement statementNegociacoes = link.prepareStatement(consultaNegociacoesSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
        statementNegociacoes.setInt(1, idLoca);
        try (ResultSet resultadoNegociacoes = statementNegociacoes.executeQuery()) {
            if (resultadoNegociacoes.next()) {
                // Reinicia o cursor do ResultSet
                resultadoNegociacoes.beforeFirst();
                List<Negociados> negociacoes = new ArrayList<>();
                while (resultadoNegociacoes.next()) {
                    String tipo = resultadoNegociacoes.getString("tipo");
                    float valor = resultadoNegociacoes.getFloat("valor");
                    System.out.println("Negociado add - locacao " + idLoca + " tipo " + tipo + " valor "+ valor);
                    // Cria um objeto Negociado com os valores do ResultSet
                    Negociados negociado = new Negociados(tipo, valor);
                    // Adiciona o objeto Negociado à cacheNegociados
                    negociacoes.add(negociado);
                }
                
                cacheNegociado.put(idLoca, negociacoes);
            }
        }
    } catch (SQLException e) {
        // Tratamento de erro
        e.printStackTrace();
    }
    }
    public CarregaQuarto carregarDadosQuarto() {
        int numeroQuarto = 0;
        String data;
        String status;
        String tipo;
        Connection link = null;
        try {
            link = new fazconexao().conectar();
            String consultaSQL = "select * from status order by numeroquarto";
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            ResultSet resultado = statement.executeQuery();
            while (resultado.next()) {

                numeroQuarto = resultado.getInt("numeroquarto");
                status = resultado.getString("atualquarto");
                data = String.valueOf(resultado.getTimestamp("horastatus"));
                tipo = new fquartos().getTipo(numeroQuarto);

                CarregaQuarto quarto = new CarregaQuarto();
                quarto.setNumeroQuarto(numeroQuarto);
                quarto.setTipoQuarto(tipo);
                quarto.setStatusQuarto(status);
                quarto.setHoraStatus(data);

                cacheQuarto.put(numeroQuarto, quarto);
                if (status.contains("ocupado")) {
                    carregarOcupado(numeroQuarto);
                }
            }
            // Fechar os recursos
            resultado.close();
            statement.close();
            link.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Certifique-se de que a conexão seja encerrada mesmo se ocorrerem exceções
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Map<Integer, CarregaQuarto> getCacheQuarto() {
        return cacheQuarto;
    }

    public Map<Integer, DadosOcupados> getCacheOcupado() {
        return cacheOcupado;
    }

    public void mostrarCacheQuarto() {
        if (cacheQuarto.isEmpty()) {
            System.out.println("A cache de quartos está vazia.");
        } else {
            System.out.println("Conteúdo da cache de quartos:");
            for (Map.Entry<Integer, CarregaQuarto> entry : cacheQuarto.entrySet()) {
                int numeroQuarto = entry.getKey();
                CarregaQuarto quarto = entry.getValue();
                System.out.println("Número do Quarto: " + numeroQuarto + " Tipo de Quarto: " + quarto.getTipoQuarto());
                System.out.println("Status do Quarto: " + quarto.getStatusQuarto() + " Hora do Status: " + quarto.getHoraStatus());
                System.out.println("--------------------------------------");
            }
        }
        if (cacheOcupado.isEmpty()) {
            System.out.println("A cache de quartos está vazia.");
        } else {
            System.out.println("Conteúdo da cache de OCUPADOS:");
            for (Map.Entry<Integer, DadosOcupados> entry : cacheOcupado.entrySet()) {
                int numeroQuarto = entry.getKey();
                DadosOcupados quarto = entry.getValue();
                System.out.println("Número do Quarto: " + numeroQuarto);
                System.out.println("--------------------------------------");
            }
        }
    }

    public void mostrarCacheProdutosVendidos() {
        if (cacheProdutosVendidos.isEmpty()) {
            System.out.println("A cache de produtos vendidos está vazia.");
        } else {
            System.out.println("Conteúdo da cache de produtos vendidos:");
            for (Map.Entry<Integer, List<DadosVendidos>> entry : cacheProdutosVendidos.entrySet()) {
                int idLocacao = entry.getKey();
                List<DadosVendidos> produtosVendidos = entry.getValue();
                System.out.println("ID da Locação: " + idLocacao);
                for (DadosVendidos dadosVendidos : produtosVendidos) {
                    System.out.println("ID do Produto: " + dadosVendidos.idProduto
                            + ", Quantidade Vendida: " + dadosVendidos.quantidadeVendida);
                }
                System.out.println("--------------------------------------");
            }
        }
        if (cacheNegociado.isEmpty()) {
            System.out.println("A cache de negociados está vazia.");
        } else {
            System.out.println("Conteúdo da cache de negociados:");
            for (Map.Entry<Integer, List<Negociados>> entry : cacheNegociado.entrySet()) {
                int idLocacao = entry.getKey();
                List<Negociados> negociados = entry.getValue();
                System.out.println("ID da Locação: " + idLocacao);
                for (Negociados negociado : negociados) {
                    System.out.println("Tipo: " + negociado.tipo
                            + ", Valor: " + negociado.valor);
                }
                System.out.println("--------------------------------------");
            }
        }
    }

    public static class DadosVendidos {

        public int idProduto;
        public int quantidadeVendida;

        public DadosVendidos(int id, int qnt) {
            idProduto = id;
            quantidadeVendida = qnt;
        }

    }

    public static class Negociados {

        public String tipo;
        public float valor;

        public Negociados(String tp, float val) {
            tipo = tp;
            valor = val;
        }

    }
}
