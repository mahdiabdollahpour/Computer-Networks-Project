package Part2Codes;

import java.util.Scanner;

public class Part2 {


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
//        System.out.println((char)0);
        int n = in.nextInt();
        Peer peer = new Peer("P1",n );

        while (true) {
            String command = in.nextLine();
            if(command.length() <= 1){
                continue;
            }
            String[] ss = command.split(" ");
            if (ss[1].equals("-receive")) {
                peer.requestFile(ss[2]);
            } else if (ss[1].equals("-serve")) {
                peer.serveFile(ss[3], ss[5]);
            }
        }


    }
}
