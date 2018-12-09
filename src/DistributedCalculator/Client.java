
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
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public Client(String IP, int port) {
        this.socket = new Socket();
        this.IP = IP;
        this.port = port;
        try {
//            System.out.println("connecting");
            socket.connect(new InetSocketAddress(IP, port), 5000);
//            System.out.println("connected");
            this.dataInputStream = new DataInputStream(socket.getInputStream());

            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("client created");
    }

    public void askCalc(String op, double op1, double op2) {
//        System.out.println("wanna send");
        try {
//            System.out.println("sending");

            String message = op + "," + Double.toString(op1) + "," + Double.toString(op2);
            int lenn = message.getBytes().length;
            System.out.println("client message len:" + lenn);
            System.out.println("client req :" + message);
            dataOutputStream.writeInt(lenn);
            dataOutputStream.writeBytes(message);
            while (dataInputStream.available() <= 0) {
                int len = dataInputStream.readInt();
                byte[] bytes = new byte[len];
                boolean flag = true;
                while (flag) {
                    if (dataInputStream.available() >= len) {
                        dataInputStream.read(bytes);
                        flag = false;
                    }
                }

                String result = new String(bytes);
                System.out.println(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeConnection() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
