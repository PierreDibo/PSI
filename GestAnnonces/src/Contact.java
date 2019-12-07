
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Contact extends Client implements Runnable, MessagesServeurClient {

    private final String pseudo;
    private final InetAddress iaddr;
    private final int port;
    private final Socket socket;
    private MessageType messageType;
    
    public Contact(String ps, InetAddress addr, int p, Socket s) {
        this.pseudo = ps;
        this.iaddr = addr;
        this.port = p;
        this.socket = s;
        this.messageType = null;
    }

    public String getPseudo() {
        return pseudo;
    }

    public InetAddress getIaddr() {
        return iaddr;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void write(String msg) throws IOException {
        if (!socket.isClosed()) {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            output.write(msg);
            output.newLine();
            output.flush();
        }
    }

     @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                for (String message; (message = br.readLine()) != null;) {
                    System.out.println(message);
                    //parsing(message);
                }
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            } /*catch (InterruptedException ex) {
                Logger.getLogger(ClientServeur.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        /*

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
                    parse_callOpen(msg);
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

        private void parse_callOpen(String[] msg) throws IOException, InterruptedException {
            String pseudo;
            InetAddress addr;
            int port, i;

            i = 0;

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
        }*/
}
