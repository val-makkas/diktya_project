import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isRead;
    private String sender;
    private String receiver;
    private String body;

    public Message(String sender, String receiver, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.isRead = false;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markRead() {
        this.isRead = true;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return isRead ? "From: " + sender + ", Message: " + body + "*" : "From: " + sender + ", Message: " + body;
    }
}
