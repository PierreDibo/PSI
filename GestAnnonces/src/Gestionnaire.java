
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Gestionnaire {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try (final ServerSocket server = new ServerSocket(1027)) {
            while (true) {
                Socket clientSocket = server.accept();

                new Thread(new ClientEcouteur(clientSocket)).start();
                new Thread(new ClientEcrivain(clientSocket, "WELCOME")).start();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class ClientEcouteur implements Runnable {

        private final Socket client;

        public ClientEcouteur(Socket s) {
            this.client = s;
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
                String str;
                while ((str = input.readLine()) != null) {
                    System.out.println(str);
                }
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    static class ClientEcrivain implements Runnable {

        private final Socket client;
        private final String message;

        public ClientEcrivain(Socket s, String msg) {
            this.client = s;
            this.message = msg;
        }

        @Override
        public void run() {
            try {
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                output.write(this.message + " ***\n");
                output.flush();
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
