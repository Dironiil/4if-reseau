package tp.server;

import tp.data.Message;
import tp.data.ServerMessage;

import java.io.*;
import java.net.Socket;
import java.util.Map;

class ClientThread extends Thread {

    private final Map<Socket, ObjectOutputStream> allClientsOutputStream;
    private final Socket clientSocket;
    private final String clientName;

    public ClientThread(Socket clientSocket, Map<Socket, ObjectOutputStream> allClientsOutputStream) {
        this.allClientsOutputStream = allClientsOutputStream;
        this.clientSocket = clientSocket;
        this.clientName = clientSocket.getInetAddress().toString();
    }

    @Override
    public void run() {
        try (ObjectInputStream socIn = new ObjectInputStream(clientSocket.getInputStream())) {
            boolean quit = false;
            while (!quit) {
                Message msg = (Message) socIn.readObject();
                if (msg == null) {
                    System.out.println("[CLOSED/WARNING] Connection from " + clientName + " forcefully closed (null)");
                    quit = true;
                } else {
                    switch (msg.getMetadata()) {
                        case RENAME -> {
                            msg = new ServerMessage(msg.getUser() + " s'est renommé " + msg.getContent(), msg.getTimestamp());
                        }
                        case QUIT -> {
                            quit = true;
                            allClientsOutputStream.remove(clientSocket);
                            msg = new ServerMessage(msg.getUser() + " a quitté le chat.", msg.getTimestamp());
                        }
                    }
                    System.out.println("\t[" + clientName + "] " + msg);
                    synchronized (allClientsOutputStream) {
                        for (ObjectOutputStream socOut : allClientsOutputStream.values()) {
                            socOut.writeObject(msg);
                        }
                    }
                }
            }
            System.out.println("[CLOSED] Connection from " + clientName + " closed.");
        } catch (IOException e) {
            System.out.println("[CLOSED/WARNING] Connection from " + clientName + " forcefully closed (IOException).");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
