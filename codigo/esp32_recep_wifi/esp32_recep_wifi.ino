/*
 * ESP32 RECEPÇÃO - SUBSTITUTO DO ARDUINO RECEP
 * Recebe comandos da Serial USB (Java) e transmite via UDP Broadcast na Rede Sem Fio
 */

#include <WiFi.h>
#include <WiFiUdp.h>

// 📶 CONFIGURAÇÕES DA REDE WI-FI LOCAL
const char* ssid = "VENUS CLIENTE";
const char* password = "venus123";

// 📡 CONFIGURAÇÕES DE UDP
const int udpPort = 12345;
const char* broadcastIP = "255.255.255.255"; // Envia para todas as placas da rede local

WiFiUDP udp;
String inputString = "";
bool stringComplete = false;

#define LED_STATUS 2 // LED interno do ESP32 para sinalização visual

void setup() {
  Serial.begin(9600);
  
  pinMode(LED_STATUS, OUTPUT);
  digitalWrite(LED_STATUS, LOW);
  
  inputString.reserve(100);

  // Conecta ao Wi-Fi
  WiFi.begin(ssid, password);
  
  // Pisca o LED enquanto tenta conectar
  while (WiFi.status() != WL_CONNECTED) {
    digitalWrite(LED_STATUS, HIGH);
    delay(250);
    digitalWrite(LED_STATUS, LOW);
    delay(250);
  }

  // LED aceso direto indica conectado com sucesso ao Wi-Fi
  digitalWrite(LED_STATUS, HIGH);
  delay(1000);
  digitalWrite(LED_STATUS, LOW);
  
  // Inicia o UDP
  udp.begin(udpPort);
}

void verificarConexaoWiFi() {
  static unsigned long ultimoCheckWiFi = 0;
  static bool estavaDesconectado = false;
  unsigned long agora = millis();
  
  if (agora - ultimoCheckWiFi >= 5000) { // verifica a cada 5 segundos
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

void loop() {
  verificarConexaoWiFi();

  // Lê os comandos vindos do computador via USB (Serial)
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
      // Handshake do Java para verificar se a placa está ativa
      Serial.print("OK\n");
    } else if (inputString.length() > 0) {
      // Envia o comando via rede local sem fio (UDP Broadcast)
      enviarViaWiFi(inputString);
    }
    
    inputString = "";
    stringComplete = false;
  }
}

void enviarViaWiFi(String comando) {
  // Pisca o LED rapidamente ao enviar dados
  digitalWrite(LED_STATUS, HIGH);

  // Envia o comando 3 vezes com um pequeno intervalo para garantir a entrega via UDP Broadcast
  for (int i = 0; i < 3; i++) {
    udp.beginPacket(broadcastIP, udpPort);
    udp.print(comando + "\n");
    udp.endPacket();
    if (i < 2) {
      delay(80);
    }
  }

  delay(50);
  digitalWrite(LED_STATUS, LOW);
}
