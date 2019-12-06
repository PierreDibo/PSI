
import java.net.Socket;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Contact implements Runnable {

    private final String pseudo;
    private final Socket socket;

    public Contact(String ps, Socket s) {
        this.pseudo = ps;
        this.socket = s;
    }

    public String getPseudo() {
        return pseudo;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
