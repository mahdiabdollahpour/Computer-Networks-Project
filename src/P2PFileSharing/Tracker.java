package P2PFileSharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Tracker extends Thread {
    private ArrayList<PeerDetail> peerDetails;

    private DatagramSocket datagramSocket;
    private int port;

    private int messageMaxLen = 128;
    private byte[] receive = new byte[messageMaxLen];

    public Tracker(int port) {
        this.port = port;
        peerDetails = new ArrayList<>();

        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.start();
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
                if (offset != -1) {

                    for (int i = 1; i < messageMaxLen; i++) {
                        byteArrayList.add(receive[i]);
                    }
                } else {
                    byte[] mess = new byte[byteArrayList.size()];
                    for (int i = 0; i < byteArrayList.size(); i++) {
                        mess[i] = byteArrayList.get(i);
                    }
                    String message = new String(mess);
                    processMessage(message);
                    byteArrayList = new ArrayList<>();
                }

                receive = new byte[messageMaxLen];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void processMessage(String message) {
        String[] ss = message.split(",");
        switch (ss[0]) {

            case "list":
                StringBuilder res = new StringBuilder("");
                for (int i = 0; i < peerDetails.size(); i++) {
                    res.append(peerDetails.get(i).toString());
                    res.append(",");
                }
                break;


        }
    }

    private static StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }


}
