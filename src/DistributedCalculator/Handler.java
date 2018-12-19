package DistributedCalculator;

import java.io.*;
import java.net.Socket;

public class Handler extends Thread {
    private Socket socket;
    //    private OutputStream outputStream;
//    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public Handler(Socket socket) {
//        super("Handler Thread");
        System.out.println("Handler creating");
        try {
            System.out.println("Streams stablishing");
            this.socket = socket;
//            this.outputStream = socket.getOutputStream();
//            this.inputStream = socket.getInputStream();
            InputStream inputStream = socket.getInputStream();

            this.dataInputStream = new DataInputStream(inputStream);
//            System.out.println("jflkjdfkl");
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            System.out.println("stream stabloished");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
    }

    public void run() {
//        System.out.println("handler started");
        try {
            while (true) {
                if (dataInputStream.available() > 0) {
//                    int len = inputStream.read();
//                    System.out.println("Len : " + len);
                    int len = dataInputStream.readInt();
                    System.out.println("handler recieved message with len: " + len);
                    byte[] bytes = new byte[len];
                    boolean flag = true;
                    while (flag) {
                        if (dataInputStream.available() >= len) {
                            int len_read = dataInputStream.read(bytes);
//                            System.out.println("len read : " + len_read);
                            flag = false;
                        }
                    }

                    String mess = new String(bytes).toLowerCase();
//                    System.out.println("handler :" + mess);
                    compute(mess);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compute(String s) {
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
//        System.out.println("answer computed :" + res + " , " + time);
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
}
