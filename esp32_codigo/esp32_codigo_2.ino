// Define o pino do LED interno. 
// A maioria das placas ESP32 usa o pino 2. Se a sua não piscar, mude para o pino correspondente.
const int ledPin = 2; 

unsigned long tempoAnterior = 0;
const long intervalo = 1000; // Tempo de piscar (1000ms = 1 segundo)
int estadoLed = LOW;
unsigned long contadorCiclos = 0;

void setup() {
  // Inicializa a comunicação serial a 115200 bps
  Serial.begin(115200);
  while (!Serial) {
    ; // Aguarda a conexão da porta serial (necessário em algumas placas)
  }

  // Configura o pino do LED como saída
  pinMode(ledPin, OUTPUT);

  Serial.println("=======================================");
  Serial.println("   TESTE DE ESTABILIDADE DO ESP32      ");
  Serial.println("=======================================");
  Serial.println("Se o LED piscar e o contador subir, o ESP32 esta 100%!");
}

void loop() {
  unsigned long tempoAtual = millis();

  // Pisca o LED sem usar o comando delay(), o que ajuda a ver se o chip trava
  if (tempoAtual - tempoAnterior >= intervalo) {
    tempoAnterior = tempoAtual;

    // Inverte o estado do LED
    if (estadoLed == LOW) {
      estadoLed = HIGH;
    } else {
      estadoLed = LOW;
    }
    digitalWrite(ledPin, estadoLed);

    // Mostra no Monitor Serial que o ESP32 continua vivo e rodando
    contadorCiclos++;
    Serial.print("[OK] ESP32 Ativo. Tempo ligado: ");
    Serial.print(tempoAtual / 1000);
    Serial.print(" segundos. Ciclos de pisca: ");
    Serial.println(contadorCiclos);
  }
}