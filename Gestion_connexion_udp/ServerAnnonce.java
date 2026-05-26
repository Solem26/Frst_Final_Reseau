package Gestion_connexion_udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerAnnonce {

    public static void main(String[] args) {

        final int PORT = 7000;

        try {

            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            while (true) {

                String msg = "SERVEUR DISPONIBLE 127.0.0.1:5000";

                byte[] data = msg.getBytes();

                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        InetAddress.getByName("255.255.255.255"),
                        PORT
                );

                socket.send(packet);

                System.out.println("Annonce envoyée");

                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}