
import java.net.InetAddress;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ClientRemote {

    protected final String pseudo;
    protected final InetAddress iaddr;
    protected final int port;

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

    public boolean isEquals(InetAddress addr, int p) {
        return iaddr.equals(addr) && port == p;
    }
}
