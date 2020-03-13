
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ClientEcouteur implements Runnable {

    private static final Object LOCK = new Object();

    private final Socket socket;
    private final String protocole;
    private ClientRemote clientRemote;

    private final InetAddress clientAddr;
    private MessageType messageType;

    public ClientEcouteur(Socket socket, String protocole, InetAddress clientAddress) {
        this.socket = socket;
        this.protocole = protocole;
        this.clientRemote = null;
        this.clientAddr = clientAddress;
        this.messageType = null;
    }

    public String getProtocole() {
        return protocole;
    }

    public InetAddress getClientAddr() {
        return clientAddr;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public ClientRemote getClientRemote() {
        return clientRemote;
    }

    public void setClientRemote(ClientRemote clientRemote) {
        this.clientRemote = clientRemote;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            for (String message; !this.socket.isClosed() && (message = br.readLine()) != null;) {
                String msg = message;
                if (message.length() > 0 && message.substring(0, 3).equals("***")) {
                    message = message.substring(3, message.length());
                }
                if (message.length() - 3 > 0) {
                    msg = message.substring(message.length() - 3, message.length());
                }
                if (msg.equals(MessageType.END.getMessage())) {
                    message = message.replace("***", " ***");
                }
                try {
                    parsing(message, br);
                } catch (NumberFormatException ex1) {
                    System.err.println(MessageType.MSG_INVALID);
                }
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void parsing(String message, BufferedReader br) throws UnknownHostException, IOException {
        String[] msg;
        int i;

        System.out.println(message);

        msg = message.split("\\s+");
        i = 0;

        try {
            this.messageType = MessageType.valueOf(msg[i++]);
        } catch (IllegalArgumentException ex) {
            this.messageType = MessageType.INVALID;
        }

        switch (this.messageType) {
            case CONNECT_SUCCESS:
                if (this.protocole == null) {
                    parse_connectSuccess(msg, i);
                } else {
                    parse_connectSuccessSSL(msg, i);
                }
                break;
            case BYE:
                this.socket.close();
                break;
            default:
                break;
        }
    }

    private void parse_connectSuccess(String[] msg, int i) throws UnknownHostException {
        setClientRemote(new ClientRemote(msg[i++], clientAddr, Integer.parseInt(msg[i++])));
        new Thread(new ClientServeur(clientRemote)).start();
    }

    private void parse_connectSuccessSSL(String[] msg, int i) throws UnknownHostException {
        try {
            setClientRemote(new ClientRemote(msg[i++], clientAddr, Integer.parseInt(msg[i++])));
            SSLClientServeur serverRunnable = new SSLClientServeur(this.protocole, this.clientAddr, clientRemote.port);
            serverRunnable.start();
            synchronized (LOCK) {
                LOCK.wait();
            }
            serverRunnable.stop();
        } catch (NoSuchAlgorithmException | IOException | InterruptedException ex) {
            Logger.getLogger(ClientEcouteur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
