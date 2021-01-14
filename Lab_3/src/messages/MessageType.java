package messages;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {

    CONFIRM("confirm"),
    TEXT("text"),
    CONNECT("connect"),
    ALIVE("alive"),
    ALTERNATE("alternate");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private static final Map<String, MessageType> values = new HashMap<>();

    static
    {
        for(MessageType messageType : MessageType.values())
        {
            values.put(messageType.getType(), messageType);
        }
    }
}
