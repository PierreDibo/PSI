
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
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
public class ClientServeur extends Client implements Runnable {

    private final InetAddress iaddr;
    private final int port;

    public ClientServeur(InetAddress addr, int port) {
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

    static class Ecouteur implements Runnable, MessagesServeurClient {

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
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientServeur.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void parsing(String message) throws IOException, InterruptedException {
            String[] msg;
            int i;

            msg = message.split("\\s+");
            i = 0;

            try {
                this.messageType = MessageType.valueOf(msg[i++]);
            } catch (IllegalArgumentException ex) {
                this.messageType = MessageType.INVALID;
            }
            msg = Arrays.copyOfRange(msg, i, msg.length);
            switch (this.messageType) {
                case CALL_OPEN:
                    parse_callOpen(msg, i);
                    break;
                case CALL:
                    System.out.println(String.join(" ", Arrays.copyOfRange(msg, i, msg.length)));
                    break;
                case CALL_CLOSE:
                    System.out.println(String.join(" ", Arrays.copyOfRange(msg, i - 1, msg.length)));
                    parse_callClose(msg);
                    break;
                default:
                    System.out.println(String.join(" ", Arrays.copyOfRange(msg, i - 1, msg.length)));
                    invalid(this.socket);
                    break;
            }
        }

        private void parse_callOpen(String[] msg, int i) throws IOException, InterruptedException {
            String pseudo;
            InetAddress addr;
            int port;


            System.out.println(String.join(" ", Arrays.copyOfRange(msg, i, msg.length)));

            pseudo = msg[i++];
            addr = InetAddress.getByName(msg[i++]);
            port = Integer.parseInt(msg[i++]);

            if (msg.length == messageType.getParameters()) {
                if (Policy.isBanned(pseudo) == null) {
                    if (Policy.isAccepted(pseudo) == null) {
                        Contact contact = new Contact(pseudo, addr, port, this.socket);
                        Policy.addContact(contact);
                        callOpenSuccess(this.socket, pseudo);
                    } else {
                        contactAlreadyConnected(this.socket, pseudo);
                    }
                } else {
                    contactBanned(this.socket);
                    this.socket.close();
                }
            } else {
                callOpenFailure(this.socket);
                this.socket.close();
            }
        }

        private void parse_callClose(String[] msg) throws InterruptedException {
            String pseudo;
            int i;

            i = 0;
            pseudo = msg[i++];

            if (msg.length == messageType.getParameters()) {
                Policy.removeContact(pseudo);
            } else {
                invalid(this.socket);
            }
        }

    }

}
