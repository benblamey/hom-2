package com.benblamey.hom.manager;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Offsets {
    public static class OffsetInfo implements JSONAware {
        public OffsetInfo(List<String> parts) {
            // Parse output from kafka-consumer-groups.sh
            this.GROUP_TOPIC = parts.get(0);
            this.PARTITION = parts.get(1);
            this.CURRENT_OFFSET = parts.get(2);
            this.LOG_END_OFFSET = parts.get(3);
            this.LAG = parts.get(4);
            this.CONSUMER_ID = parts.get(5);
            this.HOST = parts.get(5);
            this.CLIENT_ID = parts.get(5);
        }

        String GROUP_TOPIC;
        String PARTITION;
        String CURRENT_OFFSET;
        String LOG_END_OFFSET;
        String LAG;
        String CONSUMER_ID;
        String HOST;
        String CLIENT_ID;

        @Override
        public String toJSONString() {
            return JSONObject.toJSONString(
                    Map.of("GROUP_TOPIC", this.GROUP_TOPIC,
                            "PARTITION", this.PARTITION,
                            "CURRENT_OFFSET", this.CURRENT_OFFSET,
                            "LOG_END_OFFSET", this.LOG_END_OFFSET,
                            "LAG", this.LAG,
                            "CONSUMER_ID", this.CONSUMER_ID,
                            "HOST", this.HOST,
                            "CLIENT_ID", this.CLIENT_ID));
        }
    }

    static List<OffsetInfo> fetchOffsets() {
        String[] args = {
                "/kafka_2.13-3.0.0/bin/kafka-consumer-groups.sh",
                "--bootstrap-server",
                CommandLineArguments.getKafkaBootstrapServerConfig(),
                "--describe",
                "--all-groups"
        };
        String result;
        try {
            result = Util.executeShellLogAndBlock(args);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
//        System.out.println(result);
        List<OffsetInfo> offsetInfos = Arrays.stream(result.split("\\n"))
                .filter(line -> line.startsWith("app-hom-tier-"))
                .map(line -> Arrays.stream(line.split("\s+")).toList())
                .map(parts -> new OffsetInfo(parts))
                .toList();
        return offsetInfos;
    }

    public static void printOffsets(Manager m) throws InterruptedException {

        while (true) {
            Thread.sleep(3000);
            fetchOffsets();
            "".toString();
        }
    }

}
