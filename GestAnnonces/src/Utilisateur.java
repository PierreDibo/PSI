
import java.net.Socket;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Utilisateur {

    private final int identifiant;
    private static int compteur = 0;
    private String pseudo;
    private String motDePasse;
    private final Socket socket;
    private final String ip;
    private final int port;

    public Utilisateur(String pseudo, String motDePasse, Socket socket) {
        this.identifiant = compteur++;
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
        this.socket = socket;
        this.ip = null;
        this.port = 0;
    }

    public Utilisateur(String pseudo, String motDePasse, Socket socket, String ip, int p) {
        this.identifiant = compteur++;
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
        this.socket = socket;
        this.ip = ip;
        this.port = p;
    }

    public Utilisateur(int id, String pseudo, String motDePasse, Socket socket) {
        this.identifiant = id;
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
        this.socket = socket;
        this.ip = null;
        this.port = 0;
    }

    public Utilisateur(int id) {
        this.identifiant = id;
        this.pseudo = null;
        this.motDePasse = null;
        this.socket = null;
        this.ip = null;
        this.port = 0;
    }

    public int getIdentifiant() {
        return identifiant;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.identifiant;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Utilisateur other = (Utilisateur) obj;
        return this.identifiant == other.identifiant;
    }

    @Override
    public String toString() {
        return "Utilisateur -> " + "pseudo : " + pseudo + ", identifiant : " + identifiant;
    }

}
