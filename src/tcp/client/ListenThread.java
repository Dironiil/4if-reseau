package tcp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

class ListenThread extends Thread {

    private final Socket listenSocket;
    private final Consumer<String> onReceiveAction;

    public ListenThread(Socket listenSocket, Consumer<String> onReceiveAction) {
        this.listenSocket = listenSocket;
        this.onReceiveAction = onReceiveAction;
    }

    @Override
    public void run() {
        try (BufferedReader socIn = new BufferedReader(new InputStreamReader(listenSocket.getInputStream()))) {
            String msg = socIn.readLine();
            while (msg != null) {
                onReceiveAction.accept(msg);
                msg = socIn.readLine();
            }
        } catch (IOException ignored) {
        } finally {
            onReceiveAction.accept("\n<span style='color: #F84545;'>&lt;CHAT CLOSED&gt;</span>\n");
        }
    }
}
