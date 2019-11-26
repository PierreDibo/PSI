
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    public static final int PACKET_SIZE = 576;
    private static final int IP_GESTIONNAIRE = 0, PORT_GESTIONNAIRE = 1, IP_CLIENT = 2, PORT_CLIENT = 3;
    private static int WITHOUTCHAT = 2, WITHCHAT = 4;

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
            System.err.println("Usage : java Client ip_server port_tcp [ip_client port_upd]");
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
                    DatagramSocket udp = new DatagramSocket(portUs, iaUs);

                    new Thread(new Ecrivain(tcp)).start();
                    new Thread(new EcouteurTCP(tcp)).start();
                    new Thread(new EcouteurUDP(udp)).start();
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
                    //InetAddress iaddr = InetAddress.getByName(input[i++]);
                    //int port = Integer.parseInt(input[i++]);
                    //System.out.println(iaddr.toString());
                    //DatagramSocket dtDock = new DatagramSocket(port, iaddr);
                    DatagramSocket dtDock = new DatagramSocket(null);
                    InetSocketAddress addr = new InetSocketAddress(input[i++], Integer.parseInt(input[i++]));
                    dtDock.bind(addr);
                    byte buf[] = content.getBytes();
                    DatagramPacket packet
                            = new DatagramPacket(buf, buf.length,
                                    dtDock.getInetAddress(),
                                    dtDock.getPort());
                    dtDock.send(packet);
                    break;
                case INVALID:
                    System.out.println(MessageType.MSG_INVALID);
                    break;
                default:
                    writetcp(content);
                    break;
            }

        }

        private void writetcp(String content) throws IOException {
            if (!this.sockettcp.isClosed()) {
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.sockettcp.getOutputStream()));
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

    static class EcouteurUDP implements Runnable {

        private final DatagramSocket socketudp;

        public EcouteurUDP(DatagramSocket udp) {
            this.socketudp = udp;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[PACKET_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    socketudp.receive(packet);

                    String str = new String(packet.getData());

                    System.out.println(str);
                    packet.setLength(buffer.length);
                }
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class MessagesClient {

        public static void joinThread(Thread t) throws InterruptedException {
            t.start();
            t.join();
        }
    }

}
