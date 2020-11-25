//package udp.server;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//
//public class ServerControlThread extends Thread {
//
//    int port;
//
//    public ServerControlThread(int port) throws IOException {
//        this.port = port;
//    }
//
//    @Override
//    public void run() {
//        try {
//            System.out.println("Server ready...");
//            DatagramSocket socket = new DatagramSocket(port);
//            while (true) {
//                // Réception des connexions en unicast, renvoi de l'adresse + port sur lesquels écouter
//                byte[] buf = new byte[256];
//                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
//                socket.receive(receivedPacket);
//
//                InetAddress clientAddr = receivedPacket.getAddress();
//                int clientPort = receivedPacket.getPort();
//
//                buf = ("228.5.6.7\n" + port).getBytes();
//                DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, clientAddr, clientPort);
//
//                socket.send(sendPacket);
//            }
//        } catch (SocketException e) {
//            System.out.println("Server closed.");
//        } catch (IOException e) {
//            System.err.println("Internet server error, see stack trace :");
//            e.printStackTrace();
//        }
//    }
//
//}
