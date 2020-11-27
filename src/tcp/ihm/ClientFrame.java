package tcp.ihm;

import tcp.client.Client;
import tcp.utils.ActionFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Classe IHM principale du client de chat TCP, elle représente la fenêtre du client. Elle contient de plus une méthode
 * {@link #main(String[])} permettant de lancer cette fenêtre.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class ClientFrame extends JFrame {

    /* COLORS CONSTANT */

    private static final Color BG_GENERAL_COLOR = new Color(41, 67, 75);
    private static final Color BORDER_COLOR = new Color(22, 24, 40);
    private static final Color BG_ZONE_COLOR = new Color(48, 85, 101);
    private static final Color BG_BUTTON_COLOR = new Color(62, 97, 128);
    private static final Color TEXT_COLOR = new Color(226, 226, 226);

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    /* VISUAL COMPONENTS */

    private JPanel chatPanel;
    private JPanel sendMessagePanel;

    private ChatArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    /* FUNCTIONAL ATTRIBUTES */

    private Client client;

    /* INITIALIZATION */

    public ClientFrame(String host, int port) {
        this.setTitle("Messagerie 4IF");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Dimension dim = new Dimension(500, 700);
        this.setSize(dim);
        this.setMinimumSize(dim);
        this.setMaximumSize(dim);
        this.getContentPane().setBackground(BG_GENERAL_COLOR);
        this.setResizable(false);

        initComponents();

        this.client = new Client(host, port, chatArea::addLine);
        try {
            client.start();
        } catch (IOException e) {
            chatArea.addErrorLine(e.getMessage());
        }
    }

    private void initComponents() {
        /* Graphic initialization */

        this.getContentPane().setLayout(new BorderLayout());

        chatPanel = new JPanel();
        chatPanel.setLayout(new FlowLayout());
        chatPanel.setBackground(TRANSPARENT);
        sendMessagePanel = new JPanel();
        sendMessagePanel.setLayout(new FlowLayout());
        sendMessagePanel.setSize(new Dimension(800, 60));
        sendMessagePanel.setPreferredSize(new Dimension(800, 60));
        sendMessagePanel.setBackground(TRANSPARENT);

        this.getContentPane().add(chatPanel, BorderLayout.CENTER);
        this.getContentPane().add(sendMessagePanel, BorderLayout.PAGE_END);

        Dimension chatDimension = new Dimension(450, 590);
        chatArea = new ChatArea();
        chatArea.setBackground(BG_ZONE_COLOR);
        chatArea.setMaximumSize(chatDimension);

        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setSize(chatDimension);
        scroll.setPreferredSize(chatDimension);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBackground(TRANSPARENT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        chatPanel.add(scroll);

        Dimension messageDimension = new Dimension(350, 50);
        messageField = new JTextField();
        messageField.setSize(messageDimension);
        messageField.setPreferredSize(messageDimension);
        messageField.setMaximumSize(messageDimension);
        messageField.setBackground(BG_ZONE_COLOR);
        messageField.setForeground(TEXT_COLOR);
        messageField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        Dimension sendButtonDimension = new Dimension(75, 50);
        sendButton = new JButton();
        sendButton.setSize(sendButtonDimension);
        sendButton.setPreferredSize(sendButtonDimension);
        sendButton.setMaximumSize(sendButtonDimension);
        sendButton.setBackground(BG_BUTTON_COLOR);
        sendButton.setForeground(TEXT_COLOR);
        sendButton.setText("Envoyer");
        sendButton.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        sendMessagePanel.add(messageField);
        sendMessagePanel.add(sendButton);

        /* Actions initialization */

        ActionFunction sendMessage = () -> {
            String msg = messageField.getText();
            if (msg.equalsIgnoreCase("/quit")) {
                this.dispose();
            } else {
                System.out.println("\tSend message: " + msg);
                try {
                    client.sendMessage(msg);
                } catch (IOException e) {
                    chatArea.addErrorLine(e.getMessage());
                }
                messageField.setText("");
            }
        };

        sendButton.addActionListener(a -> {
            sendMessage.act();
        });

        messageField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage.act();
                }
            }
        });
    }

    /* OTHER METHODS */

    @Override
    public void dispose() {
        try {
            client.close();
        } catch (IOException e) {
            System.err.println("Couldn't properly close the client...");
        } finally {
            this.setVisible(false);
            super.dispose();
        }
    }

    /* MAIN METHOD */

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ClientFrame <host> <port>");
            System.exit(1);
        }

        JFrame f = new ClientFrame(args[0], Integer.parseInt(args[1]));
        f.setVisible(true);
    }

}
