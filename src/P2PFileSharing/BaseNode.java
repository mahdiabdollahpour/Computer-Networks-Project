package P2PFileSharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public abstract class BaseNode {
     DatagramSocket datagramSocket;
     int messageMaxLen = 128;
     ArrayList<PeerDetail> peerDetails;

     static final String FILE_NOT_FOUND = "file not found";
     static final String FILE_REQUEST = "file request";
     static final String FILE_RESPONSE = "file response";
     static final String PEER_LIST = "peer list";

     void sendMessage(byte[] bytes, String iden, String dest_ip, int dest_port) {
        try {
            if (bytes != null) {
                int parts_num = bytes.length / (messageMaxLen - 1);
                for (int i = 0; i < parts_num; i++) {
                    byte[] chunk = new byte[messageMaxLen];
                    chunk[0] = (byte) (parts_num - (i));
                    for (int i1 = 1; i1 < chunk.length; i1++) {
                        chunk[i1] = bytes[parts_num * i + i1];
                    }
                    DatagramPacket datagramPacket = new DatagramPacket(chunk, chunk.length, InetAddress.getByName(dest_ip), dest_port);
                    datagramSocket.send(datagramPacket);
                }
            }
            byte[] iden_bytes = iden.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(iden_bytes, iden_bytes.length, InetAddress.getByName(dest_ip), dest_port);
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    abstract void processMessage(byte[] mess, String iden, String host_ip, int host_port);
    static StringBuilder data(byte[] a) {
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
