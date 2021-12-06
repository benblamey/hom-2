package com.benblamey.hom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Tier {

    Logger logger = LoggerFactory.getLogger(Tier.class);

    // Increasing this will probably break the code where we count up the offsets.
    // Assumes only 1 partition.
    private static final int DEFAULT_WORKERS_PER_CONTAINER_TIER = 1;

    ArrayList<String> podNames = new ArrayList<>();
    String friendlyTierId; // Friendly. Doesn't need to be unique
    String jexlExpression;
    String uniqueTierId;
    String inputTopic;
    String outputTopic;
    String kafkaApplicationID;

    private static String generateUniqueTierID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public Tier(String jexlExpression, int index, String inputTopic) throws IOException, InterruptedException {
        this.friendlyTierId = Integer.toString(index);
        this.jexlExpression = jexlExpression.toString();
        this.uniqueTierId = generateUniqueTierID();
        this.inputTopic = inputTopic;
        this.outputTopic = "hom-topic-" + this.friendlyTierId + "-" + this.uniqueTierId;
        this.kafkaApplicationID = "app-hom-tier-" + this.friendlyTierId + "-" + this.uniqueTierId;
        this.podNames = new ArrayList<String>();

        for (int i = 0; i < DEFAULT_WORKERS_PER_CONTAINER_TIER; i++) {
            String podname = this.addPodToTier();
            this.podNames.add(podname);
        }
    }

    public Map<String, Object> toMap() {
        // For JSON, REST API.
        // return a mutable map
        return new HashMap(Map.of(
                "friendlyTierId", this.friendlyTierId, // Friendly. Doesn't need to be unique
                "jexlExpression", this.jexlExpression,
                "uniqueTierId", this.uniqueTierId,
                "inputTopic", this.inputTopic,
                "outputTopic", this.outputTopic,
                "kafkaApplicationID", this.kafkaApplicationID
        ));
    }

    String addPodToTier() throws IOException, InterruptedException {
        int index = podNames.size();
        String podname = "engine-" + friendlyTierId + "-" + uniqueTierId + "-" + index;

        String[] args = {
                "kubectl",
                "run",
                // pod name
                podname,
                "--image",
                "hom-impl-2.stream-worker2",
                "--command",
                // Image is currently local-only for now.
                "--image-pull-policy=Never",
                "--restart=Always",
                "--",
                "java",
                "-cp",
                "output.jar",
                "-DKAFKA_BOOTSTRAP_SERVER=" + CommandLineArguments.getKafkaBootstrapServerConfig(),
                //"-DKAFKA_BOOTSTRAP_SERVER=localhost:19092",
                // Stream ID used within Kafka
                "-DKAFKA_APPLICATION_ID="+ kafkaApplicationID,
                "-DINPUT_TOPIC=" + inputTopic,
                "-DOUTPUT_TOPIC=" + outputTopic,
                "-DJEXL_EXPRESSION=" + jexlExpression,
                "com.benblamey.hom.engine.PipelineEngineMain"
        };
        Util.executeShellLogAndBlock(args);
        return podname;
    }

    public void  setScale(int newScale) throws IOException, InterruptedException {
        List<String> podNames = this.podNames;

        if (newScale == podNames.size()) {
            logger.info("tier " + friendlyTierId + " already has count " + newScale + ". nothing to do.");
        } else if (newScale > podNames.size()) {
            int toAdd = newScale - podNames.size();
            for (int i = 0; i < toAdd; i++) {
                String podName = addPodToTier();
                podNames.add(podName);
            }
        } else {
            int toRemove = podNames.size() - newScale;
            for (int i = 0; i < toRemove; i++) {
                String podName = podNames.remove(podNames.size() - 1);
                Util.executeShellLogAndBlock(new String[]{"kubectl", "delete", "pod", podName});
            }
        }
    }
}
