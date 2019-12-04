public class Utilisateur{

	private String pseudo;
	private String motDePasse;
	private String host;
	private int port;

	public Utilisateur(String pseudo, String motDePasse, String host, int port){
		this.pseudo = pseudo;
		this.motDePasse = motDePasse;
		this.host = host;
		this.port = port;
	}

	public String getPseudo(){
		return this.pseudo;
	}

	public void setPseudo(String pseudo){
		this.pseudo = pseudo;
	}

	public String getMotDePasse(){
		return this.motDePasse;
	}

	public void setMotDePasse(String motDePasse){
		this.motDePasse = motDePasse;
	}

	public String getHost(){
		return this.host;
	}

	public void setHost(String host){
		this.host = host;
	}

	public int getPort(){
		return this.port;
	}

	public void setPort(int port){
		this.port = port;
	}
}