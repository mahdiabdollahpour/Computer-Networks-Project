package DistributedCalculator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            waitForConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitForConnection() {
        try {
            while (true) {
                System.out.println("server is listening");
                Socket socket = serverSocket.accept();
                System.out.println("Got a Connection");

                Handler handler = new Handler(socket);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
