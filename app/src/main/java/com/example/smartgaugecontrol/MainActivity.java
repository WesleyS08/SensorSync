package com.example.smartgaugecontrol;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    private TextView textViewStatus;
    private TextView textViewMessageHistory;
    private TextView textViewMovementCount;

    private MqttClient arduinoClient;
    private static final String ARDUINO_MQTT_TOPIC = "BehYNK2qm%QRo5Wwm@8ouJ";

    private int movementCount = 0;
    private static final long RECONNECTION_INTERVAL = 3000; // Intervalo de 3 segundos para tentar reconectar

    private boolean connected = false;
    private List<String> messageHistory = new ArrayList<>();
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewMessageHistory = findViewById(R.id.textViewMessageHistory);
        textViewMovementCount = findViewById(R.id.textViewMovementCount);

        setupArduinoMqttClient();
    }
    public void sendMessageToServer(View view) {
        // Mensagem a ser enviada para o Arduino
        String messageToSend = "Aviso Enviado !!!";

        // Envia a mensagem para o Arduino
        publishToArduino(messageToSend);
    }
    private void setupArduinoMqttClient() {
        new Thread(() -> {
            while (!connected) { // Executa enquanto não estiver conectado
                try {
                    String clientId = MqttClient.generateClientId();
                    arduinoClient = new MqttClient("tcp://public.mqtthq.com:1883", clientId, new MemoryPersistence());

                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);

                    arduinoClient.connect(options);
                    arduinoClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            connected = false; // A conexão foi perdida
                            updateStatusText("Conexão MQTT perdida. Tentando reconectar...");
                            try {
                                Thread.sleep(RECONNECTION_INTERVAL); // Aguarda o intervalo antes de tentar reconectar
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            String receivedMessage = new String(message.getPayload(), StandardCharsets.UTF_8);
                            updateStatusText("Mensagem recebida: " + receivedMessage); // Adiciona um aviso sobre a mensagem recebida
                            updateMessageHistory(receivedMessage);

                            if (receivedMessage.contains("Movimento detectado em")) {
                                movementCount++;
                                updateMovementCount();
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            // Não é necessário neste caso
                        }
                    });

                    arduinoClient.subscribe(ARDUINO_MQTT_TOPIC);

                    updateStatusText("Conectado ao servidor MQTT.");
                    publishToArduino("Conexão estabelecida com sucesso!"); // Envia uma mensagem para o servidor MQTT
                    connected = true; // Conexão estabelecida com sucesso
                } catch (Exception e) {
                    updateStatusText("Falha ao conectar ao servidor MQTT. Tentando novamente...");
                    e.printStackTrace();
                    try {
                        Thread.sleep(RECONNECTION_INTERVAL); // Aguarda o intervalo antes de tentar novamente
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void updateMessageHistory(String message) {
        // Verifica se a mensagem não foi enviada do dispositivo Android
        if (!message.startsWith("Conexão estabelecida com sucesso!") && !message.startsWith("Mensagem recebida:")) {
            runOnUiThread(() -> {
                messageHistory.add(message);
                showLastFiveMessages();
            });
        }
    }

    private void updateMovementCount() {
        runOnUiThread(() -> {
            textViewMovementCount.setText("Contagem de Movimentos: " + movementCount);
            playSoundOnMovementCountIncrease();
        });
    }

    private void playSoundOnMovementCountIncrease() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound);
        }

        if (movementCount > 0) {
            mediaPlayer.start();
        }
    }

    private void showLastFiveMessages() {
        StringBuilder historyBuilder = new StringBuilder();
        int startIndex = Math.max(0, messageHistory.size() - 10);
        for (int i = startIndex; i < messageHistory.size(); i++) {
            historyBuilder.append(messageHistory.get(i)).append("\n");
        }
        textViewMessageHistory.setText(historyBuilder.toString());
    }

    private void updateStatusText(String message) {
        runOnUiThread(() -> textViewStatus.append("\n" + message));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (arduinoClient != null && arduinoClient.isConnected()) {
                arduinoClient.disconnect();
            }
            // Libera o MediaPlayer quando a activity é destruída
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishToArduino(String value) {
        try {
            if (arduinoClient != null && arduinoClient.isConnected()) {
                String message = value;
                arduinoClient.publish(ARDUINO_MQTT_TOPIC, message.getBytes(), 0, false);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}