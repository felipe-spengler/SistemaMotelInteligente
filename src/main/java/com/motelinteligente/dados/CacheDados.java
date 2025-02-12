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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author johnc
 */
public class CacheDados {

    private static CacheDados instancia;
    private Map<Integer, CarregaQuarto> cacheQuarto = new HashMap<>();
    private Map<Integer, DadosOcupados> cacheOcupado = new HashMap<>();
    public Map<Integer, List<DadosVendidos>> cacheProdutosVendidos = new HashMap<>();
    public Map<Integer, Timestamp> despertador = new HashMap<>();
    public static SerialPort arduinoPort;
    private static final Logger logger = LoggerFactory.getLogger(fquartos.class);

    private CacheDados() {
        // Construtor privado para impedir instâncias diretas
    }

    public static synchronized CacheDados getInstancia() {
        if (instancia == null) {
            instancia = new CacheDados();
        }
        return instancia;
    }

    public static void carregaArduino() {
        if (arduinoPort != null && arduinoPort.isOpen()) {
            return; // A conexão já está aberta
        }

        SerialPort[] portas = SerialPort.getCommPorts();
        System.out.println("Portas seriais disponíveis:");

        for (SerialPort porta : portas) {
            System.out.println("Nome: " + porta.getSystemPortName() + ", Descrição: " + porta.getDescriptivePortName());

            // Verifica se a porta corresponde ao USB-SERIAL CH340
            if (porta.getDescriptivePortName().toLowerCase().contains("usb-serial ch340")) {
                System.out.println("Arduino (CH340) encontrado na porta: " + porta.getSystemPortName());
                arduinoPort = porta; // Define a porta do Arduino
                break;
            }
        }

        if (arduinoPort == null) {
            JOptionPane.showMessageDialog(null, "Nenhum Arduino (CH340) encontrado.");
            return;
        }

        if (!arduinoPort.openPort()) {
            JOptionPane.showMessageDialog(null, "Falha ao abrir a porta " + arduinoPort.getSystemPortName() + " - Conecte o Arduino.");
            return;
        }

        // Configura os parâmetros de comunicação
        arduinoPort.setBaudRate(9600); // Taxa de transmissão
        arduinoPort.setNumDataBits(8); // Bits de dados
        arduinoPort.setNumStopBits(1); // Bits de parada
        arduinoPort.setParity(SerialPort.NO_PARITY); // Paridade
    }

    public static SerialPort getArduinoPort() {
        return arduinoPort;
    }

    public static void fecharConexao() {
        if (arduinoPort != null && arduinoPort.isOpen()) {
            arduinoPort.closePort();
        }
    }

    public void limparCaches() {
        cacheQuarto.clear();
        cacheOcupado.clear();
        cacheProdutosVendidos.clear();
        despertador.clear();
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
        int idLoca = quartodao.getIdLocacao(numeroQuarto);
        Timestamp entrada = quartodao.getHoraInicio(idLoca);
        DadosOcupados ocupado = new DadosOcupados(entrada, idLoca, valPeriodo, valPernoite, pessoas, valAdicional, tempo);
        cacheOcupado.put(numeroQuarto, ocupado);
        carregaProdutosNegociadosCache(idLoca);
    }

    
    public void carregaProdutosNegociadosCache(int idLoca) {

        // Consulta produtos prevendidos no banco de dados e adiciona à cache
        String consultaProdutosSQL = "SELECT idproduto, quantidade FROM prevendidos WHERE idlocacao = ?";
        try ( Connection link = new fazconexao().conectar();  PreparedStatement statementProdutos = link.prepareStatement(consultaProdutosSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            statementProdutos.setInt(1, idLoca);
            try ( ResultSet resultadoProdutos = statementProdutos.executeQuery()) {
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
        try ( Connection link = new fazconexao().conectar();  PreparedStatement statementNegociacoes = link.prepareStatement(consultaNegociacoesSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            statementNegociacoes.setInt(1, idLoca);
            try ( ResultSet resultadoNegociacoes = statementNegociacoes.executeQuery()) {
                if (resultadoNegociacoes.next()) {
                    // Reinicia o cursor do ResultSet
                    resultadoNegociacoes.beforeFirst();
                    List<Negociados> negociacoes = new ArrayList<>();
                    while (resultadoNegociacoes.next()) {
                        String tipo = resultadoNegociacoes.getString("tipo");
                        float valor = resultadoNegociacoes.getFloat("valor");
                        System.out.println("Negociado add - locacao " + idLoca + " tipo " + tipo + " valor " + valor);
                        // Cria um objeto Negociado com os valores do ResultSet
                        Negociados negociado = new Negociados(tipo, valor);
                        // Adiciona o objeto Negociado à cacheNegociados
                        negociacoes.add(negociado);
                    }

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
        // Cria o JFrame para exibir os dados
        JFrame frame = new JFrame("Cache de Quartos");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Cria um JTextArea para exibir o conteúdo
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        // Adiciona os dados da cacheQuarto
        if (cacheQuarto.isEmpty()) {
            textArea.append("A cache de quartos está vazia.\n");
        } else {
            textArea.append("Conteúdo da cache de quartos:\n");
            for (Map.Entry<Integer, CarregaQuarto> entry : cacheQuarto.entrySet()) {
                int numeroQuarto = entry.getKey();
                CarregaQuarto quarto = entry.getValue();
                textArea.append("Número do Quarto: " + numeroQuarto + ", Tipo de Quarto: " + quarto.getTipoQuarto() + "\n");
                textArea.append("Status do Quarto: " + quarto.getStatusQuarto() + ", Hora do Status: " + quarto.getHoraStatus() + "\n");
                textArea.append("--------------------------------------\n");
            }
        }

        // Adiciona os dados da cacheOcupado
        if (cacheOcupado.isEmpty()) {
            textArea.append("\nA cache de quartos OCUPADOS está vazia.\n");
        } else {
            textArea.append("\nConteúdo da cache de OCUPADOS:\n");
            for (Map.Entry<Integer, DadosOcupados> entry : cacheOcupado.entrySet()) {
                int numeroQuarto = entry.getKey();
                textArea.append("Número do Quarto Ocupado: " + numeroQuarto + "\n");
                textArea.append("--------------------------------------\n");
            }
        }

        // Adiciona o JTextArea a um JScrollPane para suporte a rolagem
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane);

        // Torna a janela visível
        frame.setVisible(true);
    }

    public void mostrarCacheProdutosVendidos() {
        // Cria o JFrame para exibir os dados
        JFrame frame = new JFrame("Cache de Produtos Vendidos");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Cria um JTextArea para exibir o conteúdo
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        // Adiciona os dados da cacheProdutosVendidos
        if (cacheProdutosVendidos.isEmpty()) {
            textArea.append("A cache de produtos vendidos está vazia.\n");
        } else {
            textArea.append("Conteúdo da cache de produtos vendidos:\n");
            for (Map.Entry<Integer, List<DadosVendidos>> entry : cacheProdutosVendidos.entrySet()) {
                int idLocacao = entry.getKey();
                List<DadosVendidos> produtosVendidos = entry.getValue();
                textArea.append("ID da Locação: " + idLocacao + "\n");
                for (DadosVendidos dadosVendidos : produtosVendidos) {
                    textArea.append("ID do Produto: " + dadosVendidos.idProduto
                            + ", Quantidade Vendida: " + dadosVendidos.quantidadeVendida + "\n");
                }
                textArea.append("--------------------------------------\n");
            }
        }

        // Adiciona o JTextArea a um JScrollPane para suporte a rolagem
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane);

        // Torna a janela visível
        frame.setVisible(true);
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
