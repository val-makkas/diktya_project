import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isRead;
    private String sender;
    private String receiver;
    private String body;
    private int id;

    public Message(int id, String sender, String receiver, String body) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.isRead = false;
    }

    public int getId() {
        return id;
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
        return !isRead ? "From: " + sender + "*" : "From: " + sender;
    }
}
