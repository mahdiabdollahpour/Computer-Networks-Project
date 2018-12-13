package P2PFileSharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Tracker extends BaseNode implements Runnable {


    private byte[] receive = new byte[messageMaxLen];


    public Tracker(int port) {
        super(port);
        new Thread(this).start();
    }

    public void run() {
        try {
            DatagramPacket DpReceive = null;
            System.out.println("waiting for a message");
            ArrayList<Byte> byteArrayList = new ArrayList<>();
            while (true) {
                DpReceive = new DatagramPacket(receive, receive.length);

                // Step 3 : revieve the textOutofBytes in byte buffer.

                datagramSocket.receive(DpReceive);
//                System.out.println("ghjk");
                int offset = receive[0];

                for (int i = 1; i < messageMaxLen; i++) {
                    byteArrayList.add(receive[i]);
                }
                byte[] mess = new byte[byteArrayList.size()];
                for (int i = 0; i < byteArrayList.size(); i++) {
                    mess[i] = byteArrayList.get(i);
                }
                String message = textOutofBytes(mess).toString();
                processMessage(null, message, DpReceive.getAddress().getHostName(), DpReceive.getPort());
                byteArrayList = new ArrayList<>();

                receive = new byte[messageMaxLen];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void processMessage(byte[] mess, String iden, String host_ip, int host_port) {
//        System.out.println(iden);
        String[] ss = iden.split(",");

        switch (ss[0]) {


            case PEER_LIST:
                PeerDetail peerDetail = new PeerDetail(host_ip, host_port);
                if (!peerDetails.contains(peerDetail)) {
                    peerDetails.add(peerDetail);
                }
                StringBuilder res = new StringBuilder("");
                for (int i = 0; i < peerDetails.size(); i++) {
                    res.append(peerDetails.get(i).toString());
                    res.append(";");
                }
                sendMessage(res.toString().getBytes(), PEER_LIST , host_ip, host_port);

                break;
            default:
                System.out.println("Unknown Message");
                break;

        }
    }


}
