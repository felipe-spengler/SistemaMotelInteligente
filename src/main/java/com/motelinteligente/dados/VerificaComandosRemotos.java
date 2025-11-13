package com.motelinteligente.dados;

import com.motelinteligente.arduino.ConectaArduino;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.Date;

public class VerificaComandosRemotos extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(VerificaComandosRemotos.class);
    private final HttpClient client = HttpClient.newHttpClient();

    // Base URL do site
    private final String baseUrl = "https://motelinteligente.com";

    // Pega a filial do arquivo
    private final String filial = CarregarVariaveis.getFilial();

    // ID da unidade (você pode manter 2 ou ler de outro lugar se quiser)
    private final int intervalo = 1200; // 1,2 segundos
    private boolean executando = true;

    @Override
    public void run() {
        logger.info("Thread de verificação de comandos remotos iniciada ( Filial " + filial);
        while (executando) {
            try {
                // 1️⃣ Busca o comando remoto
                String getUrl = baseUrl + "/get_comandos.php?filial=" + filial;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(getUrl))
                        .timeout(java.time.Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();

                if (body != null && !body.trim().equals("") && !body.trim().equals("null")) {
                    JSONObject json = new JSONObject(body);
                    int idComando = json.getInt("id");
                    String comando = json.getString("comando");

                    logger.info("Comando remoto recebido: " + comando);

                    // 2️⃣ Executa a ação correspondente
                    processarComando(comando);

                    // 3️⃣ Confirma execução no servidor
                    String confirmaUrl = baseUrl + "/confirma_comando.php?filial=" + filial + "&id=" + idComando;
                    HttpRequest confirma = HttpRequest.newBuilder()
                            .uri(URI.create(confirmaUrl))
                            .timeout(java.time.Duration.ofSeconds(5))
                            .build();
                    client.send(confirma, HttpResponse.BodyHandlers.discarding());

                    logger.info("Comando " + idComando + " confirmado com sucesso.");
                }

                Thread.sleep(intervalo);
            } catch (Exception e) {
                logger.error("Erro ao verificar comandos remotos: " + e.getMessage(), e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void processarComando(String comando) {
        try {
            String[] partes = comando.split(" ");
            if (partes.length != 2) {
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
                    String statusAtual = cache.getCacheQuarto().get(quartoEmFoco).getStatusQuarto();

                    if (statusAtual.equals("livre")) {
                        mudaStatusNaCache(quartoEmFoco, "ocupado-periodo");
                        if (new fquartos().registraLocacao(quartoEmFoco)) {
                            if (!new fquartos().setStatus(quartoEmFoco, "ocupado-periodo")) {
                                JOptionPane.showMessageDialog(null, "Falha ao iniciar locação no banco!");
                            } else {
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
                                    new ConectaArduino(888);
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
                }
                else if (acao.equals("reproduzir")) {
                    new playSound().playSound("som/mensagem conferencia.wav");
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao processar comando remoto: " + comando, e);
        }
    }

    private boolean mudaStatusNaCache(int quartoMudar, String statusColocar) {
        CacheDados dados = CacheDados.getInstancia();
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
        quarto.setStatusQuarto(statusColocar);
        quarto.setHoraStatus(String.valueOf(timestamp));
        dados.getCacheQuarto().put(quartoMudar, quarto);

        if (statusColocar.equals("limpeza")) {
            dados.getCacheOcupado().remove(quartoMudar);
        }
        return true;
    }

    public void parar() {
        executando = false;
    }
}
