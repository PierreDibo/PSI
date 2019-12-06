
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

    static class Policy {

        protected static final HashMap<String, Contact> ACCEPTED = new HashMap<>();
        protected static final HashMap<String, Contact> BANNED = new HashMap<>();
    }

    //PROTOCOL_TSL = 0
    private static final int ERROR = -1, IP_GESTIONNAIRE = 0, PORT_GESTIONNAIRE = 1;

    /**
     * @param args the command line arguments
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
        Socket socket;

        if (args.length != 2) {
            System.err.println("Usage : java Client ip_gestionnaire port_gestionnaire");
            System.exit(ERROR);
        }
        
        InetAddress ia = InetAddress.getByName(args[IP_GESTIONNAIRE]);
        int port = Integer.parseInt(args[PORT_GESTIONNAIRE]);
        
        switch (args.length) {
            case 2:
                socket = new Socket(ia, port);

                new Thread(new ClientEcrivain(socket)).start();
                new Thread(new ClientEcouteur(socket)).start();
                break;
        }
    }

}
