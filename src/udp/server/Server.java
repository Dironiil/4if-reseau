//package udp.server;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//public class Server {
//
//    public static void main(String[] args) {
//        if (args.length != 1) {
//            System.out.println("Usage: java Server <port>");
//            System.exit(1);
//        }
//
//        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
//            ServerControlThread server = new ServerControlThread(Integer.parseInt(args[0]));
//            server.start();
//            while (!"/close".equalsIgnoreCase(stdIn.readLine()));
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
