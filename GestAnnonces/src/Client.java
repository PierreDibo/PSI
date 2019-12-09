
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Client {

    protected static class Policy {

        protected static final HashMap<String, ArrayList<Contact>> ACCEPTED = new HashMap<>();
        protected static final HashMap<String, ArrayList<Contact>> BANNED = new HashMap<>();

        protected static void create(String pseudo) {
            ACCEPTED.put(pseudo, new ArrayList<>());
            BANNED.put(pseudo, new ArrayList<>());
        }

        protected static Contact isBanned(String mypseudo, String pseudoToFound) {
            return BANNED.get(mypseudo).stream()
                    .filter(contact -> pseudoToFound.equals(contact.pseudo))
                    .findAny()
                    .orElse(null);
        }

        protected static Contact isBanned(String mypseudo, InetAddress addr, int p) {
            return BANNED.get(mypseudo).stream()
                    .filter(contact -> contact.isEquals(addr, p))
                    .findAny()
                    .orElse(null);
        }

        protected static Contact isAccepted(String mypseudo, String pseudoToFound) {
            return ACCEPTED.get(mypseudo).stream()
                    .filter(contact -> pseudoToFound.equals(contact.pseudo))
                    .findAny()
                    .orElse(null);
        }

        protected static Contact isAccepted(String mypseudo, InetAddress addr, int p) {
            return ACCEPTED.get(mypseudo).stream()
                    .filter(contact -> contact.isEquals(addr, p))
                    .findAny()
                    .orElse(null);
        }

        protected static void addContact(String pseudo, Contact contact) {
            ACCEPTED.get(pseudo).add(contact);
        }

        protected static void addBanContact(String pseudo, Contact contact) {
            BANNED.get(pseudo).add(contact);
        }

        protected static void removeContact(String pseudo, String contactToRemove) {
            ArrayList<Contact> contacts = ACCEPTED.get(pseudo);

            contacts.removeIf(c -> c.pseudo.equals(contactToRemove));
        }

        protected static void removeContact(String pseudo, InetAddress addr, int p) {
            ArrayList<Contact> contacts = ACCEPTED.get(pseudo);

            contacts.removeIf(c -> c.isEquals(addr, p));
        }

        protected static void removeBanContact(String pseudo, String contactToRemove) {
            ArrayList<Contact> contacts = BANNED.get(pseudo);

            contacts.removeIf(c -> c.pseudo.equals(contactToRemove));
        }

        protected static void removeBanContact(String pseudo, InetAddress addr, int p) {
            ArrayList<Contact> contacts = BANNED.get(pseudo);

            contacts.removeIf(c -> c.isEquals(addr, p));
        }

        protected static void print(String pseudo) {
            ArrayList<Contact> contacts = ACCEPTED.get(pseudo);
            contacts.forEach(System.out::println);
        }

        protected static void clean(String pseudo) {
            ArrayList<Contact> contacts = ACCEPTED.get(pseudo);
            contacts.forEach(new Consumer<Contact>() {
                @Override
                public void accept(Contact c) {
                    try {
                        c.getSocket().close();
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }

    protected static class PolicySSL {

        protected static final HashMap<String, ArrayList<ContactSSL>> ACCEPTED = new HashMap<>();
        protected static final HashMap<String, ArrayList<ClientRemote>> BANNED = new HashMap<>();

        protected static void create(String pseudo) {
            ACCEPTED.put(pseudo, new ArrayList<>());
            BANNED.put(pseudo, new ArrayList<>());
        }

        protected static ClientRemote isBanned(String mypseudo, String pseudoToFound) {
            return BANNED.get(mypseudo).stream()
                    .filter(contact -> pseudoToFound.equals(contact.pseudo))
                    .findAny()
                    .orElse(null);
        }

        protected static ClientRemote isBanned(String mypseudo, InetAddress addr, int p) {
            return BANNED.get(mypseudo).stream()
                    .filter(contact -> contact.isEquals(addr, p))
                    .findAny()
                    .orElse(null);
        }

        protected static ContactSSL isAccepted(String mypseudo, String pseudoToFound) {
            return ACCEPTED.get(mypseudo).stream()
                    .filter(contact -> pseudoToFound.equals(contact.pseudo))
                    .findAny()
                    .orElse(null);
        }

        protected static ContactSSL isAccepted(String mypseudo, InetAddress addr, int p) {
            return ACCEPTED.get(mypseudo).stream()
                    .filter(contact -> contact.isEquals(addr, p))
                    .findAny()
                    .orElse(null);
        }

        protected static void addContact(String pseudo, ContactSSL contact) {
            ACCEPTED.get(pseudo).add(contact);
        }

        protected static void addBanContact(String pseudo, ClientRemote contact) {
            BANNED.get(pseudo).add(contact);
        }

        protected static void removeContact(String pseudo, String contactToRemove) {
            ArrayList<ContactSSL> contacts = ACCEPTED.get(pseudo);

            contacts.removeIf(c -> c.pseudo.equals(contactToRemove));
        }

        protected static void removeContact(String pseudo, InetAddress addr, int p) {
            ArrayList<ContactSSL> contacts = ACCEPTED.get(pseudo);

            contacts.removeIf(c -> c.isEquals(addr, p));
        }

        protected static void removeBanContact(String pseudo, String contactToRemove) {
            ArrayList<ClientRemote> contacts = BANNED.get(pseudo);

            contacts.removeIf(c -> c.pseudo.equals(contactToRemove));
        }

        protected static void removeBanContact(String pseudo, InetAddress addr, int p) {
            ArrayList<ClientRemote> contacts = BANNED.get(pseudo);

            contacts.removeIf(c -> c.isEquals(addr, p));
        }

        protected static void print(String pseudo) {
            ArrayList<ContactSSL> contacts = ACCEPTED.get(pseudo);
            contacts.forEach(System.out::println);
        }

        protected static void clean(String pseudo) {
            ArrayList<ContactSSL> contacts = ACCEPTED.get(pseudo);
            contacts.forEach(new Consumer<ContactSSL>() {
                @Override
                public void accept(ContactSSL c) {
                    try {
                        c.getSocket().close();
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }

    //PROTOCOL_TSL = 0
    protected static final int ATTENTE = 100, ERROR = -1, IP_GESTIONNAIRE = 0, PORT_GESTIONNAIRE = 1, PROTOCOLE_INDEX = 2,
            IP_CLIENT = 2, IP_CLIENT_SECURE = 3;

    /**
     * @param args the command line arguments
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws UnknownHostException, IOException, KeyStoreException {
        InetAddress ia, clientAddress = null;
        String protocole = null;
        Socket socket;
        int port;

        if (args.length < 3 || args.length > 4) {
            System.err.println("Usage : java Client ip_gestionnaire port_gestionnaire [TLSv1.2] ip_client");
            System.exit(ERROR);
        }

        ia = InetAddress.getByName(args[IP_GESTIONNAIRE]);
        port = Integer.parseInt(args[PORT_GESTIONNAIRE]);
        socket = new Socket(ia, port);

        switch (args.length) {
            case 3:
                clientAddress = InetAddress.getByName(args[IP_CLIENT]);
                break;
            case 4:
                protocole = args[PROTOCOLE_INDEX];
                clientAddress = InetAddress.getByName(args[IP_CLIENT_SECURE]);
                break;
            default:
                System.err.println("Should not be reached");
                System.exit(ERROR);
                break;
        }

        ClientEcouteur ecouteur = new ClientEcouteur(socket, protocole, clientAddress);
        new Thread(ecouteur).start();
        new Thread(new ClientEcrivain(socket, ecouteur)).start();
    }

}
