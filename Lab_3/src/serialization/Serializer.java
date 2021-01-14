package serialization;

import messages.Message;
import java.io.*;

public class Serializer {
    public static byte[] serialize(Message message) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static Message deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                Message msg = (Message) objectInputStream.readObject();
                objectInputStream.close();
                return msg;
            }
        }
    }
}
