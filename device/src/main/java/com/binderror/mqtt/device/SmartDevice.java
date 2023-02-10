package com.binderror.mqtt.device;

import com.binderror.mqtt.common.MQTTConnection;
import com.binderror.mqtt.common.ServerException;
import com.typesafe.config.Config;

public class SmartDevice implements Runnable {
    private String deviceId;

    private String softwareVersion = "v1";
    private float temperature;
    private float pressure;
    private Config config;
    private MQTTConnection mqttConnection;

    public SmartDevice(String deviceId, Config config) throws ServerException {
        this.deviceId = deviceId;
        this.config = config;
        mqttConnection = new MQTTConnection(config, deviceId);
        subscribe(config.getString("device.subscribe.topic"));
    }

    private void subscribe(String subscribeTopic) throws ServerException {
        mqttConnection.subscribe("device/deviceType/" + deviceId + "/" + subscribeTopic,  (topic, msg) -> {
            byte[] payload = msg.getPayload();
            //all the actions for the incoming message from the server
            softwareVersion = new String(payload);
        });
    }

    private void publish(String publishTopic, byte[] payload) throws ServerException {
        mqttConnection.publish("device/deviceType/" + deviceId + "/" + publishTopic, payload);
    }

    public void run() {
        System.out.println("Device - " + deviceId + " started");
        while(true) {
            //gather sensor data
            temperature = (float) ((Math.random() * (45 - 14)) + 14);
            pressure = (float) ((Math.random() * (32 - 28)) + 28);
            try {
                publish(config.getString("device.publish.topic"), (softwareVersion + "," + deviceId+ "," + temperature + "," + pressure).getBytes());
                Thread.sleep(config.getInt("device.frequency"));
            } catch (ServerException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
