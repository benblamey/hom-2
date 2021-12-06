package com.benblamey.hom.manager;

import java.io.IOException;
import java.util.Map;

public interface ITier {
    Map<String, Object> toMap();

    void setScale(int newScale) throws IOException, InterruptedException;

    void remove() throws IOException, InterruptedException;

    String getOutputTopic();

    String getKafkaApplicationID();
}
