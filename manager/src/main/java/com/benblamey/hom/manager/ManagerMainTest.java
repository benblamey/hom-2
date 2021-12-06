package com.benblamey.hom.manager;

import java.io.IOException;
import java.util.Scanner;

public class ManagerMainTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner s = new Scanner(System.in);

        Manager m = new Manager();

        Thread t = new Thread(() -> {
            try {
                Offsets.printOffsets(m);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        m.cleanup();

        System.out.println("Add a tier.....");
        s.nextLine();
        m.addDemoJexlTier();

        System.out.println("Press enter to continue.....");
        s.nextLine();
        m.addDemoJexlTier();

        System.out.println("Press enter to continue.....");
        s.nextLine();
        m.addDemoJexlTier();

        System.out.println("Press enter to continue.....");
        s.nextLine();
        m.removeTier();

        System.out.println("Press enter to continue.....");
        s.nextLine();
        m.removeTier();

        System.out.println("Press enter to continue.....");
        s.nextLine();
        m.removeTier();

        System.out.println("Press enter to continue.....");
        s.nextLine();
        m.cleanup();
    }

}
