package messages;


public class ConnectMessage extends Message{

    public ConnectMessage(String messageID) {
        super(MessageType.CONNECT, messageID);
    }
}
