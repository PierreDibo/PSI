import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class ServiceUDP implements Runnable {

    final static int taille = 217;            // Taille des messages UDP  200 pour le msg + la taille du type de message et ses paramètres :                //  System.out.println("Message reçu : " + msg);				//Affichage de tout le message : MESP ...  Reste a n'affiche r QUE le message, pas la commande avec le message
    boolean connect = true;
    DatagramSocket ds;


    public ServiceUDP(DatagramSocket ds) {
        this.ds = ds;
    }


    private void afficheMessageClient(String mess) {
        String message = "";
        String id;

        String[] messArray;
        String messClean = mess.replace("+++", ""); //Supprimer les +++ a la fin du msg reçu
        messArray = messClean.split(" ");

        id = messArray[1];
        for (int i = 2; i < messArray.length; i++) {
            message += (messArray[i] + " ");
        }
        System.out.println("\t" + id + " vous envoie : " + message);

    }

    public void run() {
        try {
            byte[] message = new byte[taille];
            DatagramPacket paquetRecu = new DatagramPacket(message, message.length);
            while (connect)                // Tant que le joueur ne s'est pas déconnecter.
            {
                ds.receive(paquetRecu);
                String msg = new String(paquetRecu.getData());
                afficheMessageClient(msg);

            }

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}