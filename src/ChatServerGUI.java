import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerGUI {
    private JFrame frame;
    private JTextArea logArea;
    private static final int PORT = 5000;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static DefaultListModel<String> clientListModel = new DefaultListModel<>();
    private static JList<String> clientList;

    public ChatServerGUI() {
        frame = new JFrame("🌐 Chat Server");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(40, 40, 40));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);

        clientList = new JList<>(clientListModel);
        clientList.setBackground(new Color(30, 30, 30));
        clientList.setForeground(Color.GREEN);
        frame.add(new JScrollPane(clientList), BorderLayout.EAST);

        frame.setVisible(true);
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                log("✅ Server started on port " + PORT);

                while (true) {
                    new ClientHandler(serverSocket.accept()).start();
                }
            } catch (IOException e) {
                log("❌ Server error: " + e.getMessage());
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                username = in.readLine(); // Read username
                log(username + " joined the chat!");
                clientListModel.addElement(username);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    log(message);
                    broadcast(message);
                }
            } catch (IOException e) {
                log(username + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                clientListModel.removeElement(username);
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatServerGUI::new);
    }
}
