package tcp.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Classe implémentant la partie purement fonctionnelle d'un Client de chat, en utilisant le protocole TCP.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
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

    /**
     * Construit un client se connectant à l'hôte et au port donné en paramètre, et exécutant l'action donnée en
     * paramètre lorsqu'il reçoit un message.
     * @param host L'hôte auquel se connecter.
     * @param port Le port sur lequel se connecter.
     * @param onReceiveAction L'action à exécuter avec un message reçu.
     */
    public Client(String host, int port, Consumer<String> onReceiveAction) {
        this.host = host;
        this.port = port;
        this.closed = true;

        this.onReceiveAction = onReceiveAction;

        this.pseudo = "Anonymous";
    }

    /* PUBLIC METHODS */

    /**
     * Lance le client (càd. le connecte et commence à écouter et envoyer des messages).
     * @throws IOException En cas d'erreur lors de la connexion.
     */
    public void start() throws IOException {
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

    /**
     * Envoie le message passé en paramètre au reste du chat.
     * @param message Le message à envoyer.
     * @throws IOException Si le client est fermé ou si une erreur de connexion arrive.
     */
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

    /**
     * Effectue l'action appropriée avec un message reçu.
     * @param message Le message reçu.
     */
    public void receiveMessage(String message) {
        onReceiveAction.accept(message);
    }

    /**
     * Ferme le client s'il était ouvert.
     * @throws IOException Si une erreur arrive lors de la fermeture.
     */
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

    /**
     * Formate un message selon {@link #formatMessage(String, boolean)}, avec le paramètre withPseudo = false.
     * @param message Le message à formater.
     * @return Le message formaté.
     */
    private String formatMessage(String message) {
        return formatMessage(message, false);
    }

    /**
     * Formate un message passé en paramètre, selon deux formats possibles : en affichant ou non le pseudo dans
     * l'en-tête du message.
     * @param message Le message à formatter.
     * @param withPseudo Faut-il afficher le pseudo dans l'en-tête du message ?
     * @return Le message formatté.
     */
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
