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
    private final String deviceId;
    private IMqttClient mqttClient;

    public MQTTConnection(Config config, String deviceId) throws ServerException {
        this.config = config;
        this.deviceId = deviceId;
        this.mqttClient = initMQTTClient();
        connect();
    }

    public IMqttClient initMQTTClient() throws ServerException {
        try {
            return new MqttClient(config.getString("broker.url"), deviceId);
        } catch (MqttException e) {
            throw new ServerException(e);
        }
    }

    public void connect() throws ServerException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(config.getBoolean("device.automatic.reconnect"));
        options.setCleanSession(config.getBoolean("device.clean.session"));
        options.setConnectionTimeout(config.getInt("device.connection.timeout"));
        if(config.getBoolean("device.auth.enabled")) {
            options.setUserName(deviceId);
            options.setPassword(config.getString("device.auth.password").toCharArray());
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

    public void publish(String topic, byte[] payload) throws ServerException {
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(config.getInt("device.publish.qos"));
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
