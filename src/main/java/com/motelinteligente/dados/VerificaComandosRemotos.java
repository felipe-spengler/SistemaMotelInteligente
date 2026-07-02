package com.motelinteligente.dados;

import com.motelinteligente.arduino.ConectaArduino;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

// Adaptado para MQTT
public class VerificaComandosRemotos extends Thread implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(VerificaComandosRemotos.class);

    // ===================================================================================
    // CONFIGURAÇÃO MQTT - CARREGADA DE ARQUIVO EXTERNO (application.properties)
    // ===================================================================================
    private static final String BROKER_URL = CarregarVariaveis.getMqttUrl();
    // ===================================================================================

    private MqttClient mqttClient;
    private final String filial = CarregarVariaveis.getFilial();
    private final String TOPIC_COMANDOS = "motel/" + filial + "/comandos";

    // Cliente HTTP legado apenas para confirmar execução no banco de dados
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final String baseUrl = "https://www.motelinteligente.com";

    private boolean executando = true;

    @Override
    public void run() {
        logger.info("Iniciando serviço MQTT de comandos remotos (Filial " + filial + ")");
        logger.info("Tópico de escuta: " + TOPIC_COMANDOS);

        conectarMqtt();

        int falhasConsecutivas = 0;

        // Loop de verificação de conexão (Watchdog)
        while (executando) {
            try {
                if (mqttClient == null || !mqttClient.isConnected()) {
                    logger.warn("Monitor: MQTT desconectado. Tentando reconectar... (Tentativa "
                            + (falhasConsecutivas + 1) + ")");
                    conectarMqtt();

                    if (mqttClient == null || !mqttClient.isConnected()) {
                        falhasConsecutivas++;

                        if (falhasConsecutivas >= 3) {
                            boolean temInternet = NetworkUtils.checkInternetConnection();
                            if (temInternet) {
                                logger.error(
                                        "ALERTA CRÍTICO: Internet disponível mas falha ao conectar no MQTT (tcp://145.223.30.211:1883). Verifique firewall ou se o broker está offline.");
                            } else {
                                logger.warn("Sem conexão com a internet. Aguardando restabelecimento...");
                            }
                            // Backoff: espera mais tempo se estiver falhando muito
                            Thread.sleep(15000);
                        }
                    } else {
                        falhasConsecutivas = 0;
                        logger.info("MQTT Reconectado com sucesso!");
                    }
                } else {
                    falhasConsecutivas = 0;
                }
                Thread.sleep(5000); // Verifica a cada 5 segundos
            } catch (InterruptedException e) {
                executando = false;
            } catch (Exception e) {
                logger.error("Erro no loop de monitoramento MQTT", e);
            }
        }
    }

    // Gera um ID único na inicialização da classe para manter consistência nas
    // reconexões
    private final String clientId = "MotelClient_" + filial + "_" + System.currentTimeMillis();

    private void conectarMqtt() {
        try {
            if (mqttClient != null && mqttClient.isConnected())
                return;

            // Se o objeto existe mas não está conectado, tente fechar para limpar recursos
            if (mqttClient != null) {
                try {
                    mqttClient.close();
                } catch (Exception e) {
                    // Ignora erro ao fechar
                }
            }

            // Persistencia em memória (não salva mensagens em disco se crashar, ok para
            // esse uso)
            mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
            mqttClient.setCallback(this);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false); // Retém a sessão se cair brevemente
            connOpts.setAutomaticReconnect(false); // Desativa reconexão automática da lib para não conflitar com nosso
                                                   // loop manual
            connOpts.setConnectionTimeout(10);
            connOpts.setKeepAliveInterval(60);
            // connOpts.setUserName("admin"); // TODO: Descomente e ajuste se configurar
            // usuario/senha no Mosquitto
            // connOpts.setPassword("senha".toCharArray());

            logger.info("Conectando ao broker MQTT: " + BROKER_URL + " ...");
            mqttClient.connect(connOpts);
            logger.info("Conectado! Inscrevendo no tópico: " + TOPIC_COMANDOS);

            // QoS 1: Garante que a mensagem chegue pelo menos uma vez
            mqttClient.subscribe(TOPIC_COMANDOS, 1);

        } catch (MqttException me) {
            logger.warn("Falha na conexão MQTT (tentando novamente em breve): " + me.getMessage() + " (código "
                    + me.getReasonCode() + ")");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("Conexão MQTT perdida! Causa: " + (cause != null ? cause.getMessage() : "Desconhecida"));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        logger.info(">>> MENSAGEM MQTT RECEBIDA: " + payload);

        try {
            // Espera JSON no formato: { "id": 123, "comando": "abrir entrada" }
            // Se enviar só a string "abrir entrada", vai dar erro no JSON, então tratamos
            // os dois casos
            String comando;
            int idComando = -1;

            if (payload.trim().startsWith("{")) {
                JSONObject json = new JSONObject(payload);
                idComando = json.optInt("id", -1);
                comando = json.getString("comando");
            } else {
                // Suporte para envio simples de texto (ex: teste manual via terminal)
                comando = payload;
            }

            processarComando(comando);

            // Se veio com ID, confirmamos no servidor PHP que foi feito
            if (idComando != -1) {
                confirmarExecucaoHttp(idComando);
            }

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem MQTT: " + payload, e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Não utilizamos envio por aqui, apenas recebimento
    }

    private void confirmarExecucaoHttp(int idComando) {
        try {
            String confirmaUrl = baseUrl + "/confirma_comando.php?filial=" + filial + "&id=" + idComando;
            HttpRequest confirma = HttpRequest.newBuilder()
                    .uri(URI.create(confirmaUrl))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            httpClient.send(confirma, HttpResponse.BodyHandlers.discarding());
            logger.info("Comando " + idComando + " confirmado via HTTP com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao confirmar comando " + idComando + " via HTTP", e);
        }
    }

    private void processarComando(String comando) {
        try {
            String[] partes = comando.split(" ");
            if (partes.length < 2) {
                logger.warn("Formato inválido do comando: " + comando);
                return;
            }

            String acao = partes[0];
            String numero = partes[1];

            if (acao.equals("abrir")) {
                switch (numero) {
                    case "entrada" ->
                        new ConectaArduino(888);
                    case "saida" ->
                        new ConectaArduino(999);
                    default ->
                        new ConectaArduino(Integer.parseInt(numero));
                }
            } else {
                int quartoEmFoco = Integer.parseInt(numero);
                if (acao.equals("locar")) {
                    CacheDados cache = CacheDados.getInstancia();
                    // Proteção null check
                    if (cache.getCacheQuarto().get(quartoEmFoco) == null) {
                        logger.error("Quarto " + quartoEmFoco + " não encontrado no cache!");
                        return;
                    }
                    String statusAtual = cache.getCacheQuarto().get(quartoEmFoco).getStatusQuarto();

                    if (statusAtual.equals("livre")) {
                        String periodo = (partes.length > 2) ? partes[2].replace("-", " ") : null;
                        float desconto = (partes.length > 3) ? Float.parseFloat(partes[3]) : 0;
                        
                        // Define se o status final será periodo ou pernoite
                        String statusALocar = (periodo != null && periodo.toLowerCase().contains("pernoite")) 
                                              ? "ocupado-pernoite" : "ocupado-periodo";

                        mudaStatusNaCache(quartoEmFoco, statusALocar);
                        fquartos dao = new fquartos();
                        
                        if (dao.registraLocacao(quartoEmFoco, periodo)) {
                            if (!dao.setStatus(quartoEmFoco, statusALocar)) {
                                JOptionPane.showMessageDialog(null, "Falha ao iniciar locação no banco!");
                            } else {
                                if (desconto > 0) {
                                    int idLocacao = dao.getIdLocacao(quartoEmFoco);
                                    int idCaixa = configGlobal.getInstance().getCaixa();
                                    dao.salvaAntecipado(idLocacao, "desconto", desconto, idCaixa);
                                }

                                configGlobal config = configGlobal.getInstance();
                                config.setMudanca(true);
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(500);
                                        new ConectaArduino(quartoEmFoco);
                                        Thread.sleep(800);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                    try {
                                        Thread.sleep(500);
                                        new ConectaArduino(888);
                                        Thread.sleep(800);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                    
                                }).start();
                            }
                        } else {
                            logger.error("Erro ao tentar locar - comando remoto - return false ");
                            JOptionPane.showMessageDialog(null, "Falha ao iniciar locação!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Tentando alugar quarto não disponível");
                        logger.error("Tentando alugar quarto não disponível");
                    }
                } else if (acao.equals("reservar")) {
                    mudaStatusNaCache(quartoEmFoco, "reservado");
                    configGlobal config = configGlobal.getInstance();
                    config.setMudanca(true);

                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            fquartos quarto = new fquartos();
                            String statusAntes = quarto.getStatus(quartoEmFoco);
                            quarto.setStatus(quartoEmFoco, "reservado");
                            if (!(statusAntes.equals("livre"))) {
                                quarto.alteraRegistro(quartoEmFoco, statusAntes);
                            }
                            quarto.adicionaRegistro(quartoEmFoco, "reservado");
                            return null;
                        }
                    };
                    worker.execute();
                } else if (acao.equals("manutencao")) {
                    if (quartoEmFoco != 0) {
                        mudaStatusNaCache(quartoEmFoco, "manutencao");
                        configGlobal config = configGlobal.getInstance();
                        config.setMudanca(true);

                        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                fquartos quarto = new fquartos();
                                String statusAntes = quarto.getStatus(quartoEmFoco);
                                quarto.setStatus(quartoEmFoco, "manutencao");
                                if (!(statusAntes.equals("livre"))) {
                                    quarto.alteraRegistro(quartoEmFoco, statusAntes);
                                }
                                quarto.adicionaRegistro(quartoEmFoco, "manutencao");
                                return null;
                            }
                        };
                        worker.execute();
                    }
                } else if (acao.equals("disponibilizar")) {
                    if (quartoEmFoco != 0) {
                        mudaStatusNaCache(quartoEmFoco, "livre");
                        configGlobal config = configGlobal.getInstance();
                        config.setMudanca(true);

                        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                fquartos quarto = new fquartos();
                                String statusAntes = quarto.getStatus(quartoEmFoco);
                                quarto.setStatus(quartoEmFoco, "livre");
                                if (!(statusAntes.equals("livre"))) {
                                    quarto.alteraRegistro(quartoEmFoco, statusAntes);
                                }
                                return null;
                            }
                        };
                        worker.execute();
                    }
                } else if (acao.equals("encerrar") || acao.equals("checkout")) {
                    int idLoc = Integer.parseInt(numero);
                    int quarto = -1;
                    try (Connection conn = new fazconexao().conectar()) {
                        try (PreparedStatement st = conn.prepareStatement("SELECT numquarto FROM registralocado WHERE idlocacao = ?")) {
                            st.setInt(1, idLoc);
                            try (ResultSet rs = st.executeQuery()) {
                                if (rs.next()) {
                                    quarto = rs.getInt("numquarto");
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Erro ao buscar quarto para encerramento remoto", e);
                    }

                    if (quarto != -1) {
                        mudaStatusNaCache(quarto, "limpeza");
                        CacheDados.getInstancia().getCacheOcupado().remove(quarto);
                        configGlobal.getInstance().setMudanca(true);
                        
                        // Executar ações de hardware e som
                        new playSound().playSound("som/agradecemosPreferencia.wav");
                        new ConectaArduino(999);
                        
                        // Trata impressao opcional
                        boolean imprimir = (partes.length > 2 && partes[2].equalsIgnoreCase("imprimir"));
                        if (imprimir) {
                            ImpressoraService.imprimirExtratoLocacaoPorId(idLoc);
                        }
                    }
                } else if (acao.equals("imprimir_previa") || acao.equals("previa")) {
                    int idLoc = Integer.parseInt(numero);
                    // Procura a tela de encerramento aberta para esse idLocacao e chama o método
                    // idêntico ao botão "Extrato" — assim tudo (horaSaida, produtos, etc.) sai correto
                    boolean impressoPelaTela = false;
                    for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
                        if (frame instanceof com.motelinteligente.telas.EncerraQuarto && frame.isVisible()) {
                            com.motelinteligente.telas.EncerraQuarto eq =
                                (com.motelinteligente.telas.EncerraQuarto) frame;
                            if (eq.getIdLocacao() == idLoc) {
                                SwingUtilities.invokeLater(() -> eq.imprimirExtratoPelaTela());
                                impressoPelaTela = true;
                                break;
                            }
                        }
                    }
                    // Fallback: se a tela não estiver aberta, lê do banco
                    if (!impressoPelaTela) {
                        ImpressoraService.imprimirExtratoLocacaoPorId(idLoc);
                    }
                } else if (acao.equals("abrir_checkout")) {
                    try {
                        int numQuarto = Integer.parseInt(numero);
                        com.motelinteligente.telas.TelaPrincipal principal = null;
                        for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
                            if (frame instanceof com.motelinteligente.telas.TelaPrincipal && frame.isVisible()) {
                                principal = (com.motelinteligente.telas.TelaPrincipal) frame;
                                break;
                            }
                        }
                        if (principal != null) {
                            final com.motelinteligente.telas.TelaPrincipal fPrincipal = principal;
                            SwingUtilities.invokeLater(() -> {
                                fPrincipal.abrirEncerraQuarto(numQuarto, true);
                            });
                        }
                    } catch (Exception ex) {
                        logger.error("Erro ao abrir checkout remoto: ", ex);
                    }
                } else if (acao.equals("atualizar_produtos")) {
                    try {
                        int idLoc = Integer.parseInt(numero);
                        for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
                            if (frame instanceof com.motelinteligente.telas.EncerraQuarto && frame.isVisible()) {
                                com.motelinteligente.telas.EncerraQuarto eq = (com.motelinteligente.telas.EncerraQuarto) frame;
                                if (eq.getIdLocacao() == idLoc) {
                                    eq.sincronizarProdutosRemotamente();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.error("Erro ao atualizar produtos remotamente: ", ex);
                    }
                } else if (acao.equals("reproduzir")) {
                    new playSound().playSound("som/mensagem conferencia.wav");
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao processar comando remoto: " + comando, e);
        }
    }

    private boolean mudaStatusNaCache(int quartoMudar, String statusColocar) {
        CacheDados dados = CacheDados.getInstancia();

        // 🛑 Sincroniza o bloco de código que acessa a cache 🛑
        synchronized (dados.getCacheQuarto()) {
            CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);

            // Mantemos a verificação de nulidade (segurança)
            if (quarto == null) {
                logger.error(
                        "Tentativa de mudar status do Quarto {} para {} falhou: Quarto não encontrado na Cache (Race Condition?).",
                        quartoMudar, statusColocar);
                return false;
            }

            Date dataAtual = new Date();
            Timestamp timestamp = new Timestamp(dataAtual.getTime());
            quarto.setStatusQuarto(statusColocar);
            quarto.setHoraStatus(String.valueOf(timestamp));
            dados.getCacheQuarto().put(quartoMudar, quarto);

            if (statusColocar.equals("limpeza")) {
                dados.getCacheOcupado().remove(quartoMudar);
            }
        }
        return true;
    }

    public void parar() {
        executando = false;
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            logger.error("Erro ao desconectar MQTT", e);
        }
    }
}
