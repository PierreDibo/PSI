
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Gestionnaire {

    public static volatile boolean runningEcouteur = true;
    public static final int ATTENTE = 100;
    private static final HashMap<Utilisateur, HashSet<Annonce>> ANNONCES = new HashMap<>();
    private static final int ADRESS_PORT = 0, INDEX_PORT = 1;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage : java Gestionnaire ip port_tcp [port_upd]");
        }

        switch (args.length) {
            case 2:
                try (final ServerSocket server = new ServerSocket(Integer.parseInt(args[INDEX_PORT]), 100, InetAddress.getByName(args[ADRESS_PORT]))) {
                    while (true) {
                        Socket clientSocket = server.accept();
                        new Thread(new Ecouteur(clientSocket)).start();
                        new Thread(new Ecrivain(clientSocket, MessageType.MSG_WELCOME)).start();
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case 3:
                break;
        }
    }

    static class Ecouteur implements Runnable {

        private final Socket socket;
        private Utilisateur currentUser;

        private static final String ESP = " ";

        public Ecouteur(Socket s) {
            this.socket = s;
            this.currentUser = null;
        }

        public static String checkAllAnnonces() {
            String s = "";
            for (Entry<Utilisateur, HashSet<Annonce>> entry : ANNONCES.entrySet()) {
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
            Utilisateur u = new Utilisateur(id);
            HashSet<Annonce> annonces = ANNONCES.get(u);
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
            for (Entry<Utilisateur, HashSet<Annonce>> entry : ANNONCES.entrySet()) {
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

        public boolean existsPseudo(String pseudo) {
            return ANNONCES.keySet().stream().anyMatch((u) -> (u.getPseudo().equals(pseudo)));
        }

        public boolean addUtilisateur(String pseudo, String mdp) {
            if (currentUser != null || existsPseudo(pseudo)) {
                return false;
            }
            this.currentUser = new Utilisateur(pseudo, mdp, this.socket);
            ANNONCES.put(this.currentUser, new HashSet<>());
            return true;
        }

        public boolean connectUtilisateur(String pseudo, String mdp) {
            if (!existsPseudo(pseudo)) {
                return false;
            }

            this.currentUser = ANNONCES.keySet().stream()
                    .filter((u) -> (u.getPseudo().equals(pseudo)) && (u.getMotDePasse().equals(mdp)))
                    .findFirst().orElse(this.currentUser);

            return this.currentUser != null;
        }

        public boolean updateUtilisateur(String pseudo, String mdp) {
            if (this.currentUser == null) {
                return false;
            } else {
                this.currentUser.setPseudo(pseudo);
                this.currentUser.setMotDePasse(mdp);
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

        private static String getDescription(String[] msg, int index) {
            String s = "";
            for (; index < msg.length; index++) {
                s += msg[index] + " ";
            }
            return s;
        }

        private void parse(String[] msg, Socket socket) throws IOException, InterruptedException {
            MessageType message;
            int i = 0;
            try {
                message = MessageType.valueOf(msg[i++]);
            } catch (IllegalArgumentException ex) {
                message = MessageType.INVALID;
            }
            switch (message) {
                case NEW:
                    if (msg.length == message.getParameters()) {
                        if (addUtilisateur(msg[i++], msg[i++])) {
                            MessagesGestionnaire.addUtilisateurSuccess(socket);
                        } else {
                            MessagesGestionnaire.addUtilisateurError(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CONNECT:
                    if (msg.length == message.getParameters()) {
                        if (connectUtilisateur(msg[i++], msg[i++])) {
                            MessagesGestionnaire.connectUtilisateurSuccess(socket);
                        } else {
                            MessagesGestionnaire.connectUtilisateurError(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case UPDATE:
                    if (msg.length == message.getParameters()) {
                        if (updateUtilisateur(msg[i+2], msg[i+3])) {
                            MessagesGestionnaire.updateUtilisateurSuccess(socket);
                        } else {
                            MessagesGestionnaire.updateUtilisateurError(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case DELETE:
                    if (msg.length == message.getParameters()) {
                        if (deleteUtilisateur()) {
                            MessagesGestionnaire.deleteUtilisateurSuccess(socket);
                        } else {
                            MessagesGestionnaire.deleteUtilisateurError(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case ADD_ANNONCE:
                    if (msg.length >= message.getParameters()) {
                        if (addAnnonce(new Annonce(msg[i++], Annonce.getDomaine(msg[i++].toLowerCase()), Long.parseLong(msg[i++]), getDescription(msg, i)))) {
                            MessagesGestionnaire.addAnnonceSuccess(socket);
                        } else {
                            MessagesGestionnaire.addAnnonceError(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case UPDATE_ANNONCE:
                    if (msg.length >= message.getParameters()) {
                        if (updateAnnonce(new Annonce(Integer.parseInt(msg[i++]), msg[i++], Annonce.getDomaine(msg[i++].toLowerCase()), Long.parseLong(msg[i++]), getDescription(msg, i)))) {
                            MessagesGestionnaire.updateAnnonceSuccess(socket);
                        } else {
                            MessagesGestionnaire.updateAnnonceError(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case DELETE_ANNONCE:
                    if (msg.length == message.getParameters()) {
                        if (deleteAnnonce(Integer.parseInt(msg[i++]))) {
                            MessagesGestionnaire.deleteAnnonceSuccess(socket);
                        } else {
                            MessagesGestionnaire.deleteAnnonceError(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CHECK_ALL_ANNONCES:
                    if (msg.length == message.getParameters()) {
                        MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, checkAllAnnonces())));
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CHECK_ANNONCE:
                    if (msg.length == message.getParameters()) {
                        MessagesGestionnaire.todo(socket);
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CHECK_ANNONCES_CLIENT:
                    if (msg.length == message.getParameters()) {
                        MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, checkAllAnnoncesUtilisateur(Integer.parseInt(msg[i++])))));
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CHECK_ANNONCES_DOMAINE:
                    if (msg.length == message.getParameters()) {
                        MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, checkAllAnnoncesDomaine(msg[i++]))));
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CHECK_DOMAINES:
                    if (msg.length == message.getParameters()) {
                        MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, Domaine.descripteur())));
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case WHOIS:
                    if (msg.length == message.getParameters()) {
                        MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, Domaine.descripteur())));
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CALL_OPEN:
                    if (msg.length == message.getParameters()) {
                        if (true) {
                            MessagesGestionnaire.todo(socket);
                        } else {
                            MessagesGestionnaire.todo(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case CALL_CLOSE:
                    if (msg.length == message.getParameters()) {
                        if (true) {
                            MessagesGestionnaire.todo(socket);
                        } else {
                            MessagesGestionnaire.todo(socket);
                        }
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                    break;
                case HELP:
                    MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, MessageType.MSG_HELP)));
                    break;
                case QUIT:
                    MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, MessageType.MSG_QUIT)));
                    this.socket.close();
                    break;
                case INVALID:
                    MessagesGestionnaire.invalid(socket);
                    break;
                default:
                    MessagesGestionnaire.joinThread(new Thread(new Ecrivain(socket, MessageType.MSG_TODO)));
                    break;
            }
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                String msg;
                while (!this.socket.isClosed() && (msg = input.readLine()) != null) {
                    System.out.println(msg);

                    if (msg.endsWith(MessageType.END.getMessage())) {
                        msg = msg.replace(MessageType.END.getMessage(), ESP);
                        parse(msg.split("\\s+"), socket);
                    } else {
                        MessagesGestionnaire.invalid(socket);
                    }
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.currentUser = null;
        }
    }

    static class Ecrivain implements Runnable {

        private final Socket client;
        private final String message;

        public Ecrivain(Socket s, String msg) {
            this.client = s;
            this.message = msg;
        }

        @Override
        public void run() {
            try {
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                output.write(this.message);
                output.newLine();
                output.flush();
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class MessagesGestionnaire {

        public static void joinThread(Thread t) throws InterruptedException {
            t.start();
            t.join();
        }

        // <editor-fold defaultstate="collapsed" desc="METHODS UTILISATEUR">
        private static void addUtilisateurSuccess(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_ADD_UTILISATEUR_SUCCESS)));
        }

        private static void addUtilisateurError(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_ADD_UTILISATEUR_FAILURE)));
        }

        private static void connectUtilisateurSuccess(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_CONNECT_UTILISATEUR_SUCCESS)));
        }

        private static void connectUtilisateurError(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_CONNECT_UTILISATEUR_FAILURE)));
        }

        private static void updateUtilisateurSuccess(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_UPDATE_UTILISATEUR_SUCCESS)));
        }

        private static void updateUtilisateurError(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_UPDATE_UTILISATEUR_FAILURE)));
        }

        private static void deleteUtilisateurSuccess(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_DELETE_UTILISATEUR_SUCCESS)));
        }

        private static void deleteUtilisateurError(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_DELETE_UTILISATEUR_FAILURE)));
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="METHODS ANNONCE">
        private static void addAnnonceSuccess(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_ADD_ANNONCE_SUCCESS)));
        }

        private static void addAnnonceError(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_ADD_ANNONCE_FAILURE)));
        }

        private static void updateAnnonceSuccess(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_UPDATE_ANNONCE_SUCCESS)));
        }

        private static void updateAnnonceError(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_UPDATE_ANNONCE_FAILURE)));
        }

        private static void deleteAnnonceSuccess(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_DELETE_ANNONCE_SUCCESS)));
        }

        private static void deleteAnnonceError(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_DELETE_ANNONCE_FAILURE)));
        }
        // </editor-fold>

        private static void todo(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_TODO)));
        }

        private static void invalid(Socket s) throws InterruptedException {
            joinThread(new Thread(new Ecrivain(s, MessageType.MSG_INVALID)));
        }

    }

}
