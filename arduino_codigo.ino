/*
 * ARDUINO PC - TRANSMISSOR RS485
 * 
 * Este código roda no Arduino conectado ao USB do PC da recepção.
 * Ele recebe os comandos enviados pelo software Java a 9600 bps,
 * responde o handshake "OK" para o Java, e retransmite o comando
 * através de um transceptor MAX485 (RS485) para o corredor/hallway.
 * 
 * CONEXÕES DO MAX485 NO ARDUINO:
 * - VCC -> 5V
 * - GND -> GND
 * - RX (RO) -> Pino 2 (SoftwareSerial RX)
 * - TX (DI) -> Pino 3 (SoftwareSerial TX)
 * - DE/RE (juntos) -> Pino 4 (Controle de Direção TX/RX)
 */

#include <SoftwareSerial.h>

#define MAX485_DE_RE 4
#define RS485_RX 2
#define RS485_TX 3

// Inicializa a comunicação serial via software para o MAX485
SoftwareSerial rs485(RS485_RX, RS485_TX);

String inputString = "";
bool stringComplete = false;

void setup() {
  // Inicializa a comunicação USB com o PC a 9600 bps
  Serial.begin(9600);
  
  // Inicializa a comunicação RS485 a 9600 bps
  rs485.begin(9600);
  
  // Configura pino de controle de fluxo do RS485 como saída
  pinMode(MAX485_DE_RE, OUTPUT);
  digitalWrite(MAX485_DE_RE, LOW); // Modo Receptor por padrão
  
  inputString.reserve(100);
}

void loop() {
  // Lê os dados recebidos via USB (PC)
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    inputString += inChar;
    if (inChar == '\n') {
      stringComplete = true;
    }
  }

  // Se uma linha completa foi recebida do PC
  if (stringComplete) {
    inputString.trim(); // Limpa espaços e quebras de linha nas pontas
    
    if (inputString.equalsIgnoreCase("OK")) {
      // Handshake do Java para verificar se o Arduino está online
      Serial.print("OK\n");
    } else {
      // É um comando de portão ou luz. Transmite via RS485.
      enviarRS485(inputString);
    }
    
    // Limpa a string para a próxima leitura
    inputString = "";
    stringComplete = false;
  }
}

// Envia a mensagem pelo barramento RS485 de forma segura
void enviarRS485(String comando) {
  // Coloca o MAX485 em modo de transmissão (HIGH)
  digitalWrite(MAX485_DE_RE, HIGH);
  delay(5); // Pequeno delay para estabilização do hardware
  
  // Transmite o comando completo com quebra de linha
  rs485.print(comando + "\n");
  rs485.flush(); // Aguarda o envio físico de todos os bytes
  
  delay(5); // Garante que o último bit seja transmitido antes de desligar a linha
  // Volta o MAX485 para o modo de recepção/escuta (LOW)
  digitalWrite(MAX485_DE_RE, LOW);
}
