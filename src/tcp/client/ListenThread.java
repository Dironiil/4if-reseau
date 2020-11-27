package tcp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Classe implémentant un thread d'écoute d'un client de chat utilisant TCP, permettant de recevoir et d'afficher des
 * messages.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
class ListenThread extends Thread {

    private final Socket listenSocket;
    private final Consumer<String> onReceiveAction;

    /**
     * Crée un thread d'écoute sur le socket passé en paramètre, effectuant l'action passée en paramètre lorsqu'il
     * reçoit un message.
     * @param listenSocket Socket sur lequel écouter l'arrivée de message.
     * @param onReceiveAction L'action à effectuer avec un message reçu.
     */
    public ListenThread(Socket listenSocket, Consumer<String> onReceiveAction) {
        this.listenSocket = listenSocket;
        this.onReceiveAction = onReceiveAction;
    }

    /**
     * Lance le thread d'écoute du chat. Il tournera jusqu'à ce que le socket passé à son constructeur soit déconnecté.
     */
    @Override
    public void run() {
        try (BufferedReader socIn = new BufferedReader(new InputStreamReader(listenSocket.getInputStream()))) {
            String msg = socIn.readLine();
            while (msg != null) {
                onReceiveAction.accept(msg);
                msg = socIn.readLine();
            }
        } catch (IOException ignored) {
        } finally {
            onReceiveAction.accept("\n<span style='color: #F84545;'>&lt;CHAT CLOSED&gt;</span>\n");
        }
    }
}
