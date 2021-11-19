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
            this.GROUP = parts.get(0);
            this.TOPIC = parts.get(1);
            this.PARTITION = parts.get(2);

            // -1 can signify that everything is parsed
            this.CURRENT_OFFSET = parts.get(3).equals("-") ? -1 : Long.parseLong(parts.get(3));

            this.LOG_END_OFFSET = parts.get(4).equals("-") ? -1 : Long.parseLong(parts.get(4));
            this.LAG = parts.get(5);
            this.CONSUMER_ID = parts.get(6);
            this.HOST = parts.get(7);
            this.CLIENT_ID = parts.get(8);
        }

        String GROUP;
        String TOPIC;
        String PARTITION;
        Long CURRENT_OFFSET;
        long LOG_END_OFFSET;
        String LAG;
        String CONSUMER_ID;
        String HOST;
        String CLIENT_ID;

        @Override
        public String toJSONString() {
            return JSONObject.toJSONString(
                    Map.of("GROUP", this.GROUP,
                            "TOPIC", this.TOPIC,
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
