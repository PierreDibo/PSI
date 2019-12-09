
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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
            secure(input, content, i);
        }
    }
    
    private void secure(String[] input, String content, int i) throws IOException {
        switch (this.messageType) {
            case CALL_OPEN:
                callOpenSSL(input, i);
                break;
            case CALL:
                callSSL(input, i);
                break;
            case CALL_CLOSE:
                callCloseSSL(input, i);
                break;
            default:
                write(content, this.socket);
                break;
        }
    }
    
    private void callOpenSSL(String[] input, int i) throws IOException {
        if (ecouteur.getClientRemote() != null) {
            InetAddress adresseClient = InetAddress.getByName(input[i++]);
            int portClient = Integer.parseInt(input[i++]);
            String msg = String.join(" ", Arrays.copyOfRange(input, i, input.length));
            try {
                SSLClient client = new SSLClient(ecouteur.getProtocole(), adresseClient, portClient);
                if (client.connect()) {
                    String infos = this.messageType
                            + " " + ecouteur.getClientRemote().getPseudo()
                            + " " + ecouteur.getClientRemote().getIaddr().getHostAddress()
                            + " " + ecouteur.getClientRemote().getPort()
                            + "\n" + msg + "\n";
                    client.write(infos);
                }
                client.shutdown();
            } catch (NoSuchAlgorithmException | KeyStoreException | FileNotFoundException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
                Logger.getLogger(ClientEcrivain.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Vous avez besoin d'être connecté avec un port valide.");
        }
    }
    
    private void callSSL(String[] input, int i) throws IOException {
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
    
    private void callCloseSSL(String[] input, int i) throws IOException {
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
    
    private void non_secure(String[] input, String content, int i) throws IOException {
        switch (this.messageType) {
            case CALL_OPEN:
                callOpen(input, i);
                break;
            case CALL:
                call(input, i);
                break;
            case CALL_CLOSE:
                callClose(input, i);
                break;
            case BAN:
                ban(input, i);
                break;
            case UNBAN:
                unban(input, i);
                break;
            default:
                write(content, this.socket);
                break;
        }
    }
    
    private void callOpen(String[] input, int i) throws IOException {
        if (ecouteur.getClientRemote() != null) {
            InetAddress adresseClient = InetAddress.getByName(input[i++]);
            int portClient = Integer.parseInt(input[i++]);
            
            if (Policy.isBanned(ecouteur.getClientRemote().getPseudo(), adresseClient, portClient) == null) {
                if (Policy.isAccepted(ecouteur.getClientRemote().getPseudo(), adresseClient, portClient) == null) {
                    
                    Socket s = new Socket(adresseClient, portClient);
                    Contact contact = new Contact(ecouteur.getClientRemote(), s);
                    new Thread(contact).start();
                    String infos = this.messageType
                            + " " + adresseClient.getHostAddress()
                            + " " + portClient
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
    
    private void ban(String[] input, int i) throws IOException {
        if (ecouteur.getClientRemote() != null) {
            Contact contact;
            String pseudo = input[i++];
            
            if (Policy.isBanned(ecouteur.getClientRemote().getPseudo(), pseudo) == null) {
                if ((contact = Policy.isAccepted(ecouteur.getClientRemote().getPseudo(), pseudo)) != null) {
                    Policy.removeContact(ecouteur.getClientRemote().pseudo, pseudo);
                    contact.write(MessageType.BAN.name());
                    Policy.addBanContact(ecouteur.getClientRemote().pseudo, contact);
                } else {
                    System.out.println(pseudo + " n'est pas dans votre liste de contact");
                }
            } else {
                System.out.println(pseudo + " est déjà dans votre ban liste");
            }
        } else {
            System.out.println("Vous avez besoin d'être connecté avec un port valide.");
        }
    }
    
    private void unban(String[] input, int i) {
        if (ecouteur.getClientRemote() != null) {
            Contact contact;
            String pseudo = input[i++];
            
            if (Policy.isAccepted(ecouteur.getClientRemote().getPseudo(), pseudo) == null) {
                if ((contact = Policy.isBanned(ecouteur.getClientRemote().getPseudo(), pseudo)) != null) {
                    Policy.removeBanContact(ecouteur.getClientRemote().getPseudo(), pseudo);
                    Policy.addContact(ecouteur.getClientRemote().getPseudo(), contact);
                } else {
                    System.out.println(pseudo + " n'est pas dans votre ban liste");
                }
            } else {
                System.out.println(pseudo + " n'est pas dans votre ban liste");
            }
        } else {
            System.out.println("Vous avez besoin d'être connecté avec un port valide.");
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
