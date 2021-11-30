package com.benblamey.hom.manager;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.internals.Topic;

public class TopicPeekerTest {

    public static void main(String[] args) throws InterruptedException {
        TopicPeeker tp = new TopicPeeker();
        ConsumerRecords<Long, String> sample = tp.getSample("haste-input-data");
        System.out.println(sample.count());

    }

}
