package com.benblamey.hom.manager;

import java.io.IOException;
import java.util.Map;

public abstract class Tier {

    String outputTopic;
    String uniqueTierId;
    String friendlyTierId;



    // Intended only for the 'InputTier'
    Tier(int index, String outputTopic) {
        this.friendlyTierId = Integer.toString(index);
        this.uniqueTierId = Util.generateGUID();
        this.outputTopic = outputTopic;
        new TopicSampler(outputTopic, "/data/sample-tier-" + friendlyTierId + ".jsonl");
    }

    // For the other kinds of Tier, where the output topic is uniquely-generated.
    Tier(int index) {
        this.friendlyTierId = Integer.toString(index);
        this.uniqueTierId = Util.generateGUID();
        this.outputTopic = "hom-topic-" + this.friendlyTierId + "-" + this.uniqueTierId;
        new TopicSampler(outputTopic, "/data/sample-tier-" + friendlyTierId + ".jsonl");
    }

    abstract Map<String, Object> toMap();

    abstract void setScale(int newScale) throws IOException, InterruptedException;

    abstract void remove() throws IOException, InterruptedException;

    abstract String getKafkaApplicationID();

    public String getOutputTopic() {
        return this.outputTopic;
    }

}
