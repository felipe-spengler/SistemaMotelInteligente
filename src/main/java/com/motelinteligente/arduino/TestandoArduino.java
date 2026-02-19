/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.arduino;


import com.fazecast.jSerialComm.SerialPort;
import javax.swing.JOptionPane;

/**
 *
 * @author MOTEL
 */
public class TestandoArduino {
    
    public static void main(String[] args) {
        // Encontra e abre a porta serial do Arduino
        SerialPort arduinoPort = SerialPort.getCommPort("COM4");
        if (!arduinoPort.openPort()) {
            System.out.println("Falha ao abrir a porta COM4.");
            return;
        }

        // Configura os parâmetros de comunicação
        arduinoPort.setBaudRate(9600); // Taxa de transmissão
        arduinoPort.setNumDataBits(8); // Bits de dados
        arduinoPort.setNumStopBits(1); // Bits de parada
        arduinoPort.setParity(SerialPort.NO_PARITY); // Paridade

        // Loop até que a janela seja fechada
        /*while (true) {
            // Solicita ao usuário um número entre 0 e 999
            String input = JOptionPane.showInputDialog(null, "Digite um número entre 0 e 999:");
            if (input == null) // Verifica se a janela foi fechada
                break;

            // Converte o número digitado para inteiro
            int numero;
            try {
                numero = Integer.parseInt(input);
                if (numero < 0 || numero > 999) {
                    JOptionPane.showMessageDialog(null, "Número inválido! Por favor, digite um número entre 0 e 999.");
                    continue;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Entrada inválida! Por favor, digite um número válido.");
                continue;
            }

            // Envia o número para o Arduino
            arduinoPort.writeBytes(String.format("%03d", numero).getBytes(), 3);
            System.out.println("numero enviado: " + String.format("%03d", numero));
        }
        */
        // Fecha a porta serial
        int numero = 2;
        arduinoPort.writeBytes(String.format("%03d", numero).getBytes(), 3);
        System.out.println("numero enviado: " + String.format("%03d", numero));
        arduinoPort.closePort();
    }
}
