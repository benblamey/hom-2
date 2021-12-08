package com.benblamey.hom.manager;

public  class CommandLineArguments {

    private static String getString(String argumentName) {
        return getString(argumentName, null);
    }

    private static String getString(String argumentName, String defaultValue) {
        // returns null if not specified.
        String result = System.getProperty(argumentName);
        if (result == null) {
            System.out.println(argumentName + " defaulted to " + defaultValue);
            return defaultValue;
        }
        System.out.println(argumentName + " was specified as " + result);
        return result;
    }

    public static String getKafkaBootstrapServerConfig() {
        return getString("KAFKA_BOOTSTRAP_SERVER");
    }

    public static String getDataPath() {
        return getString("DATA_PATH");
    }

    public static String getMaxWorkerReplicas() {
        return getString("MAX_WORKER_REPLICAS", "3");
    }
}
