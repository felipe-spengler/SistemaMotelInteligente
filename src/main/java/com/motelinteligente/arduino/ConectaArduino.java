package com.motelinteligente.arduino;

import com.motelinteligente.dados.CacheDados;


public class ConectaArduino {
    private static final int MAX_TENTATIVAS = 3;
    private static final int TEMPO_ESPERA_MS = 2000; // Tempo de espera entre tentativas

    public ConectaArduino(int valorPortao) {
        if (CacheDados.getArduinoPort() == null) {
            System.out.println("Porta COM não está aberta.");
            return;
        }

        if (enviarCodigoPortao(valorPortao)) {
            System.out.println("Código do portão enviado com sucesso.");
        } else {
            System.out.println("Falha ao enviar o código do portão.");
        }
    }

    private boolean enviarCodigoPortao(int valorPortao) {
        boolean sucesso = false;
        for (int tentativa = 0; tentativa < MAX_TENTATIVAS; tentativa++) {
            // Tenta enviar o código do portão após receber 'OK'
            if (enviarParaArduino("OK")) {
                sucesso = aguardarResposta();
                if (sucesso) {
                    sucesso = enviarParaArduino("Portão " + valorPortao);
                    break;
                }
            }

            try {
                Thread.sleep(TEMPO_ESPERA_MS); // Aguarda antes de tentar novamente
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return sucesso;
    }

    private boolean enviarParaArduino(String mensagem) {
        try {
            mensagem += "\n"; // Adiciona nova linha ao final da mensagem
            CacheDados.getArduinoPort().writeBytes(mensagem.getBytes(), mensagem.length());
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
            while (System.currentTimeMillis() - tempoInicio < 5000) { // 5 segundos de espera
                if (CacheDados.getArduinoPort().bytesAvailable() > 0) {
                    byte[] dadosRecebidos = new byte[CacheDados.getArduinoPort().bytesAvailable()];
                    CacheDados.getArduinoPort().readBytes(dadosRecebidos, dadosRecebidos.length);
                    resposta = new String(dadosRecebidos);
                    System.out.println("Resposta do Arduino: " + resposta.trim());
                    if (resposta.trim().equals("OK")) {
                        return true;
                    }
                }
                Thread.sleep(100); // Aguarda um pouco antes de verificar novamente
            }
            System.out.println("Resposta inesperada do Arduino: " + resposta.trim());
            return false;
        } catch (Exception e) {
            System.out.println("Erro ao aguardar resposta do Arduino: " + e.getMessage());
            return false;
        }
    }
}
