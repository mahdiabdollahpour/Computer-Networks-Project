
package DistributedCalculator;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private Socket socket;
    private String IP;
    private int port;
    //    private InputStream inputStream;
//    private OutputStream outputStream;
    private DataOutputStream objectOutputStream;
    private DataInputStream objectInputStream;

    public Client(String IP, int port) {
        this.socket = new Socket();
        this.IP = IP;
        this.port = port;
        try {
//            System.out.println("connecting");
            socket.connect(new InetSocketAddress(IP, port), 5000);
//            System.out.println("connected");
            this.objectInputStream = new DataInputStream(socket.getInputStream());

            this.objectOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("client created");
    }

    public void askCalc(String op, int op1, int op2) {
//        System.out.println("wanna send");
        try {
            System.out.println("sending");

            String message = op + "," + Integer.toString(op1) + "," + Integer.toString(op2);
            objectOutputStream.writeInt(message.length());
            objectOutputStream.writeBytes(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeConnection() {
        try {
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
