package tp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client {

    private final String host;
    private final int port;

    private String pseudo;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.pseudo = "Anonymous";
    }

    public void start() {
        try (Socket commSocket = new Socket(host, port);
             PrintStream socOut = new PrintStream(commSocket.getOutputStream());
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // creation socket ==> connexion
            ListenThread listenThread = new ListenThread(commSocket);
            listenThread.start();
            String line = "";
            while (line != null && !"/quit".equalsIgnoreCase(line)) {
                line = stdIn.readLine();
                String toSend;
                String time = DateTimeFormatter.ofPattern("h:m:s").format(LocalTime.now());
                if (line == null || line.equalsIgnoreCase("/quit")) {
                    toSend = "[" + time + "] " + pseudo + " a quitté le chat.";
                } else if (line.matches("^/rename .*$")) {
                    String newPseudo = line.split(" ")[1];
                    toSend = "[" + time + "] " + pseudo + " s'est renommé " + newPseudo;
                    pseudo = newPseudo;
                } else {
                    toSend = "<[" + time + "] " + pseudo + "> " + line;
                }
                socOut.println(toSend);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + host);
            System.exit(2);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + host);
            System.exit(3);
        }
    }

    /* MAIN METHOD */

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Client <host> <port>");
            System.exit(1);
        }
        new Client(args[0], Integer.parseInt(args[1])).start();
    }

}
