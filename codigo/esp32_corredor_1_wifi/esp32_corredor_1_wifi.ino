/*
 * ESP32 Nº 1 - RECEPTOR ESP-NOW E CONTROLADOR (QUARTOS 101 AO 105 + GERAIS)
 * COM COMUNICAÇÃO VIA ESP-NOW DIRECT (SEM CABO)
 */

#include <WiFi.h>
#include <esp_now.h>

// 📶 CONFIGURAÇÕES DA REDE WI-FI LOCAL (Para sincronizar o canal com o receptor)
const char* ssid = "VENUS CLIENTE";
const char* password = "venus123";

#define LED_STATUS 2 // LED interno para sinalizar recebimento de dados

uint8_t ultimoRemetenteMac[6];

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

void processarComandoWiFi(String cmd);
void enviarConfirmacao(String resposta);
bool souResponsavel(String cmd);

// Callback executado quando dados chegam via ESP-NOW
void OnDataRecv(const uint8_t * mac, const uint8_t *incomingData, int len) {
  // Pisca o LED de status ao receber comando
  digitalWrite(LED_STATUS, HIGH);
  
  // Registra o peer dinamicamente para poder responder o ACK
  if (!esp_now_is_peer_exist(mac)) {
    esp_now_peer_info_t peerInfo;
    memset(&peerInfo, 0, sizeof(peerInfo));
    memcpy(peerInfo.peer_addr, mac, 6);
    peerInfo.channel = 0; 
    peerInfo.encrypt = false;
    esp_now_add_peer(&peerInfo);
  }
  
  memcpy(ultimoRemetenteMac, mac, 6);
  
  char buffer[len + 1];
  memcpy(buffer, incomingData, len);
  buffer[len] = 0;
  
  String cmd = String(buffer);
  cmd.trim();
  
  Serial.print("[ESP-NOW Recebido]: ");
  Serial.println(cmd);
  
  processarComandoWiFi(cmd);
  
  delay(50);
  digitalWrite(LED_STATUS, LOW);
}

