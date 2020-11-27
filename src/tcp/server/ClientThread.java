package tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;

/**
 * Classe representant un thread serveur s'occuppant de recevoir les messages d'un client en particulier pour ensuite
 * les traiter (si necessaire) et les renvoyer a tous les autres clients.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
class ClientThread extends Thread {

    private final Map<Socket, PrintStream> allClientsOutputStream;
    private final Socket clientSocket;
    private final String clientName;
    private final StringBuilder history;

    /**
     * Cree un thread d'ecoute client pour un serveur de chat TCP, utilisant le socket passe en parametre.
     * @param clientSocket Le socket connecte au client a ecouter.
     * @param allClientsOutputStream Une map representant tous les sockets clients et leur printstream associe sur
     *                               lesquels renvoyer un message reçu.
     * @param history L'historique de chat de l'application.
     */
    public ClientThread(Socket clientSocket, Map<Socket, PrintStream> allClientsOutputStream, StringBuilder history) {
        this.allClientsOutputStream = allClientsOutputStream;
        this.clientSocket = clientSocket;
        this.clientName = clientSocket.getInetAddress().toString();
        this.history = history;

        allClientsOutputStream.get(clientSocket).println(history.toString());
    }

    /**
     * Lance le thread client avec les parametres passes au constructeur.
     */
    @Override
    public void run() {
        try (BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            boolean quit = false;
            while (!quit) {
                String msg = socIn.readLine();
                if (msg == null) {
                    quit = true;
                } else {
                    System.out.println("\t[" + clientName + "] " + msg);
                    history.append(msg).append("\n");
                    synchronized (allClientsOutputStream) {
                        for (PrintStream socOut : allClientsOutputStream.values()) {
                            socOut.println(msg);
                        }
                    }
                }
            }
            System.out.println("[CLOSED] Connection from " + clientName + " closed.");
        } catch (IOException e) {
            System.out.println("[CLOSED/WARNING] Connection from " + clientName + " forcefully closed (IOException).");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
