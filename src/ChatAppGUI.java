import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class ChatAppGUI {
    private JFrame frame;
    private JTextArea chatArea, logArea;
    private JTextField messageField;
    private JButton sendButton, startServerButton, connectClientButton, toggleUsersButton, toggleLogsButton;
    private ServerSocket serverSocket;
    private static Map<String, PrintWriter> clientWriters = new HashMap<>();
    private static DefaultListModel<String> userListModel = new DefaultListModel<>();
    private static JList<String> userList;
    private JPanel userPanel, logPanel;
    private boolean isUserListVisible = true, isLogVisible = true;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean isServer = false;
    private JLabel typingIndicator;

    public ChatAppGUI() {
        frame = new JFrame("Java Chat App");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Chat Display Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(50, 50, 50));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Typing Indicator
        typingIndicator = new JLabel(" ");
        typingIndicator.setForeground(Color.CYAN);
        frame.add(typingIndicator, BorderLayout.NORTH);

        // Online Users Panel
        userPanel = new JPanel(new BorderLayout());
        userList = new JList<>(userListModel);
        userList.setBackground(new Color(40, 40, 40));
        userList.setForeground(Color.GREEN);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        userPanel.setPreferredSize(new Dimension(150, 500));

        // Toggle Button for Users Panel (Always Visible)
        toggleUsersButton = new JButton("👥 Users");
        toggleUsersButton.addActionListener(e -> toggleUserPanel());
        userPanel.add(toggleUsersButton, BorderLayout.SOUTH);
        frame.add(userPanel, BorderLayout.WEST);

        // Server Log Panel
        logPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(Color.GREEN);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logPanel.setPreferredSize(new Dimension(200, 500));

        // Toggle Button for Server Log Panel (Always Visible)
        toggleLogsButton = new JButton("📜 Logs");
        toggleLogsButton.addActionListener(e -> toggleLogPanel());
        logPanel.add(toggleLogsButton, BorderLayout.SOUTH);
        frame.add(logPanel, BorderLayout.EAST);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send ➤");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Buttons for Server & Client Mode
        JPanel controlPanel = new JPanel();
        startServerButton = new JButton("Start Server");
        connectClientButton = new JButton("Connect as Client");
        controlPanel.add(startServerButton);
        controlPanel.add(connectClientButton);
        frame.add(controlPanel, BorderLayout.NORTH);

        // Action Listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        messageField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                notifyTyping();
            }
        });

        startServerButton.addActionListener(e -> startServer());
        connectClientButton.addActionListener(e -> connectToServer());

        frame.setVisible(true);
    }

    private void toggleUserPanel() {
        isUserListVisible = !isUserListVisible;
        userList.setVisible(isUserListVisible); // Only hide user list, not button
    }

    private void toggleLogPanel() {
        isLogVisible = !isLogVisible;
        logArea.setVisible(isLogVisible); // Only hide logs, not button
    }

    private void startServer() {
        isServer = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);
                logArea.append("✅ Server started on port 5000\n");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (IOException e) {
                logArea.append("❌ Server error: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String message;
                while ((message = in.readLine()) != null) {
                    logArea.append("📩 " + message + "\n");
                }
            } catch (IOException e) {
                logArea.append("🚪 A client disconnected.\n");
            }
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }

    private void notifyTyping() {
        out.println("TYPING...");
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5000);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                chatArea.append("✅ Connected to server!\n");

                new Thread(this::listenForMessages).start();
            } catch (IOException e) {
                chatArea.append("❌ Unable to connect to server.\n");
            }
        }).start();
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                chatArea.append(message + "\n");
                typingIndicator.setText("");
            }
        } catch (IOException e) {
            chatArea.append("❌ Disconnected from server.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatAppGUI::new);
    }
}
