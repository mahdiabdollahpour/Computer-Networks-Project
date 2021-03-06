package Part1Codes;

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
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("server is listening");
                Socket socket = serverSocket.accept();
                System.out.println("Got a Connection");

                RequestHandler handler = new RequestHandler(socket);


            }
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection(){

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
