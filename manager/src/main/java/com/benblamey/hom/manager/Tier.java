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
        init();
    }

    // For the other kinds of Tier, where the output topic is uniquely-generated.
    Tier(int index) {
        this.friendlyTierId = Integer.toString(index);
        this.uniqueTierId = Util.generateGUID();
        this.outputTopic = "hom-topic-" + this.friendlyTierId + "-" + this.uniqueTierId;
        init();
    }

    private void init() {
        String sampleJsonlPath = "/data/sample-tier-" + friendlyTierId + ".jsonl";
        new TopicSampler(outputTopic, sampleJsonlPath);

        try {
            NotebooksFromTemplates.AnalyzeTierNotebookFromTemplate(sampleJsonlPath,
                    "/data/analyze-tier-" + friendlyTierId + ".ipynb");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    abstract Map<String, Object> toMap();

    abstract void setScale(int newScale) throws IOException, InterruptedException;

    abstract void remove() throws IOException, InterruptedException;

    abstract String getKafkaApplicationID();

    public String getOutputTopic() {
        return this.outputTopic;
    }

}
