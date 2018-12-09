package DistributedCalculator;


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
        client.askCalc("ADD", 32, 56);
    }
}
