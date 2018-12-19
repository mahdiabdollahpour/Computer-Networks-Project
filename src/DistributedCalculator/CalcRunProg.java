package DistributedCalculator;


import java.util.Scanner;

public class CalcRunProg {

    public static void main(String[] args) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Server server = new Server(5000);
//                server.waitForConnection();
            }
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
            String op = in.next();
            int a = in.nextInt();
            int b = in.nextInt();
            client.askCalc(op, a, b);
        }
//            client.askCalc("ADD", 10,23);
//            client.askCalc("ADD", 11,53);

    }

}
