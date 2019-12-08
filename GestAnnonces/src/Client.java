
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Client {

    protected static class Policy {

        protected static final HashMap<String, Contact> ACCEPTED = new HashMap<>();
        protected static final HashMap<String, Contact> BANNED = new HashMap<>();

        protected static Contact isBanned(String pseudo) {
            return BANNED.get(pseudo);
        }

        protected static Contact isBanned(InetAddress addr, int port) throws UnknownHostException {
            for (Contact c : BANNED.values()) {
                if (c.getIaddr().equals(addr)) {
                    return c;
                }
            }
            return null;
        }

        protected static Contact isAccepted(String pseudo) {
            return ACCEPTED.get(pseudo);
        }

        protected static Contact isAccepted(InetAddress addr, int port) throws UnknownHostException {
            for (Contact c : ACCEPTED.values()) {
                if (c.getIaddr().equals(addr)) {
                    return c;
                }
            }
            return null;
        }

        protected static void addContact(Contact contact) {
            ACCEPTED.put(contact.getPseudo(), contact);
        }

        protected static boolean alreadyConnected(InetAddress addr, int port) throws UnknownHostException {
            return isBanned(addr, port) == null ? isAccepted(addr, port) == null : true;
        }

        protected static void addBanContact(String pseudo) {
            Contact contact = isAccepted(pseudo);
            if (isBanned(pseudo) == null) {
                if (contact != null) {
                    BANNED.put(pseudo, contact);
                    removeContact(pseudo);
                }
            }
        }

        protected static void removeContact(String peudo) {
            ACCEPTED.remove(peudo);
        }

        protected static void removeBanContact(String peudo) {
            BANNED.remove(peudo);
        }
    }

    //PROTOCOL_TSL = 0
    protected static String protocole = null;
    protected static String pseudo;
    protected static final int ATTENTE = 100, ERROR = -1, IP_GESTIONNAIRE = 0, PORT_GESTIONNAIRE = 1, PROTOCOLE_INDEX = 2;

    /**
     * @param args the command line arguments
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
        InetAddress ia;
        Socket socket;
        int port;

        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage : java Client ip_gestionnaire port_gestionnaire");
            System.exit(ERROR);
        }

        ia = InetAddress.getByName(args[IP_GESTIONNAIRE]);
        port = Integer.parseInt(args[PORT_GESTIONNAIRE]);
        socket = new Socket(ia, port);

        if (args.length == 3) {
            protocole = args[PROTOCOLE_INDEX];
        }

        new Thread(new ClientEcrivain(socket)).start();
        new Thread(new ClientEcouteur(socket)).start();

    }

}
