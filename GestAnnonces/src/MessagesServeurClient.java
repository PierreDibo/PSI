
import java.net.Socket;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public interface MessagesServeurClient extends Messages {

    default void callOpenSuccess(Socket s, String pseudo) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_CALL_OPEN_SUCCESS + " " + pseudo)));
    }

    default void callOpenFailure(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_CALL_OPEN_FAILURE)));
    }

    default void contactBanned(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_CONTACT_BANNED)));
    }

    default void contactAlreadyConnected(Socket s, String pseudo) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_CONTACT_ALREADY_CONNECTED + " " + pseudo)));
    }
}
