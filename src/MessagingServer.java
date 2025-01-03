import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MessagingServer {
    private static final Map<Integer, Account> accounts = new HashMap<>();
    private static final Random random = new Random();
    private static final AtomicInteger messageIdCounter = new AtomicInteger(0); // Atomic counter for generating unique message IDs

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java MessagingServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is running on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    private record ClientHandler(Socket socket) implements Runnable {
        @Override
            public void run() {
                try (
                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())
                ) {
                    String command = input.readUTF();
                    switch (command) {
                        case "1": // Create account
                            String username = input.readUTF();
                            int authToken = createAccount(username);
                            if (authToken > 0) {
                                output.writeUTF(String.valueOf(authToken));
                                System.out.println("Username created: " + username);
                            } else if (authToken == -1) {
                                output.writeUTF("Sorry, the user already exists");
                                System.out.println("ERROR: User couldn't be created because username already exists.");
                            } else {
                                output.writeUTF("Invalid Username");
                                System.out.println("ERROR: Invalid Username");
                            }
                            break;
                        case "2": // Show accounts
                            int clientToken = Integer.parseInt(input.readUTF());
                            if (isValidToken(clientToken)) {
                                List<String> usernames = getAllUsernames();
                                output.writeObject(usernames);
                                System.out.println("Sent all usernames to client.");
                            } else {
                                output.writeUTF("Invalid Token.");
                            }
                            break;
                        case "3": // Send message
                            int senderToken = input.readInt();
                            String receiver = input.readUTF();
                            String messageBody = input.readUTF();
                            String sendResult = sendMessage(senderToken, receiver, messageBody);
                            output.writeUTF(sendResult);
                            break;
                        case "4": // Show inbox
                            int token = input.readInt();
                            List<Message> messages = showInbox(token);
                            output.writeObject(messages);
                            break;
                        case "5": // Read message
                            int readToken = input.readInt();
                            int messageId = input.readInt();
                            readMessage(readToken, messageId, output);
                            break;
                        case "6": // Delete message
                            int deleteToken = input.readInt();
                            int deleteMessageId = input.readInt();
                            output.writeUTF(deleteMessage(deleteToken, deleteMessageId));
                            break;
                        default:
                            output.writeUTF("ERROR: Unknown command");
                    }
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private List<String> getAllUsernames() {
                synchronized (accounts) {
                    List<String> usernames = new ArrayList<>();
                    for (Account account : accounts.values()) {
                        usernames.add(account.getUsername());
                    }
                    return usernames;
                }
            }

            private boolean isValidToken(int token) {
                synchronized (accounts) {
                    return accounts.containsKey(token);
                }
            }

            private int createAccount(String username) {
                String regex = "^[a-zA-Z0-9]+$";
                if (!username.matches(regex)) {
                    return -2; // Invalid username
                }

                synchronized (accounts) {
                    if (accounts.values().stream().anyMatch(acc -> acc.getUsername().equals(username))) {
                        return -1; // Username already exists
                    }
                    int token = random.nextInt(10000) + 1;
                    accounts.put(token, new Account(username, token));
                    return token;
                }
            }

            private String sendMessage(int senderToken, String receiver, String body) {
                synchronized (accounts) {
                    Account sender = accounts.get(senderToken);
                    if (sender == null) {
                        return "ERROR: Invalid sender token";
                    }

                    Account recipient = accounts.values().stream()
                            .filter(acc -> acc.getUsername().equals(receiver))
                            .findFirst()
                            .orElse(null);

                    if (recipient == null) {
                        return "ERROR: Recipient not found";
                    }

                    Message message = new Message(generateMessageId(), sender.getUsername(), receiver, body);
                    recipient.addMessage(message);
                    return "OK";
                }
            }

            private int generateMessageId() {
                return messageIdCounter.incrementAndGet();
            }

            private List<Message> showInbox(int token) {
                synchronized (accounts) {
                    Account account = accounts.get(token);
                    return account != null ? account.getMessageBox() : Collections.emptyList();
                }
            }

            private void readMessage(int authToken, int messageId, ObjectOutputStream output) {
                Account account = accounts.get(authToken);
                if (account == null) {
                    try {
                        output.writeUTF("ERROR: Invalid token");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                List<Message> messages = account.getMessageBox();
                Message message = null;
                for (Message msg : messages) {
                    if (msg.getId() == messageId) {
                        message = msg;
                        break;
                    }
                }
                if (message == null) {
                    try {
                        output.writeUTF("Message ID does not exist");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                message.markRead();
                try {
                    output.writeUTF("(" + message.getSender() + ") " + message.getBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        public String deleteMessage(int senderToken, int messageId) {
            synchronized (accounts) {
                Account sender = accounts.get(senderToken);
                if (sender == null) {
                    return "ERROR: Invalid sender token";
                }
                Message messageToDelete = null;
                for (Message message : sender.getMessageBox()) {
                    if (message.getId() == messageId) {
                        messageToDelete = message;
                        break;
                    }
                }
                if (messageToDelete != null) {
                    sender.removeMessage(messageToDelete);
                    return "OK";
                } else {
                    return "Message does not exist";
                }
            }
        }

    }
}