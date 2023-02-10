package com.binderror.mqtt;

import com.binderror.mqtt.common.MQTTConnection;
import com.binderror.mqtt.common.ServerException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DeviceUpdater {

    public static void main(String[] args) throws ServerException {
        Config config = ConfigFactory.load();
        MQTTConnection mqttConnection = new MQTTConnection(config, config.getString("deviceupdater.deviceid"));
        for (int i = 1; i <= 10; i++) {
            mqttConnection.publish("device/deviceType/device-" + i + "/" + config.getString("deviceupdater.publish.topic"), "v2".getBytes());
        }
        mqttConnection.disconnect();
    }
}
