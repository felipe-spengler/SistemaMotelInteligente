
#include <SoftwareSerial.h>

#define MAX485_DE_RE 4
#define RS485_RX 2
#define RS485_TX 3

// Inicializa a comunicação serial via software para o MAX485
SoftwareSerial rs485(RS485_RX, RS485_TX);

String inputString = "";
bool stringComplete = false;

void setup() {
  Serial.begin(9600);
  // Inicializa a comunicação RS485 a 9600 bps
  rs485.begin(9600);
  pinMode(MAX485_DE_RE, OUTPUT);
  digitalWrite(MAX485_DE_RE, LOW); // Modo Receptor por padrão
  inputString.reserve(100);
}

void loop() {
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    inputString += inChar;
    if (inChar == '\n') {
      stringComplete = true;
    }
  }

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
