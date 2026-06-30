package com.motelinteligente.automacao;

import com.fazecast.jserialcomm.SerialPort;
import java.util.concurrent.TimeUnit;

public class TesteControtec {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("=== TESTE DE ENVIOS MULTIPLOS CONTROTEC (JAVA) ===");
        System.out.println("==================================================");

        // Identifica e lista todas as portas COM disponiveis no computador
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.err.println("Nenhuma porta serial COM encontrada no sistema.");
            return;
        }

        // Porta COM4 (Luzes/Energia)
        String targetPortName = "COM4";
        SerialPort targetPort = null;
        for (SerialPort p : ports) {
            System.out.println("Porta encontrada: " + p.getSystemPortName());
            if (p.getSystemPortName().equalsIgnoreCase(targetPortName)) {
                targetPort = p;
            }
        }

        if (targetPort == null) {
            System.err.println("Porta COM4 nao encontrada de forma ativa.");
            return;
        }

        // Configura parametros fisicos da serial Controtec
        targetPort.setBaudRate(9600);
        targetPort.setNumDataBits(8);
        targetPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        targetPort.setParity(SerialPort.NO_PARITY);

        if (!targetPort.openPort()) {
            System.err.println("Erro: Nao foi possivel abrir a porta " + targetPortName + " (Porta ocupada).");
            return;
        }

        System.out.println("Porta " + targetPortName + " aberta com sucesso!");

        try {
            // Faremos tentativas de comandos sequenciais para a placa 1 (Suite 101)
            // Testaremos diferentes estados: 0 (Desligar) e 204 (Ligar)
            int[] testesValores = {0, 204};
            byte boardAddress = 1;
            byte channel = 32; // LB032
            byte relayIndex = 1;

            for (int val : testesValores) {
                System.out.println("\n--- Enviando valor de dados: " + val + " (Suite 101) ---");
                
                // Monta o pacote Controtec de 5 bytes
                byte[] packet = new byte[5];
                packet[0] = boardAddress;
                packet[1] = channel;
                packet[2] = relayIndex;
                packet[3] = (byte) val;

                // Calcula Checksum LRC 2-complementos
                int sum = 0;
                for (int i = 0; i < 4; i++) {
                    sum += (packet[i] & 0xFF);
                }
                byte checksum = (byte) ((-sum) & 0xFF);
                packet[4] = checksum;

                System.out.print("Pacote em bytes enviados: ");
                for (byte b : packet) {
                    System.out.format("0x%02X ", b);
                }
                System.out.println();

                // Envia pela porta serial
                int bytesEscritos = targetPort.writeBytes(packet, packet.length);
                System.out.println("Bytes escritos fisicamente na serial: " + bytesEscritos);

                // Aguarda resposta da placa Controtec de volta
                TimeUnit.MILLISECONDS.sleep(200);
                if (targetPort.bytesAvailable() > 0) {
                    byte[] readBuffer = new byte[targetPort.bytesAvailable()];
                    int numRead = targetPort.readBytes(readBuffer, readBuffer.length);
                    System.out.print("RESPOSTA RECEBIDA DA PLACA CONTROTEC: ");
                    for (int i = 0; i < numRead; i++) {
                        System.out.format("0x%02X ", readBuffer[i]);
                    }
                    System.out.println();
                } else {
                    System.out.println("Nenhuma resposta recebida da placa.");
                }

                TimeUnit.SECONDS.sleep(2); // Intervalo entre testes
            }

        } catch (Exception e) {
            System.err.println("Erro no teste: " + e.getMessage());
        } finally {
            targetPort.closePort();
            System.out.println("Porta COM4 fechada.");
        }
    }
}
