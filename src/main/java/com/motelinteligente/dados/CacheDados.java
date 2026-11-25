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
import java.util.concurrent.*;
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
    private java.util.Set<Integer> portoesManuais = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(CacheDados.class);
    private static final String PREF_PORTOES_MANUAIS = "portoes_manuais";

    public Map<Integer, Timestamp> despertador = new HashMap<>();
    public static SerialPort arduinoPort;
    private static long ultimaTentativaReconexao = 0;
    private static boolean monitorIniciado = false;
    private static final Logger logger = LoggerFactory.getLogger(CacheDados.class);

    private CacheDados() {
        // Construtor privado para impedir instâncias diretas
        carregarPortoesManuais();
    }

    private void carregarPortoesManuais() {
        try {
            String salvos = prefs.get(PREF_PORTOES_MANUAIS, "");
            if (salvos != null && !salvos.trim().isEmpty()) {
                for (String parte : salvos.split(",")) {
                    String limpo = parte.trim();
                    if (!limpo.isEmpty()) {
                        portoesManuais.add(Integer.parseInt(limpo));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao carregar portões manuais salvos: {}", e.getMessage());
        }
    }

    private void salvarPortoesManuais() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Integer q : portoesManuais) {
                if (sb.length() > 0) sb.append(",");
                sb.append(q);
            }
            prefs.put(PREF_PORTOES_MANUAIS, sb.toString());
            prefs.flush();
        } catch (Exception e) {
            logger.warn("Erro ao salvar portões manuais: {}", e.getMessage());
        }
    }

    public boolean isPortaoManual(int numeroQuarto) {
        return portoesManuais.contains(numeroQuarto);
    }

    public void setPortaoManual(int numeroQuarto, boolean manual) {
        if (manual) {
            portoesManuais.add(numeroQuarto);
        } else {
            portoesManuais.remove(numeroQuarto);
        }
        CarregaQuarto q = cacheQuarto.get(numeroQuarto);
        if (q != null) {
            q.setPortaoManual(manual);
        }
        salvarPortoesManuais();
        new Thread(() -> {
            new fquartos().setPortaoManual(numeroQuarto, manual);
        }).start();
    }

    public boolean alternarPortaoManual(int numeroQuarto) {
        boolean novoEstado = !isPortaoManual(numeroQuarto);
        setPortaoManual(numeroQuarto, novoEstado);
        return novoEstado;
    }

    public static synchronized CacheDados getInstancia() {
        if (instancia == null) {
            instancia = new CacheDados();
        }
        return instancia;
    }

    private static SerialPort[] getCommPortsWithTimeout(long timeoutMs) {
        ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "jserial-getCommPorts");
            t.setDaemon(true);
            return t;
        });
        Future<SerialPort[]> fut = ex.submit(() -> {
            try {
                return SerialPort.getCommPorts();
            } catch (Throwable t) {
                logger.error("[ARDUINO] Exceção em SerialPort.getCommPorts(): {}", t.toString());
                return new SerialPort[0];
            }
        });
        try {
            return fut.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            logger.error("[ARDUINO] Timeout ao chamar SerialPort.getCommPorts() após {} ms", timeoutMs);
            fut.cancel(true);
            return new SerialPort[0];
        } catch (Exception e) {
            logger.error("[ARDUINO] Erro ao obter portas seriais: {}", e.toString());
            return new SerialPort[0];
        } finally {
            ex.shutdownNow();
        }
    }

    public static void carregaArduino() {
        System.setProperty("jSerialComm.library.path", "C:\\Windows\\System32");
        logger.debug("[ARDUINO] Iniciando carregaArduino");
        // Fecha qualquer conexão anterior mantida pela JVM para forçar reconexão limpa
        if (arduinoPort != null) {
            try {
                if (arduinoPort.isOpen()) {
                    logger.debug("[ARDUINO] Fechando conexão Arduino existente: {}", arduinoPort.getSystemPortName());
                    arduinoPort.closePort();
                } else {
                    logger.debug("[ARDUINO] Referência a arduinoPort encontrada, porém não está aberta. Limpando referência.");
                }
            } catch (Exception e) {
                logger.warn("[ARDUINO] Erro ao fechar conexão anterior: {}", e.toString());
            } finally {
                arduinoPort = null;
            }
        }

        try {
            logger.debug("[ARDUINO] Antes de SerialPort.getCommPorts()");
            SerialPort[] portas = getCommPortsWithTimeout(2000); // 2s timeout
            logger.debug("[ARDUINO] Depois de SerialPort.getCommPorts() - total: {}", portas.length);

            if (portas.length == 0) {
                logger.warn("[ARDUINO] Nenhuma porta serial encontrada.");
            }

            for (SerialPort porta : portas) {
                String descricao = porta.getDescriptivePortName().toLowerCase();
                logger.debug("[ARDUINO] Nome: {}, Descrição: {}", porta.getSystemPortName(), porta.getDescriptivePortName());

                boolean isCh340 = descricao.contains("usb-serial ch340");
                boolean isFt232 = descricao.contains("ft232r usb uart") || descricao.contains("usb serial port");
                boolean isCp210x = descricao.contains("cp210x");

                if (isCh340 || isFt232 || isCp210x) {
                    String tipo = isCh340 ? "CH340" : (isCp210x ? "CP210x" : "FT232R/Genérico");
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
            logger.debug("[ARDUINO] Tentando abrir porta {}", arduinoPort.getSystemPortName());
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
            logger.debug("[ARDUINO] Porta serial configurada: {}", arduinoPort.getSystemPortName());
            logger.debug("[ARDUINO] carregaArduino finalizado com sucesso");
        } catch (Exception ex) {
            logger.error("[ARDUINO] Exceção ao configurar a porta serial", ex);
        }
    }

    public static SerialPort getArduinoPort() {
        return obterOuReconectarArduino();
    }

    /**
     * Retorna a porta serial aberta do Arduino.
     * Caso esteja nula ou fechada (ex: cabo reconectado), tenta reconectar silenciosamente.
     */
    public static synchronized SerialPort obterOuReconectarArduino() {
        if (arduinoPort != null && arduinoPort.isOpen()) {
            return arduinoPort;
        }

        long agora = System.currentTimeMillis();
        // Debounce de 4 segundos para evitar travamento em cascata se o dispositivo estiver desconectado
        if (agora - ultimaTentativaReconexao > 4000) {
            ultimaTentativaReconexao = agora;
            logger.info("[ARDUINO] Porta serial desconectada ou fechada. Tentando reconexão silenciosa...");
            carregaArduino();
        }

        return (arduinoPort != null && arduinoPort.isOpen()) ? arduinoPort : null;
    }

    /**
     * Inicia um monitor daemon em segundo plano que verifica a cada 45 segundos
     * se a comunicação serial com o Arduino está ativa, reconectando automaticamente se necessário.
     */
    public static synchronized void iniciarMonitorArduino() {
        if (monitorIniciado) return;
        monitorIniciado = true;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ArduinoHeartbeatMonitor");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (arduinoPort == null || !arduinoPort.isOpen()) {
                    logger.debug("[ARDUINO] Heartbeat detectou Arduino desconectado. Tentando reconectar...");
                    carregaArduino();
                }
            } catch (Exception e) {
                logger.warn("[ARDUINO] Exceção no monitor de reconexão do Arduino: {}", e.getMessage());
            }
        }, 30, 45, TimeUnit.SECONDS);

        logger.info("[ARDUINO] Monitor de resiliência e reconexão silenciosa do Arduino ativado.");
    }

    public static void fecharConexao() {
        if (arduinoPort != null && arduinoPort.isOpen()) {
            try {
                arduinoPort.closePort();
            } catch (Exception ignored) {
            } finally {
                arduinoPort = null;
            }
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

        //logger.debug("[CACHE] Iniciando carregarDadosQuarto");
    synchronized (cacheQuarto) { 
        try (Connection link = new fazconexao().conectar(); 
             PreparedStatement statement = link.prepareStatement("SELECT s.*, q.tipoquarto, COALESCE(q.portao_manual, 0) AS portao_manual FROM status s LEFT JOIN quartos q ON s.numeroquarto = q.numeroquarto ORDER BY s.numeroquarto"); 
             ResultSet resultado = statement.executeQuery()) {

            
            // Opcional, dependendo da sua lógica: Se o objetivo é recarregar a cache
            // cacheQuarto.clear(); 
            // cacheOcupado.clear(); 
            
            while (resultado.next()) {
                int numeroQuarto = resultado.getInt("numeroquarto");
                String status = resultado.getString("atualquarto");
                String data = String.valueOf(resultado.getTimestamp("horastatus"));
                String tipo = resultado.getString("tipoquarto");
                if (tipo == null) {
                    tipo = new fquartos().getTipo(numeroQuarto);
                }
                boolean isManual = false;
                try {
                    isManual = resultado.getInt("portao_manual") == 1;
                } catch (SQLException ignored) {}

                if (isManual) {
                    portoesManuais.add(numeroQuarto);
                } else {
                    portoesManuais.remove(numeroQuarto);
                }

                CarregaQuarto quarto = new CarregaQuarto();
                quarto.setNumeroQuarto(numeroQuarto);
                quarto.setTipoQuarto(tipo);
                quarto.setStatusQuarto(status);
                quarto.setHoraStatus(data);
                quarto.setPortaoManual(isManual);

                // Adiciona o quarto ao mapa sincronizado
                cacheQuarto.put(numeroQuarto, quarto);
                
                if (status.contains("ocupado")) {
                    // Nota: carregarOcupado também precisa garantir acesso seguro ao cacheOcupado
                    carregarOcupado(numeroQuarto);
                }
            }
            //logger.debug("[CACHE] carregarDadosQuarto concluído com {} quartos", cacheQuarto.size());
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
