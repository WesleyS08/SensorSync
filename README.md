# 🚨 SensorSync

O SensorSync é uma solução completa para o monitoramento de presença, utilizando uma interface de visualização. O dispositivo principal é um ESP32, que se conecta a um servidor MQTT para receber informações em tempo real sobre a presença simulada.

O medidor é acompanhado por um conjunto de LEDs que oferece uma representação visual imediata do alerta enviado pelo celular. Com um design elegante e intuitivo, os LEDs mudam de cor conforme o usuário define que a presença é perigosa, proporcionando uma experiência visual envolvente.

## 📋 Funcionalidades

### 🛡️ Monitoramento de Presença
| Funcionalidade  |     | Descrição                           |
| :---------- | :--------- | :---------------------------------- |
| `Visualização Remota` |  | Os usuários podem visualizar remotamente o status de presença em sua casa, permitindo a verificação em tempo real de atividades suspeitas ou situações de emergência. |
| `Alertas de Presença` |  | O SensorSync envia alertas instantâneos aos usuários sempre que detecta presença, mantendo-os informados sobre qualquer atividade incomum. |

### 🔒 Controle Remoto de Dispositivos
| Funcionalidade  |     | Descrição                           |
| :---------- | :--------- | :---------------------------------- |
| `Acionamento Remoto` |  | Os usuários podem acionar dispositivos remotamente, como luzes ou alarmes, em resposta a uma detecção de presença, oferecendo um meio eficaz de dissuasão contra intrusos. |


### 📱 Notificações em Tempo Real
| Funcionalidade  |     | Descrição                           |
| :---------- | :--------- | :---------------------------------- |
| `Alertas Instantâneos` |  | O SensorSync envia notificações em tempo real aos usuários sempre que detecta presença, garantindo que estejam sempre cientes de qualquer atividade suspeita. |


## 🛠️ Tecnologias Utilizadas

O SensorSync foi desenvolvido utilizando as seguintes tecnologias:

- 📱 Android Studio: Utilizado para desenvolver a aplicação Android para dispositivos móveis.

- 🛠️ Arduino IDE: Utilizado para programar o ESP32 e controlar os LEDs em resposta às detecções de presença.

- 📡 MQTT: Protocolo de mensagens utilizado para comunicação entre o aplicativo e o dispositivo ESP32.

- 💡 LEDs RGB: Utilizados para fornecer feedback visual sobre a presença detectada.

## 📱 Capturas de Tela

Aqui estão algumas capturas de tela do SensorSync:

![server](/img/arduino.png)

![server](/img/arduino1.png)

![server](/img/arduino2.png)

![server](/img/server.png)

![server](/img/sistema.jpeg)

## 🛡️ Segurança

A segurança dos dados dos usuários é uma prioridade. O SensorSync utiliza os mais altos padrões de criptografia e segurança para proteger as informações confidenciais dos usuários.

## 📄 Licença

Este projeto é licenciado sob a [MIT License](https://choosealicense.com/licenses/mit/).
