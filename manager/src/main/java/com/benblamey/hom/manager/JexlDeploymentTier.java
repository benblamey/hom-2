package com.benblamey.hom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class JexlDeploymentTier implements ITier {

    Logger logger = LoggerFactory.getLogger(JexlDeploymentTier.class);

    private final String name; // name of the deployment, used in the YAML
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

    public JexlDeploymentTier(String jexlExpression, int index, String inputTopic) throws IOException, InterruptedException {
        this.friendlyTierId = Integer.toString(index);
        this.jexlExpression = jexlExpression.toString();
        this.uniqueTierId = generateUniqueTierID();
        this.inputTopic = inputTopic;
        this.outputTopic = "hom-topic-" + this.friendlyTierId + "-" + this.uniqueTierId;
        this.kafkaApplicationID = "app-hom-tier-" + this.friendlyTierId + "-" + this.uniqueTierId;

        this.name = "engine-" + friendlyTierId + "-" + uniqueTierId;

        createDeployment();
    }

    @Override
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

    @Override
    public void setScale(int newScale) throws IOException, InterruptedException {
        logger.info("setScale not implemented");
    }

    @Override
    public void remove() throws IOException, InterruptedException {
        Util.executeShellLogAndBlock(new String[] {"kubectl","delete","deployment",this.name});
    }

    @Override
    public String getOutputTopic() {
        return this.outputTopic;
    }

    @Override
    public String getKafkaApplicationID() {
        return this.kafkaApplicationID;
    }

    private void createDeployment() throws IOException, InterruptedException {
        String formattedArgList = String.join(",",
                Arrays.asList(new String[]{
                        "-cp",
                        "output.jar",
                        "-DKAFKA_BOOTSTRAP_SERVER=" + CommandLineArguments.getKafkaBootstrapServerConfig(),
                        //"-DKAFKA_BOOTSTRAP_SERVER=localhost:19092",
                        // Stream ID used within Kafka
                        "-DKAFKA_APPLICATION_ID=" + kafkaApplicationID,
                        "-DINPUT_TOPIC=" + inputTopic,
                        "-DOUTPUT_TOPIC=" + outputTopic,
                        "-DJEXL_EXPRESSION=" + jexlExpression,
                        "com.benblamey.hom.engine.PipelineEngineMain"
                }).stream().map(x -> "\"" + x + "\"").toList());

        String yaml = Util.getResourceAsStringFromUTF8("jexl_worker_tmpl.yaml")
                .replace("$deployment_name", name)
                .replace("$label", name)
                .replace("$container_name", name)
                .replace("$cmd", "\"" + "java" + "\"")
                .replace("$args", formattedArgList);

        System.out.println(yaml);

        Util.executeShellLogAndBlock(
                new String[]{
                        "kubectl",
                        "apply",
                        "-f",
                        "-"
                }, null, null, yaml);

        Util.executeShellLogAndBlock(
                new String[]{
                        "kubectl",
                        "autoscale",
                        "deployment/" + this.name,
                        "min==1"
                });
    }
}