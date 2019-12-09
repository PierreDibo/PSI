
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class SSLClient extends TrustConnexion implements Runnable {

    private static final String FILE_CLIENT = "client.jks",
            FILE_TRUSTED = "trusted.jks",
            STOREPASS = "password",
            KEYPASS = "password";
    private static final int BUFSIZ = 1024;

    private final String protocol;
    private final InetAddress iaddr;
    private final int port;
    private SSLContext context;
    private SSLEngine engine;
    private SocketChannel socketChannel;

    public SSLClient(String proto, InetAddress addr, int port)
            throws NoSuchAlgorithmException, KeyStoreException, IOException,
            FileNotFoundException, CertificateException, UnrecoverableKeyException,
            KeyManagementException {
        this.protocol = proto;
        this.iaddr = addr;
        this.port = port;
        this.init();
    }

    public boolean connect() throws IOException {
        this.socketChannel = SocketChannel.open();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.connect(new InetSocketAddress(this.iaddr, this.port));
        while (!this.socketChannel.finishConnect()) {
            // can do something here...
        }

        this.engine.beginHandshake();
        return doHandshake(this.socketChannel, this.engine);
    }

    public void read() throws IOException, InterruptedException {
        read(socketChannel, engine);
    }

    @Override
    protected void read(SocketChannel socketChannel, SSLEngine engine) throws SSLException, IOException, InterruptedException {
        peerNetData.clear();
        int waitToReadMillis = 50;
        boolean exitReadLoop = false;
        while (!exitReadLoop) {
            int bytesRead = socketChannel.read(peerNetData);
            if (bytesRead > 0) {
                peerNetData.flip();
                while (peerNetData.hasRemaining()) {
                    peerAppData.clear();
                    SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
                    switch (result.getStatus()) {
                        case OK:
                            peerAppData.flip();
                            System.out.println(StandardCharsets.UTF_8.decode(this.peerAppData).toString());
                            exitReadLoop = true;
                            break;
                        case BUFFER_OVERFLOW:
                            peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                            break;
                        case BUFFER_UNDERFLOW:
                            peerNetData = handleBufferUnderflow(engine, peerNetData);
                            break;
                        case CLOSED:
                            closeConnection(socketChannel, engine);
                            return;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                }
            } else if (bytesRead < 0) {
                handleEndOfStream(socketChannel, engine);
                return;
            }
            Thread.sleep(waitToReadMillis);
        }
    }

    public void write(String message) throws IOException {
        write(this.socketChannel, this.engine, message);
    }

    @Override
    protected void write(SocketChannel socketChannel, SSLEngine engine, String message) throws IOException {
        myAppData.clear();
        myAppData.put(message.getBytes());
        myAppData.flip();
        while (myAppData.hasRemaining()) {
            myNetData.clear();
            SSLEngineResult result = engine.wrap(myAppData, myNetData);
            switch (result.getStatus()) {
                case OK:
                    myNetData.flip();
                    while (myNetData.hasRemaining()) {
                        socketChannel.write(myNetData);
                    }
                    break;
                case BUFFER_OVERFLOW:
                    myNetData = enlargePacketBuffer(engine, myNetData);
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                case CLOSED:
                    closeConnection(socketChannel, engine);
                    return;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }
    }

    private void init() throws NoSuchAlgorithmException, KeyStoreException,
            IOException, FileNotFoundException, CertificateException,
            UnrecoverableKeyException, KeyManagementException {
        this.context = SSLContext.getInstance(this.protocol);

        this.context.init(createKeyManagers(FILE_CLIENT, STOREPASS, KEYPASS),
                createTrustManagers(FILE_TRUSTED, STOREPASS), new SecureRandom());

        this.engine = this.context.createSSLEngine(this.iaddr.getHostAddress(), this.port);

        this.engine.setUseClientMode(true);

        SSLSession session = this.engine.getSession();

        this.myAppData = ByteBuffer.allocate(BUFSIZ);
        this.myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        this.peerAppData = ByteBuffer.allocate(BUFSIZ);
        this.peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    public void shutdown() throws IOException {
        closeConnection(this.socketChannel, this.engine);
        this.executor.shutdown();
    }

    @Override
    public void run() {
        while (this.socketChannel.isConnected()) {
            try {
                read();
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(SSLClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
