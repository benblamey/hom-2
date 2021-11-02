package benblamey;

public class Hello {

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        String all_args = String.join(" ", args);
        while (true) {
            System.out.println(i++ + " Hello! args:" + all_args);
            Thread.sleep(5000);
        }
    }
}
