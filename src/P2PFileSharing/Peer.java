package P2PFileSharing;

import java.io.*;
import java.net.DatagramPacket;

import java.util.ArrayList;
import java.util.Iterator;

public class Peer extends BaseNode implements Runnable {

    private ArrayList<File> files;

    private ArrayList<FileSearch> fileSearches;

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

    private void askNextForFile(FileSearch fileSearch) {
        fileSearch.idx++;
        if (fileSearch.idx >= peerDetails.size()) {
            System.out.println("No one has " + fileSearch.fileName);
            return;
        }
        PeerDetail peerDetail = peerDetails.get(fileSearch.idx);
        sendMessage(null, FILE_REQUEST + "," + fileSearch.fileName, peerDetail.getIp(), peerDetail.getPort());
    }


    private String trackerIP;
    private int trackerPort;

    public Peer(String trackerIP, int trackerPort, int peerPort) {
        super(peerPort);
        this.trackerIP = trackerIP;
        this.trackerPort = trackerPort;
        fileSearches = new ArrayList<>();
        files = new ArrayList<>();

//        getPeersList();
        new Thread(this).start();
    }

    public void serveFile(String name, String addr) {
//        System.out.println("fghjk");
        files.add(new File(name, addr));
        System.out.println("File added");
    }

    public void requestFile(String name) {
        FileSearch fileSearch = new FileSearch(name);
        fileSearches.add(fileSearch);
        askNextForFile(fileSearch);
    }

    public void run() {
        byte[] receive = new byte[messageMaxLen];
        ArrayList<RecievingData> recievingData = new ArrayList<>();
//        ArrayList<Byte> byteArrayList = new ArrayList<>();
        try {
            while (true) {

                DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);

                // Step 3 : revieve the textOutofBytes in byte buffer.
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

    void processMessage(byte[] mess, String iden, String host_ip, int host_port) {
        System.out.println("Peer : " + iden);
//        iden = iden.toLowerCase();
        String[] ss = iden.split(",");

        switch (ss[0]) {
            case FILE_REQUEST:
                String fileName = ss[1];
                boolean found = false;
                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    if (f.name.equals(fileName)) {
                        sendMessage(f.getFile(), FILE_RESPONSE + "," + f.name + "," + f.addr, host_ip, host_port);
                    }
                    found = true;
                    break;
                }
                if (!found) {
                    sendMessage(null, FILE_NOT_FOUND + "," + fileName, host_ip, host_port);
                }
                break;
            case FILE_NOT_FOUND:
                for (int i = 0; i < fileSearches.size(); i++) {
                    if (fileSearches.get(i).fileName.equals(ss[1])) {
                        askNextForFile(fileSearches.get(i));
                    }
                }
                break;
            case FILE_RESPONSE:
                System.out.println("There is a Response");
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
                System.out.println("Unknown Message Identifier");
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
