package tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Classe principale représentant un serveur d'un système de chat en ligne.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class Server {

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
