
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class SSLClientServeur extends TrustConnexion implements Runnable {

    private static final String FILE_SERVER = "server.jks",
            FILE_TRUSTED = "trusted.jks",
            STOREPASS = "password",
            KEYPASS = "password";

    private final InetAddress iaddr;
    private final int port;
    private final SSLContext context;
    private Selector selector;
    private boolean active;

    public SSLClientServeur(String protocol, String hostAddress, int port) throws UnknownHostException, NoSuchAlgorithmException {
        this.context = SSLContext.getInstance(protocol);
        this.iaddr = InetAddress.getByName(hostAddress);
        this.port = port;
        init();
    }

    public SSLClientServeur(String protocol, InetAddress hostAddress, int port) throws NoSuchAlgorithmException {
        this.context = SSLContext.getInstance(protocol);
        this.iaddr = hostAddress;
        this.port = port;
        init();
    }

    private void init() {
        try {
            this.context.init(createKeyManagers(FILE_SERVER, STOREPASS, KEYPASS), createTrustManagers(FILE_TRUSTED, STOREPASS), new SecureRandom());
            SSLSession dummySession = this.context.createSSLEngine().getSession();
            this.myAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
            this.myNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
            this.peerAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
            this.peerNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
            dummySession.invalidate();

            this.selector = SelectorProvider.provider().openSelector();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(this.iaddr, this.port));
            serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

            this.active = true;
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | IOException | CertificateException ex) {
            Logger.getLogger(SSLClientServeur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isActive() {
        return this.active;
    }

    private void accept(SelectionKey key) throws ClosedChannelException, SSLException, IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);

        SSLEngine engine = this.context.createSSLEngine();
        engine.setUseClientMode(false);
        engine.beginHandshake();

        if (doHandshake(socketChannel, engine)) {
            socketChannel.register(this.selector, SelectionKey.OP_READ, engine);
        } else {
            socketChannel.close();
        }
    }

    public void stop() {
        this.active = false;
        this.executor.shutdown();
        this.selector.wakeup();
    }

    public void start() throws SSLException, IOException {
        while (isActive()) {
            this.selector.select();
            Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read((SocketChannel) key.channel(), (SSLEngine) key.attachment());
                }
            }
        }
    }

    @Override
    protected void read(SocketChannel socketChannel, SSLEngine engine) throws SSLException, IOException {
        this.peerNetData.clear();
        int bytesRead = socketChannel.read(this.peerNetData);
        System.out.println(socketChannel.getLocalAddress());
        System.out.println(socketChannel.getRemoteAddress());
        if (bytesRead > 0) {
            this.peerNetData.flip();
            while (peerNetData.hasRemaining()) {
                this.peerAppData.clear();
                SSLEngineResult result = engine.unwrap(this.peerNetData, this.peerAppData);
                switch (result.getStatus()) {
                    case OK:
                        this.peerAppData.flip();
                        System.out.println(StandardCharsets.UTF_8.decode(this.peerAppData).toString());
                        break;
                    case BUFFER_OVERFLOW:
                        this.peerAppData = enlargeApplicationBuffer(engine, this.peerAppData);
                        break;
                    case BUFFER_UNDERFLOW:
                        this.peerNetData = handleBufferUnderflow(engine, this.peerNetData);
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
        }
    }

    @Override
    protected void write(SocketChannel socketChannel, SSLEngine engine, String message) throws IOException {
        myAppData.clear();
        myAppData.put(message.getBytes());
        myAppData.flip();
        while (myAppData.hasRemaining()) {
            // The loop has a meaning for (outgoing) messages larger than 16KB.
            // Every wrap call will remove 16KB from the original message and send it to the remote peer.
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

    @Override
    public void run() {
        try {
            this.start();
        } catch (IOException ex) {
            Logger.getLogger(SSLClientServeur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
