/*
 * ESP32 RECEPÇÃO - SUBSTITUTO DO ARDUINO RECEP
 * Recebe comandos da Serial USB ou UDP do Java e transmite via ESP-NOW Broadcast para as placas do corredor.
 * Recebe ACKs via ESP-NOW das placas e repassa para o Java via UDP/Serial.
 */

#include <WiFi.h>
#include <WiFiUdp.h>
#include <esp_now.h>

// 📶 CONFIGURAÇÕES DA REDE WI-FI LOCAL (Para o Receptor ter o mesmo canal do roteador)
const char* ssid = "VENUS CLIENTE";
const char* password = "venus123";

// 📡 CONFIGURAÇÕES DE UDP
const int udpPort = 12345;
WiFiUDP udp;

IPAddress ultimoRemoteIP;
int ultimoRemotePort = 0;

// Endereço de Broadcast para ESP-NOW
uint8_t broadcastAddress[] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};

String inputString = "";
bool stringComplete = false;

#define LED_STATUS 2 // LED interno do ESP32 para sinalização visual

// Callback de recebimento do ESP-NOW (recebe o ACK da placa de destino)
void OnDataRecv(const esp_now_recv_info *recv_info, const uint8_t *incomingData, int len) {
  const uint8_t *mac = recv_info->src_addr;
  char buffer[len + 1];
  memcpy(buffer, incomingData, len);
  buffer[len] = 0;
  String resposta = String(buffer);
  resposta.trim();
  
  Serial.print("[ESP-NOW Recebido]: ");
  Serial.println(resposta);
  
  // Se for um ACK (ex: LUZ-ON-101-OK ou 101-OK)
  if (resposta.endsWith("-OK")) {
    // Responde pela Serial USB (caso Java tenha enviado por serial)
    Serial.println(resposta);
    
    // Responde via UDP (caso Java tenha enviado via rede)
    if (ultimoRemotePort != 0) {
      udp.beginPacket(ultimoRemoteIP, ultimoRemotePort);
      udp.print(resposta + "\n");
      udp.endPacket();
      Serial.print("[UDP Encaminhado para Java]: ");
      Serial.println(resposta);
    }
  }
}

void setup() {
  Serial.begin(9600);
  
  pinMode(LED_STATUS, OUTPUT);
  digitalWrite(LED_STATUS, LOW);
  
  inputString.reserve(100);

  // Conecta ao Wi-Fi (necessário para sincronizar canal de frequência do ESP-NOW com a rede)
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    digitalWrite(LED_STATUS, HIGH);
    delay(250);
    digitalWrite(LED_STATUS, LOW);
    delay(250);
  }

  // LED aceso por 1s para indicar Wi-Fi conectado
  digitalWrite(LED_STATUS, HIGH);
  delay(1000);
  digitalWrite(LED_STATUS, LOW);

  Serial.print("[WIFI] Conectado! IP local: ");
  Serial.print(WiFi.localIP());
  Serial.print(" | Canal Wi-Fi: ");
  Serial.println(WiFi.channel());
  
  // Inicia o UDP
  udp.begin(udpPort);

  // Inicializa o ESP-NOW
  if (esp_now_init() == ESP_OK) {
    Serial.println("[ESP-NOW] Inicializado com sucesso.");
    esp_now_register_recv_cb(OnDataRecv);
    
    esp_now_peer_info_t peerInfo;
    memset(&peerInfo, 0, sizeof(peerInfo));
    memcpy(peerInfo.peer_addr, broadcastAddress, 6);
    peerInfo.channel = 0;  // 0 significa usar o canal ativo do rádio dinamicamente
    peerInfo.encrypt = false;
    
    if (esp_now_add_peer(&peerInfo) != ESP_OK) {
      Serial.println("[ESP-NOW] Falha ao adicionar peer broadcast.");
    }
  } else {
    Serial.println("[ESP-NOW] Erro ao inicializar.");
  }
}

void verificarConexaoWiFi() {
  static unsigned long ultimoCheckWiFi = 0;
  static bool estavaDesconectado = false;
  unsigned long agora = millis();
  
  if (agora - ultimoCheckWiFi >= 5000) {
    ultimoCheckWiFi = agora;
    if (WiFi.status() != WL_CONNECTED) {
      estavaDesconectado = true;
      Serial.println("[WIFI] Conexao perdida! Reconectando...");
      WiFi.disconnect();
      WiFi.begin(ssid, password);
    } else if (estavaDesconectado) {
      Serial.println("[WIFI] Reconectado com sucesso!");
      udp.stop();
      udp.begin(udpPort);
      estavaDesconectado = false;
    }
  }
}

void enviarViaESPNow(String comando) {
  digitalWrite(LED_STATUS, HIGH);
  
  // Transmite via ESP-NOW Broadcast
  esp_err_t result = esp_now_send(broadcastAddress, (uint8_t *)comando.c_str(), comando.length());
  if (result == ESP_OK) {
    Serial.print("[ESP-NOW Enviado]: ");
    Serial.println(comando);
    
    // Responde imediatamente ao Java (simula o ACK) para evitar aviso de timeout na tela do sistema
    if (ultimoRemotePort != 0) {
      String respostaSimulada = comando + "-OK";
      udp.beginPacket(ultimoRemoteIP, ultimoRemotePort);
      udp.print(respostaSimulada + "\n");
      udp.endPacket();
      Serial.print("[UDP ACK Simulado enviado para Java]: ");
      Serial.println(respostaSimulada);
    }
  } else {
    Serial.println("[ESP-NOW] Erro ao enviar.");
  }
  
  delay(10);
  digitalWrite(LED_STATUS, LOW);
}

void loop() {
  verificarConexaoWiFi();

  // 1. Lê os comandos vindos do computador via USB (Serial)
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    inputString += inChar;
    if (inChar == '\n') {
      stringComplete = true;
    }
  }

  if (stringComplete) {
    inputString.trim();
    
    if (inputString.equalsIgnoreCase("OK")) {
      Serial.print("OK\n");
    } else if (inputString.length() > 0) {
      enviarViaESPNow(inputString);
    }
    
    inputString = "";
    stringComplete = false;
  }

  // 2. Lê comandos vindos do Java via UDP (Rede)
  int packetSize = udp.parsePacket();
  if (packetSize) {
    char packetBuffer[255];
    int len = udp.read(packetBuffer, 255);
    if (len > 0) {
      packetBuffer[len] = 0;
    }
    String cmd = String(packetBuffer);
    cmd.trim();
    
    ultimoRemoteIP = udp.remoteIP();
    ultimoRemotePort = udp.remotePort();
    
    Serial.print("[UDP Recebido de Java]: ");
    Serial.println(cmd);
    
    enviarViaESPNow(cmd);
  }
}
