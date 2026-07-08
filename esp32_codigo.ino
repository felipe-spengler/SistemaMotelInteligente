/*
 * ESP32 RECEPTOR RS485 E CONTROLADOR DE RELÉS (CORREDOR/HALLWAY)
 * 
 * Este código roda no ESP32 localizado no corredor dos quartos.
 * Ele escuta as mensagens vindas da recepção via barramento RS485 (MAX485),
 * interpreta-as e aciona os relés das lâmpadas dos quartos ou envia os
 * códigos RF (RCSwitch) para abertura dos portões/botoeiras.
 * 
 * CONEXÕES DO MAX485 NO ESP32:
 * - VCC -> 5V (ou 3.3V se o módulo suportar)
 * - GND -> GND
 * - RX (RO) -> GPIO 16 (Hardware Serial 2 RX2)
 * - TX (DI) -> GPIO 17 (Hardware Serial 2 TX2)
 * - DE/RE (juntos) -> GPIO 4 (Controle de Direção - mantido LOW para escuta)
 * 
 * CONEXÃO DO TRANSMISSOR RF (FS1000A) NO ESP32:
 * - Data -> GPIO 27 (Saída RF para controle de portões RF)
 */

#include <RCSwitch.h>

#define MAX485_DE_RE 4
#define RF_TX_PIN 27

RCSwitch mySwitch = RCSwitch();
String rs485Buffer = "";

// Mapeamento dos pinos de relés para as luzes dos quartos
// Você pode alterar os pinos de acordo com a sua placa de relés instalada
int obterPinoLuzQuarto(int numeroQuarto) {
  switch (numeroQuarto) {
    case 101: return 12; // Quarto 101 -> GPIO 12
    case 102: return 13; // Quarto 102 -> GPIO 13
    case 103: return 14; // Quarto 103 -> GPIO 14
    case 104: return 15; // Quarto 104 -> GPIO 15
    case 107: return 18; // Quarto 107 -> GPIO 18
    case 110: return 19; // Quarto 110 -> GPIO 19
    default:  return -1; // Quarto não mapeado
  }
}

// Mapeamento das botoeiras físicas dos portões (Pulso de Relé)
// Quando a recepção envia os códigos 888 (Entrada) ou 999 (Saída)
int obterPinoBotoeira(int codigoPortao) {
  switch (codigoPortao) {
    case 888: return 32; // Portão Entrada -> GPIO 32
    case 999: return 33; // Portão Saída -> GPIO 33
    default:  return -1;
  }
}

void setup() {
  Serial.begin(115200); // Debug USB
  
  // Inicializa Hardware Serial 2 para receber dados do MAX485 a 9600 bps
  // RX2=GPIO 16, TX2=GPIO 17
  Serial2.begin(9600, SERIAL_8N1, 16, 17);
  
  // Inicializa transmissor RF no pino configurado
  mySwitch.enableTransmit(RF_TX_PIN);
  
  // Configura pino de direção do MAX485
  pinMode(MAX485_DE_RE, OUTPUT);
  digitalWrite(MAX485_DE_RE, LOW); // Modo Receptor (Escuta permanente)
  
  // Configura todos os pinos de relés mapeados como SAÍDA
  int quartos[] = {101, 102, 103, 104, 107, 110};
  for (int q : quartos) {
    int pino = obterPinoLuzQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      digitalWrite(pino, HIGH); // Relés normalmente iniciam desligados (Lógica inversa/Pull-up comum)
    }
  }
  
  pinMode(32, OUTPUT); digitalWrite(32, HIGH);
  pinMode(33, OUTPUT); digitalWrite(33, HIGH);
  
  Serial.println("ESP32 RS485/RF Pronto e escutando...");
}

void loop() {
  // Lê dados vindos do barramento RS485
  while (Serial2.available() > 0) {
    char c = (char)Serial2.read();
    if (c == '\n') {
      processarComandoRS485(rs485Buffer);
      rs485Buffer = ""; // Limpa buffer
    } else if (c != '\r') {
      rs485Buffer += c;
    }
  }
}

void processarComandoRS485(String cmd) {
  cmd.trim();
  if (cmd.length() == 0) return;
  
  Serial.print("Comando Recebido no RS485: ");
  Serial.println(cmd);
  
  // 1. COMANDO DE ILUMINAÇÃO (Exemplo: LUZ-ON-101 ou LUZ-OFF-101)
  if (cmd.startsWith("LUZ-")) {
    bool ligar = cmd.indexOf("-ON-") != -1;
    int idxQuarto = cmd.lastIndexOf("-") + 1;
    int quartoNum = cmd.substring(idxQuarto).toInt();
    
    int pinoRelog = obterPinoLuzQuarto(quartoNum);
    if (pinoRelog != -1) {
      // LOW liga o módulo de relé optoacoplado comum. HIGH desliga.
      digitalWrite(pinoRelog, ligar ? LOW : HIGH);
      Serial.printf("Luz do Quarto %d -> %s (Pino %d)\n", quartoNum, ligar ? "LIGADA" : "DESLIGADA", pinoRelog);
    } else {
      Serial.printf("Aviso: Quarto %d não possui pino de luz mapeado.\n", quartoNum);
    }
  }
  
  // 2. COMANDO DE PORTÃO RF (Exemplo: 123456-24)
  else if (cmd.indexOf("-") != -1) {
    int idxHifen = cmd.indexOf("-");
    long codigoRF = cmd.substring(0, idxHifen).toInt();
    int bitLength = cmd.substring(idxHifen + 1).toInt();
    
    Serial.printf("Transmitindo RF: Código %ld, %d bits...\n", codigoRF, bitLength);
    mySwitch.send(codigoRF, bitLength);
  }
  
  // 3. COMANDO DE BOTOEIRA FÍSICA (Exemplo: 888 ou 999)
  else {
    int codPortao = cmd.toInt();
    int pinoBotoeira = obterPinoBotoeira(codPortao);
    if (pinoBotoeira != -1) {
      Serial.printf("Acionando Botoeira (Relé Pino %d) para o Portão %d...\n", pinoBotoeira, codPortao);
      digitalWrite(pinoBotoeira, LOW); // Fecha o contato do relé (Pulso)
      delay(500);                      // Mantém pressionado por 500ms
      digitalWrite(pinoBotoeira, HIGH); // Abre o contato do relé
    } else {
      Serial.printf("Aviso: Comando de botoeira %d desconhecido.\n", codPortao);
    }
  }
}
