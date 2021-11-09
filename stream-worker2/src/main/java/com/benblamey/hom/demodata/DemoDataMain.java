package com.benblamey.hom.demodata;

import java.util.concurrent.CountDownLatch;

public class DemoDataMain {

    public static void main(String[] args) throws InterruptedException {
        String all_args = String.join(" ", args);
        System.out.println(DemoDataMain.class.getName()+" args:" + all_args);

        DemoDataProducer producer = new DemoDataProducer();
        producer.start();

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
        producer.close();
    }

}
