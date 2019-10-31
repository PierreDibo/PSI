
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Client {

    public final int identifiant;
    private static int compteur = 0;
    private final ArrayList<Annonce> annonces;

    public Client() {
        this.identifiant = compteur++;
        this.annonces = new ArrayList<>();
    }

    public ArrayList<Annonce> getAnnonces() {
        return annonces;
    }

    private String choisirNom() {
        String str;
        do {
            System.out.println("Donnez un nom à votre annonce");
            try ( Scanner sc = new Scanner(System.in)) {
                str = sc.nextLine();
            }
        } while (str == null);

        return str;
    }

    private Domaine choisirDomaine() {
        Domaine d = null;
        do {
            System.out.println("Donnez un domaine d'annonce parmis les domaines suivant :");
            System.out.println(Domaine.descripteur());
            try ( Scanner sc = new Scanner(System.in)) {
                d = Annonce.getDomaine(sc.next().toLowerCase(Locale.ROOT));
            }
        } while (d == null);

        return d;
    }

    private long donnerPrix() {
        long p;
        do {
            System.out.println("Donnez un prix à votre annonce");
            try ( Scanner sc = new Scanner(System.in)) {
                p = sc.nextLong();
            }
        } while (p <= 0);

        return p;
    }

    private String donnerDesciption() {
        String desc = null;
        do {
            System.out.println("Donnez une desciption de votre annonce");
            try ( Scanner sc = new Scanner(System.in)) {
                desc = sc.nextLine();
            }
        } while (desc == null);

        return desc;
    }

    public boolean ajouterAnnonce() {
        String str;
        Domaine d;
        long prix;
        String desc;

        str = choisirNom();
        d = choisirDomaine();
        prix = donnerPrix();
        desc = donnerDesciption();

        return this.annonces.add(new Annonce(str, d, prix, desc));
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
            try {
            	Scanner in = new Scanner(System.in);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                while (in.hasNextLine())
                    out.println(in.nextLine());
                
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
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

                while ((line = br.readLine()) != null)
                    System.out.println(line);
                
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
