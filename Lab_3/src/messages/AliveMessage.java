package messages;

public class AliveMessage extends Message {
    public AliveMessage(String messageID) {
        super(MessageType.ALIVE, messageID);
    }

}