void setup() {
  Serial.begin(9600);
  Serial.println("--- ESP32 #1 ESP-NOW Inicializado e Pronto ---");

  pinMode(LED_STATUS, OUTPUT);
  digitalWrite(LED_STATUS, LOW);

  // Conecta ao Wi-Fi para alinhar o canal de rádio com o receptor
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    digitalWrite(LED_STATUS, HIGH);
    delay(250);
    digitalWrite(LED_STATUS, LOW);
    delay(250);
  }
  
  digitalWrite(LED_STATUS, HIGH);
  delay(1000);
  digitalWrite(LED_STATUS, LOW);

  Serial.print("[WIFI] Conectado com sucesso! IP local: ");
  Serial.println(WiFi.localIP());

  // Inicializa o ESP-NOW
  if (esp_now_init() == ESP_OK) {
    Serial.println("[ESP-NOW] Inicializado com sucesso.");
    esp_now_register_recv_cb(OnDataRecv);
  } else {
    Serial.println("[ESP-NOW] Erro ao inicializar.");
  }
  
  // Configura e desliga as 5 Luzes (101 a 105)
  for (int q = 101; q <= 105; q++) {
    int pino = obtenerPinoLuzQuarto(q);
    if (pino != -1) {
      pinMode(pino, OUTPUT);
      digitalWrite(pino, HIGH); // HIGH = Relé Desligado
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
  
  // Configura e desliga os 2 Portões Gerais
  pinMode(21, OUTPUT); digitalWrite(21, HIGH); // IN11
  pinMode(22, OUTPUT); digitalWrite(22, HIGH); // IN12
}

void verificarConexaoWiFi() {
  static unsigned long tempoDesconectado = 0;
  
  if (WiFi.status() != WL_CONNECTED) {
    digitalWrite(LED_STATUS, HIGH);
    
    if (tempoDesconectado == 0) {
      tempoDesconectado = millis();
    }
    
    if (millis() - tempoDesconectado >= 45000) {
      Serial.println("[WIFI] Sem conexao ha mais de 45s. Forcando reinicializacao...");
      WiFi.disconnect();
      WiFi.begin(ssid, password);
      tempoDesconectado = millis();
    }
  } else {
    digitalWrite(LED_STATUS, LOW);
    if (tempoDesconectado != 0) {
      Serial.println("[WIFI] Reconectado com sucesso!");
      tempoDesconectado = 0;
    }
  }
}

void loop() {
  verificarConexaoWiFi();
  delay(100);
}

void enviarConfirmacao(String resposta) {
  esp_err_t result = esp_now_send(ultimoRemetenteMac, (uint8_t *)resposta.c_str(), resposta.length());
  if (result == ESP_OK) {
    Serial.print("[ESP-NOW Resposta/ACK]: ");
    Serial.println(resposta);
  } else {
    Serial.println("[ESP-NOW] Erro ao enviar ACK.");
  }
}

bool souResponsavel(String cmd) {
  if (cmd.startsWith("LUZ-")) {
    int idxQuarto = cmd.lastIndexOf("-") + 1;
    int quartoNum = cmd.substring(idxQuarto).toInt();
    return (quartoNum >= 101 && quartoNum <= 105);
  } else {
    int codigoCmd = cmd.toInt();
    return (codigoCmd >= 101 && codigoCmd <= 105) || (codigoCmd == 888 || codigoCmd == 999);
  }
}

void processarComandoWiFi(String cmd) {
  cmd.trim();
  if (cmd.length() == 0) return;
  
  if (!souResponsavel(cmd)) return; // Se não é responsável, ignora completamente
  
  static String ultimoComando = "";
  static unsigned long tempoUltimoComando = 0;
  
  // Ignora comando idêntico recebido nos últimos 1.5 segundos, mas responde com ACK
  if (cmd.equals(ultimoComando) && (millis() - tempoUltimoComando < 1500)) {
    Serial.print("[ESP-NOW] Comando duplicado ignorado. Reenviando ACK: ");
    Serial.println(cmd);
    enviarConfirmacao(cmd + "-OK");
    return;
  }
  
  ultimoComando = cmd;
  tempoUltimoComando = millis();
  
  // 1. COMANDO DE LUZ (Ex: LUZ-ON-101)
  if (cmd.startsWith("LUZ-")) {
    bool ligar = cmd.indexOf("-ON-") != -1;
    int idxQuarto = cmd.lastIndexOf("-") + 1;
    int quartoNum = cmd.substring(idxQuarto).toInt();
    
    int pinoLuz = obtenerPinoLuzQuarto(quartoNum);
    if (pinoLuz != -1) {
      enviarConfirmacao(cmd + "-OK"); // Envia confirmação PRIMEIRO
      digitalWrite(pinoLuz, ligar ? LOW : HIGH); // LOW ativa o relé
      Serial.print("-> LUZ Quarto ");
      Serial.print(quartoNum);
      Serial.println(ligar ? " LIGADA" : " DESLIGADA");
    }
  }
  // 2. COMANDO DE PORTÃO (Ex: 101, 888, 999)
  else {
    int codigoCmd = cmd.toInt();
    int pinoBotoeira = -1;
    
    if (codigoCmd >= 101 && codigoCmd <= 105) {
      pinoBotoeira = obtenerPinoPortaoQuarto(codigoCmd);
    } 
    else if (codigoCmd == 888 || codigoCmd == 999) {
      pinoBotoeira = obtenerPinoPortaoGeral(codigoCmd);
    }
    
    if (pinoBotoeira != -1) {
      enviarConfirmacao(cmd + "-OK"); // Envia confirmação PRIMEIRO para evitar timeout no Java
      digitalWrite(pinoBotoeira, LOW);  // Fecha o contato
      delay(500);                       // Pulso de 500ms
      digitalWrite(pinoBotoeira, HIGH); // Abre o contato
      Serial.print("-> Portao ");
      Serial.print(codigoCmd);
      Serial.println(" acionado.");
    }
  }
}
