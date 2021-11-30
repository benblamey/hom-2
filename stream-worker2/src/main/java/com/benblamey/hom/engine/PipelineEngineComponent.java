package com.benblamey.hom.engine;


import com.benblamey.hom.CommandLineArguments;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Properties;


public class PipelineEngineComponent {

    private final Predicate<Object, Object> m_predicate;
    private KafkaStreams m_streams;

    private static Properties getStreamsConfig() {
        final Properties props = new Properties();
        // this one is mandatory for a streaming app, used as a the basis for all the topics.
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, CommandLineArguments.getKafkaApplicationID());
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CommandLineArguments.getKafkaBootstrapServerConfig());

        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    public PipelineEngineComponent(Predicate<Object, Object> predicate) {
        this.m_predicate = predicate;
    }

    private void buildStreamTopology(final StreamsBuilder builder) {
        // Create a stream from the input topic.
        builder.stream(CommandLineArguments.getInputTopic(),
                        Consumed.with(Serdes.Long(), Serdes.String()).withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .filter((k,value) -> m_predicate.test(k, JSONValue.parse(value)))
                .to(CommandLineArguments.getOutputTopic(), Produced.with(Serdes.Long(), Serdes.String()));
    }

    public void start() {
        StreamsBuilder builder = new StreamsBuilder();
        buildStreamTopology(builder);

        Properties props = getStreamsConfig();
        m_streams = new KafkaStreams(builder.build(), props);
        m_streams.start();
    }

    public void close() {
        m_streams.close();
    }

}
