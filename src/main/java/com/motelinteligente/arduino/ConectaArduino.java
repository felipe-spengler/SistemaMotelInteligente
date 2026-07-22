
package com.motelinteligente.arduino;

import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConectaArduino {

    private static final Logger logger = LoggerFactory.getLogger(ConectaArduino.class);
    private static final int MAX_TENTATIVAS = 2;
    private static final int TEMPO_ESPERA_MS = 700;
    private static final int TIMEOUT_RESPOSTA_MS = 1000;

    public ConectaArduino(int valorPortao) {
        String chamador = "Desconhecido";
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 2) {
            chamador = stack[2].getClassName() + "." + stack[2].getMethodName() + "(Linha " + stack[2].getLineNumber() + ")";
        }
        logger.info("[ARDUINO] ConectaArduino instanciado (Abertura de Portão). Valor: {}. Chamador: {} (Thread: {})", 
            valorPortao, chamador, Thread.currentThread().getName());

        configGlobal config = configGlobal.getInstance();
        String tipoPortao = config.getPortoesRF();

        if ("RF".equalsIgnoreCase(tipoPortao)) {
            if (CacheDados.getArduinoPort() == null) {
                logger.error("Erro ao enviar Codigo Portão ao Arduino. Arduino Não Conectado");
                return;
            }

            String[] dadosPortao = obterDadosPortao(valorPortao);
            if (dadosPortao != null) {
                String codigo = dadosPortao[0];
                String bitLength = dadosPortao[1];

                // Sincronizado para evitar colisão na porta Serial
                if (enviarCodigoPortao(codigo, bitLength)) {
                    logger.info("Código do portão enviado com sucesso: " + codigo);
                } else {
                    logger.error("Falha ao enviar o código do portão: " + codigo);
                    // Não exibe JOptionPane para não travar a thread
                }
            } else {
                logger.warn("Portão " + valorPortao + " não encontrado no banco de dados.");
            }
        } else if ("REDE_LOCAL".equalsIgnoreCase(tipoPortao)) {
            enviarUDP("" + valorPortao);
        } else {
            enviaBotoeira(valorPortao);
        }
    }

    private String[] obterDadosPortao(int valorPortao) {
        String consultaSQL = "SELECT codigo, bitLength FROM portoes WHERE portao = ?";
        String[] dados = null;

        try (Connection link = new fazconexao().conectar();
                PreparedStatement statement = link.prepareStatement(consultaSQL)) {

            statement.setInt(1, valorPortao);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String codigo = resultSet.getString("codigo");
                    String bitLength = resultSet.getString("bitLength");
                    dados = new String[] { codigo, bitLength };
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter dados do portão: ", e);
        }
        return dados;
    }

    private boolean enviaBotoeira(int cod) {
        synchronized (ConectaArduino.class) {
            boolean sucesso = false;

            for (int tentativa = 0; tentativa < MAX_TENTATIVAS; tentativa++) {
                limparBufferArduino();

                if (enviarParaArduino("OK")) {
                    sucesso = aguardarResposta();
                    if (sucesso) {
                        sucesso = enviarParaArduino("" + cod);
                        logger.info("Tentativa " + tentativa + " recebeu ok (Botoeira)");
                        break;
                    }
                } else {
                    logger.warn("Falha ao enviar comando inicial 'OK' para o Arduino.");
                }

                try {
                    Thread.sleep(TEMPO_ESPERA_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return sucesso;
        }
    }

    private boolean enviarCodigoPortao(String codigo, String bitLength) {
        synchronized (ConectaArduino.class) {
            boolean sucesso = false;

            for (int tentativa = 0; tentativa < MAX_TENTATIVAS; tentativa++) {
                limparBufferArduino();

                if (enviarParaArduino("OK")) {
                    sucesso = aguardarResposta();
                    if (sucesso) {
                        sucesso = enviarParaArduino(codigo + "-" + bitLength);
                        logger.info("Tentativa " + tentativa + " recebeu ok (RF)");
                        break;
                    }
                } else {
                    logger.warn("Falha ao enviar comando inicial 'OK' para o Arduino.");
                }

                try {
                    Thread.sleep(TEMPO_ESPERA_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return sucesso;
        }
    }

    private boolean enviarParaArduino(String mensagem) {
        try {
            mensagem += "\n";
            if (CacheDados.getArduinoPort() != null) {
                CacheDados.getArduinoPort().writeBytes(mensagem.getBytes(), mensagem.length());
                // logger.debug("Enviado para Arduino: " + mensagem.trim());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Erro ao enviar dados para o Arduino: ", e);
            return false;
        }
    }

    private boolean aguardarResposta() {
        try {
            long tempoInicio = System.currentTimeMillis();
            String resposta = "";

            while (System.currentTimeMillis() - tempoInicio < TIMEOUT_RESPOSTA_MS) {
                if (CacheDados.getArduinoPort().bytesAvailable() > 0) {
                    byte[] dadosRecebidos = new byte[CacheDados.getArduinoPort().bytesAvailable()];
                    CacheDados.getArduinoPort().readBytes(dadosRecebidos, dadosRecebidos.length);
                    resposta += new String(dadosRecebidos);
                    String parcial = resposta.trim();
                    logger.info("Resposta parcial do Arduino: {}", parcial);

                    // Aceita tanto "OK" quanto respostas como "Arduino pronto." (case-insensitive)
                    String lower = parcial.toLowerCase();
                    if (parcial.equalsIgnoreCase("OK") || lower.contains("pronto") || lower.contains("ok")) {
                        return true;
                    }
                }
                Thread.sleep(100);
            }

            logger.warn("Timeout aguardando resposta do Arduino. Última resposta: {}", resposta.trim());
            return false;

        } catch (Exception e) {
            logger.error("Erro ao aguardar resposta do Arduino: {}", e.getMessage(), e);
            return false;
        }
    }

    private void limparBufferArduino() {
        try {
            while (CacheDados.getArduinoPort().bytesAvailable() > 0) {
                byte[] bufferLimpeza = new byte[CacheDados.getArduinoPort().bytesAvailable()];
                CacheDados.getArduinoPort().readBytes(bufferLimpeza, bufferLimpeza.length);
            }
            System.out.println("Buffer do Arduino limpo.");
        } catch (Exception e) {
            System.out.println("Erro ao limpar buffer do Arduino: " + e.getMessage());
        }
    }

    public static void enviarComandoLuz(int numeroQuarto, boolean ligar) {
        String chamador = "Desconhecido";
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 2) {
            chamador = stack[2].getClassName() + "." + stack[2].getMethodName() + "(Linha " + stack[2].getLineNumber() + ")";
        }
        logger.info("[ARDUINO] enviarComandoLuz chamado (Luz Quarto). Quarto: {}, Ligar: {}. Chamador: {} (Thread: {})", 
            numeroQuarto, ligar, chamador, Thread.currentThread().getName());

        configGlobal config = configGlobal.getInstance();
        if (config.isLuzAtiva()) {
            String cmd = "LUZ-" + (ligar ? "ON" : "OFF") + "-" + numeroQuarto + "\n";
            String tipoPortao = config.getPortoesRF();
            
            if ("REDE_LOCAL".equalsIgnoreCase(tipoPortao)) {
                enviarUDP(cmd);
            } else if (config.isFlagArduino() && CacheDados.getArduinoPort() != null) {
                synchronized (ConectaArduino.class) {
                    try {
                        byte[] msg = cmd.getBytes();
                        CacheDados.getArduinoPort().writeBytes(msg, msg.length);
                        logger.info("Comando de luz enviado para o Arduino: " + cmd.trim());
                    } catch (Exception e) {
                        logger.error("Erro ao enviar comando de luz para o Arduino: ", e);
                    }
                }
            }
        }
    }

    public static void enviarUDP(String comando) {
        Thread enviarThread = new Thread(() -> {
            String expectedAck = comando.trim() + "-OK";
            boolean sucesso = false;
            
            for (int i = 0; i < 3; i++) {
                try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
                    socket.setBroadcast(true);
                    socket.setSoTimeout(500); // 500ms timeout para aguardar a confirmação da placa
                    
                    byte[] buffer = (comando.trim() + "\n").getBytes();
                    java.net.InetAddress address = java.net.InetAddress.getByName("255.255.255.255");
                    java.net.DatagramPacket packet = new java.net.DatagramPacket(buffer, buffer.length, address, 12345);
                    
                    // Envia o comando
                    socket.send(packet);
                    logger.info("[UDP] Comando enviado (tentativa {}/3): {}", i + 1, comando.trim());
                    
                    // Aguarda a resposta (ACK)
                    byte[] receiveBuffer = new byte[256];
                    java.net.DatagramPacket receivePacket = new java.net.DatagramPacket(receiveBuffer, receiveBuffer.length);
                    
                    try {
                        socket.receive(receivePacket);
                        String resposta = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                        logger.info("[UDP] Resposta recebida da placa ({}): {}", receivePacket.getAddress().getHostAddress(), resposta);
                        
                        if (expectedAck.equalsIgnoreCase(resposta)) {
                            logger.info("[UDP] Confirmacao de entrega recebida com sucesso: {}", resposta);
                            sucesso = true;
                            break; // Sucesso! Sai do loop de tentativas
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        logger.warn("[UDP] Timeout aguardando confirmacao para o comando: {}", comando.trim());
                    }
                } catch (Exception e) {
                    logger.error("[UDP] Erro ao enviar/receber via rede local: ", e);
                }
                
                // Pequeno intervalo antes do reenvio
                if (i < 2) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            if (!sucesso) {
                logger.error("[UDP] FALHA CRITICA: O comando '{}' nao foi confirmado por nenhuma placa apos 3 tentativas!", comando.trim());
                javax.swing.SwingUtilities.invokeLater(() -> {
                    new com.motelinteligente.telas.NotificacaoAutomacao("O comando \"" + comando.trim() + "\" não foi<br>confirmado pelas placas do corredor.");
                });
            }
        });
        enviarThread.setName("UDP-Ack-Sender-" + System.currentTimeMillis());
        enviarThread.start();
    }
}
