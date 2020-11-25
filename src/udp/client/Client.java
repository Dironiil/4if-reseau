package udp.client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client implements Closeable {

    /* ATTRIBUTES */

    private boolean closed;
    private MulticastSocket multiSocket;
    private ListenThread listenThread;

    private final InetAddress groupAddr;
    private final int groupPort;

    private String pseudo;

    /* CONSTRUCTORS */

    public Client(InetAddress groupAddr, int groupPort) {
        this.groupAddr = groupAddr;
        this.groupPort = groupPort;
        this.closed = true;

        this.pseudo = "Anonymous";
    }

    /* PUBLIC METHODS */

    public void start() throws IOException {
        multiSocket = new MulticastSocket(groupPort);
        multiSocket.joinGroup(groupAddr);

        listenThread = new ListenThread(multiSocket, this::receiveMessage);
        listenThread.start();

        this.closed = false;
    }

    public void sendMessage(String message) throws IOException {
        if (closed) {
            throw new IOException("Can't send message if the connection is closed.");
        }

        String toSend;
        if (message.matches("^/rename .*$")) {
            String newPseudo = message.split(" ")[1];
            toSend = formatMessage(pseudo + " s'est renommé " + newPseudo + ".");
            pseudo = newPseudo;
        } else {
            toSend = formatMessage(message, true);
        }

        DatagramPacket packet = new DatagramPacket(toSend.getBytes(), toSend.getBytes().length, groupAddr, groupPort);
        multiSocket.send(packet);
    }

    public void receiveMessage(String message) {
        System.out.println(message);
    }

    public void close() throws IOException {
        if (!closed) {
            this.sendMessage(pseudo + " a quitté le chat.");
            listenThread.close();
            multiSocket.leaveGroup(groupAddr);
        } else {
            System.err.println("Tried to close an already closed Client");
        }
        closed = true;
    }

    /* PRIVATE UTILITIES METHODS */

    private String formatMessage(String message) {
        return formatMessage(message, false);
    }

    private String formatMessage(String message, boolean withPseudo) {
        String time = DateTimeFormatter.ofPattern("H:m:s").format(LocalTime.now());
        StringBuilder formatted = new StringBuilder();

        if (withPseudo) {
            formatted.append("<[").append(time).append("] ").append(pseudo).append("> ").append(message);
        } else {
            formatted.append("<[").append(time).append("]> ").append(message).append("");
        }

        return formatted.toString();
    }

    /* MAIN METHOD */

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage : java Client <group address> <group port>");
            System.exit(1);
        }

        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            Client client = new Client(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
            client.start();

            String msg = stdIn.readLine();
            while (!msg.equalsIgnoreCase("/quit")) {
                client.sendMessage(msg);
                msg = stdIn.readLine();
            }
            client.close();
        } catch (UnknownHostException e) {
            System.out.println("Given address is not a valid host.");
        } catch (NumberFormatException e) {
            System.out.println("Given port is not a valid number.");
        } catch (IOException e) {
            System.out.println("There was an IO exception while launching the client : '" + e.getMessage() + "'");
            e.printStackTrace();
        }
    }

}
