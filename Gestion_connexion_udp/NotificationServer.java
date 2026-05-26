package Gestion_connexion_udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class NotificationServer {
    public static void main(String[] args) {
        final int PORT= 6000;
        try {
            DatagramSocket dsock  = new DatagramSocket(PORT);
            System.out.println("Serveur UDP en attente...");

            byte [] data = new byte[1024];
            while (true) {
                DatagramPacket dpack = new DatagramPacket(data, data.length);
                dsock.receive(dpack);
                String texte = new String(dpack.getData(), 0,dpack.getLength());
                System.out.println("Notification reçue : " + texte);
            }

            

        } catch (Exception e) {
            System.out.println("Erreur "+ e.getMessage());
            e.printStackTrace();
        }
    }
}
