/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;

import com.motelinteligente.arduino.ConectaArduino;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Frame;
import java.sql.Timestamp;
import java.util.Date;

@RestController
public class ReceberNumeroQuartoController {
    private static final Logger logger = LoggerFactory.getLogger(ReceberNumeroQuartoController.class);

    @PostMapping(value = "/receberNumeroQuarto", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> receberNumeroQuarto(@RequestBody String numeroQuarto) {
        try {
            String[] partes = numeroQuarto.split(" ");

            if (partes.length != 2) {
                logger.warn("Formato inválido recebido em receberNumeroQuarto: {}", numeroQuarto);
                return new ResponseEntity<>("Formato inválido", HttpStatus.BAD_REQUEST);
            }
            String acao = partes[0];
            String numero = partes[1];

            if ("undefined".equals(numero) || "?".equals(numero)) {
                logger.info("Ignorando comando com numero invalido: '{}'", numero);
                return new ResponseEntity<>("Comando ignorado", HttpStatus.OK);
            }

            if (acao.equals("abrir")) {
                switch (numero) {
                    case "entrada" -> new ConectaArduino(888);
                    case "saida" -> new ConectaArduino(999);
                    default -> new ConectaArduino(Integer.parseInt(numero));
                }

            } else {
                int quartoEmFoco = Integer.parseInt(numero);
                if (acao.equals("reproduzir")) {
                    new playSound().playSound("som/mensagem conferencia.wav");
                } else if (acao.equals("reservar")) {
                    if (quartoEmFoco != 0) {
                        CacheDados cache = CacheDados.getInstancia();
                        String statusAtual = cache.getCacheQuarto().get(quartoEmFoco).getStatusQuarto();
                        
                        if (statusAtual != null && statusAtual.startsWith("ocupado")) {
                            logger.warn("Tentativa de reservar quarto ocupado bloqueada: {}", quartoEmFoco);
                            return new ResponseEntity<>("Quarto ocupado, não é possível reservar.", HttpStatus.FORBIDDEN);
                        }

                        mudaStatusNaCache(quartoEmFoco, "reservado");
                        configGlobal config = configGlobal.getInstance();
                        config.setMudanca(true);

                        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
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
                    }
                } else if (acao.equals("manutencao")) {
                    if (quartoEmFoco != 0) {
                        CacheDados cache = CacheDados.getInstancia();
                        String statusAtual = cache.getCacheQuarto().get(quartoEmFoco).getStatusQuarto();
                        
                        if (statusAtual != null && statusAtual.startsWith("ocupado")) {
                            logger.warn("Tentativa de colocar quarto ocupado em manutenção bloqueada: {}", quartoEmFoco);
                            return new ResponseEntity<>("Quarto ocupado, não é possível alterar para manutenção.", HttpStatus.FORBIDDEN);
                        }

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
                        CacheDados cache = CacheDados.getInstancia();
                        String statusAtual = cache.getCacheQuarto().get(quartoEmFoco).getStatusQuarto();
                        
                        if (statusAtual != null && statusAtual.startsWith("ocupado")) {
                            logger.warn("Tentativa de disponibilizar quarto ocupado bloqueada para evitar a perda da locação: {}", quartoEmFoco);
                            return new ResponseEntity<>("Quarto ocupado, não é possível disponibilizar.", HttpStatus.FORBIDDEN);
                        }
                        
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
                } else if (acao.equals("locar")) {
                    if (quartoEmFoco != 0) {
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
                                            Thread.sleep(500); // Pausa por 0,5s
                                            new ConectaArduino(quartoEmFoco);
                                            Thread.sleep(800); // Pausa por 0,8s
                                        } catch (InterruptedException ex) {
                                            Thread.currentThread().interrupt();
                                            logger.error("Thread interrompida ao acionar Arduino", ex);
                                        }
                                        new ConectaArduino(888);
                                    }).start();
                                }
                            } else {
                                logger.error("Erro ao tentar locar - recebido SpringBoot - return false ");
                                JOptionPane.showMessageDialog(null, "Falha ao iniciar locação!");
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Tentando alugar quarto não disponível");
                            logger.error("Tentando alugar quarto não disponível");
                        }
                    }
                }
            }
            return new ResponseEntity<>("Dados recebidos com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erro ao processar receberNumeroQuarto: {}", numeroQuarto, e);
            return new ResponseEntity<>("Erro interno", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/receberPedido", consumes = "application/json")
    public ResponseEntity<String> receberPedido(@RequestBody PedidoDTO pedido) {
        try {
            logger.info("Novo pedido recebido via API: Suíte {}", pedido.getQuarto());
            
            SwingUtilities.invokeLater(() -> {
                for (Frame frame : Frame.getFrames()) {
                    if (frame instanceof com.motelinteligente.telas.TelaPrincipal) {
                        ((com.motelinteligente.telas.TelaPrincipal) frame).mostrarAlertaPedidoExterno(
                            pedido.getQuarto(), 
                            pedido.getItens(), 
                            new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date())
                        );
                        break;
                    }
                }
            });

            return new ResponseEntity<>("Pedido recebido", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erro ao processar receberPedido", e);
            return new ResponseEntity<>("Erro", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static class PedidoDTO {
        private int quarto;
        private String itens;
        public int getQuarto() { return quarto; }
        public void setQuarto(int quarto) { this.quarto = quarto; }
        public String getItens() { return itens; }
        public void setItens(String itens) { this.itens = itens; }
    }

    public boolean mudaStatusNaCache(int quartoMudar, String statusColocar) {
        CacheDados dados = CacheDados.getInstancia();
        CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
        Date dataAtual = new Date();
        Timestamp timestamp = new Timestamp(dataAtual.getTime());
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

    @GetMapping("/teste")
    public ResponseEntity<String> getTeste() {
        return ResponseEntity.ok("GET funcionando! Teste realizado com sucesso.");
    }
}