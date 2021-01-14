package messages;

import java.net.InetAddress;

public class AlternateMessage extends Message {
    private InetAddress ip;
    private int port;

    public AlternateMessage(String messageID, InetAddress ip, int port) {
        super(MessageType.ALTERNATE, messageID);
        this.ip = ip;
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
