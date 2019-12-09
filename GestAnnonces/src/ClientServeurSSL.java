
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ClientServeurSSL extends Client implements Runnable {

    private final ClientRemote client;
    private SSLServerSocketFactory ssocketFactory;

    public ClientServeurSSL(ClientRemote client) {
        this.client = client;
        init();
    }

    private void init() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("server.jks"), "password".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
            kmf.init(ks, "password".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);

            this.ssocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | NoSuchProviderException | KeyManagementException | IOException | CertificateException ex) {
            Logger.getLogger(ClientServeurSSL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        PolicySSL.create(this.client.getPseudo());

        try (final SSLServerSocket server = (SSLServerSocket) this.ssocketFactory.createServerSocket(this.client.getPort(), ATTENTE, this.client.getIaddr())) {
            while (true) {
                SSLSocket clientSocket = (SSLSocket) server.accept();
                clientSocket.setEnabledProtocols(new String[] {"TLSv1.2"});
                //clientSocket.startHandshake();
                new Thread(new ContactSSL(this.client, clientSocket)).start();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
