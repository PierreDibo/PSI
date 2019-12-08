
import java.net.InetAddress;

/**
 *
 * @author dibop
 */
public class ClientRemote {

    private final String pseudo;
    private final InetAddress iaddr;
    private final int port;

    public ClientRemote(String pseudo, InetAddress iaddr, int port) {
        this.pseudo = pseudo;
        this.iaddr = iaddr;
        this.port = port;
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

}
