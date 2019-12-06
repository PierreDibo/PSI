
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ClientEcouteur extends Client implements Runnable {

    private static final String ESP = " ";

    private final Socket socket;
    private MessageType messageType;

    public ClientEcouteur(Socket s) {
        this.socket = s;
        this.messageType = null;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            for (String message; (message = br.readLine()) != null;) {
                System.out.println(message);
                //parsing(message);
            }
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void parsing(String message) {
        String[] msg;
        int i;

        msg = message.split("\\s+");
        i = 0;

        try {
            this.messageType = MessageType.valueOf(msg[i++]);
        } catch (IllegalArgumentException ex) {
            this.messageType = MessageType.INVALID;
        }

        switch (this.messageType) {
            case CALL_OPEN:

                break;
            case CALL:

                break;
            case CALL_CLOSE:

                break;
            case BYE:
                System.out.println(this.messageType.getMessage());
                break;
            case INVALID:
                System.out.println(String.join(ESP, Arrays.copyOfRange(msg, i, msg.length)));
                break;
            default:
                //System.out.println(String.join(ESP, Arrays.copyOfRange(msg, i, msg.length)));
                System.out.println(message);
                break;
        }
    }

}
