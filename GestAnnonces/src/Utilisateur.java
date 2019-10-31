
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Pierre Dibo
 */
public class Utilisateur {

    private String pseudo;
    private char[] motDePasse;
    private final Socket socket;

    public Utilisateur(String pseudo, char[] motDePasse, Socket socket) {
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
        this.socket = socket;
    }

    public String getPseudo() {
        return pseudo;
    }

    public char[] getMotDePasse() {
        return motDePasse;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public void setMotDePasse(char[] motDePasse) {
        this.motDePasse = motDePasse;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.pseudo);
        hash = 43 * hash + Arrays.hashCode(this.motDePasse);
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
        if (!Objects.equals(this.pseudo, other.pseudo)) {
            return false;
        }
        return Arrays.equals(this.motDePasse, other.motDePasse);
    }

    @Override
    public String toString() {
        return "Utilisateur{" + "pseudo=" + pseudo + '}';
    }

}