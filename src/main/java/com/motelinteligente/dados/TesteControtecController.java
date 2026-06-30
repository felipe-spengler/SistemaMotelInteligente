package com.motelinteligente.dados;

import com.fazecast.jserialcomm.SerialPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin(origins = "*")
public class TesteControtecController {
    private static final Logger logger = LoggerFactory.getLogger(TesteControtecController.class);

    @GetMapping("/api/testecontrotec")
    public ResponseEntity<String> testarPlaca(
            @RequestParam(value = "porta", defaultValue = "COM4") String porta,
            @RequestParam(value = "placa", defaultValue = "1") int placa,
            @RequestParam(value = "canal", defaultValue = "32") int canal,
            @RequestParam(value = "rele", defaultValue = "1") int rele,
            @RequestParam(value = "valor", defaultValue = "0") int valor) {
        
        logger.info("Executando teste Controtec: Porta={}, Placa={}, Canal={}, Relé={}, Valor={}", 
                porta, placa, canal, rele, valor);

        SerialPort serialPort = SerialPort.getCommPort(porta);
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        if (!serialPort.openPort()) {
            return new ResponseEntity<>("Erro: Porta " + porta + " está ocupada ou indisponível.", HttpStatus.CONFLICT);
        }

        try {
            byte[] packet = new byte[5];
            packet[0] = (byte) placa;
            packet[1] = (byte) canal;
            packet[2] = (byte) rele;
            packet[3] = (byte) valor;

            // Cálculo do LRC
            int sum = 0;
            for (int i = 0; i < 4; i++) {
                sum += (packet[i] & 0xFF);
            }
            packet[4] = (byte) ((-sum) & 0xFF);

            serialPort.writeBytes(packet, packet.length);

            // Aguarda resposta da placa
            Thread.sleep(200);
            String respostaStr = "Comando enviado. Nenhuma resposta recebida.";
            if (serialPort.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                StringBuilder sb = new StringBuilder("Resposta: ");
                for (int i = 0; i < numRead; i++) {
                    sb.append(String.format("0x%02X ", readBuffer[i]));
                }
                respostaStr = sb.toString();
            }

            serialPort.closePort();
            return new ResponseEntity<>("SUCESSO: " + respostaStr, HttpStatus.OK);

        } catch (Exception e) {
            serialPort.closePort();
            return new ResponseEntity<>("Erro ao transmitir: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
