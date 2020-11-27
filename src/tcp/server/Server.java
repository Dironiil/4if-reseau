package tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Classe principale representant un serveur d'un systeme de chat en ligne.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class Server {

    /**
     * Methode main lançant un serveur de chat.
     * @param args Doit contenir un seul argument : le port de lancement.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            System.exit(1);
        }

        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            ServerControlThread server = new ServerControlThread(Integer.parseInt(args[0]));
            server.start();
            while (!"/close".equalsIgnoreCase(stdIn.readLine()));
            server.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
