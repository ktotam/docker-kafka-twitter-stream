package spring.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

@Service
public class ConsumerService {

    private static final Logger logger = Logger.getLogger(ConsumerService.class.getName());
    private String OUTPUT_TOPIC;
    private KafkaConsumer<String, Long> consumer;
    private Properties props;

    @PostConstruct
    private void build() {
        OUTPUT_TOPIC = "output-twitter";
        props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("group.id", "consumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");

        consumer = new KafkaConsumer<>(props);
        List<String> topics = new ArrayList<>();
        topics.add(OUTPUT_TOPIC);
        consumer.subscribe(topics);

        while (true) {
            ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Long> record : records) {
                log(record.key() + " " + record.value());
            }
        }
    }

    @PreDestroy
    private void destroy() {
        consumer.close();
    }

    private void log(String message) {
        logger.info(message);
    }



}
