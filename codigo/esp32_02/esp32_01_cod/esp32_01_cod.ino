/*
 * ESP32 Nº 2 - RECEPTOR RS485 E CONTROLADOR (QUARTOS 106 AO 110 + GERAIS)
 * COM MEMÓRIA GRAVADA PARA QUEDA DE ENERGIA (PREFERENCES)
 */

#include <Preferences.h> // Biblioteca nativa para salvar na memória flash

// 🛰️ PINOS EXCLUSIVOS DE COMUNICAÇÃO E SINALIZAÇÃO
#define MAX485_DE_RE 5   // Controle de direção (RE/DE) -> GPIO 5 (D5)
#define LED_STATUS 2     // LED interno do ESP32 para sinalização visual (GPIO 2)
// Dados: RX2 na GPIO 15 (D15) e TX2 na GPIO 23 (D23) (Tratados nativamente pela Serial2)

String rs485Buffer = "";
Preferences listaLuzes; // Cria o objeto de memória

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

// 🛑 PORTÕES GERAIS: Retorna o GPIO padronizado para Entrada e Saída
int obtenerPinoPortaoGeral(int codigoGeral) {
  switch (codigoGeral) {
    case 888: return 21; // IN11 -> Entrada Geral -> GPIO 21
    case 999: return 22; // IN12 -> Saída Geral   -> GPIO 22
    default:  return -1;
  }
}

void setup() {
  // Inicializa a Serial de depuração USB para monitoramento no PC
  Serial.begin(9600);
  Serial.println("--- ESP32 Inicializado e Pronto ---");

  // Configura o LED de status
  pinMode(LED_STATUS, OUTPUT);
  digitalWrite(LED_STATUS, LOW);

  // Inicializa a Serial2 nativa nos pinos 15 (RX2/D15) e 23 (TX2/D23)
  Serial2.begin(9600, SERIAL_8N1, 15, 23);
  
  // Configura direção do MAX485
  pinMode(MAX485_DE_RE, OUTPUT);
  digitalWrite(MAX485_DE_RE, LOW); // Modo Escuta permanente
  
  // Abre o arquivo de memória "hotel_luz" no modo leitura/escrita
  listaLuzes.begin("hotel_luz", false);

  // 💡 CONFIGURA AS LUZES LEMBRANDO DO ESTADO ANTERIOR
  for (int q = 106; q <= 110; q++) {
    int pino = obtenerPinoLuzQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      
      // Cria uma chave de texto única para o quarto, ex: "q106"
      String chaveMemory = "q" + String(q);
      
      // Busca o último estado salvo. Se for a PRIMEIRA vez que liga na vida, assume falso (desligado)
      bool estavaLigada = listaLuzes.getBool(chaveMemory.c_str(), false);
      
      // Se estava ligada (true), joga LOW no relé para ligar. Se não, joga HIGH para manter desligado.
      digitalWrite(pino, estavaLigada ? LOW : HIGH);
    }
  }
  
  // 🚗 Configura e desliga os 5 Portões Individuais (Esses sempre iniciam desligados)
  for (int q = 106; q <= 110; q++) {
    int pino = obtenerPinoPortaoQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      digitalWrite(pino, HIGH);
    }
  }

  // 🛑 Configura e desliga os 2 Portões Gerais (Esses sempre iniciam desligados)
  pinMode(21, OUTPUT); digitalWrite(21, HIGH); // IN11 -> GPIO 21
  pinMode(22, OUTPUT); digitalWrite(22, HIGH); // IN12 -> GPIO 22
}

void loop() {
  // Varre a comunicação do barramento RS485
  while (Serial2.available() > 0) {
    char c = (char)Serial2.read();
    if (c == '\n') {
      // Sinalização visual: acende o LED indicando recepção de dados
      digitalWrite(LED_STATUS, HIGH);
      
      // Limpa e exibe o comando recebido na Serial do USB (PC)
      rs485Buffer.trim();
      Serial.print("[RS485 Recebido]: ");
      Serial.println(rs485Buffer);
      
      processarComandoRS485(rs485Buffer);
      rs485Buffer = ""; 
      
      // Apaga o LED de status
      digitalWrite(LED_STATUS, LOW);
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
        Serial.print("-> Executando LUZ do Quarto ");
        Serial.print(quartoNum);
        Serial.println(ligar ? ": LIGAR (LOW)" : ": DESLIGAR (HIGH)");

        // Aciona o relé físico
        digitalWrite(pinoLuz, ligar ? LOW : HIGH); 
        
        // SALVA NA MEMÓRIA FLASH: Grava se o quarto está ligado ou desligado
        String chaveMemory = "q" + String(quartoNum);
        listaLuzes.putBool(chaveMemory.c_str(), ligar);
      }
    } else {
      Serial.print("-> Comando LUZ ignorado (Quarto ");
      Serial.print(quartoNum);
      Serial.println(" fora do range 106-110)");
    }
  }
  // 2. COMANDO DE PORTÃO (Ex: 106, 107, 888, 999)
  else {
    int codigoCmd = cmd.toInt();
    int pinoBotoeira = -1;
    
    if (codigoCmd >= 106 && codigoCmd <= 110) {
      pinoBotoeira = obtenerPinoPortaoQuarto(codigoCmd);
      if (pinoBotoeira != -1) {
        Serial.print("-> Acionando portao individual do quarto: ");
        Serial.println(codigoCmd);
      }
    }
    else if (codigoCmd == 888 || codigoCmd == 999) {
      pinoBotoeira = obtenerPinoPortaoGeral(codigoCmd);
      if (pinoBotoeira != -1) {
        Serial.print("-> Acionando portao geral: ");
        Serial.println(codigoCmd == 888 ? "ENTRADA (888)" : "SAIDA (999)");
      }
    }
      
    if (pinoBotoeira != -1) {
      digitalWrite(pinoBotoeira, LOW);  // Dá o pulso
      delay(500);                       
      digitalWrite(pinoBotoeira, HIGH); // Abre o contato
      Serial.println("-> Pulso de 500ms concluido.");
    } else {
      Serial.print("-> Codigo de portao/comando nao reconhecido: ");
      Serial.println(cmd);
    }
  }
}