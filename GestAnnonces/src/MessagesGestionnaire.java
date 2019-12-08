
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public interface MessagesGestionnaire extends Messages {

    // <editor-fold defaultstate="collapsed" desc="METHODS UTILISATEUR">
    default void addUtilisateurSuccess(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_ADD_UTILISATEUR_SUCCESS)));
    }

    default void addUtilisateurFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_ADD_UTILISATEUR_FAILURE)));
    }

    default void connectUtilisateurSuccess(String pseudo, String port, Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_CONNECT_UTILISATEUR_SUCCESS
                + " " + pseudo + " " + port)));
    }

    default void connectUtilisateurFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_CONNECT_UTILISATEUR_FAILURE)));
    }

    default void isNotConnectedUtilisateur(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_IS_NOT_CONNECTED)));
    }

    default void disconnectUtilisateurSuccess(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_DISCONNECT_UTILISATEUR_SUCCESS)));
    }

    default void disconnectUtilisateurFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_DISCONNECT_UTILISATEUR_FAILURE)));
    }

    default void updateUtilisateurSuccess(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_UPDATE_UTILISATEUR_SUCCESS)));
    }

    default void updateUtilisateurFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_UPDATE_UTILISATEUR_FAILURE)));
    }

    default void deleteUtilisateurSuccess(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_DELETE_UTILISATEUR_SUCCESS)));
    }

    default void deleteUtilisateurFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_DELETE_UTILISATEUR_FAILURE)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="METHODS ANNONCE">
    default void addAnnonceSuccess(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_ADD_ANNONCE_SUCCESS)));
    }

    default void addAnnonceFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_ADD_ANNONCE_FAILURE)));
    }

    default void updateAnnonceSuccess(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_UPDATE_ANNONCE_SUCCESS)));
    }

    default void updateAnnonceFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_UPDATE_ANNONCE_FAILURE)));
    }

    default void deleteAnnonceSuccess(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_DELETE_ANNONCE_SUCCESS)));
    }

    default void deleteAnnonceFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_DELETE_ANNONCE_FAILURE))
        );
    }
    // </editor-fold>

    default void doesntExistUtilisateur(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_IS_NOT_EXISTS)));
    }

    default void existsUtilisateurSuccess(Socket s, int port) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_WHOIS_SUCCESS)));
        InetSocketAddress inetSocketAddress = (InetSocketAddress) s.getRemoteSocketAddress();
        joinThread(new Thread(new GestionnaireEcrivain(s, inetSocketAddress.getAddress() + ":" + port)));
    }

    default void existsUtilisateurFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_WHOIS_FAILURE)));
    }

    default void help(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_WELCOME)));
    }

    default void quit(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_QUIT)));
    }

}
