
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Gestionnaire {
	private static volatile boolean runningEcouteur = true;
	public static final int ATTENTE = 100;
    private static final HashMap<Utilisateur, ArrayList<Annonce>> ANNONCES = new HashMap<>();

    public static boolean addAnnonce(Utilisateur u, Annonce e) {
        return Gestionnaire.ANNONCES.get(u).add(e);
    }

    public static boolean deleteAnnonce(Utilisateur u, int id) {
        ArrayList<Annonce> annonces = Gestionnaire.ANNONCES.get(u);

        for (int i = 0; i < annonces.size(); i++) {
            if (annonces.get(id).getIdentifiant() == id) {
                return annonces.remove(id) != null;
            }
        }

        return false;
    }

    private static void deleteSuccess(Socket s) throws InterruptedException {
    	joinThread(new Thread(new ClientEcrivain(s, "L'annonce a pu être supprimé")));
    }

    private static void deleteError(Socket s) throws InterruptedException  {
    	joinThread(new Thread(new ClientEcrivain(s, "L'annonce n'a pas pu être supprimé")));
    }

    private static void addSuccess(Socket s) throws InterruptedException  {
    	joinThread(new Thread(new ClientEcrivain(s, "L'annonce a pu être ajouté")));
    }

    private static void addError(Socket s) throws InterruptedException  {
    	joinThread(new Thread(new ClientEcrivain(s, "L'annonce n'a pas pu être ajouté")));
    }

    public static String checkAllAnnonces() {
        String s = "Annonces :\n";
        for (Entry<Utilisateur, ArrayList<Annonce>> entry : Gestionnaire.ANNONCES.entrySet()) {
            ArrayList<Annonce> values = entry.getValue();
            if (values.isEmpty()) {
                continue;
            }
            s += entry.getKey() + "\n";
            for (int i = 0; i < values.size(); i++) {
                s += values.get(i) + "\n";
            }
        }
        return s;
    }

    public static boolean existsUtilisateurs(Utilisateur o) {
        return Gestionnaire.ANNONCES.containsKey(o);
    }

    public static void addUtilisateurs(Utilisateur o) {
        Gestionnaire.ANNONCES.put(o, new ArrayList<>());
    }

    public static Utilisateur getUtilisateur(String pseudo, String mdp) {
        for (Utilisateur u : Gestionnaire.ANNONCES.keySet()) {
            if (u.getPseudo().equals(pseudo) && u.getMotDePasse().toString().equals(mdp)) {
                return u;
            }
        }
        return null;
    }

    public static void parse(String[] msg, Socket client) throws IOException, InterruptedException {
        Utilisateur u;
        int i = 1;
        switch (msg[0]) {
            case "NEW":
                u = new Utilisateur(msg[i++], msg[i++], client);
                if (!existsUtilisateurs(u)) {
                    addUtilisateurs(u);
                    u.toString();
                } else {
                	joinThread(new Thread(new ClientEcrivain(client, "L'utilisateur " + u.getPseudo() + " existe déjà.")));
                }
                break;
            case "CONNECT":
                break;
            case "QUIT":
            	joinThread(new Thread(new ClientEcrivain(client, "AUREVOIR")));
            	runningEcouteur = false;
                break;
            case "ADD_ANNONCE":
                if (msg.length == 8) {
                    if ((u = getUtilisateur(msg[i++], msg[i++])) != null) {
                        if (addAnnonce(u, new Annonce(msg[i++], Annonce.getDomaine(msg[i++]), Long.parseLong(msg[i++]), msg[i++]))) {
                            addSuccess(client);
                        } else {
                            addError(client);
                        }
                    } else {
                        addError(client);
                    }
                } else {
                    addError(client);
                }
                break;
            case "DELETE_ANNONCE":
                if (msg.length == 8) {
                    if ((u = getUtilisateur(msg[i++], msg[i++])) != null) {
                        if (addAnnonce(u, new Annonce(msg[i++], Annonce.getDomaine(msg[i++]), Long.parseLong(msg[i++]), msg[i++]))) {
                            addSuccess(client);
                        } else {
                            addError(client);
                        }
                    } else {
                        addError(client);
                    }
                } else {
                    addError(client);
                }
                break;
            case "UPDATE_ANNONCE":
                break;
            case "CHECK_ALL_ANNONCES":
            	joinThread(new Thread(new ClientEcrivain(client, checkAllAnnonces())));
                break;
            case "CHECK_ANNONCES":
            	
                break;
            /*case "CHECK_ANNONCES_UTILISATEUR":
                break;
            case "CHECK_ANNONCES_DOMAINE":
                break;*/
            case "HELP":
            	joinThread(new Thread(new ClientEcrivain(client, help())));
                break;
        }
    }
    
    public static void joinThread(Thread t) throws InterruptedException {
    	t.start();
    	t.join();
    }

    public static String help() {
        return "ADD_ANNONCE pseudo mdp nomAnnonce domaine prix description ***" + "\n"
                + "DELETE_ANNONCE id ***" + "\n"
                + "UPDATE_ANNONCE id [domaine | prix | description]+ ***" + "\n"
                + "CHECK_ALL_ANNONCES ***" + "\n"
                + "CHECK_ANNONCES_CLIENT id ***" + "\n"
                + "CHECK_ANNONCES_DOMAINE domaine ***" + "\n"
                + "ANNONCES ***" + "\n"
                + "ANNONCE ***" + "\n"
                + "NEW pseudo mdp ***" + "\n"
                + "CONNECT ***" + "\n"
                + "CONTACT ***" + "\n"
                + "QUIT ***" + "\n"
                + "HELP ";
    }
    
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
        		String[] msg;
        		while ((str = input.readLine()) != null && runningEcouteur) {
        			System.out.println(str);

        			msg = str.split("\\s+");
        			if(msg[msg.length - 1].equals("***")) {
        				parse(msg, client);
        			}

        			//System.out.println(Arrays.toString(str.split("\\s+")));
        		}

        	} catch (IOException ex) {
        		Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        	} catch (InterruptedException ex) {
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
        		output.write(this.message + Message.FIN_MESSAGE);
        		output.flush();
        	} catch (IOException ex) {
        		Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
        	}
        }
    }
}
