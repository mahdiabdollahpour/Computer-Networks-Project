import DistributedCalculator.Client;
import DistributedCalculator.Server;

public class Main {

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
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("creating client");
                Client client = new Client("127.0.0.1", 5000);
                System.out.println("here");
                client.askCalc("ADD", 32, 56);

            }
        });
        t2.start();
    }
}
