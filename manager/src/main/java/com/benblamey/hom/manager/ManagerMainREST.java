package com.benblamey.hom.manager;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ManagerMainREST {

    final static Logger logger = LoggerFactory.getLogger(ManagerMainREST.class);

    public static void main(final String[] args) throws Exception {
        Manager manager = new Manager();

        // attach shutdown handler to catch control-c
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLatch::countDown, "shutdown-hook"));

        // Clean up any old processing pods.
        manager.cleanup();

        // We use the Spark micro-framework to serve the web requests.
        // http://sparkjava.com/documentation.html#getting-started

        spark.Spark.get("/", (req, res) -> {
                    logger.info("/");
                    return "The API is running.";
                });

        spark.Spark.post("/add-base-tier/:topicid", (req, res) -> {
            String topicID = req.params(":topicid");
                    logger.info("/add-tier-base");
                    addCrossOriginHeaders(res);

                    if (!manager.getTiers().isEmpty()) {
                        throw new RuntimeException("Can only add base tier if no existing tiers");
                    }
                    manager.addBaseTier(topicID);

                    return true;
                });

        spark.Spark.post("/add-tier", (req, res) -> {
            logger.info("/add-tier");
            addCrossOriginHeaders(res);

            if (manager.getTiers().isEmpty()) {
                throw new RuntimeException("Can only add base tier if no existing tiers");
            }

            JSONParser p = new JSONParser();
            String body1 = req.body();
            logger.info(body1);

            JSONObject body = (JSONObject) p.parse(body1);
            String jexlExpression = (String) body.get("jexl_expression");
            logger.info(jexlExpression);
            manager.addJexlTier(jexlExpression);

            return true;
        });

        spark.Spark.post("/remove-tier", (req, res) -> {
            logger.info("/remove-tier");
            addCrossOriginHeaders(res);
            if (manager.getTiers().isEmpty()) {
                return false;
            } else {
                manager.removeTier();
                return true;
            }
        });

        spark.Spark.get("/info", (req, res) -> {
            logger.info("/info");
            addCrossOriginHeaders(res);
            List<Tier> tiers = manager.getTiers();
            List<Offsets.OffsetInfo> offsetInfos = Offsets.fetchOffsets();
            List<Map> tierJsonMaps = new ArrayList<>();

            for (Tier t : tiers) {
                Long sumOfCurrentOffsets = 0L;
                Long sumOfLogEndOffsets = 0L;
                Map<String, Object> jsonMap = t.toMap();

                for (Offsets.OffsetInfo oi : offsetInfos) {
                    if (oi.GROUP.equals(t.getKafkaApplicationID())) {
                        sumOfCurrentOffsets += oi.CURRENT_OFFSET;
                        sumOfLogEndOffsets += oi.LOG_END_OFFSET;
                    }
                }
                jsonMap.put("SUM_OF_CURRENT_OFFSETS", sumOfCurrentOffsets);
                jsonMap.put("SUM_OF_LOG_END_OFFSETS", sumOfLogEndOffsets);
                tierJsonMaps.add(jsonMap);
            }

            Map<String, Object> tiers1 = Map.of("tiers", tierJsonMaps);
            String json = JSONObject.toJSONString(tiers1);
            logger.debug(json);
            return json;
        });

        spark.Spark.get("/available_functions", (req, res) -> {
            logger.info("/available_functions");
            addCrossOriginHeaders(res);

            Map<String, List> tiers1 = Map.of(
                    "functions",
                    NotebookScraper.getFunctions(CommandLineArguments.getDataPath()));

            String json = JSONObject.toJSONString(tiers1);
            logger.debug(json);
            return json;
        });

        spark.Spark.get("/info-fake", (req, res) -> {
            logger.info("/info-fake");
            // to allow testing from localhost. not for public/prod. TODO: tighten this up
            addCrossOriginHeaders(res);

            String fake_response = "{\n" +
                    "  \"tiers\": [\n" +
                    "    {\n" +
                    "      \"outputTopic\": \"hom-topic-0-ec1a57b0-0200-4d9b-beae-d8453f95889b\",\n" +
                    "      \"jexlExpression\": \"data.foo > 5\",\n" +
                    "      \"SUM_OF_CURRENT_OFFSETS\": 68705,\n" +
                    "      \"uniqueTierId\": \"ec1a57b0-0200-4d9b-beae-d8453f95889b\",\n" +
                    "      \"inputTopic\": \"haste-input-data\",\n" +
                    "      \"SUM_OF_LOG_END_OFFSETS\": 142843,\n" +
                    "      \"kafkaApplicationID\": \"app-hom-tier-0-ec1a57b0-0200-4d9b-beae-d8453f95889b\",\n" +
                    "      \"friendlyTierId\": \"0\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"outputTopic\": \"hom-topic-1-369af139-38ff-45b1-b1ad-c328b9ca40f1\",\n" +
                    "      \"jexlExpression\": \"data.foo > 5\",\n" +
                    "      \"SUM_OF_CURRENT_OFFSETS\": 71371,\n" +
                    "      \"uniqueTierId\": \"369af139-38ff-45b1-b1ad-c328b9ca40f1\",\n" +
                    "      \"inputTopic\": \"hom-topic-0-ec1a57b0-0200-4d9b-beae-d8453f95889b\",\n" +
                    "      \"SUM_OF_LOG_END_OFFSETS\": 97230,\n" +
                    "      \"kafkaApplicationID\": \"app-hom-tier-1-369af139-38ff-45b1-b1ad-c328b9ca40f1\",\n" +
                    "      \"friendlyTierId\": \"1\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            return fake_response;
        });

        spark.Spark.get("/raw_offsets", (req, res) -> {
            // recommended use /info. left for debugging.
            logger.info("/raw_offsets");
            List<Offsets.OffsetInfo> offsetInfos = Offsets.fetchOffsets();
            String json = JSONObject.toJSONString(Map.of("offsets", offsetInfos));
            logger.debug(json);
            return json;
        });

        Map<String, ConsumerRecords<Long, String>> sampleByUniqueTierID = new HashMap<String, ConsumerRecords<Long, String>>();

        spark.Spark.get("/sample/:topicid", (req, res) -> {
            // TODO: remove all the sampling stuff from here?
            String topicID = req.params(":topicid");

            addCrossOriginHeaders(res);

            ConsumerRecords<Long, String> sample;
            if (sampleByUniqueTierID.containsKey(topicID)) {
                sample = sampleByUniqueTierID.get(topicID);
            } else {
                TopicPeeker tp = new TopicPeeker();
                sample = tp.getSample(topicID);
                tp.close();
                if (sample != null && sample.count() > 0) {
                    sampleByUniqueTierID.put(topicID, sample);
                }
            }

            // This is a bit of a horror show...
            // The records are serialized as JSON, so can be returned as-is over the web API.
            StringBuilder sb = new StringBuilder();
            sb.append("{ \"sample\": [");

            logger.info(sb.toString());
            sample.forEach(cr -> sb.append(cr.value() + ","));
            logger.info(sb.toString());

            // Remove the trailing comma, if it exists
            if (!sample.isEmpty()) {
                sb.deleteCharAt(sb.length() - 1);
            }
            logger.info(sb.toString());

            sb.append("] }");
            logger.info(sb.toString());

            return sb.toString();
        });

        spark.Spark.get("/scale/:tier/:scale", (req, res) -> {
            int tier = Integer.parseInt(req.params(":tier"));
            int scale = Integer.parseInt(req.params(":scale"));
            logger.info("scaling tier " + tier + " to " + scale);
            addCrossOriginHeaders(res);
            manager.getTiers().get(tier).setScale(scale);
            return true;
        });


        spark.Spark.post("/create_notebook_tier", (req, res) -> {
            addCrossOriginHeaders(res);
            logger.info("/create_notebook_tier");

            if (manager.getTiers().isEmpty()) {
                throw new RuntimeException("Can only add base tier if no existing tiers");
            }

            JSONParser p = new JSONParser();
            String body1 = req.body();
            logger.info(body1);

            JSONObject body = (JSONObject) p.parse(body1);
            String notebook_and_function = (String) body.get("notebook_and_function");

            manager.addNotebookTier(notebook_and_function);

            return true;
        });

        spark.Spark.awaitInitialization();
        logger.info("web server listening on http://0.0.0.0:" + spark.Spark.port());

        logger.info("Waiting on shutdown latch");
        shutdownLatch.await();

        logger.info("Shutdown latch was set, shutting down engine");
        spark.Spark.stop();
        spark.Spark.awaitStop();

        logger.info("Cleaning up tiers...");

        manager.cleanup();

    }

    private static void addCrossOriginHeaders(Response res) {
        res.header("Access-Control-Allow-Origin", "*");
    }

}
