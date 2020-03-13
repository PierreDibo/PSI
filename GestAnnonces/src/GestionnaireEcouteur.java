
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class GestionnaireEcouteur extends Gestionnaire implements Runnable, MessagesGestionnaire {

    private final Socket socket;
    private Utilisateur currentUser;
    private MessageType messageType;

    public GestionnaireEcouteur(Socket s) {
        this.socket = s;
        this.currentUser = null;
        this.messageType = null;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            for (String msg; (msg = input.readLine()) != null;) {
                System.out.println(msg);
                String message = msg.substring(msg.length() - 3, msg.length());
                if (message.equals(MessageType.END.getMessage())) {
                    msg = msg.replace("***", " ***");
                }
                if (msg.endsWith(MessageType.END.getMessage())) {
                    try {
                        parsing(msg);
                    } catch (NumberFormatException ex1) {
                        invalid(this.socket);
                    }
                } else {
                    invalid(this.socket);
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.currentUser = null;
    }

    public void parsing(String message) throws InterruptedException {
        String[] msg;
        int i;

        msg = message.split("\\s+");
        i = 0;

        try {
            this.messageType = MessageType.valueOf(msg[i++]);
        } catch (IllegalArgumentException ex) {
            this.messageType = MessageType.INVALID;
        }

        switch (this.messageType) {
            case NEW:
                parse_newUtilisateur(msg, i);
                break;
            case CONNECT:
                parse_connectUtilisateur(msg, i);
                break;
            case DISCONNECT:
                parse_disconnectUtilisateur(msg, i);
                break;
            case UPDATE:
                parse_updateUtilisateur(msg, i);
                break;
            case DELETE:
                parse_deleteUtilisateur(msg);
                break;
            case ADD_ANNONCE:
                parse_addAnnonce(message, msg);
                break;
            case UPDATE_ANNONCE:
                parse_updateAnnonce(msg, i);
                break;
            case DELETE_ANNONCE:
                parse_deleteAnnonce(msg, i);
                break;
            case CHECK_ALL_ANNONCES:
                parse_checkAllAnnonces(msg);
                break;
            case CHECK_ANNONCES_CLIENT:
                parse_checkAnnoncesClient(msg, i);
                break;
            case CHECK_ANNONCES_DOMAINE:
                parse_checkAnnoncesDomaine(msg, i);
                break;
            case CHECK_ANNONCE:
                parse_checkAnnonce(msg);
                break;
            case CHECK_DOMAINES:
                parse_checkDomaines(msg);
                break;
            case WHOIS:
                parse_whoIS(msg, i);
                break;
            case HELP:
                parse_help(msg);
                break;
            case QUIT:
                quit(this.socket);
                break;
            case INVALID:
                invalid(this.socket);
                break;
            default:
                System.err.println("Should not be reached");
                System.exit(ERROR);
                break;

        }
    }

    private void parse_newUtilisateur(String[] msg, int i) throws InterruptedException {
        if (msg.length == messageType.getParameters()) {
            if (addUtilisateur(msg[i++], msg[i++])) {
                addUtilisateurSuccess(this.socket);
            } else {
                addUtilisateurFailure(this.socket);
            }
        } else {
            invalid(this.socket);
        }
    }

    private void parse_connectUtilisateur(String[] msg, int i) throws InterruptedException {
        String port;
        if (msg.length == messageType.getParameters()) {
            String pseudo = msg[i++];
            if (!existsPseudo(pseudo)) {
                doesntExistUtilisateur(this.socket);
                return;
            }
            if (connectUtilisateur(pseudo, msg[i++], Integer.parseInt(port = msg[i++]))) {
                //InetSocketAddress sockaddr = (InetSocketAddress)this.socket.getRemoteSocketAddress();
                connectUtilisateurSuccess(pseudo, port, this.socket);
            } else {
                connectUtilisateurFailure(this.socket);
            }
        } else {
            invalid(this.socket);
        }
    }

    private void parse_disconnectUtilisateur(String[] msg, int i) throws InterruptedException {
        if (this.currentUser != null) {
            if (msg.length == messageType.getParameters()) {
                if (connectUtilisateur(msg[i++], msg[i++], Integer.parseInt(msg[i++]))) {
                    disconnectUtilisateurSuccess(this.socket);
                } else {
                    disconnectUtilisateurFailure(this.socket);
                }
            } else {
                invalid(this.socket);
            }
        } else {
            isNotConnectedUtilisateur(this.socket);
        }
    }

    private void parse_updateUtilisateur(String[] msg, int i) throws InterruptedException {
        if (this.currentUser != null) {
            if (msg.length == messageType.getParameters()) {
                if (updateUtilisateur(msg[i++], msg[i++], Integer.parseInt(msg[i++]))) {
                    updateUtilisateurSuccess(this.socket);
                } else {
                    updateUtilisateurFailure(this.socket);
                }
            } else {
                invalid(this.socket);
            }
        } else {
            isNotConnectedUtilisateur(this.socket);
        }
    }

    private void parse_deleteUtilisateur(String[] msg) throws InterruptedException {
        if (this.currentUser != null) {
            if (msg.length == messageType.getParameters()) {
                if (deleteUtilisateur()) {
                    deleteUtilisateurSuccess(this.socket);
                } else {
                    deleteUtilisateurFailure(this.socket);
                }
            } else {
                invalid(this.socket);
            }
        } else {
            isNotConnectedUtilisateur(this.socket);
        }
    }

    private void parse_addAnnonce(String message, String[] msg) throws InterruptedException {
        String[] m = message.split("\\|\\|");
        if (this.currentUser != null) {
            if (m.length != 2) {
                invalid(this.socket);
            } else if (msg.length + 1 >= messageType.getParameters()) {
                int i = 0;
                msg = m[1].split("\\s+");
                if (addAnnonce(new Annonce(m[0].substring(MessageType.ADD_ANNONCE.name().length() + 1), Annonce.getDomaine(msg[i++].toLowerCase()), Long.parseLong(msg[i++]), getDescription(msg, i)))) {
                    addAnnonceSuccess(this.socket);
                } else {
                    addAnnonceFailure(this.socket);
                }
            } else {
                invalid(this.socket);
            }
        } else {
            isNotConnectedUtilisateur(this.socket);
        }
    }

    private void parse_updateAnnonce(String[] msg, int i) throws InterruptedException {
        if (this.currentUser != null) {
            if (msg.length >= messageType.getParameters()) {
                if (updateAnnonce(new Annonce(Integer.parseInt(msg[i++]), msg[i++], Annonce.getDomaine(msg[i++].toUpperCase()), Long.parseLong(msg[i++]), getDescription(msg, i)))) {
                    updateAnnonceSuccess(this.socket);
                } else {
                    updateAnnonceFailure(this.socket);
                }
            } else {
                invalid(this.socket);
            }
        } else {
            isNotConnectedUtilisateur(this.socket);
        }
    }

    private void parse_deleteAnnonce(String[] msg, int i) throws InterruptedException {
        if (this.currentUser != null) {
            if (msg.length == messageType.getParameters()) {
                if (deleteAnnonce(Integer.parseInt(msg[i++]))) {
                    deleteAnnonceSuccess(this.socket);
                } else {
                    deleteAnnonceFailure(this.socket);
                }
            } else {
                invalid(this.socket);
            }
        } else {
            isNotConnectedUtilisateur(this.socket);
        }
    }

    private void parse_checkAllAnnonces(String[] msg) throws InterruptedException {
        System.out.println(msg.length + " " + messageType.getParameters());
        if (msg.length == messageType.getParameters()) {
            joinThread(new Thread(new GestionnaireEcrivain(this.socket, checkAllAnnonces())));
        } else {
            invalid(this.socket);
        }
    }

    private void parse_checkAnnoncesClient(String[] msg, int i) throws InterruptedException {
        if (msg.length == messageType.getParameters()) {
            joinThread(new Thread(new GestionnaireEcrivain(this.socket, checkAllAnnoncesUtilisateur(Integer.parseInt(msg[i])))));
        } else {
            invalid(this.socket);
        }
    }

    private void parse_checkAnnoncesDomaine(String[] msg, int i) throws InterruptedException {
        if (msg.length == messageType.getParameters()) {
            joinThread(new Thread(new GestionnaireEcrivain(this.socket, checkAllAnnoncesDomaine(msg[i]))));
        } else {
            invalid(this.socket);
        }
    }

    private void parse_checkAnnonce(String[] msg) throws InterruptedException {
        if (msg.length == messageType.getParameters()) {
            todo(this.socket);
        } else {
            invalid(this.socket);
        }
    }

    private void parse_checkDomaines(String[] msg) throws InterruptedException {
        if (msg.length == messageType.getParameters()) {
            joinThread(new Thread(new GestionnaireEcrivain(this.socket, Domaine.descripteur())));
        } else {
            invalid(this.socket);
        }
    }

    private void parse_whoIS(String[] msg, int i) throws InterruptedException {
        if (msg.length == messageType.getParameters()) {
            Utilisateur u = getUtilisateur(Integer.parseInt(msg[i]));
            if (u == null) {
                existsUtilisateurFailure(this.socket);
            } else {
                existsUtilisateurSuccess(this.socket, u.getPort());
            }
        } else {
            invalid(this.socket);
        }
    }

    private void parse_help(String[] msg) throws InterruptedException {
        if (msg.length == messageType.getParameters()) {
            help(this.socket);
        } else {
            invalid(this.socket);
        }
    }

    protected static Utilisateur getUtilisateur(int id) {
        for (Utilisateur entry : ANNONCES.keySet()) {
            if (entry.getIdentifiant() == id) {
                return entry;
            }
        }
        return null;
    }

    protected static String getDescription(String[] msg, int index) {
        String s = "";
        for (; index < msg.length; index++) {
            if (msg[index].equals("***")) {
                break;
            }
            s += msg[index] + " ";
        }
        return s;
    }

    protected boolean existsPseudo(String pseudo) {
        return ANNONCES.keySet().stream().anyMatch((u) -> (u.getPseudo().equals(pseudo)));
    }

    public static String checkAllAnnonces() {
        String s = "";
        for (Map.Entry<Utilisateur, HashSet<Annonce>> entry : ANNONCES.entrySet()) {
            HashSet<Annonce> values = entry.getValue();
            if (values.isEmpty()) {
                continue;
            }
            s += entry.getKey() + "\n";
            Iterator<Annonce> iter = values.iterator();
            while (iter.hasNext()) {
                s += iter.next() + "\n";
            }
        }
        return s;
    }

    public static String checkAllAnnoncesUtilisateur(int id) {
        Utilisateur u = getUtilisateur(id);

        HashSet<Annonce> annonces = ANNONCES.get(u);
        System.out.println(annonces);
        String s = "";

        if (annonces == null) {
            return null;
        }

        s += u + "\n";
        Iterator<Annonce> iter = annonces.iterator();
        while (iter.hasNext()) {
            s += iter.next() + "\n";
        }
        return s;
    }

    public static String checkAllAnnoncesDomaine(String d) {
        String s = "", tmp = "";
        for (Map.Entry<Utilisateur, HashSet<Annonce>> entry : ANNONCES.entrySet()) {
            HashSet<Annonce> values = entry.getValue();

            if (values.isEmpty()) {
                continue;
            }
            Iterator<Annonce> iter = values.iterator();
            while (iter.hasNext()) {
                Annonce annonce = iter.next();
                if (Annonce.getDomaine(d).equals(annonce.getDomaine())) {
                    tmp += annonce + "\n";
                }
            }
            if (tmp.compareTo("") != 0) {
                s += entry.getKey() + "\n";
                s += tmp;
            }
            tmp = "";
        }
        return s;
    }

    public static String checkAllDomaines() {
        return Domaine.descripteur();
    }

    public boolean addUtilisateur(String pseudo, String mdp) {
        if (currentUser != null || existsPseudo(pseudo)) {
            return false;
        }
        ANNONCES.put(new Utilisateur(pseudo, Hashing.hash(mdp, SALT), this.socket), new HashSet<>());
        return true;
    }

    public boolean connectUtilisateur(String pseudo, String mdp, int port) {
        if (!existsPseudo(pseudo)) {
            return false;
        }

        this.currentUser = ANNONCES.keySet().stream()
                .filter((u) -> (u.getPseudo().equals(pseudo)) && (Hashing.checkPassword(u.getMotDePasse(), mdp, SALT)))
                .findFirst().orElse(this.currentUser);

        if (this.currentUser != null) {
            currentUser.setPort(port);
            return true;
        } else {
            return false;
        }
    }

    public boolean updateUtilisateur(String pseudo, String mdp, int port) {
        if (this.currentUser == null) {
            return false;
        } else {
            this.currentUser.setPseudo(pseudo);
            this.currentUser.setMotDePasse(Hashing.hash(mdp, SALT));
            this.currentUser.setPort(port);
            return true;
        }
    }

    public boolean deleteUtilisateur() {
        if (this.currentUser == null) {
            return false;
        } else {
            ANNONCES.remove(this.currentUser).clear();
            this.currentUser = null;
            return true;
        }
    }

    public boolean addAnnonce(Annonce e) {
        HashSet<Annonce> annonces;
        if (this.currentUser == null || (annonces = ANNONCES.get(this.currentUser)) == null) {
            return false;
        }
        return annonces.add(e);
    }

    public boolean updateAnnonce(Annonce e) {
        HashSet<Annonce> annonces;
        if (this.currentUser == null || (annonces = ANNONCES.get(this.currentUser)) == null) {
            return false;
        } else {
            if (annonces.contains(e)) {
                annonces.remove(e);
                return annonces.add(e);
            } else {
                return false;
            }
        }
    }

    public boolean deleteAnnonce(int id) {
        HashSet<Annonce> annonces;

        if (this.currentUser == null || (annonces = ANNONCES.get(this.currentUser)) == null) {
            return false;
        } else {
            Annonce dummy = new Annonce(id);
            if (annonces.contains(dummy)) {
                return annonces.remove(dummy);
            } else {
                return false;
            }
        }
    }
}
