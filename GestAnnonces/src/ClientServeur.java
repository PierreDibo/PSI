
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ClientServeur extends Client implements Runnable {

    private final ClientRemote client;

    public ClientServeur(ClientRemote client) {
        this.client = client;
    }

    @Override
    public void run() {
        Policy.create(this.client.getPseudo());
        try (final ServerSocket server = new ServerSocket(this.client.getPort(), ATTENTE, this.client.getIaddr())) {
            while (true) {
                Socket clientSocket = server.accept();
                new Thread(new Contact(this.client, clientSocket)).start();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
