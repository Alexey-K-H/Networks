package messages;

public class ConfirmMessage extends Message {

    public ConfirmMessage(String messageID) {
        super(MessageType.CONFIRM, messageID);
    }

}