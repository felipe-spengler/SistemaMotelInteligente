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
import java.util.HashMap;
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
    private Map<Integer, CarregaQuarto> cacheQuarto = new java.util.concurrent.ConcurrentHashMap<>();
    private Map<Integer, DadosOcupados> cacheOcupado = new java.util.concurrent.ConcurrentHashMap<>();

    public Map<Integer, Timestamp> despertador = new HashMap<>();
    public static SerialPort arduinoPort;
    private static final Logger logger = LoggerFactory.getLogger(CacheDados.class); // Corrigido a classe de log

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
        logger.info("[ARDUINO] Iniciando carregaArduino");
        if (arduinoPort != null && arduinoPort.isOpen()) {
            logger.info("[ARDUINO] Porta já aberta: {}", arduinoPort.getSystemPortName());
            return; // A conexão já está aberta
        }

        try {
            logger.info("[ARDUINO] Antes de SerialPort.getCommPorts()");
            SerialPort[] portas = SerialPort.getCommPorts();
            logger.info("[ARDUINO] Depois de SerialPort.getCommPorts() - total: {}", portas.length);

            if (portas.length == 0) {
                logger.warn("[ARDUINO] Nenhuma porta serial encontrada.");
            }

            for (SerialPort porta : portas) {
                String descricao = porta.getDescriptivePortName().toLowerCase();
                logger.info("[ARDUINO] Nome: {}, Descrição: {}", porta.getSystemPortName(), porta.getDescriptivePortName());

                boolean isCh340 = descricao.contains("usb-serial ch340");
                boolean isFt232 = descricao.contains("ft232r usb uart") || descricao.contains("usb serial port");

                if (isCh340 || isFt232) {
                    String tipo = isCh340 ? "CH340" : "FT232R/Genérico";
                    logger.info("[ARDUINO] Arduino ({}) encontrado na porta: {}", tipo, porta.getSystemPortName());
                    arduinoPort = porta; // Define a porta do Arduino
                    break; // Para o loop assim que encontrar o primeiro
                }
            }
        } catch (Exception ex) {
            logger.error("[ARDUINO] Erro ao enumerar portas seriais", ex);
            return;
        }

        if (arduinoPort == null) {
            logger.warn("[ARDUINO] Nenhum Arduino encontrado.");
            return;
        }

        try {
            logger.info("[ARDUINO] Tentando abrir porta {}", arduinoPort.getSystemPortName());
            if (!arduinoPort.openPort()) {
                logger.error("[ARDUINO] Falha ao abrir a porta {}", arduinoPort.getSystemPortName());
                return;
            }
        } catch (Exception ex) {
            logger.error("[ARDUINO] Exceção ao abrir a porta serial", ex);
            return;
        }

        try {
            // Configura os parâmetros de comunicação
            arduinoPort.setBaudRate(9600); // Taxa de transmissão
            arduinoPort.setNumDataBits(8); // Bits de dados
            arduinoPort.setNumStopBits(1); // Bits de parada
            arduinoPort.setParity(SerialPort.NO_PARITY); // Paridade
            logger.info("[ARDUINO] Porta serial configurada: {}", arduinoPort.getSystemPortName());
        } catch (Exception ex) {
            logger.error("[ARDUINO] Exceção ao configurar a porta serial", ex);
        }
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

        despertador.clear();
    }

    public void carregarOcupado(int numeroQuarto) {
        float valPeriodo = 0, valPernoite = 0, valAdicional = 0;
        int pessoas = 2;
        fquartos quartodao = new fquartos();
        valPernoite = quartodao.getValorQuarto(numeroQuarto, "pernoite");
        valPeriodo = quartodao.getValorQuarto(numeroQuarto, "periodo");
        valAdicional = quartodao.getAdicional(numeroQuarto);
        try {
            pessoas = quartodao.getPessoas(numeroQuarto);
        } catch (Exception e) {
            pessoas = 2;
        }
        String tempo = quartodao.getPeriodo(numeroQuarto);
        int idLoca = quartodao.getIdLocacao(numeroQuarto);
        Timestamp entrada = quartodao.getHoraInicio(idLoca);
        
        String periodoLocado = quartodao.getPeriodoLocado(idLoca);
        boolean isAuto = (periodoLocado != null); // Agora considera autoatendimento se o campo existir, mesmo que vazio

        DadosOcupados ocupado = new DadosOcupados(entrada, idLoca, valPeriodo, valPernoite, pessoas, valAdicional, tempo, isAuto);
        cacheOcupado.put(numeroQuarto, ocupado);

    }

    public CarregaQuarto carregarDadosQuarto() {

        logger.info("[CACHE] Iniciando carregarDadosQuarto");
    synchronized (cacheQuarto) { 
        try (Connection link = new fazconexao().conectar(); 
             PreparedStatement statement = link.prepareStatement("select * from status order by numeroquarto"); 
             ResultSet resultado = statement.executeQuery()) {

            logger.info("[CACHE] Query de status executada");
            // Opcional, dependendo da sua lógica: Se o objetivo é recarregar a cache
            // cacheQuarto.clear(); 
            // cacheOcupado.clear(); 
            
            while (resultado.next()) {
                int numeroQuarto = resultado.getInt("numeroquarto");
                String status = resultado.getString("atualquarto");
                String data = String.valueOf(resultado.getTimestamp("horastatus"));
                String tipo = new fquartos().getTipo(numeroQuarto);

                CarregaQuarto quarto = new CarregaQuarto();
                quarto.setNumeroQuarto(numeroQuarto);
                quarto.setTipoQuarto(tipo);
                quarto.setStatusQuarto(status);
                quarto.setHoraStatus(data);

                // Adiciona o quarto ao mapa sincronizado
                cacheQuarto.put(numeroQuarto, quarto);
                
                if (status.contains("ocupado")) {
                    // Nota: carregarOcupado também precisa garantir acesso seguro ao cacheOcupado
                    carregarOcupado(numeroQuarto);
                }
            }
            logger.info("[CACHE] carregarDadosQuarto concluído com {} quartos", cacheQuarto.size());
        } catch (SQLException e) {
            logger.error("Erro ao carregar dados dos quartos", e);
        }
    } // Fim do bloco sincronizado
    return null;
}

    public Map<Integer, CarregaQuarto> getCacheQuarto() {
        return cacheQuarto;
    }

    public Map<Integer, DadosOcupados> getCacheOcupado() {
        return cacheOcupado;
    }

    public void mostrarCacheQuarto() {
        JFrame frame = new JFrame("Cache de Quartos");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

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

        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane);
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
