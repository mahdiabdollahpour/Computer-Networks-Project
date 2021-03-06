package Part2Codes;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class Peer implements Runnable {
    private Thread broadcastThread;
    private ArrayList<File> files;

    private ArrayList<FileSearch> fileSearches;
    private String name;

    private static class RecievingData {
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        int port;
        String ip;

        public RecievingData(ArrayList<Byte> byteArrayList, String ip, int port) {
            this.byteArrayList = byteArrayList;
            this.ip = ip;
            this.port = port;
        }
    }

    private static class File {
        private String name;
        private String addr;

        public File(String name, String addr) {
            this.name = name;
            this.addr = addr;

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

    private static class FileSearch {
        int idx;
        String fileName;

        public FileSearch(String fileName) {
            this.idx = -1;
            this.fileName = fileName;
        }
    }

    private DatagramSocket datagramSocket;
    private int messageMaxLen = 14000;
    private int port;

    private MulticastSocket multicastSocket;
    private static final int MULTICAST_PORT = 4446;
    private String lookingForAFile = null;

    static final char FILE_FOUND = 'a';
    static final char FILE_REQUEST = 'b';
    static final char FILE_LOOKUP = 'c';
    static final char FILE_RESPONSE = 'd';


    public Peer(String name, int peerPort) {

        this.port = peerPort;
        try {
            System.out.println("Creating socket");
            datagramSocket = new DatagramSocket(port);
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.name = name;

        fileSearches = new ArrayList<>();
        files = new ArrayList<>();
        broadcastThread = new Thread(() -> {
            try {
                byte[] buf = new byte[messageMaxLen];
                InetAddress group = null;
                group = InetAddress.getByName("230.0.0.0");
                multicastSocket.joinGroup(group);
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    multicastSocket.receive(packet);
                    processMessage(null, textOutofBytes(buf).toString(), packet.getAddress().getHostName(), packet.getPort());

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        broadcastThread.start();


        new Thread(this).start();
    }

    public void serveFile(String fname, String addr) {
//        System.out.println("fghjk");
        files.add(new File(fname, addr));
        System.out.println(name + " : File added");
    }

    public void requestFile(String fname) {
        FileSearch fileSearch = new FileSearch(fname);


        fileSearches.add(fileSearch);
        lookingForAFile = fname;
        byte[] data = (FILE_LOOKUP + "," + fileSearch.fileName).getBytes();
        try {
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName("230.0.0.0"), MULTICAST_PORT);
            datagramSocket.send(datagramPacket);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(name + " : request message sent");
//        sendMessage(null, , "230.0.0.0", MULTICAST_PORT);

    }

    public void run() {
        byte[] receive = new byte[messageMaxLen];
        ArrayList<RecievingData> recievingData = new ArrayList<>();
//        ArrayList<Byte> byteArrayList = new ArrayList<>();
        try {

            while (true) {

                DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);


                ArrayList<Byte> byteArrayList = null;
                datagramSocket.receive(DpReceive);
                String host = DpReceive.getAddress().getHostName();
                int sourcePort = DpReceive.getPort();
                boolean found = false;
                int idx = 0;
                for (int i = 0; i < recievingData.size(); i++) {
                    RecievingData rd = recievingData.get(i);
                    if (rd.ip.equals(host) && rd.port == sourcePort) {
                        byteArrayList = recievingData.get(i).byteArrayList;
                        found = true;
                        idx = i;
                        break;
                    }
                }
                if (!found) {
                    recievingData.add(new RecievingData(new ArrayList<>(), host, sourcePort));
                    idx = recievingData.size() - 1;
                    byteArrayList = recievingData.get(idx).byteArrayList;
                }
                byte[] offarr = new byte[4];
                byte[] lenarr = new byte[4];
                for (int i = 0; i < 4; i++) {
                    offarr[i] = receive[i];
                }
                for (int i = 4; i < 8; i++) {
                    lenarr[i - 4] = receive[i];
                }
                int offset = new BigInteger(offarr).intValue();
                int lenset = new BigInteger(lenarr).intValue();
                System.out.println("offset : " + offset);
                System.out.println("len : " + lenset);

                if (offset != 0) {

                    for (int i = 8; i < lenset; i++) {
                        byteArrayList.add(receive[i]);
                    }

                } else {   //offset  = 0
                    byte[] mess = new byte[byteArrayList.size()];
                    for (int i = 0; i < byteArrayList.size(); i++) {
                        mess[i] = byteArrayList.get(i);
                    }
                    byte[] iden_bytes = new byte[receive.length - 4];
                    for (int i = 4; i < receive.length; i++) {
                        iden_bytes[i - 4] = receive[i];
                    }
                    System.out.println("iden :" + textOutofBytes(iden_bytes));
                    processMessage(mess, textOutofBytes(iden_bytes).toString(), host, sourcePort);
                    recievingData.remove(idx);

                }

                receive = new byte[messageMaxLen];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    void processMessage(byte[] mess, String iden, String host_ip, int host_port) {
        System.out.println(name + " : " + iden);
        String[] ss = iden.split(",");
        if (host_port == datagramSocket.getPort()) {
            return;
        }
        switch (ss[0].charAt(0)) {
            case FILE_FOUND:
                if (lookingForAFile != null) {
                    sendMessage(null, FILE_REQUEST + "," + lookingForAFile, host_ip, host_port);
                    lookingForAFile = null;
                }
                break;
            case FILE_REQUEST:
                String fileName = ss[1];

                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    if (f.name.equals(fileName)) {
                        sendMessage(f.getFile(), FILE_RESPONSE + "," + f.name + "," + f.addr, host_ip, host_port);
                        break;
                    }
                }
                break;
            case FILE_LOOKUP:
                fileName = ss[1];
                System.out.println(name + " : received request file name : " + fileName);
                boolean found = false;
                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    if (f.name.equals(fileName)) {
                        System.out.println(name + " : sending found");
                        sendMessage(null, FILE_FOUND + "", host_ip, host_port);
                        break;
                    }
                }

                break;

            case FILE_RESPONSE:
                System.out.println(name + " : There is a Response :" + iden);
//                byte[] fileinB = textOutofBytes(mess).toString().getBytes();
//                System.out.println();
                byte[] fileinB = mess;
                System.out.println("file in B" + fileinB.length);
//                System.out.println("file in mess" + mess.length);
                files.add(new File(ss[1], ss[2], fileinB));
                Iterator<FileSearch> iterable = fileSearches.iterator();
                while (iterable.hasNext()) {
                    FileSearch fileSearch = iterable.next();
                    if (fileSearch.fileName.equals(ss[1])) {
                        fileSearches.remove(fileSearch);
                        break;
                    }

                }
                break;

            default:
                System.out.println(name + "Unknown Message Identifier");
                break;
        }
    }


    void sendMessage(byte[] bytes, String iden, String dest_ip, int dest_port) {
        try {
            if (bytes != null) {
                System.out.println("to send : " + bytes.length);
                int parts_num = (int) Math.ceil((double)( bytes.length * 1.0) / (double)(messageMaxLen - 8));
                System.out.println(parts_num);
//                System.out.println(parts_num + "," + bytes.length);
                int sent = 0;
                for (int i = 0; i < parts_num; i++) {
                    byte[] partbyte = ByteBuffer.allocate(4).putInt(parts_num - i).array();
                    byte[] chunk = new byte[Math.min(messageMaxLen, bytes.length - sent)];
                    byte[] lenbyte = ByteBuffer.allocate(4).putInt(chunk.length).array();
                    for (int j = 0; j < 4; j++) {
                        chunk[j] = partbyte[j];
                    }
                    System.out.println("chunk size :" + chunk.length);
//                    System.out.println("lenByte size :" + lenbyte.length);
                    for (int j = 4; j < 8; j++) {

                        chunk[j] =
                                lenbyte[j - 4];
                    }
                    //&& messageMaxLen * i + (i1 - 1) < bytes.length
                    for (int i1 = 8; i1 < chunk.length; i1++) {
                        chunk[i1] =
                                bytes[sent + (i1 - 8)];
                    }
                    sent += chunk.length - 8;
                    DatagramPacket datagramPacket = new DatagramPacket(chunk, chunk.length, InetAddress.getByName(dest_ip), dest_port);
                    datagramSocket.send(datagramPacket);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("sent amount : " + sent);
            }
            byte[] iden_bytes = iden.getBytes();
            byte[] mess_bytes = new byte[4 + iden_bytes.length];
            byte[] partbyte = ByteBuffer.allocate(4).putInt(0).array();
            for (int j = 0; j < 4; j++) {
                mess_bytes[j] = partbyte[j];
            }

            for (int i = 4; i < mess_bytes.length; i++) {
                mess_bytes[i] = iden_bytes[i - 4];
            }
            DatagramPacket datagramPacket = new DatagramPacket(mess_bytes, mess_bytes.length, InetAddress.getByName(dest_ip), dest_port);
            datagramSocket.send(datagramPacket);
            System.out.println("last part sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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
