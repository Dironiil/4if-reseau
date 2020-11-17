package tp.data;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {

    private final String user;
    private final String content;
    private final LocalTime timestamp;
    private final Metadata metadata;

    public Message(String user, String content, LocalTime timestamp, Metadata metadata) {
        this.user = user;
        this.content = content;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    public String getUser() {
        return user == null ? "Anonymous" : user;
    }

    public String getContent() {
        return content;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getStringTimestamp() {
        return DateTimeFormatter.ofPattern("H:m:s").format(timestamp);
    }

    @Override
    public String toString() {
        return "<[" + getStringTimestamp() + "] " + getUser() + "> " + getContent();
    }
    
    public enum Metadata {
        TEXT,
        QUIT,
        RENAME
    }
}
