package Part1Codes;


import java.util.Scanner;

public class Part1 {

    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            new Server(5000);
        });
        t.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Client client = new Client("127.0.0.1", 5000);

        Scanner in = new Scanner(System.in);
        while (true) {
            String op = in.next().toLowerCase();
            int a = in.nextInt();
            int b = 0;
            if (!(op.equals("sin") || op.equals("cos") || op.equals("tan") || op.equals("cot"))) {
                b = in.nextInt();

            }
            client.operateVariables(op, a, b);
        }



    }

}
