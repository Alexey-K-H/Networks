package messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
    private MessageType type;
    private String messageID;

    public Message(MessageType type, String messageID) {
        this.type = type;
        this.messageID = messageID;
    }

    public MessageType getType() {
        return type;
    }

    public String getMessageID() {
        return messageID;
    }
}
