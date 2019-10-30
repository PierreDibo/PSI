
/**
 *
 * @author Pierre Dibo
 */
public class Annonce {

    public final int identifiant;
    private static int compteur = 0;
    private final Domaine domaine;
    private long prix;
    private String description;

    public Annonce(Domaine d, long p, String desc) {
        this.identifiant = compteur++;
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

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
