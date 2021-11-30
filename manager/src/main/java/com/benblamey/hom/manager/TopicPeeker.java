package com.benblamey.hom.manager;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class TopicPeeker {

    private static final Logger logger = LoggerFactory.getLogger(TopicPeeker.class);
    private final KafkaConsumer<Long, String> consumer;

    public TopicPeeker() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", CommandLineArguments.getKafkaBootstrapServerConfig());
        props.setProperty("group.id", "peeker");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("auto.offset.reset", "earliest");
        props.put("key.deserializer", LongDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());

        consumer = new KafkaConsumer<Long, String>(props);
   }

    public ConsumerRecords<Long, String> getSample(String topicID) throws InterruptedException {
        consumer.subscribe(List.of(topicID));
        consumer.seekToBeginning(consumer.assignment());

        ConsumerRecords<Long, String> records = null;
        for (int i= 0; i < 10; i++) {
            records = consumer.poll(Duration.ofMillis(100));
            logger.info(consumer.groupMetadata().toString());
            logger.info("records fetched: " + records.count());

            // It can take a few seconds for the consumer to join the group
            if (records.count() > 0) {
                return records;
            }
            Thread.sleep(1000);
        }

        return records;
    }

    public void close() {
        consumer.close();
    }



}
