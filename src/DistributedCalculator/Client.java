
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

    public Client(String IP, int serverPort) {
        this.socket = new Socket();
        this.IP = IP;
        this.port = serverPort;
        try {
            socket.connect(new InetSocketAddress(IP, serverPort), 5000);
            this.dataInputStream = new DataInputStream(socket.getInputStream());

            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void operateVariables(String op, double op1, double op2) {
        try {
            String message = op + "," + Double.toString(op1) + "," + Double.toString(op2);
            int lenn = message.getBytes().length;
            dataOutputStream.writeInt(lenn);
            dataOutputStream.writeBytes(message);
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
            System.out.println("RESULT : " + result);
//            }
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
