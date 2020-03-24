package com.example.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Properties;

@Service
public class StreamService {

    private KafkaStreams streams;

    @PostConstruct
    private void build() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Long> stream = builder.stream("input-twitter")
                .groupBy(((key, value) -> key))
                .windowedBy(TimeWindows.of(Duration.ofMinutes(10)))
                .count()
                .toStream()
                .transform(() -> new Transformer<Windowed<Object>, Long, KeyValue<String, Long>>() {
                    @Override
                    public void init(ProcessorContext context) {
                    }

                    @Override
                    public KeyValue<String, Long> transform(Windowed<Object> key, Long value) {
//                        System.out.println(("key: " + key.key() + "\n" +
//                                "count: " +  value + "\n" +
//                                "start time: " + key.window().startTime().toString() + "\n" +
//                                "end time: " + key.window().endTime().toString())
//                                .replaceAll("T", " ").replaceAll("Z", " "));
                        return new KeyValue<>(String.valueOf(key.key()), value);
                    }

                    @Override
                    public void close() {

                    }
                });
        stream.to("output-twitter", Produced.with(Serdes.String(), Serdes.Long()));
        Topology topology = builder.build();

        streams = new KafkaStreams(topology, props);
        streams.start();
    }

    @PreDestroy
    private void close() {
        streams.close();
    }


}
