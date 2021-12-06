package com.benblamey.hom;

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

    public static String getInputTopic() {
        return getString("INPUT_TOPIC");
    }

    public static String getOutputTopic() {
        return getString("OUTPUT_TOPIC");
    }

    public static String getJexlExpression() {
        return getString("JEXL_EXPRESSION");
    }

    public static String getKafkaApplicationID() {
            return getString("KAFKA_APPLICATION_ID");
        }

    public static Object getPythonFunctionName() {
        return getString("PYTHON_FUNCTION_NAME");
    }

    public static Object getPythonFilename() {
        return getString("PYTHON_FILENAME");
    }
}
