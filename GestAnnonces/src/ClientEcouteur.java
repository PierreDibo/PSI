
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
            BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            for (String message; (message = br.readLine()) != null;) {
                String msg = message.substring(message.length() - 3, message.length());
                if (msg.equals(MessageType.END.getMessage())) {
                    message = message.replace("***", " ***");
                }
                parsing(message);
            }
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void parsing(String message) throws UnknownHostException {
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
            case BYE:
                System.out.println(this.messageType.getMessage());
                break;
            case INVALID:
                System.out.println(String.join(ESP, Arrays.copyOfRange(msg, i, msg.length)));
                break;
            case CONNECT_SUCCESS:
                pseudo = msg[i++];
                System.out.println(message);
                new Thread(new ClientServeur(InetAddress.getByName(msg[i++]), Integer.parseInt(msg[i++]))).start();
                break;
            default:
                //System.out.println(String.join(ESP, Arrays.copyOfRange(msg, i, msg.length)));
                System.out.println(message);
                break;
        }
    }

}
