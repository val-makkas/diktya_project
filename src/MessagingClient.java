import java.io.*;
import java.net.*;
import java.util.*;

public class MessagingClient {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java MessagingClient <ip> <port> <command> [args...]");
            return;
        }

        String serverIp = args[0];
        int port = Integer.parseInt(args[1]);
        String command = args[2];

        try (Socket socket = new Socket(serverIp, port);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeUTF(command);

            switch (command) {
                case "1": // Create account
                    String username = args[3];
                    output.writeUTF(username);

                    output.flush();
                    try {
                        String response = input.readUTF();
                        System.out.println(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "2": // Show accounts
                    int senderToken = Integer.parseInt(args[3]);
                    output.writeUTF(String.valueOf(senderToken));

                    try {
                        Object response = input.readObject();
                        printNumberedList(response);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "3": // SEnd message
                    int sender = Integer.parseInt(args[3]);
                    String receiver = args[4];
                    String body = args[5];

                    output.writeInt(sender);
                    output.writeUTF(receiver);
                    output.writeUTF(body);

                    output.flush();
                    try {
                        String response = input.readUTF();
                        System.out.println(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "4": // Show Inbox
                    int token = Integer.parseInt(args[3]);
                    output.writeInt(token);

                    output.flush();
                    try {
                        Object response = input.readObject();
                        printNumberedList(response);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "5": // Read Message
                    break;
                case "6": // Delete Message
                    break;
                default:
                    System.out.println("Unknown command");
                    return;
            }
        }
    }
    private static void printNumberedList(Object obj) {
        if (obj instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                System.out.println((i + 1) + ". " + list.get(i));
            }
        } else {
            System.out.println("Unexpected response format.");
        }
    }
}
