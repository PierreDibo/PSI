
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author Pierre Dibo
 */
public class Client {

    public final int identifiant;
    private static int compteur = 0;
    private final String nom;
    private final ArrayList<Annonce> annonces;

    public Client(String n) {
        this.identifiant = compteur++;
        this.nom = n;
        this.annonces = new ArrayList<>();
    }

    public String getNom() {
        return nom;
    }

    public ArrayList<Annonce> getAnnonces() {
        return annonces;
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
        Domaine d;
        long prix;
        String desc;

        d = choisirDomaine();
        prix = donnerPrix();
        desc = donnerDesciption();

        return this.annonces.add(new Annonce(d, prix, desc));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Client nomClient");
            return;
        }
        try (final Socket socket = new Socket(InetAddress.getLocalHost(), 0)) {
            Client client = new Client(args[0]);

            Thread ecrivain = new Thread(() -> {

            });

            Thread ecouteur = new Thread(() -> {

            });

            ecrivain.start();
            ecouteur.start();

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

}
