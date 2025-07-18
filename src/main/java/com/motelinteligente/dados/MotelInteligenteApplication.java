package com.motelinteligente.dados;

import com.motelinteligente.arduino.ConectaArduino;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MotelInteligenteApplication {
    private static final Logger logger = LoggerFactory.getLogger(MotelInteligenteApplication.class);
    public static void main(String[] args) {

        SSLUtil.disableSSLCertificateChecking();
        // Inicializa a aplicação Spring Boot normalmente
        SpringApplication.run(MotelInteligenteApplication.class, args);
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            URLConnection con = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String externalIP = reader.readLine();
            //String externalIP = InetAddress.getLocalHost().getHostAddress();
            //System.out.println("IP interno: " + externalIP);
            reader.close();

            // Verifica o IP no banco de dados
            try {
                // Estabelecer a conexão com o banco de dados
                Connection link = new fazconexao().conectar();
                // Preparar a declaração SQL
                PreparedStatement statement = link.prepareStatement("SELECT meuip FROM configuracoes");

                // Executar a consulta SQL e obter o resultado
                ResultSet resultado = statement.executeQuery();

                // Processar o resultado
                if (resultado.next()) {
                    String meuip = resultado.getString("meuip");
                    if ((meuip == null) || (!meuip.equals(externalIP))) {
                        // Declaração SQL para atualizar o campo 'meuip'
                        String sql = "UPDATE configuracoes SET meuip = ?";
                        try {
                            PreparedStatement statementUpdate = link.prepareStatement(sql);
                            statementUpdate.setString(1, externalIP);
                            statementUpdate.executeUpdate();

                            // Fechar recursos
                            statementUpdate.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } 
                }

                // Fechar recursos
                resultado.close();
                statement.close();
                link.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    @RestController
    class ReceberNumeroQuartoController {

        @PostMapping(value = "/receberNumeroQuarto", consumes = "text/plain", produces = "text/plain")
        public ResponseEntity<String> receberNumeroQuarto(@RequestBody String numeroQuarto) {
            //logger.info("Recebeu requisição com corpo: {}", numeroQuarto);
            String[] partes = numeroQuarto.split(" ");

            if (partes.length != 2) {
                return new ResponseEntity<>("Formato inválido", HttpStatus.BAD_REQUEST);
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
                if (acao.equals("reproduzir")) {
                    new playSound().playSound("som/mensagem conferencia.wav");
                } else if (acao.equals("reservar")) {
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
                                //logger.info("o status era " + statusAntes);
                                if (!(statusAntes.equals("livre"))) {
                                    quarto.alteraRegistro(quartoEmFoco, statusAntes);
                                }
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
                        //logger.info("Spring boot - disponibilizar " + quartoEmFoco);
                        // a seguir acontece em background
                        SwingWorker<Void, Void> worker;
                        worker = new SwingWorker<Void, Void>() {
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
                if (acao.equals("locar")) {
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
                                            ex.printStackTrace();
                                        }
                                        new ConectaArduino(888);
                                    }).start(); // Inicia a thread
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
            
            if (statusColocar.equals("limpeza")) {
                dados.getCacheOcupado().remove(quartoMudar);
            }
            return true;

        }

        // Novo endpoint GET para teste
        @GetMapping("/teste")
        public ResponseEntity<String> getTeste() {
            return ResponseEntity.ok("GET funcionando! Teste realizado com sucesso.");
        }
    }

}
