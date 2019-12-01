
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
 * @author dibop
 */
public class SSLClientServer extends SSLPeer implements Runnable {

    private static final String FILE_SERVER = "server.jks",
            FILE_TRUSTED = "trusted.jks",
            STOREPASS = "password",
            KEYPASS = "password";

    private final String protocol;
    private final InetAddress iaddr;
    private final int port;
    private SSLContext context;
    private boolean active;
    private Selector selector;

    public SSLClientServer(String proto, InetAddress addr, int port)
            throws NoSuchAlgorithmException, KeyStoreException, IOException,
            FileNotFoundException, CertificateException, UnrecoverableKeyException,
            KeyManagementException {
        this.protocol = proto;
        this.iaddr = addr;
        this.port = port;
        this.init();
    }

    private void init() throws NoSuchAlgorithmException, KeyStoreException,
            IOException, FileNotFoundException, CertificateException,
            UnrecoverableKeyException, KeyManagementException {
        this.context = SSLContext.getInstance(protocol);
        this.context.init(createKeyManagers(FILE_SERVER, STOREPASS, KEYPASS), createTrustManagers(FILE_TRUSTED, STOREPASS), new SecureRandom());

        SSLSession dummySession = context.createSSLEngine().getSession();
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
    }

    private boolean isActive() {
        return this.active;
    }

    private void accept(SelectionKey key) throws Exception {
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

    public void start() throws Exception {
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
    protected void read(SocketChannel socketChannel, SSLEngine engine) throws IOException, InterruptedException {
        this.peerNetData.clear();
        int bytesRead = socketChannel.read(this.peerNetData);
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
            write(socketChannel, engine, "Hello! I am your server!");
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
        } catch (Exception ex) {
            Logger.getLogger(SSLClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
