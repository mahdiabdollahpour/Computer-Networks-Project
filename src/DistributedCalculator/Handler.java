package DistributedCalculator;

import java.io.*;
import java.net.Socket;

public class Handler extends Thread {
    private Socket socket;
    //    private OutputStream outputStream;
//    private InputStream inputStream;
    private DataInputStream objectInputStream;
    private DataOutputStream objectOutputStream;

    public Handler(Socket socket) {
//        super("Handler Thread");
        System.out.println("Handler creating");
        try {
            System.out.println("Streams stablishing");
            this.socket = socket;
//            this.outputStream = socket.getOutputStream();
//            this.inputStream = socket.getInputStream();
            InputStream inputStream = socket.getInputStream();

            this.objectInputStream = new DataInputStream(inputStream);
//            System.out.println("jflkjdfkl");
            this.objectOutputStream = new DataOutputStream(socket.getOutputStream());
//            System.out.println("stream stabloished");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
//        System.out.println("handler started");
        try {
            while (true) {
                if (objectInputStream.available() > 0) {
//                    int len = inputStream.read();
//                    System.out.println("Len : " + len);
                    int len = objectInputStream.readInt();
                    byte[] bytes = new byte[len];
                    objectInputStream.read(bytes);
                    System.out.println(new String(bytes));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
