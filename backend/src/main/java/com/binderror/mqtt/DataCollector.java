package com.binderror.mqtt;

import com.binderror.mqtt.common.KafkaConnection;
import com.binderror.mqtt.common.MQTTConnection;
import com.binderror.mqtt.common.ServerException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DataCollector {

    public static void main(String[] args) throws ServerException {
        Config config = ConfigFactory.load();
        MQTTConnection mqttConnection = new MQTTConnection(config, config.getString("datacollector.deviceid"));
        KafkaConnection kafkaConnection = new KafkaConnection(config);
        subscribe(mqttConnection, kafkaConnection, config.getString("datacollector.mqtt.subscribe.topic"),
                config.getString("datacollector.target.kafka.topic"));
    }

    private static void subscribe(MQTTConnection mqttConnection, final KafkaConnection kafkaConnection,
                                  String subscribeTopic, final String kafkaTopic) throws ServerException {
        mqttConnection.subscribe(subscribeTopic, (topic, msg) -> {
            byte[] payload = msg.getPayload();
            kafkaConnection.publish(kafkaTopic, topic, new String(payload));
        });
    }

}
