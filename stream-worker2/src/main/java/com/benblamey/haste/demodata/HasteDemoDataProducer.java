package com.benblamey.haste.demodata;

import com.benblamey.haste.CommandLineArguments;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class HasteDemoDataProducer  {

    public static final String INPUT_TOPIC = "haste-input-data";
    private static final int NUM_OF_MESSAGES = 500;
    private Thread m_producerThread;
    private boolean m_stopProducerThread = false;

    private Producer<Long, String> createProducer() {
        final Properties props = new Properties();
        //props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "haste-backend-1"); not known ?!
        props.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CommandLineArguments.getKafkaBootstrapServerConfig());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    private void runProducer(final int sendMessageCount)  {
        final Producer<Long, String> producer = createProducer();

        try {
            for (long index = 1; index <= sendMessageCount; index++) {
                if (m_stopProducerThread) {
                    System.out.println("Stopping writing demo data.");
                    return;
                };

                JSONObject obj = new JSONObject();
                obj.put("name", "obj_" + index);
                obj.put("id", index);
                obj.put("foo", new Random().nextInt(100));
                obj.put("bar", new Random().nextDouble() * 1000);
                obj.put("wibble", new Random().nextDouble() * 1000);
                //System.out.print(obj);

                final ProducerRecord<Long, String> record = new ProducerRecord<>(INPUT_TOPIC, index, obj.toJSONString());
                // This is non-blocking.
                RecordMetadata metadata = producer.send(record).get();
                System.out.printf("sent record(key=%s value='%s')" + " metadata(partition=%d, offset=%d)\n",
                        record.key(), record.value(), metadata.partition(), metadata.offset());

                Thread.sleep(100);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    public void start() {
        // Write some messages to the input stream.
        // 5000 takes apx 10 seconds.
        m_producerThread = new Thread(() -> runProducer(NUM_OF_MESSAGES), "input-message-producer");
        m_producerThread.start();
        System.out.println("input messages started.");
    }

    public void close() {
        m_stopProducerThread = true;
        try {
            m_producerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
