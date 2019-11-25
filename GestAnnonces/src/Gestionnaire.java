
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

        public void updateUtilisateur(String pseudo, String mdp) {
            this.currentUser.setPseudo(pseudo);
            this.currentUser.setMotDePasse(mdp);
        }

        public void deleteUtilisateur(int id) {
            HashSet<Annonce> annonces;
            ANNONCES.remove(new Utilisateur(id));
            this.currentUser = null;
        }

        public boolean addAnnonce(Utilisateur u, Annonce e) {
            HashSet<Annonce> annonces;
            if ((annonces = ANNONCES.get(u)) == null) {
                return false;
            }
            return annonces.add(e);
        }

        public boolean updateAnnonce(Utilisateur u, Annonce e) {
            HashSet<Annonce> annonces;
            if ((annonces = ANNONCES.get(u)) == null) {
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

        public boolean deleteAnnonce(Utilisateur u, int id) {
            HashSet<Annonce> annonces;

            if ((annonces = ANNONCES.get(u)) == null) {
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
                    MessagesGestionnaire.todo(socket);
                    break;
                case CONNECT:
                    MessagesGestionnaire.todo(socket);
                    break;
                case UPDATE:
                    MessagesGestionnaire.todo(socket);
                    break;
                case DELETE:
                    MessagesGestionnaire.todo(socket);
                    break;
                case ADD_ANNONCE:
                    MessagesGestionnaire.todo(socket);
                    break;
                case UPDATE_ANNONCE:
                    MessagesGestionnaire.todo(socket);
                    break;
                case DELETE_ANNONCE:
                    MessagesGestionnaire.todo(socket);
                    break;
                case CHECK_ALL_ANNONCES:
                    MessagesGestionnaire.todo(socket);
                    break;
                case CHECK_ANNONCE:
                    MessagesGestionnaire.todo(socket);
                    break;
                case CHECK_ANNONCES_CLIENT:
                    MessagesGestionnaire.todo(socket);
                    break;
                case CHECK_ANNONCES_DOMAINE:
                    MessagesGestionnaire.todo(socket);
                    break;
                case CHECK_DOMAINES:
                    MessagesGestionnaire.todo(socket);
                    break;
                case OPEN_CALL_UTILISATEUR:
                    MessagesGestionnaire.todo(socket);
                    break;
                case CLOSE_CALL:
                    MessagesGestionnaire.todo(socket);
                    break;
                case HELP:
                    MessagesGestionnaire.todo(socket);
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
