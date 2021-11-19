package com.benblamey.hom.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Util {
    static String executeShellLogAndBlock(String[] args) throws IOException, InterruptedException {
        String cmdAndArgs = String.join(" ", args);
        System.out.println("Executing " + cmdAndArgs);
        System.out.flush();

        Process cmdProc = Runtime.getRuntime().exec(args);
        cmdProc.waitFor();

        System.out.println("Process exited with code: " + cmdProc.exitValue());
        String stdErr = new String(cmdProc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(stdErr);
        String stdOut = new String(cmdProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(stdOut);
        System.out.flush();

        return stdOut;
    }
}
