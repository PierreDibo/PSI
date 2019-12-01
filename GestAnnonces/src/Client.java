
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Client {

    public static final int ATTENTE = 100;
    public static final int PACKET_SIZE = 576;
    private static final int IP_GESTIONNAIRE = 0, PORT_GESTIONNAIRE = 1, IP_CLIENT = 2, PORT_CLIENT = 3;

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
        Socket tcp;
        if (args.length != 2 && args.length != 4) {
            System.err.println("Usage : java Client ip_gestionnaire port_gestionnaire [ip_client port_client]");
            System.exit(-1);
        }
        try {
            InetAddress ia = InetAddress.getByName(args[IP_GESTIONNAIRE]);
            int port = Integer.parseInt(args[PORT_GESTIONNAIRE]);

            switch (args.length) {
                case 2:
                    tcp = new Socket(ia, port);

                    new Thread(new Ecrivain(tcp)).start();
                    new Thread(new EcouteurTCP(tcp)).start();

                    break;
                case 4:
                    InetAddress iaUs = InetAddress.getByName(args[IP_CLIENT]);
                    int portUs = Integer.parseInt(args[PORT_CLIENT]);
                    tcp = new Socket(ia, port);

                    new Thread(new Ecrivain(tcp)).start();
                    new Thread(new EcouteurTCP(tcp)).start();
                    new Thread(new Server(iaUs, portUs)).start();
                    break;
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
          
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

    static class EcouteurTCP implements Runnable {

        private final Socket socket;

        public EcouteurTCP(Socket socket) {
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

        private final InetAddress iaddr;
        private final int port;

        public Server(InetAddress s, int p) {
            this.iaddr = s;
            this.port = p;
        }

        @Override
        public void run() {
            try (final ServerSocket server = new ServerSocket(port, ATTENTE, iaddr)) {
                while (true) {
                    Socket clientSocket = server.accept();
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
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
