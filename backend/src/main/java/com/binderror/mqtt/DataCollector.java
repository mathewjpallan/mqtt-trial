package com.binderror.mqtt;

import com.binderror.mqtt.common.KafkaConnection;
import com.binderror.mqtt.common.MQTTConnection;
import com.binderror.mqtt.common.ServerException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DataCollector {

    public static void main(String[] args) throws ServerException {
        Config config = ConfigFactory.load();
        MQTTConnection mqttConnection = new MQTTConnection(config, config.getString("datacollector.deviceid"),
                config.getString("datacollector.devicepassword"));
        KafkaConnection kafkaConnection = new KafkaConnection(config);
        subscribe(mqttConnection, kafkaConnection, config.getString("datacollector.mqtt.subscribe.topic"),
                config.getString("datacollector.target.kafka.topic"));
    }

    private static void subscribe(MQTTConnection mqttConnection, final KafkaConnection kafkaConnection,
                                  String subscribeTopic, final String kafkaTopic) throws ServerException {
        mqttConnection.subscribe(subscribeTopic, (topic, msg) -> {
            byte[] payload = msg.getPayload();
            //The mqtt topic name is used as the key while inserting to Kafka to ensure that all messages from a device
            //go to the same partition. This is to ensure that the order of arrival of
            //messages into the mqtt topic is retained in the kafka topic partition as well.
            kafkaConnection.publish(kafkaTopic, topic, new String(payload));
        });
    }

}
