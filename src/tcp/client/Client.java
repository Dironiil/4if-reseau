package tcp.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class Client implements Closeable {

    /* ATTRIBUTES */

    private boolean closed;
    private Socket socket;
    private PrintStream socOut;
    private Consumer<String> onReceiveAction;

    private final String host;
    private final int port;

    private String pseudo;

    /* CONSTRUCTORS */

    public Client(String host, int port, Consumer<String> onReceiveAction) {
        this.host = host;
        this.port = port;
        this.closed = true;

        this.onReceiveAction = onReceiveAction;

        this.pseudo = "Anonymous";
    }

    /* PUBLIC METHODS */

    public void start() throws IOException{
        try {
            socket = new Socket(host, port);
            socOut = new PrintStream(socket.getOutputStream());

            // creation socket ==> connexion
            ListenThread listenThread = new ListenThread(socket, this::receiveMessage);
            listenThread.start();

            socOut.println(formatMessage(pseudo + " a rejoint le chat."));

            this.closed = false;
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + host);
            throw new UnknownHostException("Don't know about host: " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + host);
            throw new IOException("Couldn't get I/O for the connection to: " + host);
        }
    }

    public void sendMessage(String message) throws IOException {
        if (closed) {
            throw new IOException("Can't send message if the connection is closed.");
        }
        if (!socket.isConnected()) {
            this.close();
            throw new IOException("Connection unexpectedly closed.");
        }

        String toSend;
        if (message.matches("^/rename .*$")) {
            String newPseudo = message.split(" ")[1];
            toSend = formatMessage(pseudo + " s'est renommé " + newPseudo + ".");
            pseudo = newPseudo;
        } else {
            toSend = formatMessage(message, true);
        }

        socOut.println(toSend);
    }

    public void receiveMessage(String message) {
        onReceiveAction.accept(message);
    }

    public void close() throws IOException {
        if (!closed) {
            socOut.println(formatMessage(pseudo + " a quitté le chat."));
            socOut.close();
            socket.close();
        } else {
            System.err.println("Tried to close an already closed Client");
        }
        closed = true;
    }

    /* PRIVATE UTILITIES METHODS */

    private String formatMessage(String message) {
        return formatMessage(message, false);
    }

    private String formatMessage(String message, boolean withPseudo) {
        String time = DateTimeFormatter.ofPattern("H:m:s").format(LocalTime.now());
        StringBuilder formatted = new StringBuilder();

        if (withPseudo) {
            formatted.append("<b>&lt;<i>[").append(time).append("]</i> ").append(pseudo).append("&gt;</b> ").append(message);
        } else {
            formatted.append("<b>&lt;<i>[").append(time).append("]</i>&gt; ").append(message).append("</b>");
        }

        return formatted.toString();
    }

}
