
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dibop
 */
public class ClientServeur extends Client implements Runnable {

    private final String pseudo;
    private final InetAddress iaddr;
    private final int port;

    public ClientServeur(String pseudo, InetAddress addr, int port) {
        this.pseudo = pseudo;
        this.iaddr = addr;
        this.port = port;
    }

    @Override
    public void run() {
        try (final ServerSocket server = new ServerSocket(port, ATTENTE, iaddr)) {
            while (true) {
                Socket clientSocket = server.accept();
                new Thread(new Ecouteur(clientSocket)).start();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class Ecouteur implements Runnable {

        private final Socket socket;
        private MessageType messageType;

        public Ecouteur(Socket s) {
            this.socket = s;
            this.messageType = null;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                for (String message; (message = br.readLine()) != null;) {
                    parsing(message);
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
                default:
                    
                    break;
            }
        }
    }

}
