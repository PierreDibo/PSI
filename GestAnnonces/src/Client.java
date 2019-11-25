
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
    private static final int ADRESS_PORT = 0, INDEX_PORT = 1;

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
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage : java Client ip port_tcp [port_upd]");
        }
        try {
            InetAddress ia = InetAddress.getByName(args[ADRESS_PORT]);
            int port = Integer.parseInt(args[INDEX_PORT]);

            switch (args.length) {
                case 2:
                    tcp = new Socket(ia, port);

                    new Thread(new Ecrivain(tcp)).start();
                    new Thread(new Ecouteur(tcp)).start();

                    break;
                case 3:
                    tcp = new Socket(ia, port);
                    DatagramSocket udp = new DatagramSocket(port, ia);

                    new Thread(new Ecrivain(tcp, udp)).start();
                    new Thread(new Ecouteur(tcp, udp)).start();

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
        private final DatagramSocket socketudp;

        public Ecrivain(Socket socket) {
            this.sockettcp = socket;
            this.socketudp = null;
        }

        public Ecrivain(Socket tcp, DatagramSocket upd) {
            this.sockettcp = tcp;
            this.socketudp = upd;
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
                    if (!this.sockettcp.isClosed()) {
                        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.sockettcp.getOutputStream()));
                        output.write(content);
                        output.newLine();
                        output.flush();
                    }
                    if (this.socketudp != null) {
                        if (this.sockettcp.isClosed() && this.socketudp.isClosed()) {
                            break;
                        }
                    } else {
                        if (this.sockettcp.isClosed()) {
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    static class Ecouteur implements Runnable {

        private final Socket sockettcp;
        private final DatagramSocket socketudp;

        public Ecouteur(Socket tcp) {
            this.sockettcp = tcp;
            this.socketudp = null;
        }

        public Ecouteur(Socket tcp, DatagramSocket udp) {
            this.sockettcp = tcp;
            this.socketudp = udp;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(sockettcp.getInputStream()));
                String line;

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith(MessageType.MSG_QUIT)) {
                        this.sockettcp.close();
                        break;
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
