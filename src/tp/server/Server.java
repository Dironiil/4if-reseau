package tp.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Map<Socket, ObjectOutputStream> allClientSockets = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            System.exit(1);
        }

        try {
            ServerSocket listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
            System.out.println("Server ready...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("[CONNECT] Connection from:" + clientSocket.getInetAddress());
                allClientSockets.put(clientSocket, new ObjectOutputStream(clientSocket.getOutputStream()));
                ClientThread ct = new ClientThread(clientSocket, allClientSockets);
                ct.start();
            }
        } catch (IOException e) {
            System.err.println("Internel server error, see stack trace :");
            e.printStackTrace();
        }
    }

}
