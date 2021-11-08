package com.benblamey.haste.demodata;

import java.util.concurrent.CountDownLatch;

public class HasteDemoDataMain {

    public static void main(String[] args) throws InterruptedException {
        String all_args = String.join(" ", args);
        System.out.println(HasteDemoDataMain.class.getName()+" args:" + all_args);

        HasteDemoDataProducer producer = new HasteDemoDataProducer();
        producer.start();

        // attach shutdown handler to catch control-c
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("haste-backend-shutdown-hook") {
            @Override
            public void run() {
                shutdownLatch.countDown();
            }
        });

        System.out.println("Waiting on shutdown latch");
        shutdownLatch.await();

        System.out.println("Shutdown latch was set, shutting down engine");
        producer.close();
    }

}
