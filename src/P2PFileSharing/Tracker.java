package P2PFileSharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Tracker extends BaseNode implements Runnable {

    private DatagramSocket datagramSocket;
    private int port;
    private byte[] receive = new byte[messageMaxLen];


    public Tracker(int port) {

        this.port = port;
        peerDetails = new ArrayList<>();

        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    public void run() {
        try {
            DatagramPacket DpReceive = null;
            System.out.println("waiting for a message");
            ArrayList<Byte> byteArrayList = new ArrayList<>();
            while (true) {
                DpReceive = new DatagramPacket(receive, receive.length);

                // Step 3 : revieve the data in byte buffer.

                datagramSocket.receive(DpReceive);
                int offset = receive[0];
                if (offset != 0) {

                    for (int i = 1; i < messageMaxLen; i++) {
                        byteArrayList.add(receive[i]);
                    }
                } else {
                    byte[] mess = new byte[byteArrayList.size()];
                    for (int i = 0; i < byteArrayList.size(); i++) {
                        mess[i] = byteArrayList.get(i);
                    }
                    String message = new String(mess);
                    processMessage(null, message, DpReceive.getAddress().getHostName(), DpReceive.getPort());
                    byteArrayList = new ArrayList<>();
                }

                receive = new byte[messageMaxLen];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void processMessage(byte[] mess, String iden, String host_ip, int host_port) {
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
                sendMessage(res.toString().getBytes(), PEER_LIST + "," , host_ip, host_port);

                break;
            default:
                System.out.println("Unknown Message");
                break;

        }
    }


}
