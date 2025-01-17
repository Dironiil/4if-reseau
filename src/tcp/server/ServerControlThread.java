package tcp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe centrale d'un serveur de chat en ligne, elle implemente le Thread de contrôle du-dit serveur. Ce thread reçoit
 * les nouvelles connexions client et les gere pour les ajouter au chat.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class ServerControlThread extends Thread {

    private ServerSocket listenSocket;

    private final Map<Socket, PrintStream> allClientSockets = Collections.synchronizedMap(new HashMap<>());
    private final StringBuilder history = new StringBuilder();
    private static final File historyFile = new File("history.txt");

    /**
     * Cree le thread de contrôle d'un serveur de chat TCP.
     * @param port Le port sur lequel le thread devra ecouter les connections.
     * @throws IOException S'il y a une erreur a l'initialisation du socket d'ecoute.
     */
    public ServerControlThread(int port) throws IOException {
        listenSocket = new ServerSocket(port);

        BufferedReader historyFileReader = new BufferedReader(new FileReader(historyFile));
        String line;
        while ((line = historyFileReader.readLine()) != null) {
            history.append(line).append('\n');
        }
    }

    /**
     * Lance le thread de contrôle d'un serveur de chat, ecoutant et gerant les connexions client sur le port passe au
     * constructeur de cet objet.
     */
    @Override
    public void run() {
        try {
            System.out.println("Server ready...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("[CONNECT] Connection from:" + clientSocket.getInetAddress());
                allClientSockets.put(clientSocket, new PrintStream(clientSocket.getOutputStream()));
                ClientThread ct = new ClientThread(clientSocket, allClientSockets, history);
                ct.start();
            }
        } catch (SocketException e) {
            System.out.println("Server closed.");
        } catch (IOException e) {
            System.err.println("Internet server error, see stack trace :");
            e.printStackTrace();
        } finally {
            try {
                FileWriter historyFileWriter = new FileWriter(historyFile);
                historyFileWriter.append(history.toString());
                historyFileWriter.flush();
            } catch (IOException e) {
                System.out.println("L'Histoire est inaccessible.");
            }
        }
    }

    public void close() throws IOException {
        listenSocket.close();
        for (Socket s : allClientSockets.keySet()) {
            s.close();
        }
    }

}
