package udp.client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe implementant un Client de chat, en utilisant le protocole UDP multicast (via un groupe). Elle contient de plus
 * une methode {@link #main(String[])} permettant de lancer un client (en console).
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class Client implements Closeable {

    /* ATTRIBUTES */

    private boolean closed;
    private MulticastSocket multiSocket;
    private ListenThread listenThread;

    private final InetAddress groupAddr;
    private final int groupPort;

    private String pseudo;

    /* CONSTRUCTORS */

    /**
     * Construit un client se connectant au groupe dont l'adresse et le port sont passes en parametre.
     * @param groupAddr L'adresse du groupe auquel se connecter.
     * @param groupPort Le port sur lequel se connecter.
     */
    public Client(InetAddress groupAddr, int groupPort) {
        this.groupAddr = groupAddr;
        this.groupPort = groupPort;
        this.closed = true;

        this.pseudo = "Anonymous";
    }

    /* PUBLIC METHODS */

    /**
     * Lance le client (cad. le connecte au groupe et commence a ecouter et envoyer des messages).
     * @throws IOException En cas d'erreur lors de la connexion.
     */
    public void start() throws IOException {
        multiSocket = new MulticastSocket(groupPort);
        multiSocket.joinGroup(groupAddr);

        listenThread = new ListenThread(multiSocket, this::receiveMessage);
        listenThread.start();

        this.closed = false;
    }

    /**
     * Envoie le message passe en parametre au reste du groupe.
     * @param message Le message a envoyer.
     * @throws IOException Si le client est ferme.
     */
    public void sendMessage(String message) throws IOException {
        if (closed) {
            throw new IOException("Can't send message if the connection is closed.");
        }

        String toSend;
        if (message.matches("^/rename .*$")) {
            String newPseudo = message.split(" ")[1];
            toSend = formatMessage(pseudo + " s'est renomme " + newPseudo + ".");
            pseudo = newPseudo;
        } else {
            toSend = formatMessage(message, true);
        }

        DatagramPacket packet = new DatagramPacket(toSend.getBytes(), toSend.getBytes().length, groupAddr, groupPort);
        multiSocket.send(packet);
    }

    /**
     * Affiche le message reçu.
     * @param message Le message reçu.
     */
    public void receiveMessage(String message) {
        System.out.println(message);
    }

    /**
     * Ferme le client s'il etait ouvert.
     * @throws IOException Si une erreur arrive lors de la fermeture.
     */
    public void close() throws IOException {
        if (!closed) {
            this.sendMessage(pseudo + " a quitte le chat.");
            listenThread.close();
            multiSocket.leaveGroup(groupAddr);
        } else {
            System.err.println("Tried to close an already closed Client");
        }
        closed = true;
    }

    /* PRIVATE UTILITIES METHODS */

    /**
     * Formate un message selon {@link #formatMessage(String, boolean)}, avec le parametre withPseudo = false.
     * @param message Le message a formater.
     * @return Le message formate.
     */
    private String formatMessage(String message) {
        return formatMessage(message, false);
    }

    /**
     * Formate un message passe en parametre, selon deux formats possibles : en affichant ou non le pseudo dans
     * l'en-tete du message.
     * @param message Le message a formater.
     * @param withPseudo Faut-il afficher le pseudo dans l'en-tete du message ?
     * @return Le message formate.
     */
    private String formatMessage(String message, boolean withPseudo) {
        String time = DateTimeFormatter.ofPattern("H:m:s").format(LocalTime.now());
        StringBuilder formatted = new StringBuilder();

        if (withPseudo) {
            formatted.append("<[").append(time).append("] ").append(pseudo).append("> ").append(message);
        } else {
            formatted.append("<[").append(time).append("]> ").append(message).append("");
        }

        return formatted.toString();
    }

    /* MAIN METHOD */

    /**
     * Methode main lançant un serveur de chat.
     * @param args Doit contenir deux arguments : l'adresse et le port du groupe où se connecte le client
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage : java Client <group address> <group port>");
            System.exit(1);
        }

        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            Client client = new Client(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
            client.start();

            String msg = stdIn.readLine();
            while (!msg.equalsIgnoreCase("/quit")) {
                client.sendMessage(msg);
                msg = stdIn.readLine();
            }
            client.close();
        } catch (UnknownHostException e) {
            System.out.println("Given address is not a valid host.");
        } catch (NumberFormatException e) {
            System.out.println("Given port is not a valid number.");
        } catch (IOException e) {
            System.out.println("There was an IO exception while launching the client : '" + e.getMessage() + "'");
            e.printStackTrace();
        }
    }

}
