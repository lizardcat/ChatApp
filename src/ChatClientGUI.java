import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatClientGUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClientGUI(String serverAddress, int serverPort) {
        // Apply Material Design-inspired UI theme
        UIManager.put("TextField.background", new Color(40, 40, 40));
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.CYAN);
        UIManager.put("TextField.border", BorderFactory.createLineBorder(Color.GRAY));
        UIManager.put("Button.background", new Color(30, 136, 229));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 14));

        // Create Main Frame
        frame = new JFrame("🗨️ Material Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 500);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(30, 30, 30)); // Dark background

        // Chat Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBackground(new Color(50, 50, 50));
        chatArea.setForeground(Color.WHITE);
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(30, 30, 30));

        // Message Field
        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.setBackground(new Color(40, 40, 40));
        messageField.setForeground(Color.WHITE);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        inputPanel.add(messageField, BorderLayout.CENTER);

        // Send Button
        sendButton = new JButton("Send ➤");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendButton.setBackground(new Color(30, 136, 229));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(inputPanel, BorderLayout.SOUTH);

        // Action Listeners for Enter Key
        messageField.addActionListener(e -> sendMessage());

        // Show GUI
        frame.setVisible(true);

        // Connect to server
        connectToServer(serverAddress, serverPort);
    }

    private void connectToServer(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a thread to listen for incoming messages
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            chatArea.append("❌ Unable to connect to server.\n");
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                chatArea.append(message + "\n");
            }
        } catch (IOException e) {
            chatArea.append("❌ Disconnected from server.\n");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText(""); // Clear input field after sending
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI("localhost", 5000));
    }
}
