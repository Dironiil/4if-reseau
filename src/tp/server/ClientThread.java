package tp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;

class ClientThread extends Thread {

    private final Map<Socket, PrintStream> allClientsOutputStream;
    private final Socket clientSocket;
    private final String clientName;

    public ClientThread(Socket clientSocket, Map<Socket, PrintStream> allClientsOutputStream) {
        this.allClientsOutputStream = allClientsOutputStream;
        this.clientSocket = clientSocket;
        this.clientName = clientSocket.getInetAddress().toString();
    }

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
