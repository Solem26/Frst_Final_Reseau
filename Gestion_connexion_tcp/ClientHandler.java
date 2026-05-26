package Gestion_connexion_tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private String nom; 
    private Socket soc;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.soc = socket;
    }

    public String getNom() {
        return nom;
    }

    /*Envoyer un message à un client */
    public void sendMessage(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {

        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(soc.getInputStream(), "UTF-8")
            );

            out = new PrintWriter(soc.getOutputStream(), true);

            /* le client envoie son nom  */
            nom = in.readLine();

            System.out.println(nom + " s'est connecté");

            // diffusion aux autres clients
            ChatServer.broadcast(nom + " s'est connecté", null);

            String message;

            /* Boucle  de réception des messages*/
            while (true) {

                message = in.readLine();

                if (message == null || message.equalsIgnoreCase("QUIT")) {
                    break;
                }

                System.out.println(nom + ": " + message);

                // diffusion du message à tous les clients
                ChatServer.broadcast(nom + ": " + message, this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                 // suppression du client
                ChatServer.removeClient(this);
                soc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

           
        }
    }
}