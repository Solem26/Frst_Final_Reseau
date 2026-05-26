package Gestion_connexion_tcp;

import java.io.*;
import java.net.*;


 
public class ServiceClient {

    public static void main(String[] args) {

        final int PORT_TCP = 5000;
        final int PORT_UDP = 6000;
        final String HOST = "127.0.0.1";

        try {

            // connexion TCP au serveur de chat
            Socket socket = new Socket(HOST, PORT_TCP);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8")
            );

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // lecture clavier
            BufferedReader clavier = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            // socket UDP pour notifications
            DatagramSocket udpSocket = new DatagramSocket();

            /*
             * 1. DEMANDER au client de saisir son nom
             */
            System.out.print("Entrer votre nom : ");
            String nom = clavier.readLine();

            // envoyer le nom au serveur TCP
            out.println(nom);
            System.out.println("Demarrer la conversation: >");

            
            sendUDP(udpSocket, "NOTIF " + nom + " connecté", HOST, PORT_UDP);

            Thread reception = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Connexion fermée");
                }
            });

            reception.start();

            
            String message;

            while (true) {

                message = clavier.readLine();

                if (message.equalsIgnoreCase("QUIT")) {

                    out.println("QUIT");

                    // notification UDP déconnexion
                    sendUDP(udpSocket,
                            "NOTIF " + nom + " déconnecté",
                            HOST,
                            PORT_UDP);

                    break;
                }

                out.println(message);
            }

            socket.close();
            udpSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Méthode UDP pour gerer la notification     */
    public static void sendUDP(DatagramSocket socket,
                               String message,
                               String host,
                               int port) {

        try {

            byte[] data = message.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName(host),
                    port
            );

            socket.send(packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}