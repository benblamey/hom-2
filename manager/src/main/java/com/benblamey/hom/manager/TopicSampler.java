package com.benblamey.hom.manager;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class TopicSampler {

    private static final int RECORD_COUNT = 500;
    private static final Logger logger = LoggerFactory.getLogger(TopicSampler.class);
    private final Thread t;
    private boolean m_stop = false;
    private String groupId;

    // TODO: these members can be static.

    public TopicSampler(String topic, String outputFilepathJsonl) {
        // "Start and forget"
        t = new Thread(null,
                () -> {
                    try {
                        exportSample(topic, outputFilepathJsonl);
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "topic-sampler-" + topic
        );
        t.start();

        logger.debug("TopicSampler thread started: " + t.getName());
    }

    public void close(){
        logger.info("sampler terminating.");
        m_stop = true;
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportSample(String topicID, String outputFilepath) throws InterruptedException, IOException {
        groupId = "sampler-" + topicID;

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", CommandLineArguments.getKafkaBootstrapServerConfig());
        props.setProperty("group.id", "sampler");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("auto.offset.reset", "earliest");
        props.put("key.deserializer", LongDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());

        KafkaConsumer<Long, String> consumer = new KafkaConsumer<Long, String>(props);
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            consumer.subscribe(List.of(topicID));
            consumer.seekToBeginning(consumer.assignment());
            logger.debug("TopicSampler subscribed to: " + topicID);

            int count = 0;

            fileWriter = new FileWriter(outputFilepath);
            bufferedWriter = new BufferedWriter(fileWriter);

            while (!m_stop) {
                ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(500));
                logger.info(consumer.groupMetadata().toString());
                logger.info("records fetched: " + records.count());

                Iterable<ConsumerRecord<Long, String>> recordsForTopic = records.records(topicID);
                for (ConsumerRecord<Long, String> record : recordsForTopic) {
                    String msgJson = record.value();
                    // Newlines within strings are always escaped in JSON, per spec.
                    String jsonOneLine = msgJson.replace("\n", "") + "\n";
                    bufferedWriter.append(jsonOneLine);
                    count++;
                    if (count == RECORD_COUNT) {
                        break;
                    }
                }
                logger.info("Now written total of " + count + " lines to file.");
                consumer.commitSync();
                bufferedWriter.flush();
                if (count == RECORD_COUNT) {
                    break;
                }
            }
        } finally {
            consumer.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (fileWriter != null) fileWriter.close();

            Util.executeShellLogAndBlock(
                    new String[]{
                            "bash",
                            "-ec",
                            "/kafka_2.13-3.0.1/bin/kafka-consumer-groups.sh",
                            "--bootstrap-server",
                            CommandLineArguments.getKafkaBootstrapServerConfig(),
                            "--delete",
                            "--group",
                            groupId
                    }
            );
        }
        logger.debug("TopicSampler for " + topicID + " terminated.");
    }


}
