
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Gestionnaire {

    protected static final HashMap<Utilisateur, HashSet<Annonce>> ANNONCES = new HashMap<>();
    protected static final int ERROR = -1, INDEX_ADRESS = 0, INDEX_PORT = 1, ATTENTE = 100;
    protected static final byte[] SALT = new byte[16];
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage : java Gestionnaire ip port");
            System.exit(ERROR);
        }
        
        new SecureRandom().nextBytes(SALT);

        switch (args.length) {
            case 2:
                try (final ServerSocket server = new ServerSocket(Integer.parseInt(args[INDEX_PORT]), ATTENTE, InetAddress.getByName(args[INDEX_ADRESS]))) {
                    while (true) {
                        Socket clientSocket = server.accept();
                        new Thread(new GestionnaireEcouteur(clientSocket)).start();
                        new Thread(new GestionnaireEcrivain(clientSocket, MessageType.MSG_WELCOME)).start();
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case 3:
                break;
        }
    }

}
