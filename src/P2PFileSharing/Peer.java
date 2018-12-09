package P2PFileSharing;

import java.io.*;
import java.util.ArrayList;

public class Peer {
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

    public void serveFile(String name, String addr) {
        files.add(new File(name, addr));
    }

    public void receiveFile(String name) {

    }
}
