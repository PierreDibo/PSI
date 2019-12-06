
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
}
