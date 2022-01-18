package com.benblamey.hom.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

public class Util {
    static String executeShellLogAndBlock(String[] args) throws IOException, InterruptedException {
        return executeShellLogAndBlock(args, null, null, null);
    }

    static String executeShellLogAndBlock(String[] args,
                                          String[] environmentVariables,
                                          File workingDir,
                                          String stdin) throws IOException, InterruptedException {
        String cmdAndArgs = String.join(" ", args);
        System.out.println("Executing " + cmdAndArgs);
        System.out.flush();

        Process cmdProc = Runtime.getRuntime().exec(args,
                environmentVariables,
                workingDir);

        if (stdin != null) {
            cmdProc.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
            cmdProc.getOutputStream().flush();
            cmdProc.getOutputStream().close();
        }

        cmdProc.waitFor();

        System.out.println("Process exited with code: " + cmdProc.exitValue());
        String stdErr = new String(cmdProc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(stdErr);
        String stdOut = new String(cmdProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(stdOut);
        System.out.flush();

        return stdOut;
    }

    static String getResourceAsStringFromUTF8(String name) throws IOException {
        String s = new String(getClassLoader().getResourceAsStream(name).readAllBytes(), StandardCharsets.UTF_8);
        return s;
    }

    static String generateGUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
