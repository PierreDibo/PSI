
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 */
public class Gestionnaire implements Runnable {

    public static final int ATTENTE = 100;
    private final Socket client;

    public Gestionnaire(Socket c) {
        this.client = c;
    }

    public void addAnnonce() {

    }

    public void deleteAnnonce() {

    }

    public void checkAllAnnonces() {

    }

    public void empty() {
        System.out.println("ADD_ANNONCE"
                + "DELETE_ANNONCE"
                + "UPDATE_ANNONCE"
                + "CHECK_ALL_ANNONCES"
                + "CHECK_ANNONCES_CLIENT"
                + "CHECK_ANNONCES_DOMAINE"
                + "ANNONCES"
                + "ANNONCE"
                + "CONTACT"
                + "CONNECT"
                + "NEW nom prenom mdp"
                + "QUIT"
                + "HELP"
        );
    }

    @Override
    public void run() {
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {

        }
        try (final ServerSocket server = new ServerSocket(Integer.parseInt(args[1]), ATTENTE, InetAddress.getByName(args[0]))) {
            while (true) {
                Socket clientSocket = server.accept();
                Thread th = new Thread((new Gestionnaire(clientSocket)));
                th.start();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
