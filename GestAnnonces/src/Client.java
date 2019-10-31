
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 */
public class Client {

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
            Socket socket = new Socket(InetAddress.getLocalHost(), 1027);

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
            while (this.socket.isConnected()) {
                try {
                    BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
                    output.write(SC.nextLine() + Message.FIN_MESSAGE);
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
                BufferedReader input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                String str;
                while ((str = input.readLine()) != null) {
                    System.out.println(str);
                }
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
