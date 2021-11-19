package com.benblamey.hom.manager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        spark.Spark.post("/add-tier", (req, res) -> {
            logger.info("/add-tier");

            JSONParser p = new JSONParser();
            String body1 = req.body();
            logger.info(body1);

            JSONObject body = (JSONObject) p.parse(body1);
            String jexlExpression = (String) body.get("jexl_expression");
            logger.info(jexlExpression);
            manager.addDemoTier(jexlExpression);

            return true;
        });

        spark.Spark.post("/remove-tier", (req, res) -> {
            logger.info("/remove-tier");
            manager.removeTier();
            return true;
        });

        spark.Spark.get("/info", (req, res) -> {
            logger.info("/info");
            List<Manager.Tier> tiers = manager.getTiers();

            Map<String, Object> tiers1 = Map.of("tiers", tiers);
            String json = JSONObject.toJSONString(tiers1);
            logger.debug(json);
            return json;
        });

        spark.Spark.get("/offsets", (req, res) -> {
            logger.info("/offsets");
            List<Offsets.OffsetInfo> offsetInfos = Offsets.fetchOffsets();
            String json = JSONObject.toJSONString(Map.of("offsets",offsetInfos));
            logger.debug(json);
            return json;
        });

        spark.Spark.awaitInitialization();
        logger.info("web server listening on http://0.0.0.0:" + spark.Spark.port() );

        logger.info("Waiting on shutdown latch");
        shutdownLatch.await();

        logger.info("Shutdown latch was set, shutting down engine");
        spark.Spark.stop();
        spark.Spark.awaitStop();

        logger.info("Cleaning up tiers...");
        manager.cleanup();
    }

}
