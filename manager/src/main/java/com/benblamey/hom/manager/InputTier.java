package com.benblamey.hom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Placeholder for existin Kafka stream repr. the input source for the system.
public class InputTier  extends Tier {

    Logger logger = LoggerFactory.getLogger(InputTier.class);
    static final int tierId = 0;

    public InputTier(String outputTopic) {
        super(tierId, outputTopic);
    }

    @Override
    public Map<String, Object> toMap() {
        // For JSON, REST API.
        // return a mutable map
        return new HashMap(Map.of(
                "friendlyTierId", this.friendlyTierId, // Friendly. Doesn't need to be unique
                "uniqueTierId", this.uniqueTierId,
                "inputTopic", "",
                "outputTopic", this.outputTopic,
                "kafkaApplicationID", "",
                "jexlExpression", ""
        ));
    }

    @Override
    void setScale(int newScale) throws IOException, InterruptedException {
        logger.error("setScale() not implemented.");
    }

    @Override
    public void remove() throws IOException, InterruptedException {
        logger.debug("IntputTier removed - nothing to do within the Tier itself");
    }

    @Override
    String getKafkaApplicationID() {
        return "";
    }
}
