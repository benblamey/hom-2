package com.benblamey.hom.py;

import py4j.GatewayServer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

public class Py4JMain {

    public static void main(String[] args) throws IOException, InterruptedException {
//        String cmdAndArgs = String.join(" ", args);
//        System.out.println("Executing " + cmdAndArgs);
//        System.out.flush();

        String currentDirectory = System.getProperty("user.dir");
        System.out.println("The current working directory is " + currentDirectory);

        String[] cmdargs = {
            "python3","./stream-worker2/pysrc/foo.py"
        };

//        System.in.read();

        ProcessBuilder pb = new ProcessBuilder(cmdargs);
        pb.directory(new File(currentDirectory));
        System.out.println(pb.directory());
        pb.inheritIO();
        Process cmdProc = pb.start();
        //Process cmdProc = null;

        try {

            //Process cmdProc = Runtime.getRuntime().exec(cmdargs);

            Thread.sleep(6000);

//        String stdErr = new String(cmdProc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
//        System.out.println(stdErr);
//        String stdOut = new String(cmdProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
//        System.out.println(stdOut);
//        System.out.flush();

            //assert (cmdProc.isAlive());

//            GatewayServer.turnLoggingOff();
            GatewayServer server = new GatewayServer();
            server.start();

            GatewayServer.turnLoggingOn();
            Logger logger = getLogger("py4j");
            logger.setLevel(Level.ALL);
            
            IHello hello = (IHello) server.getPythonServerEntryPoint(
                    new Class[]{IHello.class});

            try {
                hello.sayHello();
                hello.sayHello(2, "Hello World");

                Thread.sleep(3000);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            finally {
                server.shutdown();
            }

        } finally {
            if (cmdProc != null) {
                assert cmdProc.isAlive();
                cmdProc.destroy();
                cmdProc.waitFor();
            }
        }

    }
}