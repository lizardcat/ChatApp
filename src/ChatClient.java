import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost"; // Change if running on another machine
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader userInput;

    public ChatClient() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to chat server.");

            // Input & output streams for communication
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            userInput = new BufferedReader(new InputStreamReader(System.in));

            // Start thread to listen for messages
            new Thread(new IncomingReader()).start();

            // Main loop for sending messages
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(message); // Send to server
            }
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            if (userInput != null) userInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thread to continuously listen for incoming messages from server
    private class IncomingReader implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message); // Display received messages
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        }
    }

    public static void main(String[] args) {
        new ChatClient();
    }
}
