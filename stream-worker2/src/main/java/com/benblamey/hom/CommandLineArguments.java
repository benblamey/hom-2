package com.benblamey.hom;

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


    public static String getInputTopic() {
        // returns null if not specified.
        String result = System.getProperty("INPUT_TOPIC");
        if (result == null) {
            throw new RuntimeException("INPUT_TOPIC is null.");
        }
        System.out.println("INPUT_TOPIC is " + result);
        return result;
    }

    public static String getOutputTopic() {
        // returns null if not specified.
        String result = System.getProperty("OUTPUT_TOPIC");
        if (result == null) {
            throw new RuntimeException("OUTPUT_TOPIC is null.");
        }
        System.out.println("OUTPUT_TOPIC is " + result);
        return result;
    }

    public static String getJexlExpression() {
        // returns null if not specified.
        String result = System.getProperty("JEXL_EXPRESSION");
        if (result == null) {
            throw new RuntimeException("JEXL_EXPRESSION is null.");
        }
        System.out.println("JEXL_EXPRESSION is " + result);
        return result;
    }

    public static String getKafkaApplicationID() {
            // returns null if not specified.
            String result = System.getProperty("KAFKA_APPLICATION_ID");
            if (result == null) {
                throw new RuntimeException("KAFKA_APPLICATION_ID is null.");
            }
            System.out.println("KAFKA_APPLICATION_ID is " + result);
            return result;
        }
}
