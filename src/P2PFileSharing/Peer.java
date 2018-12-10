package P2PFileSharing;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Peer {

    private DatagramSocket datagramSocket ;
    private ArrayList<File> files = new ArrayList<>();

    private static class File {
        private String name;
        private String addr;

        public File(String name, String addr) {
            this.name = name;
            this.addr = addr;
            //TODO:check if file really exists
        }

        public File(String name, String addr, byte[] bytes) {
            this.name = name;
            this.addr = addr;
            storeFile(bytes);
        }

        public byte[] getFile() {
            //auto closeable
            try (FileInputStream fileInputStream = new FileInputStream(new java.io.File(addr))) {
                int len = fileInputStream.available();
                byte[] bytes = new byte[len];
                for (int i = 0; i < len; i++) {
                    bytes[i] = (byte) fileInputStream.read();
                }
                return bytes;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void storeFile(byte[] bytes) {
            // auto closeable
            try (FileOutputStream fileOutputStream = new FileOutputStream(new java.io.File(addr))) {

                fileOutputStream.write(bytes);
                fileOutputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private String trackerIP;
    private int trackerPort;

    public Peer(String trackerIP, int trackerPort) {
        this.trackerIP = trackerIP;
        this.trackerPort = trackerPort;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void serveFile(String name, String addr) {
        files.add(new File(name, addr));
    }

    public void receiveFile(String name) {

    }

    public void getPeersList() {
        byte buf[] = "list".getBytes();
        try {
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length,InetAddress.getByName(trackerIP) , trackerPort);
            datagramSocket.send(datagramPacket);
            System.out.println("message sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
