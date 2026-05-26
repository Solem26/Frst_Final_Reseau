package Gestion_connexion_tcp;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ChatServer {

    // Liste des clients connectés (threads)
    public static List<ClientHandler> listc = new ArrayList<>();

    public static void main(String[] args) {

        final int PORT = 5000;

        try (ServerSocket ss = new ServerSocket(PORT)) {

            System.out.println("Serveur TCP démarré sur le port " + PORT);

            while (true) {

                // Attente de connexion client TCP
                Socket soc = ss.accept();

                System.out.println("Client connecté : " + soc.getInetAddress());

                // Création d'un gestionnaire pour ce client
                ClientHandler handler = new ClientHandler(soc);

                // Ajout du client dans la liste globale
                synchronized (listc) {
                    listc.add(handler);
                }

                // Lancement d'un thread pour gérer  client
                new Thread(handler).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * methode pour diffuser un message à tous les clients
     */
    public static void broadcast(String message, ClientHandler sender) {

        synchronized (listc) {
            for (ClientHandler c : listc) {

                // éviter d'envoyer au même client 
                if (sender == null || c != sender) {
                    c.sendMessage(message);
                }
            }
        }
    }

    /* Supprimer un client quand il se déconnecte  */
    public static void removeClient(ClientHandler c) {

        synchronized (listc) {
            listc.remove(c);
        }

        // notifier les autres clients
        broadcast(c.getNom() + " s'est déconnecté", null);
    }
}