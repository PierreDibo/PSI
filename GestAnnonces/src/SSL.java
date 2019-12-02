
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class SSL {

    private static final String HOST = "localhost";
    private static final int PROTOCOL_TSL = 0, IP_GESTIONNAIRE = 1, PORT_GESTIONNAIRE = 2, PORT_CLIENT = 3;
    private static final Object LOCK = new Object();
    private static String protocole = null;
    private static final byte[] SALT = new byte[16];

    static class ConsoleInputReadTask implements Callable<String> {

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public String call() throws IOException {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(System.in));
            String input;
            do {

                try {
                    while (!br.ready()) {
                        Thread.sleep(200);
                    }
                    input = br.readLine();
                } catch (InterruptedException e) {
                    System.out.println("ConsoleInputReadTask() cancelled");
                    return null;
                }
            } while ("".equals(input));
            return input;
        }

        public void close() {

        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SSLClientServer serverRunnable = null;
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(SALT);

        if (args.length < 2 && args.length > 4) {
            System.err.println("Usage : java Client [protocole] ip_gestionnaire port_gestionnaire [port_client]");
            System.exit(-1);
        }

        try {
            switch (args.length) {
                case 2:
                    connectGestionnaire(args);
                    break;
                case 3:
                    connectGestionnaire(args);
                    new Thread(new Server(Integer.parseInt(args[PORT_CLIENT]))).start();
                    break;
                case 4:
                    connectGestionnaire(args);
                    serverRunnable = new SSLClientServer(protocole = args[PROTOCOL_TSL], InetAddress.getByName(HOST), Integer.parseInt(args[PORT_CLIENT]));
                    Thread server = new Thread(serverRunnable);
                    server.start();

                    synchronized (LOCK) {
                        LOCK.wait();
                        serverRunnable.stop();
                    }
                    break;
            }

        } catch (UnknownHostException ex) {
            Logger.getLogger(SSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException | KeyStoreException
                | CertificateException | UnrecoverableKeyException
                | KeyManagementException | IOException | InterruptedException ex) {
            Logger.getLogger(SSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (serverRunnable != null) {
            serverRunnable.stop();
        }
    }

    private static void connectGestionnaire(String[] args) throws UnknownHostException, IOException {
        Socket socket;
        InetAddress addrGest;
        int portGest;

        addrGest = InetAddress.getByName(args[IP_GESTIONNAIRE]);
        portGest = Integer.parseInt(args[PORT_GESTIONNAIRE]);
        socket = new Socket(addrGest, portGest);

        new Thread(new Ecrivain(socket)).start();
        new Thread(new Ecouteur(socket)).start();
    }

    static class Ecrivain implements Runnable {

        private final Socket sockettcp;

        public Ecrivain(Socket socket) {
            this.sockettcp = socket;
        }

        private void parse(String content) throws IOException {
            MessageType message;
            String[] input = content.split("\\s+");
            int i = 0;
            try {
                message = MessageType.valueOf(input[i++]);
            } catch (IllegalArgumentException ex) {
                message = MessageType.INVALID;
            }
            switch (message) {
                case CALL_OPEN:
                case CALL:
                case CALL_CLOSE:
                    if (protocole == null) {
                        InetAddress iaddr = InetAddress.getByName(input[i++]);
                        int port = Integer.parseInt(input[i++]);
                        //System.out.println(iaddr.toString());
                        //DatagramSocket dtDock = new DatagramSocket(port, iaddr);
                        Socket socket = new Socket(iaddr, port);
                        writetcp(content, socket);
                    } else {
                        InetAddress iaddr = InetAddress.getByName(input[i++]);
                        int port = Integer.parseInt(input[i++]);
                        try {
                            SSLClient client = new SSLClient(protocole, iaddr, port);
                            if (client.connect()) {
                                client.write(String.join(" ", Arrays.copyOfRange(input, i, input.length)));
                                client.shutdown();
                            }
                        } catch (NoSuchAlgorithmException | KeyStoreException | FileNotFoundException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
                            Logger.getLogger(SSL.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;
                default:
                    writetcp(content, this.sockettcp);
                    break;
            }

        }

        private void writetcp(String content, Socket socket) throws IOException {
            if (!socket.isClosed()) {
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                output.write(content);
                output.newLine();
                output.flush();
            }
        }

        @Override
        public void run() {
            ConsoleInputReadTask console = new ConsoleInputReadTask();
            while (true) {
                try {
                    String content = console.call();

                    while (!content.contains(MessageType.END.getMessage())) {
                        content += " " + console.call();
                    }

                    //content = content.replace(MessageType.END.getMessage(), ESP);
                    parse(content);

                    if (this.sockettcp.isClosed()) {
                        break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    static class Ecouteur implements Runnable {

        private final Socket socket;

        public Ecouteur(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith(MessageType.MSG_QUIT)) {
                        this.socket.close();
                        break;
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class Server implements Runnable {

        private final int port;

        public Server(int p) {
            this.port = p;
        }

        @Override
        public void run() {
            try (final ServerSocket server = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = server.accept();
                    System.out.println(clientSocket);
                    new Thread(new EcouteurClient(clientSocket)).start();
                }
            } catch (UnknownHostException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class MessagesClient {

        public static void joinThread(Thread t) throws InterruptedException {
            t.start();
            t.join();
        }
    }

    private static class EcouteurClient implements Runnable {

        private final Socket socket;

        public EcouteurClient(Socket clientSocket) {
            this.socket = clientSocket;
        }

        @Override
        public void run() {
            while (this.socket.isConnected()) {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    String msg;
                    while ((msg = input.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SSL.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
