#include <WiFi.h>
#include <Wire.h>
#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <LiquidCrystal_I2C.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

// Parâmetros do servidor MQTT
#define MQTT_CLIENT_ID "micropython-weather-demo"
#define MQTT_BROKER    "public.mqtthq.com"
#define MQTT_USER      ""
#define MQTT_PASSWORD  ""
#define MQTT_TOPIC     "BehYNK2qm%QRo5Wwm@8ouJ"

// Pinos do LCD
#define SDA_PIN        21
#define SCL_PIN        22

// Pino do sensor PIR
#define PIR_PIN        14

// Pinos dos LEDs
#define LED_PIN_TEMP_1 32 



WiFiClient espClient;
PubSubClient client(espClient);
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", -10800, 60000); // -10800 segundos (UTC-3) para São Paulo

LiquidCrystal_I2C lcd(0x27, 16, 2); // Endereço I2C e tamanho do LCD

void setup() {
  Serial.begin(115200);

  // Inicialização do sensor PIR
  pinMode(PIR_PIN, INPUT);

  // Configurar pino do LED como saída
  pinMode(LED_PIN_TEMP_1, OUTPUT);

  // Inicialização do LCD
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Conectando");
  lcd.setCursor(0, 1);
  lcd.print("wi-fi ....");

  // Conectar ao Wi-Fi
  connectToWiFi();

  // Inicializar NTPClient para obter data e hora
  timeClient.begin();

  // Conectar ao servidor MQTT
  client.setServer(MQTT_BROKER, 1883);
  client.setCallback(callback);
  reconnectMQTT();
}


void callback(char* topic, byte* payload, unsigned int length) {
  // Convertendo a carga útil (payload) para uma string
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

  Serial.println("Mensagem MQTT recebida:");
  Serial.println(message);

  // Verifica se a mensagem recebida é 'Aviso Enviado !!!'
  if (message == "Aviso Enviado !!!") {
    // Limpa o LCD
    lcd.clear();
    
    // Centraliza o texto "Aviso Recebido" no LCD
    int textLength = 15; // Comprimento do texto "Aviso Recebido"
    int centerPosition = (16 - textLength) / 2; // Calcula a posição central
    lcd.setCursor(centerPosition, 0); // Define o cursor na primeira linha, posição central
    lcd.print("Aviso Recebido");
    
    // Acende o LED por 10 segundos
    digitalWrite(LED_PIN_TEMP_1, HIGH);
    delay(10000); // Espera 10 segundos
    digitalWrite(LED_PIN_TEMP_1, LOW); // Desliga o LED
  }
}

void loop() {
  if (!client.connected()) {
    reconnectMQTT();
  }
  client.loop();

  timeClient.update();

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Conectado ao MQTT");

  // Verificar o estado do sensor PIR
  int pirState = digitalRead(PIR_PIN);
  if (pirState == HIGH) {
    Serial.println("Movimento detectado!");
    lcd.setCursor(0, 1);
    lcd.print("Movimento!");

    // Obter data e hora atuais
    String formattedTime = timeClient.getFormattedTime();
    String formattedDate = getFormattedDate();
    char buffer[256];
    sprintf(buffer, "Movimento detectado em: %s %s", formattedDate.c_str(), formattedTime.c_str());

    // Enviar mensagem para o servidor MQTT
    client.publish(MQTT_TOPIC, buffer);
  } else {
    lcd.setCursor(0, 1);
    lcd.print("Sem mov.");
  }

  delay(3000); // Aguarda 3 segundos antes de verificar novamente
}

void connectToWiFi() {
  WiFi.begin("Wokwi-GUEST", "");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    lcd.print(".");
    Serial.println("Tentando conectar ao Wi-Fi...");
  }
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Wi-Fi conectado");
  Serial.println("Conectado ao Wi-Fi");
}

void reconnectMQTT() {
  while (!client.connected()) {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Conectando MQTT...");
    Serial.print("Tentando conectar ao MQTT...");

    if (client.connect(MQTT_CLIENT_ID, MQTT_USER, MQTT_PASSWORD)) {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Conectado ao MQTT");
      Serial.println("Conectado ao servidor MQTT");

      // Enviar mensagem indicando que a conexão foi bem-sucedida
      client.publish(MQTT_TOPIC, "Conexão MQTT estabelecida com sucesso!");
      client.subscribe(MQTT_TOPIC);
    } else {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Falha na conexao");
      Serial.print("Falha na conexao MQTT. Estado=");
      Serial.println(client.state());
      delay(5000); // Espera 5 segundos antes de tentar novamente
    }
  }
}

String getFormattedDate() {
  time_t rawtime = timeClient.getEpochTime();
  struct tm * timeinfo = localtime(&rawtime);
  char buffer[11];
  sprintf(buffer, "%02d/%02d/%04d", timeinfo->tm_mday, timeinfo->tm_mon + 1, timeinfo->tm_year + 1900);
  return String(buffer);
}
