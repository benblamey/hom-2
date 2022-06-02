package com.benblamey.hom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class PyWorkerDeploymentTier extends Tier {

    private String error = ""; // cannot be null.

    // For debugging
    public static void main(String[] args) throws IOException, InterruptedException {
        new PyWorkerDeploymentTier("foo.ipynb::hej", 0, "input-topic-foo");
    }

    public static final String PYWORKER_SCRIPT_DIR = ".pyworker";

    Logger logger = LoggerFactory.getLogger(PyWorkerDeploymentTier.class);

    private final String name; // name of the deployment, used in the YAML
    private final String pythonFilenameAndFunction;

    String inputTopic;
    String kafkaApplicationID;

    public PyWorkerDeploymentTier(String pythonFilenameAndFunction, int index, String inputTopic) throws IOException, InterruptedException {
        super(index);
        this.inputTopic = inputTopic;
        this.kafkaApplicationID = "app-hom-tier-" + this.friendlyTierId + "-" + this.uniqueTierId;
        this.name = "engine-" + friendlyTierId + "-" + uniqueTierId;
        this.pythonFilenameAndFunction = pythonFilenameAndFunction;

        Thread t = new Thread(null,
                () -> {
                    try {
                        createDeployment();
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "pyworker-tier-creator-" + this.name
        );
        t.start();
    }

    @Override
    public Map<String, Object> toMap() {
        // For JSON, REST API.
        // return a mutable map
        return new HashMap(Map.of(
                "friendlyTierId", this.friendlyTierId, // Friendly. Doesn't need to be unique
                "uniqueTierId", this.uniqueTierId,
                "inputTopic", this.inputTopic,
                "outputTopic", this.outputTopic,
                "kafkaApplicationID", this.kafkaApplicationID,
                "jexlExpression", this.pythonFilenameAndFunction, // back compat with web GUI
                "error", this.error
        ));
    }

    @Override
    public void setScale(int newScale) throws IOException, InterruptedException {
        logger.info("setScale not implemented");
    }

    @Override
    public void remove() throws IOException, InterruptedException {
        // Stop the sampler.
        super.remove();

        Thread t = new Thread(null,
                () -> {
                    try {
                        Util.executeShellLogAndBlock(new String[]{"kubectl", "delete", "deployment", this.name});
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "pyworker-tier-remover-" + this.name
        );
        t.start();
    }

    @Override
    public String getKafkaApplicationID() {
        return this.kafkaApplicationID;
    }

    private void createDeployment() throws IOException, InterruptedException {
        // python3 -m py_stream_worker kafka-service:9092 haste-input-data output-topic-foo groupidfoo example.py hejfunction

        String[] s = pythonFilenameAndFunction.split("::");
        String notebookFilenameWithExt = s[0];
        String function = s[1];
        String scriptFileNameAndExtension = name + ".py";

        Util.ProcessExecutionResult convertResult = Util.executeShellLogAndBlock(
                new String[]{
                        "python3",
                        "-m",
                        "nbconvert",
                        "/data/"+notebookFilenameWithExt,
                        "--output",
                        scriptFileNameAndExtension,
                        "--output-dir=/data/" + PYWORKER_SCRIPT_DIR,
                        //"--execute", // execute prior to export
                        "--to",
                        "python"
                });


        System.out.println(convertResult.stdOut);

        if (convertResult.exitCode != 0) {
            this.error = "Could not convert notebook to .py. Check for syntax errors in ipynb file (even if your function is OK).";
            return;
        }

        List<String> args = new ArrayList<String>();

        args.addAll(Arrays.asList(
                "sh -c ./data/nb_worker_context.sh",
                ";",
                "python3",
                "-u",
                "-m",
                "py_stream_worker",
                CommandLineArguments.getKafkaBootstrapServerConfig(),
                inputTopic,
                outputTopic,
                kafkaApplicationID,
                "/data/" + PYWORKER_SCRIPT_DIR + "/" + scriptFileNameAndExtension,
                function));

        String yaml = Util.getResourceAsStringFromUTF8("py_worker_tmpl.yaml")
                .replace("$deployment_name", name)
                .replace("$label", name)
                .replace("$container_name", name)
                .replace("$cmd", "\"" + "bash" + "\"")
                .replace("$args", "\"-ec\",\""+String.join(" ", args)+"\"");

        System.out.println(yaml);

        Util.ProcessExecutionResult s1 = Util.executeShellLogAndBlock(
                new String[]{
                        "kubectl",
                        "apply",
                        "-f",
                        "-"
                }, null, yaml, true);

        Util.ProcessExecutionResult s2 = Util.executeShellLogAndBlock(
                new String[]{
                        "kubectl",
                        "autoscale",
                        "deployment/" + this.name,
                        "--min=1",
                        "--max=" + CommandLineArguments.getMaxWorkerReplicas(),
                });
    }
}