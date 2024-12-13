import java.util.ArrayList;
import java.util.List;

public class Account {
    private String username;
    private int authToken;
    private List<Message> messageBox;

    public Account(String username, int authToken) {
        this.username = username;
        this.authToken = authToken;
        this.messageBox = new ArrayList<Message>();
    }

    public String getUsername() {
        return username;
    }

    public int getAuthToken() {
        return authToken;
    }

    public List<Message> getMessageBox() {
        return messageBox;
    }

    public void addMessage(Message message) {
        messageBox.add(message);
    }

    public void removeMessage(Message message) {
        messageBox.remove(message);
    }
}
