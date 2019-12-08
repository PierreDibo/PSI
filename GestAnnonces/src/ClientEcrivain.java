
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ClientEcrivain extends Client implements Runnable, Messages {

    private Socket socket;
    private final ClientEcouteur ecouteur;
    private MessageType messageType;

    public ClientEcrivain(Socket s, ClientEcouteur ecouteur) {
        this.socket = s;
        this.ecouteur = ecouteur;
        this.messageType = null;
    }

    public void setSocketClient(Socket socketClient) {
        this.socket = socketClient;
    }

    @Override
    public void run() {
        ConsoleInputReadTask console = new ConsoleInputReadTask();
        for (;;) {
            try {
                String content = console.call();

                while (!content.contains(MessageType.END.getMessage())) {
                    content += " " + console.call();
                }

                String msg = content.substring(0, content.length() - 3);

                if (this.socket.isClosed()) {
                    if (ecouteur.getClientRemote() != null && ecouteur.getClientRemote().getPseudo() != null) {
                        Policy.clean(ecouteur.getClientRemote().getPseudo());
                    }
                    break;
                }
                try {
                    parse(msg);
                } catch (NumberFormatException ex) {
                    System.err.println("Erreur d'écriture");
                    write(MessageType.MSG_INVALID, this.socket);
                }
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void parse(String content) throws IOException {
        String[] input = content.split("\\s+");
        int i = 0;
        try {
            messageType = MessageType.valueOf(input[i++]);
        } catch (IllegalArgumentException ex) {
            messageType = MessageType.INVALID;
        }
        if (ecouteur.getProtocole() == null) {
            non_secure(input, content, i);
        } else {
            secure(input, content);
        }
    }

    private void secure(String[] input, String content) throws IOException {
        switch (this.messageType) {
            case CALL_OPEN:
                break;
            case CALL:
                break;
            case CALL_CLOSE:
                break;
            default:
                write(content, this.socket);
                break;
        }
    }

    private void non_secure(String[] input, String content, int i) throws IOException {
        switch (this.messageType) {
            case CALL_OPEN:
                callOpen(input, content, i);
                break;
            case CALL:
                call(input, i);
                break;
            case CALL_CLOSE:
                callClose(input, i);
                break;
            default:
                write(content, this.socket);
                break;
        }
    }

    private void callOpen(String[] input, String content, int i) throws IOException {
        if (ecouteur.getClientRemote() != null) {
            InetAddress adresseClient = InetAddress.getByName(input[i++]);
            int portClient = Integer.parseInt(input[i++]);

            if (Policy.isBanned(ecouteur.getClientRemote().getPseudo(), adresseClient, portClient) == null) {
                Policy.print(ecouteur.getClientRemote().getPseudo());
                if (Policy.isAccepted(ecouteur.getClientRemote().getPseudo(), adresseClient, portClient) == null) {
                    Socket s = new Socket(adresseClient, portClient);
                    Contact contact = new Contact(ecouteur.getClientRemote(), s);
                    new Thread(contact).start();
                    String infos = this.messageType
                            + " " + ecouteur.getClientRemote().getPseudo()
                            + " " + ecouteur.getClientRemote().getIaddr().getHostAddress()
                            + " " + ecouteur.getClientRemote().getPort();
                    contact.write(infos);
                } else {
                    System.out.println("Déja connecté.");
                }
            } else {
                System.out.println("Dans ta ban liste");
            }
        } else {
            System.out.println("Vous avez besoin d'être connecté avec un port valide.");
        }
    }

    private void call(String[] input, int i) throws IOException {
        if (ecouteur.getClientRemote() != null) {
            Contact contact;
            String pseudo = input[i++];
            String message = String.join(" ", Arrays.copyOfRange(input, i, input.length));

            if (Policy.isBanned(ecouteur.getClientRemote().getPseudo(), pseudo) == null) {
                if ((contact = Policy.isAccepted(ecouteur.getClientRemote().getPseudo(), pseudo)) != null) {
                    contact.write(this.messageType.name() + " " + message);
                } else {
                    System.out.println(pseudo + " n'est pas dans votre liste de contact");
                }
            } else {
                System.out.println(pseudo + " est dans votre ban liste");
            }
        } else {
            System.out.println("Vous avez besoin d'être connecté avec un port valide.");
        }
    }

    private void callClose(String[] input, int i) throws IOException {
        if (ecouteur.getClientRemote() != null) {
            Contact contact;
            String pseudo = input[i++];

            if (Policy.isBanned(ecouteur.getClientRemote().getPseudo(), pseudo) == null) {
                if ((contact = Policy.isAccepted(ecouteur.getClientRemote().getPseudo(), pseudo)) != null) {
                    contact.write(this.messageType.name());
                } else {
                    System.out.println(pseudo + " n'est pas dans votre liste de contact");
                }
            } else {
                System.out.println(pseudo + " est dans votre ban liste");
            }
        } else {
            System.out.println("Vous avez besoin d'être connecté avec un port valide.");
        }
    }

    private void write(String content, Socket socket) throws IOException {
        if (socket.isConnected()) {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            output.write(content + MessageType.END.getMessage());
            output.newLine();
            output.flush();
        }
    }

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

    }

}
