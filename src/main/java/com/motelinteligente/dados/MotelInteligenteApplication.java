package com.motelinteligente.dados;

import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.telas.TelaPrincipal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MotelInteligenteApplication {

    public static void main(String[] args) {
        // Inicializa a aplicação Spring Boot normalmente
        SpringApplication.run(MotelInteligenteApplication.class, args);
    }

    @RestController
    class ReceberNumeroQuartoController {

        @PostMapping(value = "/receberNumeroQuarto", consumes = "text/plain", produces = "text/plain")
        public ResponseEntity<String> receberNumeroQuarto(@RequestBody String numeroQuarto) {
            String[] partes = numeroQuarto.split(" ");

            if (partes.length != 2) {
                return new ResponseEntity<>("Formato inválido", HttpStatus.BAD_REQUEST);
            }
            String acao = partes[0];
            String numero = partes[1];
            System.out.println(acao + numero);
            if (acao.equals("abrir")) {
                switch (numero) {
                    case "entrada" ->
                        new ConectaArduino(888);
                    case "saida" ->
                        new ConectaArduino(999);
                    default ->
                        new ConectaArduino(Integer.parseInt(numero));
                }
            }
            int quartoEmFoco = Integer.parseInt(numero);
            if (acao.equals("reservar")) {
                if (quartoEmFoco != 0) {
                    mudaStatusNaCache(quartoEmFoco, "reservado");
                    configGlobal config = configGlobal.getInstance();
                    config.setMudanca(true);

                    // a seguir acontece em background
                    SwingWorker<Void, Void> worker;
                    worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            fquartos quarto = new fquartos();
                            String statusAntes = quarto.getStatus(quartoEmFoco);
                            System.out.println("o status era "+ statusAntes);
                            quarto.setStatus(quartoEmFoco, "reservado");
                            if(!(statusAntes.equals("livre"))) quarto.alteraRegistro(quartoEmFoco, statusAntes);
                            quarto.adicionaRegistro(quartoEmFoco, "reservado");
                            return null;
                        }

                    };
                    worker.execute();
                }
            }
            if (acao.equals("manutencao")) {
                if (quartoEmFoco != 0) {
                    mudaStatusNaCache(quartoEmFoco, "manutencao");
                    configGlobal config = configGlobal.getInstance();
                    config.setMudanca(true);

                    // a seguir acontece em background
                    SwingWorker<Void, Void> worker;
                    worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            fquartos quarto = new fquartos();
                            String statusAntes = quarto.getStatus(quartoEmFoco);

                            quarto.setStatus(quartoEmFoco, "manutencao");
                            System.out.println("o status era "+ statusAntes);
                            if(!(statusAntes.equals("livre"))) quarto.alteraRegistro(quartoEmFoco, statusAntes);
                            quarto.adicionaRegistro(quartoEmFoco, "manutencao");
                            return null;
                        }

                    };
                    worker.execute();
                }
            }
            if (acao.equals("disponibilizar")) {
                if (quartoEmFoco != 0) {
                    mudaStatusNaCache(quartoEmFoco, "livre");
                    configGlobal config = configGlobal.getInstance();
                    config.setMudanca(true);

                    // a seguir acontece em background
                    SwingWorker<Void, Void> worker;
                    worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            fquartos quarto = new fquartos();
                            String statusAntes = quarto.getStatus(quartoEmFoco);
                            quarto.setStatus(quartoEmFoco, "livre");
                            if(!(statusAntes.equals("livre"))) quarto.alteraRegistro(quartoEmFoco, statusAntes);
                            return null;
                        }

                    };
                    worker.execute();
                }
            }
            if (acao.equals("locar")) {
                if (quartoEmFoco != 0) {
                    mudaStatusNaCache(quartoEmFoco, "ocupado-periodo");
                    if (new fquartos().registraLocacao(quartoEmFoco)) {
                        if (!new fquartos().setStatus(quartoEmFoco, "ocupado-periodo")) {
                            JOptionPane.showMessageDialog(null, "Falha ao iniciar locação no banco!");
                        } else {
                            configGlobal config = configGlobal.getInstance();
                            config.setMudanca(true);
                            //abreportao
                            new ConectaArduino(quartoEmFoco);
                            try {
                                Thread.sleep(1000); // Pausa por 1 segundo
                            } catch (InterruptedException ex) {
                                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            new ConectaArduino(888);

                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Falha ao iniciar locação!");

                    }
                }
            }
            // Retornar uma resposta para o cliente
            return new ResponseEntity<>("Dados recebidos com sucesso", HttpStatus.OK);
        }

        public boolean mudaStatusNaCache(int quartoMudar, String statusColocar) {
            CacheDados dados = CacheDados.getInstancia();
            // Obtém o quarto da cache
            CarregaQuarto quarto = dados.getCacheQuarto().get(quartoMudar);
            Date dataAtual = new Date();
            Timestamp timestamp = new Timestamp(dataAtual.getTime());
            // Atualiza o status e a data do quarto
            quarto.setStatusQuarto(statusColocar);
            quarto.setHoraStatus(String.valueOf(timestamp));
            // Atualiza o quarto na cache
            dados.getCacheQuarto().put(quartoMudar, quarto);
            if (statusColocar.contains("ocupado")) {
                dados.carregarOcupado(quartoMudar);
            }
            if (statusColocar.equals("limpeza")) {
                dados.getCacheOcupado().remove(quartoMudar);
            }
            return true;

        }
    }
}
