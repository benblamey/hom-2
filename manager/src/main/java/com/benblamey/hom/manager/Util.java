package com.benblamey.hom.manager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

public class Util {
    static ProcessExecutionResult executeShellLogAndBlock(String[] args) throws IOException, InterruptedException {
        return executeShellLogAndBlock(args, null, null);
    }

    static ProcessExecutionResult executeShellLogAndBlock(String[] args,
                                          File workingDir,
                                          String stdin) throws IOException, InterruptedException {
        String cmdAndArgs = String.join(" ", args);
        System.out.println("Executing " + cmdAndArgs);
        System.out.flush();

        Process cmdProc = new ProcessBuilder(args)
                .redirectErrorStream(true) // merge with stdOut
                .directory(workingDir)
                .start();

        if (stdin != null) {
            cmdProc.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
            cmdProc.getOutputStream().flush();
            cmdProc.getOutputStream().close();
        }

        String stdOut = "";
        int bytesRead = -1;
        do {
            byte[] bytes = cmdProc.getInputStream().readNBytes(1024);
            bytesRead = bytes.length;
            stdOut += new String(bytes, StandardCharsets.UTF_8);
        } while (bytesRead > 0);

        // Think this is redundant (because we got to end of ouput stream) but to be sure.
        cmdProc.waitFor();

        do {
            byte[] bytes = cmdProc.getInputStream().readNBytes(1024);
            bytesRead = bytes.length;
            stdOut += new String(bytes, StandardCharsets.UTF_8);
        } while (bytesRead > 0);


        int exitCode = cmdProc.exitValue();
        System.out.println("Process exited with code: " + exitCode);

        System.out.println(stdOut);
        System.out.flush();

        ProcessExecutionResult result = new ProcessExecutionResult();
        result.stdOut = stdOut;
        result.exitCode = exitCode;

        return result;
    }

    static String getResourceAsStringFromUTF8(String name) throws IOException {
        String s = new String(getClassLoader().getResourceAsStream(name).readAllBytes(), StandardCharsets.UTF_8);
        return s;
    }

    static String generateGUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private static final Random random = new Random();

    public static String randomAlphaString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }

    public static class ProcessExecutionResult {
        public String stdOut;
        public int exitCode;
    }
}
