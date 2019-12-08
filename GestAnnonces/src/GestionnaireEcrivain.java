
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class GestionnaireEcrivain extends Gestionnaire implements Runnable {

    private final Socket socket;
    private final String message;

    public GestionnaireEcrivain(Socket s, String msg) {
        this.socket = s;
        this.message = msg;
    }

    @Override
    public void run() {
        try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            output.write(this.message + MessageType.END.getMessage());
            output.newLine();
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
