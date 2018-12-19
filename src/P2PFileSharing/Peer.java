package P2PFileSharing;

import java.io.*;
import java.net.DatagramPacket;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class Peer extends BaseNode implements Runnable {
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

    private static class FileSearch {
        int idx;
        String fileName;

        public FileSearch(String fileName) {
            this.idx = -1;
            this.fileName = fileName;
        }
    }
//
//    private void askNextForFile(FileSearch fileSearch) {
//        fileSearch.idx++;
//        if (fileSearch.idx >= peerDetails.size()) {
//            System.out.println("No one has " + fileSearch.fileName);
//            return;
//        }
//        PeerDetail peerDetail = peerDetails.get(fileSearch.idx);
//        sendMessage(null, FILE_REQUEST + "," + fileSearch.fileName, peerDetail.getIp(), peerDetail.getPort());
//    }


    private String trackerIP;
    private int trackerPort;

    public Peer(String name, String trackerIP, int trackerPort, int peerPort) {
        super(peerPort);
        this.name = name;
        this.trackerIP = trackerIP;
        this.trackerPort = trackerPort;
        fileSearches = new ArrayList<>();
        files = new ArrayList<>();
        broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        broadcastThread.start();

//        getPeersList();
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
        byte[] data = (FILE_REQUEST + "," + fileSearch.fileName).getBytes();
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
                String host = DpReceive.getAddress().getHostAddress();
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
                    byteArrayList = new ArrayList<>();
                    recievingData.add(new RecievingData(byteArrayList, host, sourcePort));
                    idx = recievingData.size() - 1;
                }

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
                    byte[] iden_bytes = new byte[messageMaxLen];
                    for (int i = 0; i < receive.length - 1; i++) {
                        iden_bytes[i] = receive[i + 1];
                    }
                    processMessage(mess, textOutofBytes(iden_bytes).toString(), host, sourcePort);
                    recievingData.remove(idx);

                }

                receive = new byte[messageMaxLen];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String lookingForAFile = null;

    void processMessage(byte[] mess, String iden, String host_ip, int host_port) {
        System.out.println(name + " : " + iden);
//        iden = iden.toLowerCase();
        String[] ss = iden.split(",");
//        System.out.println("IDEN " + ss[0]);

        switch (ss[0]) {
            case FILE_FOUND:
                if (lookingForAFile != null) {
                    sendMessage(null, FILE_DEMAND + "," + lookingForAFile, host_ip, host_port);
                    lookingForAFile = null;
                }
                break;
            case FILE_DEMAND:
                String fileName = ss[1];

                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    if (f.name.equals(fileName)) {
                        sendMessage(f.getFile(), FILE_RESPONSE + "," + f.name + "," + f.addr, host_ip, host_port);
                        break;
                    }
                }
                break;
            case FILE_REQUEST:
                fileName = ss[1];
                System.out.println(name + " : received request file name : " + fileName);
                boolean found = false;
                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    if (f.name.equals(fileName)) {
//                        System.out.println(f.name + " =?=" + fileName);
                        System.out.println(name + " : sending found");
                        sendMessage(null, FILE_FOUND, host_ip, host_port);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    sendMessage(null, FILE_NOT_FOUND + "," + fileName, host_ip, host_port);
                }
                break;
            case FILE_NOT_FOUND:
//                System.out.println("File not found form a peer");
//                for (int i = 0; i < fileSearches.size(); i++) {
//                    if (fileSearches.get(i).fileName.equals(ss[1])) {
//                        askNextForFile(fileSearches.get(i));
//                    }
//                }
                break;
            case FILE_RESPONSE:
                System.out.println(name + " : There is a Response :" + iden);
                files.add(new File(ss[1], ss[2], textOutofBytes(mess).toString().getBytes()));
                Iterator<FileSearch> iterable = fileSearches.iterator();
                while (iterable.hasNext()) {
                    FileSearch fileSearch = iterable.next();
                    if (fileSearch.fileName.equals(ss[1])) {
                        fileSearches.remove(fileSearch);
                        break;
                    }

                }
                break;
            case PEER_LIST:
//                System.out.println("hiiiiiiiiiiiiiiii");
                String list = new String(textOutofBytes(mess).toString().getBytes());
                System.out.println(list);
                peerDetails = parsePeerList(list);
                break;
            default:
                System.out.println(name + "Unknown Message Identifier");
                break;
        }
    }

    public void getPeersList() {
//        byte buf[] = "list".getBytes();
        sendMessage(null, PEER_LIST, trackerIP, trackerPort);


    }

    private ArrayList<PeerDetail> parsePeerList(String list) {

        String[] ss = list.split(";");
        ArrayList<PeerDetail> peerDetailArrayList = new ArrayList<>();
        for (int i = 0; i < ss.length; i++) {
            String[] sss = ss[i].split(",");
            peerDetailArrayList.add(new PeerDetail(sss[0], Integer.parseInt(sss[1])));
        }
        return peerDetailArrayList;

    }

}
