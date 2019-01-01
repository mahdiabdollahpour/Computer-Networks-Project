package DistributedCalculator;

import java.io.*;
import java.net.Socket;

public class RequestHandler extends Thread {
    private Socket socket;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public RequestHandler(Socket socket) {

        try {
            this.socket = socket;
            InputStream inputStream = socket.getInputStream();
            this.dataInputStream = new DataInputStream(inputStream);
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
    }

    public void run() {
        try {
            while (true) {
                if (dataInputStream.available() > 0) {

                    int len = dataInputStream.readInt();

                    byte[] bytes = new byte[len];
                    boolean flag = true;
                    while (flag) {
                        if (dataInputStream.available() >= len) {
                            int len_read = dataInputStream.read(bytes);
                            flag = false;
                        }
                    }

                    String mess = new String(bytes).toLowerCase();
                    parseRequest(mess);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseRequest(String s) {
        String[] ss = s.split(",");
        double op1 = Double.parseDouble(ss[1]);
        double op2 = 0;
        if (ss.length >= 3) {
            op2 = Double.parseDouble(ss[2]);

        }
        long time = 0;
        double res = 0;
        switch (ss[0]) {
            case "add":
                time = System.currentTimeMillis();
                res = op1 + op2;
                time = System.currentTimeMillis() - time;
                break;
            case "subtract":
                time = System.currentTimeMillis();
                res = op1 - op2;
                time = System.currentTimeMillis() - time;
                break;
            case "divide":
                time = System.currentTimeMillis();
                res = op1 / op2;
                time = System.currentTimeMillis() - time;
                break;
            case "multiply":
                time = System.currentTimeMillis();
                res = op1 * op2;
                time = System.currentTimeMillis() - time;
                break;
            case "sin":
                time = System.currentTimeMillis();
                res = Math.sin(op1);
                time = System.currentTimeMillis() - time;
                break;
            case "cos":
                time = System.currentTimeMillis();
                res = Math.cos(op1);
                time = System.currentTimeMillis() - time;
                break;
            case "tan":
                time = System.currentTimeMillis();
                res = Math.tan(op1);
                time = System.currentTimeMillis() - time;
                break;
            case "cot":
                time = System.currentTimeMillis();
                res = 1 / Math.tan(op1);
                time = System.currentTimeMillis() - time;
                break;
            default:
                break;
        }

        send_result(res, time);
    }

    void send_result(double res, long time) {
        String message = res + "," + time;
        try {
            dataOutputStream.writeInt(message.length());
            dataOutputStream.writeBytes(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
