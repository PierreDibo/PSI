
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dibop
 */
public class Demo {

    private static final Object LOCK = new Object();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            SSLClientServer serverRunnable = (new SSLClientServer("TLSv1.2", InetAddress.getByName("localhost"), 9222));
            Thread server = new Thread(serverRunnable);
            server.start();

            synchronized (LOCK) {
                LOCK.wait();
                serverRunnable.stop();
            }

            /* Thread.sleep(1000);
            SSLClient client = new SSLClient("TLSv1.2", InetAddress.getByName("localhost"), 9222);
            client.connect();
            client.write("Hello! I am a client!");
            client.read();
            client.shutdown();*/
            //serverRunnable.stop();
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | KeyManagementException | InterruptedException ex) {
            Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

}
