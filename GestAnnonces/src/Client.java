
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Client {

    static class ConsoleInputReadTask implements Callable<String> {

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public String call() throws IOException {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(System.in));
            String input;
            do {
                try {
                    // wait until we have data to complete a readLine()
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
    public final int identifiant;
    private static int compteur = 0;
    private static final Scanner SC = new Scanner(System.in);

    public Client() {
        this.identifiant = compteur++;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));

            new Thread(new Ecrivain(socket)).start();
            new Thread(new Ecouteur(socket)).start();

        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class Ecrivain implements Runnable {

        private final Socket socket;

        public Ecrivain(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            ConsoleInputReadTask console = new ConsoleInputReadTask();
            while (this.socket != null && !this.socket.isInputShutdown()) {
                try {
                    BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
                    String content = console.call();
                    while (!content.contains(Message.FIN_MESSAGE.trim())) {
                        content += " " + console.call();
                    }

                    output.write(content + "\n");
                    output.flush();

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
                    if (line.startsWith("AUREVOIR")) {
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
