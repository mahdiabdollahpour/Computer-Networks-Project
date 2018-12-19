package P2PFileSharing;

public class Main {


    public static void main(String[] args) {
        try {
            Thread t = new Thread(() -> {
                Tracker tracker = new Tracker(5000);
//                tracker.start();
            });

            t.start();
            Thread.sleep(3000);
            Peer peer = new Peer("P1", "127.0.0.1", 5000, 4000);
            peer.getPeersList();
//        System.out.println("aaaaaaaaaaaa");
            peer.serveFile("data2.txt", "data2.txt");
            peer.serveFile("data.txt", "data.txt");

            Peer peer2 = new Peer("P2", "127.0.0.1", 5000, 3000);
            peer2.getPeersList();
            Thread.sleep(3000);
            peer2.requestFile("data.txt");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
