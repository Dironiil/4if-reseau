package udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

/**
 * Classe implémentant un thread d'écoute d'un client de chat utilisant UDP multicast, permettant de recevoir et
 * d'afficher des messages envoyés au groupe de multicast.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class ListenThread extends Thread {

    private final MulticastSocket multiSocket;
    private final Consumer<String> onReceiveAction;

    private boolean closed;

    public ListenThread(MulticastSocket multiSocket, Consumer<String> onReceiveAction) throws IOException {
        this.multiSocket = multiSocket;
        multiSocket.setSoTimeout(3000);
        this.onReceiveAction = onReceiveAction;

        this.closed = true;
    }

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

    public void close() {
        closed = true;
    }
}
