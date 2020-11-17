package tp.client;

import tp.data.Message;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalTime;

public class Client {

    private final String host;
    private final int port;

    private String pseudo;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (Socket commSocket = new Socket(host, port);
             ObjectOutputStream socOut = new ObjectOutputStream(commSocket.getOutputStream());
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // creation socket ==> connexion
            ListenThread listenThread = new ListenThread(commSocket);
            listenThread.start();
            String line = "";
            while (line != null && !"/quit".equalsIgnoreCase(line)) {
                line = stdIn.readLine();
                Message toSend;
                if (line == null || line.equalsIgnoreCase("/quit")) {
                    toSend = new Message(pseudo, line, LocalTime.now(), Message.Metadata.QUIT);
                } else if (line.matches("^/rename .*$")) {
                    String newPseudo = line.split(" ")[1];
                    toSend = new Message(pseudo, newPseudo, LocalTime.now(), Message.Metadata.RENAME);
                    pseudo = newPseudo;
                } else {
                    toSend = new Message(pseudo, line, LocalTime.now(), Message.Metadata.TEXT);
                }
                socOut.writeObject(toSend);
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
