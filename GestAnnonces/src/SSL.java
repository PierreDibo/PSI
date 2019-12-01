
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
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
public class SSL {

    private static final int PROTOCOL_TSL = 0, IP_GESTIONNAIRE = 1, PORT_GESTIONNAIRE = 2, IP_CLIENT = 3, PORT_CLIENT = 4;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Socket tcp;
        SSLClient client;
        InetAddress addrGest, addrClient;
        String protocol;
        int portGest, portClient;
        if (args.length != 2 && args.length != 4) {
            System.err.println("Usage : java Client [protocole] ip_gestionnaire port_gestionnaire [port_client]");
            System.exit(-1);
        }

        try {
            protocol = args[PROTOCOL_TSL];

            addrClient = InetAddress.getByName(args[IP_CLIENT]);
            portClient = Integer.parseInt(args[PORT_CLIENT]);
            client = new SSLClient(protocol, addrClient, portClient);
            switch (args.length) {
                case 2:
                    addrGest = InetAddress.getByName(args[IP_GESTIONNAIRE]);
                    portGest = Integer.parseInt(args[PORT_GESTIONNAIRE]);
                    tcp = new Socket(addrGest, portGest);

                    new Thread(new Ecrivain(tcp)).start();
                    new Thread(new Ecouteur(tcp)).start();

                    break;
                case 3:
                    break;
            }

        } catch (UnknownHostException ex) {
            Logger.getLogger(SSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException | KeyStoreException
                | CertificateException | UnrecoverableKeyException
                | KeyManagementException | IOException ex) {
            Logger.getLogger(SSL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class Ecrivain implements Runnable {

        private final Socket sockettcp;

        private static final String ESP = " ";

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
                    InetAddress iaddr = InetAddress.getByName(input[i++]);
                    int port = Integer.parseInt(input[i++]);
                    //System.out.println(iaddr.toString());
                    //DatagramSocket dtDock = new DatagramSocket(port, iaddr);
                    Socket socket = new Socket(iaddr, port);
                    writetcp(content, socket);
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
            Client.ConsoleInputReadTask console = new Client.ConsoleInputReadTask();
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

}
