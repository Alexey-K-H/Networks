import java.net.InetAddress;
import java.util.Objects;

public class Neighbour {
    private InetAddress ip;
    private int port;

    private long lastTime;

    private Neighbour alternateNeighbour;

    public Neighbour(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        this.lastTime = 0;
    }


    public Neighbour(InetAddress ip, int port, long lastTime) {
        this.ip = ip;
        this.port = port;
        this.lastTime = lastTime;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Neighbour getAlternateNeighbour() {
        return alternateNeighbour;
    }

    public void setAlternateNeighbour(Neighbour alternateNeighbour) {
        this.alternateNeighbour = alternateNeighbour;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbour neighbour = (Neighbour) o;
        return port == neighbour.port &&
                Objects.equals(ip, neighbour.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
