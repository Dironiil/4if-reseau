package tp.data;

import java.time.LocalTime;

public class ServerMessage extends Message {

    public ServerMessage(String content, LocalTime timestamp) {
        super("", content, timestamp, Metadata.TEXT);
    }

    @Override
    public String toString() {
        return "[" + getStringTimestamp() + "] " + getContent();
    }
}
