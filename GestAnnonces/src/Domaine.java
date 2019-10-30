
/**
 *
 * @author Pierre Dibo
 */
public enum Domaine {
    voiture, moto, musique, electromenager, telephone, autres;

    public static String descripteur() {
        return Domaine.voiture.name() + ", "
                + Domaine.moto.name() + ", "
                + Domaine.musique.name() + ", "
                + Domaine.electromenager.name() + ", "
                + Domaine.telephone.name() + ", "
                + Domaine.autres.name();
    }
}
