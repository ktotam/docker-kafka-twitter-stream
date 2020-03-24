package com.example.streams;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

@Service
public class ProducerService {

    private String INPUT_TOPIC;
    private KafkaProducer<String, String> producer;

    @PostConstruct
    private void build() {
        INPUT_TOPIC = "input-twitter";
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());

        producer = new KafkaProducer<>(producerProperties);

    }

    @PreDestroy
    private void destroy() {
        producer.close();
    }

    void send(String key, String value) {
        producer.send(new ProducerRecord<>(INPUT_TOPIC, key, value));
    }
}
