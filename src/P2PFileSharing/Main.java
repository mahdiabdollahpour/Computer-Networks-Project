package P2PFileSharing;

import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Peer peer = new Peer("P1",n );

        while (true) {
            String command = in.nextLine();
            if(command.length() <= 1){
                continue;
            }
            String[] ss = command.split(" ");
            if (ss[1].equals("-receive")) {
                peer.requestFile(ss[3]);
            } else if (ss[1].equals("-serve")) {
                peer.serveFile(ss[3], ss[5]);
            }
        }


    }
}
