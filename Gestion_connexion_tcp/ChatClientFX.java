package Gestion_connexion_tcp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ChatClientFX extends Application {

    // Composants de l'interface graphique
    private TextArea areaMessages;
    private Label labelAnnonceBroadcast;
    private TextField champSaisie;
    private TextField champPseudo;
    private TextField champIP;
    private Button boutonConnexion;
    private Button boutonEnvoyer;

    // Éléments de connexion Réseau
    private Socket socketTCP;
    private PrintWriter outTCP;
    private BufferedReader inTCP;
    private DatagramSocket socketEnvoiUDP; // Pour envoyer vers le port 6000
    private DatagramSocket socketReceptionBroadcast; // Pour écouter le port 7000

    private boolean estConnecte = false;
    private boolean ecouteBroadcast = true;
    private String nomUtilisateur;

    // Configuration des ports d'après vos fichiers
    private final int PORT_TCP = 5000;
    private final int PORT_UDP_NOTIF = 6000;
    private final int PORT_UDP_ANNOUNCE = 7000;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Plateforme Messagerie Multi-Protocole (Aguila)");

        // --- 1. ZONE DE CONNEXION & BANDEAU DE BROADCAST (Haut) ---
        VBox conteneurHaut = new VBox(5);
        conteneurHaut.setPadding(new Insets(10));
        conteneurHaut.setStyle("-fx-background-color: #f4f4f4;");

        HBox barreConnexion = new HBox(10);
        champPseudo = new TextField("Aguila");
        champPseudo.setPromptText("Votre nom");
        champPseudo.setPrefWidth(120);

        champIP = new TextField("127.0.0.1");
        champIP.setPromptText("Adresse IP");
        champIP.setPrefWidth(120);

        boutonConnexion = new Button("Se connecter");
        barreConnexion.getChildren().addAll(
                new Label("Nom :"), champPseudo,
                new Label("IP :"), champIP,
                boutonConnexion
        );

        // Bandeau pour afficher l'annonce magique reçue du port 7000
        labelAnnonceBroadcast = new Label("Recherche de serveurs en cours (Broadcast port 7000)...");
        labelAnnonceBroadcast.setStyle("-fx-background-color: #e6f2ff; -fx-text-fill: #0066cc; -fx-padding: 5px; -fx-font-weight: bold;");
        labelAnnonceBroadcast.setMaxWidth(Double.MAX_VALUE);

        conteneurHaut.getChildren().addAll(barreConnexion, labelAnnonceBroadcast);

        // --- 2. ZONE D'HISTORIQUE DES MESSAGES (Centre) ---
        areaMessages = new TextArea();
        areaMessages.setEditable(false);
        areaMessages.setWrapText(true);

        // --- 3. ZONE DE SAISIE DE MESSAGE (Bas) ---
        HBox barreSaisie = new HBox(10);
        barreSaisie.setPadding(new Insets(10));

        champSaisie = new TextField();
        champSaisie.setPromptText("Écrivez votre message ici...");
        champSaisie.setPrefWidth(460);
        champSaisie.setDisable(true);

        boutonEnvoyer = new Button("Envoyer");
        boutonEnvoyer.setDisable(true);

        barreSaisie.getChildren().addAll(champSaisie, boutonEnvoyer);

        // --- 4. ARCHITECTURE ---
        BorderPane racine = new BorderPane();
        racine.setTop(conteneurHaut);
        racine.setCenter(areaMessages);
        racine.setBottom(barreSaisie);

        // --- 5. ACTIONS ---
        boutonConnexion.setOnAction(e -> gererConnexion());
        boutonEnvoyer.setOnAction(e -> envoyerMessage());
        champSaisie.setOnAction(e -> envoyerMessage());

        // Gestion de la fermeture de l'application
        primaryStage.setOnCloseRequest(e -> deconnecterCompletement());

        // ÉTAPE CLÉ : Démarrer l'écoute permanente du Broadcast (Port 7000) dès le lancement
        new Thread(this::ecouterBroadcastPort7000).start();

        Scene scene = new Scene(racine, 600, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * THREAD : Écoute en continu le port 7000 (Équivalent de votre ClientAnnonce)
     */
    private void ecouterBroadcastPort7000() {
        byte[] buffer = new byte[1024];
        try {
            // Écoute sur le port 7000
            socketReceptionBroadcast = new DatagramSocket(PORT_UDP_ANNOUNCE);

            while (ecouteBroadcast) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socketReceptionBroadcast.receive(packet); // Bloquant

                String annonce = new String(packet.getData(), 0, packet.getLength());

                // Mise à jour du bandeau bleu en haut de l'interface graphique
                Platform.runLater(() -> labelAnnonceBroadcast.setText("Annonce Réseau : " + annonce));
            }
        } catch (Exception e) {
            // Fermeture normale du socket d'écoute
        }
    }

    /**
     * Gère la connexion TCP (Port 5000) et l'envoi de la notification UDP (Port 6000)
     */
    private void gererConnexion() {
        if (!estConnecte) {
            String ip = champIP.getText().trim();
            nomUtilisateur = champPseudo.getText().trim();

            if (nomUtilisateur.isEmpty()) {
                areaMessages.appendText("[Système] Veuillez entrer un nom.\n");
                return;
            }

            try {
                // 1. Connexion TCP
                socketTCP = new Socket(ip, PORT_TCP);
                outTCP = new PrintWriter(socketTCP.getOutputStream(), true);
                inTCP = new BufferedReader(new InputStreamReader(socketTCP.getInputStream(), "UTF-8"));

                // 2. Socket pour envoyer la notification au NotificationServer (Port 6000)
                socketEnvoiUDP = new DatagramSocket();

                // 3. Protocole : Envoyer le nom en premier au serveur TCP
                outTCP.println(nomUtilisateur);

                // 4. Notification UDP transmise au serveur de notif (Port 6000)
                sendUDP("NOTIF " + nomUtilisateur + " connecté", ip, PORT_UDP_NOTIF);

                estConnecte = true;
                areaMessages.appendText("[Système] Vous avez rejoint le chat.\n");

                // Changements graphiques
                boutonConnexion.setText("Déconnexion");
                champSaisie.setDisable(false);
                boutonEnvoyer.setDisable(false);
                champPseudo.setDisable(true);
                champIP.setDisable(true);

                // Thread d'écoute des messages du chat (TCP)
                new Thread(this::ecouterMessagesTCP).start();

            } catch (Exception ex) {
                areaMessages.appendText("[Erreur] Connexion impossible : " + ex.getMessage() + "\n");
            }
        } else {
            deconnecter();
        }
    }

    private void ecouterMessagesTCP() {
        try {
            String msg;
            while (estConnecte && (msg = inTCP.readLine()) != null) {
                final String messageRecu = msg;
                Platform.runLater(() -> areaMessages.appendText(messageRecu + "\n"));
            }
        } catch (Exception e) {
            Platform.runLater(() -> areaMessages.appendText("[Système] Connexion fermée.\n"));
        } finally {
            Platform.runLater(this::reinitialiserInterface);
        }
    }

    private void envoyerMessage() {
        String message = champSaisie.getText().trim();
        if (!message.isEmpty()) {
            if (message.equalsIgnoreCase("QUIT")) {
                deconnecter();
            } else {
                outTCP.println(message);
                champSaisie.clear();
            }
        }
    }

    /**
     * Envoi d'un paquet UDP vers le serveur de notification (Port 6000)
     */
    private void sendUDP(String message, String host, int port) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            if (socketEnvoiUDP != null && !socketEnvoiUDP.isClosed()) {
                socketEnvoiUDP.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deconnecter() {
        if (estConnecte) {
            estConnecte = false;
            try {
                if (outTCP != null) outTCP.println("QUIT");

                // Envoyer notification de déconnexion sur le port 6000
                sendUDP("NOTIF " + nomUtilisateur + " déconnecté", champIP.getText().trim(), PORT_UDP_NOTIF);

                if (socketTCP != null) socketTCP.close();
                if (socketEnvoiUDP != null) socketEnvoiUDP.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            reinitialiserInterface();
        }
    }

    /**
     * Coupe TOUT, y compris le récepteur de broadcast du port 7000 lors de la fermeture de la fenêtre
     */
    private void deconnecterCompletement() {
        deconnecter();
        ecouteBroadcast = false;
        if (socketReceptionBroadcast != null && !socketReceptionBroadcast.isClosed()) {
            socketReceptionBroadcast.close();
        }
    }

    private void reinitialiserInterface() {
        estConnecte = false;
        boutonConnexion.setText("Se connecter");
        champSaisie.setDisable(true);
        boutonEnvoyer.setDisable(true);
        champPseudo.setDisable(false);
        champIP.setDisable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}