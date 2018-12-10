package P2PFileSharing;

public class Main {


    public static void main(String[] args) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Tracker tracker = new Tracker(5000);
//                tracker.start();
            }
        });
        t.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Peer peer = new Peer("127.0.0.1",5000);
        peer.getPeersList();


    }
}
