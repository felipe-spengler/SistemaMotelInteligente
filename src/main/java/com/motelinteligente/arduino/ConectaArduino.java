package com.motelinteligente.arduino;

import com.motelinteligente.dados.CacheDados;
import com.motelinteligente.dados.configGlobal;
import com.motelinteligente.dados.fazconexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConectaArduino {

    private static final int MAX_TENTATIVAS = 2;
    private static final int TEMPO_ESPERA_MS = 700; // Meio segundo entre tentativas (caso aumente depois)
    private static final int TIMEOUT_RESPOSTA_MS = 1000; // 1 segundo de espera pela resposta do Arduino

    public ConectaArduino(int valorPortao) {
        configGlobal config = configGlobal.getInstance();
        boolean portoesRF = config.getPortoesRF();

        if (portoesRF) {
            if (CacheDados.getArduinoPort() == null) {
                JOptionPane.showMessageDialog(null, "Porta COM (conexão) não está aberta.");
                return;
            }

            String[] dadosPortao = obterDadosPortao(valorPortao);
            if (dadosPortao != null) {
                String codigo = dadosPortao[0];
                String bitLength = dadosPortao[1];

                if (enviarCodigoPortao(codigo, bitLength)) {
                    System.out.println("Código do portão enviado com sucesso.");
                } else {
                    System.out.println("Falha ao enviar o código do portão.");
                }
            } else {
                System.out.println("Portão não encontrado no banco de dados.");
            }
        } else {
            // Código caso o modo seja botoeira
        }
    }

    private String[] obterDadosPortao(int valorPortao) {
        String consultaSQL = "SELECT codigo, bitLength FROM portoes WHERE portao = ?";
        Connection link = null;
        String[] dados = null;

        try {
            link = new fazconexao().conectar();
            PreparedStatement statement = link.prepareStatement(consultaSQL);
            statement.setInt(1, valorPortao);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String codigo = resultSet.getString("codigo");
                String bitLength = resultSet.getString("bitLength");
                dados = new String[]{codigo, bitLength};
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Erro ao obter dados do portão: " + e.getMessage());
        } finally {
            try {
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (SQLException e) {
                System.out.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }

        return dados;
    }

    private boolean enviarCodigoPortao(String codigo, String bitLength) {
        boolean sucesso = false;

        for (int tentativa = 0; tentativa < MAX_TENTATIVAS; tentativa++) {
            limparBufferArduino();

            if (enviarParaArduino("OK")) {
                sucesso = aguardarResposta();
                if (sucesso) {
                    sucesso = enviarParaArduino(codigo + "-" + bitLength);
                    System.out.println("Tentativa " + tentativa + " recebeu ok");
                    break;
                } 
            } else {
                System.out.println("Falha ao enviar comando inicial 'OK' para o Arduino.");
            }

            try {
                Thread.sleep(TEMPO_ESPERA_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return sucesso;
    }

    private boolean enviarParaArduino(String mensagem) {
        try {
            mensagem += "\n"; // Adiciona quebra de linha
            CacheDados.getArduinoPort().writeBytes(mensagem.getBytes(), mensagem.length());
            System.out.println("Enviado para Arduino: " + mensagem.trim());
            return true;
        } catch (Exception e) {
            System.out.println("Erro ao enviar dados para o Arduino: " + e.getMessage());
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
