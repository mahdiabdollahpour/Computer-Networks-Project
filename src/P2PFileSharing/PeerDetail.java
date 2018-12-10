package P2PFileSharing;

public class PeerDetail {
    private String ip;
    private int port;

    public PeerDetail(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return ip + "," + port ;
    }
}
