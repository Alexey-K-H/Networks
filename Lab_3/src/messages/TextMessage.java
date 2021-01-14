package messages;

public class TextMessage extends Message {
    private String name;
    private String content;

    public TextMessage(String content, String messageID, String name) {
        super(MessageType.TEXT, messageID);
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
