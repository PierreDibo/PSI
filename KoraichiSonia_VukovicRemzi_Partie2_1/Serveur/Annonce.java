public class Annonce{

	private int idAnnonce; //auto-incrementÃ© dans Database
	private String nomAnnonce;
	private Domaines domaine;
	private int prix;
	private String descriptif;
	private String utilisateurAnnonce;

	public Annonce(int idAnnonce, String nomAnnonce, Domaines domaine, int prix, String descriptif, String utilisateurAnnonce){
		this.idAnnonce = idAnnonce;
		this.nomAnnonce = nomAnnonce;
		this.domaine = domaine;
		this.prix = prix;
		this.descriptif = descriptif;
		this.utilisateurAnnonce = utilisateurAnnonce;
	}

	public int getIdAnnonce(){
		return this.idAnnonce;
	}

	public void setIdAnnonce(int idAnnonce){
		this.idAnnonce = idAnnonce;
	}

	public String getNomAnnonce(){
		return this.nomAnnonce;
	}

	public void setNomAnnonce(String nomAnnonce){
		this.nomAnnonce = nomAnnonce;
	}

	public Domaines getDomaine(){
		return this.domaine;
	}

	public void setDomaine(Domaines domaine){
		this.domaine = domaine;
	}

	public int getPrix(){
		return this.prix;
	}

	public void setPrix(int prix){
		this.prix = prix;
	}

	public String getDescriptif(){
		return this.descriptif;
	}

	public void setDescriptif(String descriptif){
		this.descriptif = descriptif;
	}

	public String getUtilisateurAnnonce(){
		return this.utilisateurAnnonce;
	}

	public void setUtilisateurAnnonce(String utilisateurAnnonce){
		this.utilisateurAnnonce = utilisateurAnnonce;
	}
}