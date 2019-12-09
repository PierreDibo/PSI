
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public abstract class TrustConnexion {

    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    protected ByteBuffer myAppData;
    protected ByteBuffer myNetData;
    protected ByteBuffer peerAppData;
    protected ByteBuffer peerNetData;

    protected abstract void write(SocketChannel socketChannel, SSLEngine engine, String message) throws SSLException, IOException;

    protected abstract void read(SocketChannel socketChannel, SSLEngine engine) throws SSLException, IOException, InterruptedException;

    protected ByteBuffer enlargePacketBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    protected ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    protected ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }

    protected void closeConnection(SocketChannel socketChannel, SSLEngine engine) throws SSLException, IOException {
        engine.closeOutbound();
        doHandshake(socketChannel, engine);
        socketChannel.close();
    }

    protected void handleEndOfStream(SocketChannel socketChannel, SSLEngine engine) throws SSLException, IOException {
        engine.closeInbound();
        closeConnection(socketChannel, engine);
    }

    protected boolean doHandshake(SocketChannel socketChannel, SSLEngine engine) throws SSLException, IOException {
        SSLEngineResult result;
        HandshakeStatus handshakeStatus;
        ByteBuffer mAD, pAD;
        int appBufferSize;

        appBufferSize = engine.getSession().getApplicationBufferSize();
        mAD = ByteBuffer.allocate(appBufferSize);
        pAD = ByteBuffer.allocate(appBufferSize);

        this.myNetData.clear();
        this.peerNetData.clear();

        handshakeStatus = engine.getHandshakeStatus();
        while (handshakeStatus != HandshakeStatus.FINISHED && handshakeStatus != HandshakeStatus.NOT_HANDSHAKING) {
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    if (socketChannel.read(peerNetData) < 0) {
                        if (engine.isInboundDone() && engine.isOutboundDone()) {
                            return false;
                        }
                        try {
                            engine.closeInbound();
                        } catch (SSLException e) {
                        }
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    peerNetData.flip();
                    try {
                        result = engine.unwrap(peerNetData, pAD);
                        peerNetData.compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    switch (result.getStatus()) {
                        case OK:
                            break;
                        case BUFFER_OVERFLOW:
                            pAD = enlargeApplicationBuffer(engine, pAD);
                            break;
                        case BUFFER_UNDERFLOW:
                            peerNetData = handleBufferUnderflow(engine, peerNetData);
                            break;
                        case CLOSED:
                            if (engine.isOutboundDone()) {
                                return false;
                            } else {
                                engine.closeOutbound();
                                handshakeStatus = engine.getHandshakeStatus();
                                break;
                            }
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_WRAP:
                    myNetData.clear();
                    try {
                        result = engine.wrap(mAD, myNetData);
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
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
                            try {
                                myNetData.flip();
                                while (myNetData.hasRemaining()) {
                                    socketChannel.write(myNetData);
                                }
                                peerNetData.clear();
                            } catch (IOException ex) {
                                handshakeStatus = engine.getHandshakeStatus();
                            }
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        executor.execute(task);
                    }
                    handshakeStatus = engine.getHandshakeStatus();
                    break;
                case FINISHED:
                    break;
                case NOT_HANDSHAKING:
                    break;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
        }
        return true;
    }

    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
            FileNotFoundException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreIS = new FileInputStream(filepath)) {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    protected TrustManager[] createTrustManagers(String filepath, String keystorePassword)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
            FileNotFoundException, IOException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream trustStoreIS = new FileInputStream(filepath)) {
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }
}
