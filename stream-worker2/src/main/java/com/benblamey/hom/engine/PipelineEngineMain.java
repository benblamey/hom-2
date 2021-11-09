package com.benblamey.hom.engine;

import com.benblamey.hom.CommandLineArguments;
import com.benblamey.hom.demodata.DemoDataMain;
import com.benblamey.hom.demodata.DemoDataProducer;

import java.util.concurrent.CountDownLatch;

public class PipelineEngineMain {

    public static void main(String[] args) throws InterruptedException {
        String all_args = String.join(" ", args);
        System.out.println(DemoDataMain.class.getName()+" args: " + all_args);

        PipelineEngineComponent pipelineEngine = new PipelineEngineComponent(
                JexlExpressions.jexlToPredicate(
                CommandLineArguments.getJexlExpression()));
        pipelineEngine.start();

        // attach shutdown handler to catch control-c
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
            @Override
            public void run() {
                shutdownLatch.countDown();
            }
        });

        System.out.println("Waiting on shutdown latch");
        shutdownLatch.await();

        System.out.println("Shutdown latch was set, shutting down engine");
        pipelineEngine.close();
    }


}
