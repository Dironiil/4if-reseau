package tp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class ListenThread extends Thread {

    private final Socket listenSocket;

    public ListenThread(Socket listenSocket) {
        this.listenSocket = listenSocket;
    }

    @Override
    public void run() {
        try (BufferedReader socIn = new BufferedReader(new InputStreamReader(listenSocket.getInputStream()))) {
            String msg = socIn.readLine();
            while (msg != null) {
                System.out.println(msg);
                msg = socIn.readLine();
            }
        } catch (IOException ignored) {
        } finally {
            System.out.println("Chat closed");
        }
    }
}
