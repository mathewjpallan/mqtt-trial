package com.binderror.mqtt.common;

import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaConnection {

    private KafkaProducer<String, String> producer;
    private Config config;
    public KafkaConnection(Config config) {
        this.config = config;
        producer = new KafkaProducer<String, String>(kafkaProperties(config));
    }

    private Properties kafkaProperties(Config config) {
        Properties kafkaProperties = new Properties();
        kafkaProperties.put("bootstrap.servers", config.getString("datacollector.target.kafka.broker"));
        kafkaProperties.put("acks", "all");
        kafkaProperties.put("retries", 0);
        kafkaProperties.put("batch.size", 16384);
        kafkaProperties.put("linger.ms", 1);
        kafkaProperties.put("buffer.memory", 33554432);
        kafkaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return kafkaProperties;
    }

    public void publish(String kafkaTopic, String key, String message) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(kafkaTopic, key, message);
        producer.send(producerRecord);
    }
}
