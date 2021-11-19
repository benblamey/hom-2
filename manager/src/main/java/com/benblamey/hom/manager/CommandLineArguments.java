package com.benblamey.hom.manager;

public  class CommandLineArguments {

    public static String getKafkaBootstrapServerConfig() {
        // returns null if not specified.
        String result = System.getProperty("KAFKA_BOOTSTRAP_SERVER");
        if (result == null) {
            // for dev
            result = "localhost:19092";
        }
        System.out.println("KAFKA_BOOTSTRAP_SERVER is " + result);
        return result;
    }

}
