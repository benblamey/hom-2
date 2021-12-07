package com.benblamey.hom.manager;

public  class CommandLineArguments {


    private static String getString(String argumentName) {
        // returns null if not specified.
        String result = System.getProperty(argumentName);
        if (result == null) {
            throw new RuntimeException(argumentName + " is null.");
        }
        System.out.println(argumentName + " is " + result);
        return result;
    }

    public static String getKafkaBootstrapServerConfig() {
        return getString("KAFKA_BOOTSTRAP_SERVER");
    }

    public static String getDataPath() {
        return getString("DATA_PATH");
    }
}
