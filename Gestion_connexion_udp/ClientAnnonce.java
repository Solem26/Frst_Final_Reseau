package Gestion_connexion_udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientAnnonce {
    
    public static void main(String[] args) {

        final int PORT = 7000;

        try {

            DatagramSocket socket = new DatagramSocket(PORT);

            byte[] buffer = new byte[1024];

            System.out.println("En attente des annonces...");

            while (true) {

                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                String msg = new String(
                        packet.getData(),
                        0,
                        packet.getLength()
                );

                System.out.println("Annonce reçue : " + msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

