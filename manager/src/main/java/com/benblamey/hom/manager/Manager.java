package com.benblamey.hom.manager;

import org.json.simple.JSONAware;

import java.io.IOException;
import java.util.*;

public class Manager {

    public List<Tier> getTiers() {
        return m_tiers;
    }

    public class Tier implements JSONAware {
        String friendlyTierId = "friendlyTierId"; // Friendly. Doesn't need to be unique
        String jexlExpression = "jexlExpression";
        String uniqueTierId = "uniqueTierId";
        String inputTopic = "inputTopic";
        String outputTopic = "outputTopic";

        @Override
        public String toJSONString() {
            return org.json.simple.JSONObject.toJSONString(Map.of(
                    "friendlyTierId", this.friendlyTierId, // Friendly. Doesn't need to be unique
                    "jexlExpression", this.jexlExpression,
                    "uniqueTierId", this.uniqueTierId,
                    "inputTopic", this.inputTopic,
                    "outputTopic", this.outputTopic
            ));
        }
    }

    private String generateUniqueTierID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private List<Tier> m_tiers = new ArrayList<Tier>();

    public void cleanup() throws IOException, InterruptedException {
        String s = Util.executeShellLogAndBlock(new String[]{"kubectl", "get", "pods"});
//        NAME                                          READY   STATUS             RESTARTS   AGE
//        demo-data                                     1/1     Running            1          47h
//        engine-1ba4f755-17f6-49d5-a884-403a2e63f66d   1/1     Running            0          4m5s
//        engine-30c37dc8-fc07-4c8a-a9af-729bc2af63bc   0/1     CrashLoopBackOff   6          10m
//        kafka                                         1/1     Running            0          114s
//        manager                                       1/1     Running            0          28m
        for (String line : s.split("\\n")) {
            if (line.startsWith("engine-")) {
                String pod_name = line.split(" +")[0];
                Util.executeShellLogAndBlock(new String[]{"kubectl", "delete", "pod", pod_name});
            }
        }
    }

    public void addDemoTier() throws IOException, InterruptedException {
        String jexlExpression = "data.foo > " + (m_tiers.size() + 1) * 5;
        addDemoTier(jexlExpression);
    }

    public void addDemoTier(String jexlExpression) throws IOException, InterruptedException {
        Tier tier = new Tier();
        tier.friendlyTierId = Integer.toString(m_tiers.size());
        tier.jexlExpression = jexlExpression;
        tier.uniqueTierId = generateUniqueTierID();
        tier.inputTopic = m_tiers.isEmpty() ? "haste-input-data" : m_tiers.get(m_tiers.size() - 1).outputTopic;
        tier.outputTopic = "hom-topic-" + tier.friendlyTierId + "-" + tier.uniqueTierId;
        m_tiers.add(tier);

        String[] args = {
                "kubectl",
                "run",
                // pod name
                "engine-" + tier.friendlyTierId + "-" + tier.uniqueTierId,
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
                "-DKAFKA_APPLICATION_ID=app-hom-tier-" + tier.friendlyTierId + tier.uniqueTierId,
                "-DINPUT_TOPIC=" + tier.inputTopic,
                "-DOUTPUT_TOPIC=" + tier.outputTopic,
                "-DJEXL_EXPRESSION=" + tier.jexlExpression,
                "com.benblamey.hom.engine.PipelineEngineMain"
        };
        Util.executeShellLogAndBlock(args);
    }

    public void removeTier() {
        if (m_tiers.isEmpty()) {
            throw new RuntimeException("no tiers exist to remove");
        }
        Tier tier = m_tiers.get(m_tiers.size() - 1);
        m_tiers.remove(m_tiers.size() - 1);
    }
}
