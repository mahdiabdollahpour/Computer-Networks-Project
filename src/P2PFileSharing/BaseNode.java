package P2PFileSharing;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public abstract class BaseNode {
    DatagramSocket datagramSocket;
    int messageMaxLen = 128;
    ArrayList<PeerDetail> peerDetails;
    int port;
//    protected static final int SEND_PORT = 5000;
//    protected static final int RECEIVE_PORT = 4000;
    protected MulticastSocket multicastSocket;
    protected static final int MULTICAST_PORT = 4446;

    public BaseNode(int port) {
        this.port = port;
        peerDetails = new ArrayList<>();
        try {
            System.out.println("Creating socket");
            datagramSocket = new DatagramSocket(port);
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static final String FILE_FOUND = "file found";
    static final String FILE_DEMAND = "file demand";
    static final String FILE_NOT_FOUND = "file not found";
    static final String FILE_REQUEST = "file request";
    static final String FILE_RESPONSE = "file response";
    static final String PEER_LIST = "peer list";

    void sendMessage(byte[] bytes, String iden, String dest_ip, int dest_port) {
//        System.out.println("sending message " + iden);
        try {
            if (bytes != null) {

                int parts_num = (int) Math.ceil(bytes.length * 1.0 / (messageMaxLen - 1));
//                System.out.println(parts_num + "," + bytes.length);
                for (int i = 0; i < parts_num; i++) {
                    byte[] chunk = new byte[messageMaxLen];
                    chunk[0] = (byte) (parts_num - i);
                    for (int i1 = 1; i1 < chunk.length && parts_num * i + (i1 - 1) < bytes.length; i1++) {
                        chunk[i1] =
                                bytes[parts_num * i + (i1 - 1)];
                    }
                    DatagramPacket datagramPacket = new DatagramPacket(chunk, chunk.length, InetAddress.getByName(dest_ip), dest_port);
                    datagramSocket.send(datagramPacket);
                }
            }
            byte[] iden_bytes = iden.getBytes();
            byte[] mess_bytes = new byte[messageMaxLen];
            mess_bytes[0] = 0;
            for (int i = 1; i <= iden_bytes.length; i++) {
                mess_bytes[i] = iden_bytes[i - 1];
            }
            DatagramPacket datagramPacket = new DatagramPacket(mess_bytes, mess_bytes.length, InetAddress.getByName(dest_ip), dest_port);
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract void processMessage(byte[] mess, String iden, String host_ip, int host_port);

    static StringBuilder textOutofBytes(byte[] a) {
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
