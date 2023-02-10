package com.binderror.mqtt.common;

import com.typesafe.config.Config;
import org.eclipse.paho.client.mqttv3.*;

/**
 * MQTTConnection class abstracts the MQTT connection handling and eclipse paho specific code and exposes helper methods to the
 * devices that need to connect to an MQTT server
 */
public class MQTTConnection {

    /*
        Configuration object that represents the contents of application.conf
     */
    private final Config config;
    private final String clientId;

    private final String password;
    private IMqttClient mqttClient;

    public MQTTConnection(Config config, String clientId, String clientPassword) throws ServerException {
        this.config = config;
        this.clientId = clientId;
        this.password = clientPassword;
        this.mqttClient = initMQTTClient();
        connect();
    }

    public IMqttClient initMQTTClient() throws ServerException {
        try {
            return new MqttClient(config.getString("broker.url"), clientId);
        } catch (MqttException e) {
            throw new ServerException(e);
        }
    }

    private void connect() throws ServerException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(config.getBoolean("broker.automatic.reconnect"));
        options.setCleanSession(config.getBoolean("broker.clean.session"));
        options.setConnectionTimeout(config.getInt("broker.connection.timeout"));
        if(config.getBoolean("broker.auth.enabled")) {
            options.setUserName(clientId);
            options.setPassword(password.toCharArray());
        }
        try {
            mqttClient.connect(options);
        } catch (MqttException e) {
            throw new ServerException(e);
        }
    }

    public void disconnect() throws ServerException {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            throw new ServerException(e);
        }
    }

    public void publish(String topic, int qos, byte[] payload) throws ServerException {
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(qos);
        try {
            mqttClient.publish(topic, msg);
        } catch (MqttException e) {
            throw new ServerException(e);
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) throws ServerException {
        try {
            mqttClient.subscribe(topic, listener);
        } catch (MqttException e) {
            throw new ServerException(e);
        }
    }
}
