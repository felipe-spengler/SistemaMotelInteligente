package com.motelinteligente.dados;

import com.motelinteligente.arduino.ConectaArduino;
import com.motelinteligente.telas.TelaPrincipal;
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
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MotelInteligenteApplication {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(fprodutos.class);

    public static void main(String[] args) {
        // Inicializa a aplicação Spring Boot normalmente
        SpringApplication.run(MotelInteligenteApplication.class, args);
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            URLConnection con = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String externalIP = reader.readLine();
            System.out.println("ServidorIniciado: " + externalIP);
            logger.debug("Servidor Iniciado: " + externalIP);
            reader.close();

            //verifica o ip no banco de dados
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
                            int linhasAfetadas = statementUpdate.executeUpdate();
                            if (linhasAfetadas > 0) {
                                System.out.println("Campo 'meuip' atualizado com sucesso.");
                            } else {
                                System.out.println("Nenhuma linha foi atualizada.");
                            }

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
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            logger.debug("Recebeu do sistema Spring Boot: " + numeroQuarto);
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
                                System.out.println("o status era " + statusAntes);
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
                                logger.info("o status era " + statusAntes);
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
                        logger.info("Spring boot - disponibilizar " + quartoEmFoco);
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
                                    //abreportao
                                    new ConectaArduino(quartoEmFoco);
                                    logger.info("Alugou - Arduino abrir " + quartoEmFoco);
                                    try {
                                        Thread.sleep(1000); // Pausa por 1 segundo
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    new ConectaArduino(888);
                                    logger.info("Alugou - Arduino abrir entrada");

                                }
                            } else {
                                logger.error("Erro ao tentar locar - recebido SpringBoot - return false ");
                                JOptionPane.showMessageDialog(null, "Falha ao iniciar locação!");

                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Tentando alugar quarto não disponível");
                            logger.error("Tentou Inicializar quarto com status ", statusAtual);
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
