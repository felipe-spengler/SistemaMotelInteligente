package com.motelinteligente.arduino;


import com.fazecast.jSerialComm.SerialPort;
import com.motelinteligente.dados.CacheDados;

public class ConectaArduino {

    public ConectaArduino(int valor) {
        // Encontra e abre a porta serial do Arduino
        CacheDados cache = CacheDados.getInstancia();
        
        SerialPort arduinoPort = cache.getArduino();
        /*if (!arduinoPort.openPort()) {
            System.out.println("Falha ao abrir a porta COM4.");
            return;
        }

        // Configura os parâmetros de comunicação
        arduinoPort.setBaudRate(9600); // Taxa de transmissão
        arduinoPort.setNumDataBits(8); // Bits de dados
        arduinoPort.setNumStopBits(1); // Bits de parada
        arduinoPort.setParity(SerialPort.NO_PARITY); // Paridade
        */
        // Envia o valor para o Arduino
        arduinoPort.writeBytes(String.format("%03d", valor).getBytes(), 3);
        System.out.println("Valor enviado para o Arduino: " + String.format("%03d", valor));

        // Fecha a porta serial
        //arduinoPort.closePort();
        return;
    }
}

    