package P2PFileSharing;

import java.util.Objects;

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

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerDetail that = (PeerDetail) o;
        return port == that.port &&
                Objects.equals(ip, that.ip);
    }


}
