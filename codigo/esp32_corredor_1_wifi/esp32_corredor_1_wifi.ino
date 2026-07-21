/*
 * ESP32 Nº 1 - RECEPTOR WI-FI E CONTROLADOR (QUARTOS 101 AO 105 + GERAIS)
 * COM COMUNICAÇÃO VIA UDP BROADCAST (SEM CABO)
 */

#include <WiFi.h>
#include <WiFiUdp.h>

// 📶 CONFIGURAÇÕES DA REDE WI-FI LOCAL
const char* ssid = "VENUS CLIENTE";
const char* password = "venus123";

// 📡 CONFIGURAÇÕES DE UDP
const int udpPort = 12345;
WiFiUDP udp;

#define LED_STATUS 2 // LED interno para sinalizar recebimento de dados

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
  Serial.begin(9600);
  Serial.println("--- ESP32 #1 Wi-Fi Inicializado e Pronto ---");

  pinMode(LED_STATUS, OUTPUT);
  digitalWrite(LED_STATUS, LOW);

  // Conecta ao Wi-Fi
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

  Serial.print("[WIFI] Conectado com sucesso! IP local: ");
  Serial.println(WiFi.localIP());

  // Inicia escuta UDP
  udp.begin(udpPort);
  Serial.print("[UDP] Escuta iniciada na porta: ");
  Serial.println(udpPort);
  
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
    digitalWrite(LED_STATUS, HIGH); // Acende o LED na hora para indicar sinal perdido
    
    // Se acabou de cair, registra o tempo inicial da queda
    if (tempoDesconectado == 0) {
      tempoDesconectado = millis();
    }
    
    // Meio-termo: Se ficar mais de 45 segundos direto sem conseguir se reconectar sozinho,
    // aí sim nós forçamos o WiFi.begin() manualmente para destravar a placa.
    if (millis() - tempoDesconectado >= 45000) {
      Serial.println("[WIFI] Sem conexao ha mais de 45s. Forcando reinicializacao...");
      WiFi.disconnect();
      WiFi.begin(ssid, password);
      tempoDesconectado = millis(); // Reseta o cronômetro para tentar de novo se necessário
    }
  } else {
    // Se está conectado, apaga o LED
    digitalWrite(LED_STATUS, LOW);
    
    // Se estava desconectado antes e agora voltou, reinicia a escuta UDP para garantir
    if (tempoDesconectado != 0) {
      Serial.println("[WIFI] Reconectado com sucesso!");
      udp.stop();
      udp.begin(udpPort);
      tempoDesconectado = 0; // Zera o cronômetro
    }
  }
}

void loop() {
  verificarConexaoWiFi();
  
  int packetSize = udp.parsePacket();
  if (packetSize) {
    // Pisca o LED de status ao receber comando
    digitalWrite(LED_STATUS, HIGH);
    
    char packetBuffer[255];
    int len = udp.read(packetBuffer, 255);
    if (len > 0) {
      packetBuffer[len] = 0; // Termina a string
    }
    
    String cmd = String(packetBuffer);
    cmd.trim();
    
    Serial.print("[UDP Recebido]: ");
    Serial.println(cmd);
    
    processarComandoWiFi(cmd);
    
    delay(100);
    digitalWrite(LED_STATUS, LOW);
  }
}

void enviarConfirmacao(String resposta) {
  udp.beginPacket(udp.remoteIP(), udp.remotePort());
  udp.print(resposta + "\n");
  udp.endPacket();
  Serial.print("[UDP Resposta/ACK]: ");
  Serial.println(resposta);
}

bool souResponsavel(String cmd) {
  if (cmd.startsWith("LUZ-")) {
    int idxQuarto = cmd.lastIndexOf("-") + 1;
    int quartoNum = cmd.substring(idxQuarto).toInt();
    return (quartoNum >= 101 && quartoNum <= 105);
  } else {
    int codigoCmd = cmd.toInt();
    return (codigoCmd >= 101 && codigoCmd <= 105); // Somente quartos 101 ao 105
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
    Serial.print("[UDP] Comando duplicado ignorado. Reenviando ACK: ");
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
      digitalWrite(pinoLuz, ligar ? LOW : HIGH); // LOW ativa o relé módulo
      Serial.print("-> LUZ Quarto ");
      Serial.print(quartoNum);
      Serial.println(ligar ? " LIGADA" : " DESLIGADA");
      enviarConfirmacao(cmd + "-OK");
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
      digitalWrite(pinoBotoeira, LOW);  // Fecha o contato
      delay(500);                       // Pulso de 500ms
      digitalWrite(pinoBotoeira, HIGH); // Abre o contato
      Serial.print("-> Portao ");
      Serial.print(codigoCmd);
      Serial.println(" acionado.");
      enviarConfirmacao(cmd + "-OK");
    }
  }
}
