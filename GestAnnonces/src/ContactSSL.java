
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ContactSSL extends Client implements Runnable {

    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    protected final ClientRemote remote;
    protected final SSLSocket socket;
    protected String pseudo;
    protected InetAddress iaddr;
    protected int port;
    protected MessageType messageType;

    public ContactSSL(ClientRemote remote, SSLSocket socket) {
        this.remote = remote;
        this.socket = socket;
        this.pseudo = null;
    }

    public ContactSSL(ClientRemote remote, SSLSocket socket, String pseudo, InetAddress iaddr, int port) {
        this.remote = remote;
        this.socket = socket;
        this.pseudo = pseudo;
        this.iaddr = iaddr;
        this.port = port;
    }

    protected void write(String msg) throws IOException {
        if (this.socket.isConnected()) {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            output.write(msg + MessageType.END.getMessage());
            output.newLine();
            output.flush();
        }
    }

    public void shutdown() throws IOException {
        this.socket.close();
        this.executor.shutdown();
    }

    public ClientRemote getRemote() {
        return remote;
    }

    public SSLSocket getSocket() {
        return socket;
    }

    public String getPseudo() {
        return this.pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public InetAddress getIaddr() {
        return this.iaddr;
    }

    public void setIaddr(InetAddress iaddr) {
        this.iaddr = iaddr;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            for (String message; !this.socket.isClosed() && (message = br.readLine()) != null;) {
                String msg = message.substring(message.length() - 3, message.length());
                if (msg.equals(MessageType.END.getMessage())) {
                    message = message.replace("***", " ***");
                }
                try {
                    parsing(message);
                } catch (NumberFormatException ex) {
                    System.err.println("Erreur message envoyé");
                    write(MessageType.MSG_INVALID);
                }
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void parsing(String message) throws UnknownHostException, IOException {
        String[] input = message.split("\\s+");
        int i = 0;
        try {
            messageType = MessageType.valueOf(input[i++]);
        } catch (IllegalArgumentException ex) {
            messageType = MessageType.INVALID;
        }

        switch (this.messageType) {
            case CALL_OPEN:
                System.out.println(message);
                parse_callOpen(input, i);
                break;
            case CALL_OPEN_SUCCESS:
                System.out.println(message);
                parse_callOpenSuccess(input, i);
                break;
            case CALL_OPEN_ERROR:
                System.out.println(message);
                break;
            case CALL:
                System.out.println("From " + this.pseudo + ": " + String.join(" ", Arrays.copyOfRange(input, i, input.length)));
                write("SENT");
                break;
            case CALL_CLOSE:
                System.out.println(message);
                parse_callClose();
                break;
            case CALL_CLOSE_OK:
                System.out.println(message);
                parse_callCloseOk();
                break;
            case SENT:
                System.out.println(message);
                break;
            case ALREADY_CONNECTED:
                System.out.println(message);
                break;
            default:
                System.out.println(message);
                System.err.println("Should not be reached");
                System.exit(ERROR);
                break;
        }
    }

    private void parse_callOpen(String[] input, int i) throws UnknownHostException, IOException {
        String contactPseudo = input[i++];
        InetAddress contactAddr = InetAddress.getByName(input[i++]);
        int contactPort = Integer.parseInt(input[i++]);

        if (this.pseudo == null) {
            setPseudo(contactPseudo);
            setIaddr(contactAddr);
            setPort(contactPort);
            if (Policy.isBanned(this.remote.getPseudo(), this.pseudo) == null) {
                if (Policy.isAccepted(this.remote.getPseudo(), this.pseudo) == null) {
                    write(MessageType.CALL_OPEN_SUCCESS.name() + " " + this.remote.getPseudo());
                    PolicySSL.addContact(this.remote.getPseudo(), this);
                } else {
                    write(MessageType.MSG_CALL_OPEN_FAILURE + "\n" + MessageType.MSG_CONTACT_ALREADY_CONNECTED);
                }
            } else {
                write(MessageType.MSG_CALL_OPEN_FAILURE + "\n" + MessageType.MSG_CONTACT_BANNED);
                System.out.println(MessageType.MSG_CONTACT_BANNED);
            }
        } else {
            write(MessageType.MSG_CALL_OPEN_FAILURE + "\n" + MessageType.MSG_CONTACT_ALREADY_CONNECTED);
            System.out.println(MessageType.MSG_CONTACT_ALREADY_CONNECTED);
        }
    }

    private void parse_callOpenSuccess(String[] input, int i) throws IOException {
        String contactPseudo = input[i++];

        if (this.pseudo != null) {
            return;
        }
        setPseudo(contactPseudo);
        if (Policy.isBanned(this.remote.getPseudo(), this.pseudo) == null) {
            if (Policy.isAccepted(this.remote.getPseudo(), this.pseudo) == null) {
                write(MessageType.CALL_OPEN_SUCCESS.name() + " " + this.remote.getPseudo());
                PolicySSL.addContact(this.remote.getPseudo(), this);
            } else {
                write(MessageType.MSG_CALL_OPEN_FAILURE + "\n" + MessageType.MSG_CONTACT_ALREADY_CONNECTED);
                System.out.println(MessageType.MSG_CONTACT_ALREADY_CONNECTED);
            }
        } else {
            write(MessageType.MSG_CALL_OPEN_FAILURE + "\n" + MessageType.MSG_CONTACT_BANNED);
            System.out.println(MessageType.MSG_CONTACT_BANNED);
        }
    }

    private void parse_callClose() throws IOException {
        Contact contact = Policy.isAccepted(this.remote.getPseudo(), this.pseudo);
        String infos = MessageType.CALL_CLOSE_OK.name();
        contact.write(infos);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.shutdown();
        Policy.removeContact(this.remote.getPseudo(), this.pseudo);
        System.out.println(infos);
    }

    private void parse_callCloseOk() throws IOException {
        Contact contact = Policy.isAccepted(this.remote.getPseudo(), this.pseudo);
        String infos = MessageType.CALL_CLOSE_OK.name();
        contact.write(infos);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.shutdown();
        Policy.removeContact(this.remote.getPseudo(), this.pseudo);
        System.out.println(infos);
    }

    public boolean isEquals(InetAddress addr, int p) {
        return iaddr != null && port != 0 && iaddr.equals(addr) && port == p;
    }

    @Override
    public String toString() {
        return "Contact{" + "pseudo=" + pseudo + ", iaddr=" + iaddr + ", port=" + port + '}';
    }

}
