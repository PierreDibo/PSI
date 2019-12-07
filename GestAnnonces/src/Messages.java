
import java.net.Socket;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public interface Messages {

    public default void joinThread(Thread t) throws InterruptedException {
        t.start();
        t.join();
    }

    default void todo(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_TODO)));
    }

    default void invalid(Socket s) throws InterruptedException {
        joinThread(new Thread(new GestionnaireEcrivain(s, MessageType.MSG_INVALID)));
    }

}
