/*
 * ESP32 Nº 1 - RECEPTOR RS485 E CONTROLADOR (QUARTOS 101 AO 105 + GERAIS)
 * Mapeamento padronizado de acordo com a tabela definitiva do projeto.
 */

// 🛰️ PINOS EXCLUSIVOS DE COMUNICAÇÃO DO MAX485
#define MAX485_DE_RE 4   // Controle de direção (RE/DE) -> GPIO 4
// Dados: RX2 na GPIO 16 e TX2 na GPIO 17 (Tratados nativamente pela Serial2)

String rs485Buffer = "";

// 💡 LUZES (101 ao 105): Retorna o GPIO com base no quarto
int obtenerPinoLuzQuarto(int numeroQuarto) {
  switch (numeroQuarto) {
    case 101: return 13; // IN1 -> GPIO 13
    case 102: return 12; // IN2 -> GPIO 12
    case 103: return 14; // IN3 -> GPIO 14
    case 104: return 27; // IN4 -> GPIO 27
    case 105: return 26; // IN5 -> GPIO 26
    default:  return -1; 
  }
}

// 🚗 PORTÕES INDIVIDUAIS (101 ao 105): Retorna o GPIO com base no quarto
int obtenerPinoPortaoQuarto(int numeroQuarto) {
  switch (numeroQuarto) {
    case 101: return 25; // IN6  -> GPIO 25
    case 102: return 33; // IN7  -> GPIO 33
    case 103: return 32; // IN8  -> GPIO 32
    case 104: return 18; // IN9  -> GPIO 18
    case 105: return 19; // IN10 -> GPIO 19
    default:  return -1;
  }
}

// 🛑 PORTÕES GERAIS: Retorna o GPIO com base no comando especial
int obtenerPinoPortaoGeral(int codigoGeral) {
  switch (codigoGeral) {
    case 888: return 21; // IN11 -> Entrada Geral -> GPIO 21
    case 999: return 22; // IN12 -> Saída Geral   -> GPIO 22
    default:  return -1;
  }
}

void setup() {
  // Inicializa a Serial2 nativa nos pinos 16 (RX2) e 17 (TX2)
  Serial2.begin(9600, SERIAL_8N1, 16, 17);
  
  // Configura direção do MAX485
  pinMode(MAX485_DE_RE, OUTPUT);
  digitalWrite(MAX485_DE_RE, LOW); // Modo Escuta permanente
  
  // Configura e desliga as 5 Luzes (101 a 105)
  for (int q = 101; q <= 105; q++) {
    int pino = obtenerPinoLuzQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      digitalWrite(pino, HIGH); // HIGH = Relé Desligado (Low Level Trigger)
    }
  }
  
  // Configura e desliga os 5 Portões Individuais (101 a 105)
  for (int q = 101; q <= 105; q++) {
    int pino = obtenerPinoPortaoQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      digitalWrite(pino, HIGH);
    }
  }
  
  // Configura e desliga os 2 Portões Gerais (ESP1 Exclusivo)
  pinMode(21, OUTPUT); digitalWrite(21, HIGH); // IN11
  pinMode(22, OUTPUT); digitalWrite(22, HIGH); // IN12
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
  
  // 1. COMANDO DE LUZ (Ex: LUZ-ON-101)
  if (cmd.startsWith("LUZ-")) {
    bool ligar = cmd.indexOf("-ON-") != -1;
    int idxQuarto = cmd.lastIndexOf("-") + 1;
    int quartoNum = cmd.substring(idxQuarto).toInt();
    
    // O ESP1 só processa se for do bloco dele (101 a 105)
    if (quartoNum >= 101 && quartoNum <= 105) {
      int pinoLuz = obtenerPinoLuzQuarto(quartoNum);
      if (pinoLuz != -1) {
        digitalWrite(pinoLuz, ligar ? LOW : HIGH); // LOW ativa o relé módulo
      }
    }
  }
  // 2. COMANDO DE PORTÃO (Ex: 101, 888, 999)
  else {
    int codigoCmd = cmd.toInt();
    int pinoBotoeira = -1;
    
    // Verifica se é portão individual deste bloco
    if (codigoCmd >= 101 && codigoCmd <= 105) {
      pinoBotoeira = obtenerPinoPortaoQuarto(codigoCmd);
    } 
    // Verifica se é portão geral (exclusividade do ESP1)
    else if (codigoCmd == 888 || codigoCmd == 999) {
      pinoBotoeira = obtenerPinoPortaoGeral(codigoCmd);
    }
    
    // Se achou o pino neste ESP, dá o pulso físico na botoeira
    if (pinoBotoeira != -1) {
      digitalWrite(pinoBotoeira, LOW);  // Fecha o contato do relé
      delay(500);                       // Pulso de 500ms
      digitalWrite(pinoBotoeira, HIGH); // Abre o contato (segurança)
    }
  }
}