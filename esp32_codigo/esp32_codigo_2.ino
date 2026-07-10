/*
 * ESP32 Nº 2 - RECEPTOR RS485 E CONTROLADOR (QUARTOS 106 AO 110 + ENTRADA/SAÍDA GERAIS)
 * Mapeamento padronizado para o segundo ESP32 com Entrada (888) e Saída (999) gerais.
 */

// 🛰️ PINOS EXCLUSIVOS DE COMUNICAÇÃO DO MAX485
#define MAX485_DE_RE 4   // Controle de direção (RE/DE) -> GPIO 4
// Dados: RX2 na GPIO 16 e TX2 na GPIO 17 (Tratados nativamente pela Serial2)

// 🛑 PORTÕES GERAIS (IN15 e IN16 no Relé)
#define PIN_ENTRADA_GERAL 21  // IN15 -> GPIO 21 (Ajuste o GPIO conforme sua placa/ligação física)
#define PIN_SAIDA_GERAL 22    // IN16 -> GPIO 22 (Ajuste o GPIO conforme sua placa/ligação física)

String rs485Buffer = "";

// 💡 LUZES (106 ao 110): Retorna o GPIO padronizado
int obtenerPinoLuzQuarto(int numeroQuarto) {
  switch (numeroQuarto) {
    case 106: return 13; // IN1 -> GPIO 13
    case 107: return 12; // IN2 -> GPIO 12
    case 108: return 14; // IN3 -> GPIO 14
    case 109: return 27; // IN4 -> GPIO 27
    case 110: return 26; // IN5 -> GPIO 26
    default:  return -1; 
  }
}

// 🚗 PORTÕES INDIVIDUAIS (106 ao 110): Retorna o GPIO padronizado
int obtenerPinoPortaoQuarto(int numeroQuarto) {
  switch (numeroQuarto) {
    case 106: return 25; // IN6  -> GPIO 25
    case 107: return 33; // IN7  -> GPIO 33
    case 108: return 32; // IN8  -> GPIO 32
    case 109: return 18; // IN9  -> GPIO 18
    case 110: return 19; // IN10 -> GPIO 19
    default:  return -1;
  }
}

// 🛑 PORTÕES GERAIS: Retorna o GPIO correspondente
int obtenerPinoPortaoGeral(int codigoGeral) {
  switch (codigoGeral) {
    case 888: return PIN_ENTRADA_GERAL; // Entrada Geral (IN15)
    case 999: return PIN_SAIDA_GERAL;   // Saída Geral (IN16)
    default:  return -1;
  }
}

void setup() {
  // Inicializa a Serial2 nativa nos pinos 16 (RX2) e 17 (TX2)
  Serial2.begin(9600, SERIAL_8N1, 16, 17);
  
  // Configura direção do MAX485
  pinMode(MAX485_DE_RE, OUTPUT);
  digitalWrite(MAX485_DE_RE, LOW); // Modo Escuta permanente
  
  // Configura e desliga as 5 Luzes (106 a 110)
  for (int q = 106; q <= 110; q++) {
    int pino = obtenerPinoLuzQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      digitalWrite(pino, HIGH); // HIGH = Relé Desligado (Low Level Trigger)
    }
  }
  
  // Configura e desliga os 5 Portões Individuais (106 a 110)
  for (int q = 106; q <= 110; q++) {
    int pino = obtenerPinoPortaoQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      digitalWrite(pino, HIGH);
    }
  }

  // Configura e desliga os Portões Gerais (Entrada/Saída)
  pinMode(PIN_ENTRADA_GERAL, OUTPUT);
  digitalWrite(PIN_ENTRADA_GERAL, HIGH); // HIGH = Relé Desligado
  pinMode(PIN_SAIDA_GERAL, OUTPUT);
  digitalWrite(PIN_SAIDA_GERAL, HIGH);   // HIGH = Relé Desligado
}

void loop() {
  // Varre a comunicação do barramento RS485
  while (Serial2.available() > 0) {
    char c = (char)Serial2.read();
    if (c == '\n') {
      processarComandoRS485(rs485Buffer);
      rs485Buffer = ""; 
    } else if (c != '\r') {
      rs485Buffer += c;
    }
  }
}

void processarComandoRS485(String cmd) {
  cmd.trim();
  if (cmd.length() == 0) return;
  
  // 1. COMANDO DE LUZ (Ex: LUZ-ON-106)
  if (cmd.startsWith("LUZ-")) {
    bool ligar = cmd.indexOf("-ON-") != -1;
    int idxQuarto = cmd.lastIndexOf("-") + 1;
    int quartoNum = cmd.substring(idxQuarto).toInt();
    
    // O ESP2 só processa se for do bloco dele (106 a 110)
    if (quartoNum >= 106 && quartoNum <= 110) {
      int pinoLuz = obtenerPinoLuzQuarto(quartoNum);
      if (pinoLuz != -1) {
        digitalWrite(pinoLuz, ligar ? LOW : HIGH); // LOW ativa o relé
      }
    }
  }
  // 2. COMANDO DE PORTÃO (Ex: 106, 888, 999)
  else {
    int codigoCmd = cmd.toInt();
    int pinoBotoeira = -1;
    
    // O ESP2 processa se for portão individual do bloco dele (106 a 110)
    if (codigoCmd >= 106 && codigoCmd <= 110) {
      pinoBotoeira = obtenerPinoPortaoQuarto(codigoCmd);
    }
    // Ou se for portão geral (Entrada/Saída) que agora foi movido para o ESP2
    else if (codigoCmd == 888 || codigoCmd == 999) {
      pinoBotoeira = obtenerPinoPortaoGeral(codigoCmd);
    }
    
    // Dá o pulso físico na botoeira caso o portão pertença a este ESP2
    if (pinoBotoeira != -1) {
      digitalWrite(pinoBotoeira, LOW);  // Fecha o contato do relé (Pulso)
      delay(500);                       // Pulso de 500ms
      digitalWrite(pinoBotoeira, HIGH); // Abre o contato (segurança)
    }
  }
}
