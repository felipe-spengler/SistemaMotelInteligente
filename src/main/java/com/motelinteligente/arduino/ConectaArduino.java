
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
        configGlobal config = configGlobal.getInstance();
        boolean portoesRF = config.getPortoesRF();

        if (portoesRF) {
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
                    System.out.println("Resposta parcial do Arduino: " + resposta.trim());

                    if (resposta.trim().equalsIgnoreCase("OK")) {
                        return true;
                    }
                }
                Thread.sleep(100);
            }

            System.out.println("Timeout aguardando resposta do Arduino. Última resposta: " + resposta.trim());
            return false;

        } catch (Exception e) {
            System.out.println("Erro ao aguardar resposta do Arduino: " + e.getMessage());
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

}
