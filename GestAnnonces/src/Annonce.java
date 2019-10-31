
/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Annonce {

    private final int identifiant;
    private static int compteur = 0;
    private String nom;
    private final Domaine domaine;
    private long prix;
    private String description;

    public Annonce(String nom, Domaine d, long p, String desc) {
        this.identifiant = compteur++;
        this.nom = nom;
        this.domaine = d;
        this.prix = p;
        this.description = desc;
    }

    public int getIdentifiant() {
        return identifiant;
    }

    public Domaine getDomaine() {
        return domaine;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrix(long prix) {
        this.prix = prix;
    }
    

    public static Domaine getDomaine(String s) {
        switch (s) {
            case "voiture":
                return Domaine.voiture;
            case "moto":
                return Domaine.moto;
            case "musique":
                return Domaine.musique;
            case "electromenager":
                return Domaine.electromenager;
            case "telephone":
                return Domaine.telephone;
            case "autres":
                return Domaine.autres;
            default:
                return null;
        }
    }

    public long getPrix() {
        return prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Annonce: " + nom + "\n"
                + "Domaine : " + domaine + "\n"
                + "Identifiant : " + identifiant + "\n"
                + "Prix : " + prix + "€\n"
                + "Description :\n" + description + "\n";
    }

}
