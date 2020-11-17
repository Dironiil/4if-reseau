package tp.client;

import tp.data.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

class ListenThread extends Thread {

    private final Socket listenSocket;

    public ListenThread(Socket listenSocket) {
        this.listenSocket = listenSocket;
    }

    @Override
    public void run() {
        try (ObjectInputStream socIn = new ObjectInputStream(listenSocket.getInputStream())) {
            Message msg = (Message) socIn.readObject();
            while (msg != null) {
                System.out.println(msg);
                msg = (Message) socIn.readObject();
            }
        } catch (IOException ignored) {
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Chat closed");
        }
    }
}
