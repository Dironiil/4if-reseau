package tcp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Classe implementant un thread d'ecoute d'un client de chat utilisant TCP, permettant de recevoir et d'afficher des
 * messages.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
class ListenThread extends Thread {

    private final Socket listenSocket;
    private final Consumer<String> onReceiveAction;

    /**
     * Cree un thread d'ecoute sur le socket passe en parametre, effectuant l'action passee en parametre lorsqu'il
     * reçoit un message.
     * @param listenSocket Socket sur lequel ecouter l'arrivee de message.
     * @param onReceiveAction L'action a effectuer avec un message reçu.
     */
    public ListenThread(Socket listenSocket, Consumer<String> onReceiveAction) {
        this.listenSocket = listenSocket;
        this.onReceiveAction = onReceiveAction;
    }

    /**
     * Lance le thread d'ecoute du chat. Il tournera jusqu'a ce que le socket passe a son constructeur soit deconnecte.
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
