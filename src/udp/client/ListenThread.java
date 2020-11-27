package udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

/**
 * Classe implementant un thread d'ecoute d'un client de chat utilisant UDP multicast, permettant de recevoir et
 * d'afficher des messages envoyes au groupe de multicast.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class ListenThread extends Thread {

    private final MulticastSocket multiSocket;
    private final Consumer<String> onReceiveAction;

    private boolean closed;

    /**
     * Cree un thread d'ecoute sur le socket passe en parametre, effectuant l'action passee en parametre lorsqu'il
     * reçoit un message.
     * @param multiSocket Socket sur lequel ecouter l'arrivee de message.
     * @param onReceiveAction L'action a effectuer avec un message reçu.
     */
    public ListenThread(MulticastSocket multiSocket, Consumer<String> onReceiveAction) throws IOException {
        this.multiSocket = multiSocket;
        multiSocket.setSoTimeout(3000);
        this.onReceiveAction = onReceiveAction;

        this.closed = true;
    }

    /**
     * Lance le thread d'ecoute du chat. Il tournera jusqu'a ce que le thread ait reçu une demande de fermeture.
     */
    @Override
    public void run() {
        closed = false;

        byte[] buf = new byte[1000];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (true) {
            try {
                multiSocket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                onReceiveAction.accept(msg);
            } catch (SocketTimeoutException e) {
                if (closed) {
                    break;
                }
            } catch (IOException e) {
                System.err.println("[ERROR] Error while trying to receive packets.");
            }
        }
    }

    /**
     * Demande au thread de cesser son activite. Cette fermeture peut prendre quelques secondes a etre prise en compte.
     */
    public void close() {
        closed = true;
    }
}
