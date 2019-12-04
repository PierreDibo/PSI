import java.util.ArrayList;

public class Database{

  public ArrayList<Utilisateur> listeUtilisateurs;
  public ArrayList<Utilisateur> listeUtilisateursConnectes;
  public ArrayList<Annonce> listeAnnonces;
  public int idAnnonce;

  public Database(){
    listeUtilisateurs = new ArrayList<Utilisateur>();
    listeUtilisateursConnectes = new ArrayList<Utilisateur>();
    listeAnnonces = new ArrayList<Annonce>();
    idAnnonce = 0;
  }

  public ArrayList getListeUtilisateurs(){
      return this.listeUtilisateurs;
  }

  public ArrayList getListeUtilisateursConnectes(){
    return this.listeUtilisateursConnectes;
  }

  public ArrayList getListeAnnonces(){
      return this.listeAnnonces;
  }

  public int  getNombreAnnonces(){
    return this.listeAnnonces.size();
  }

  public Utilisateur rechercheUtilisateurParPseudo(String pseudo){
    for(int i = 0; i < listeUtilisateurs.size(); i++){
      if(listeUtilisateurs.get(i).getPseudo().equals(pseudo)){
        return listeUtilisateurs.get(i);
      }
    }
    return null;
  }

  public boolean ajoutAnnonceDatabase(String nomAnnonce, Domaines domaine, int prix, String descriptif, String utilisateurAnnonce){
    Annonce tmp = new Annonce(idAnnonce, nomAnnonce, domaine, prix, descriptif, utilisateurAnnonce);
    listeAnnonces.add(tmp);
    this.idAnnonce++;
    return true;
  }

  public boolean supprimerAnnoncesUtilisateurDatabase(String pseudoUtilisateurAnnonce, String motDePasse){
    for(int i = 0; i < listeAnnonces.size(); i++){
      if(listeAnnonces.get(i).getUtilisateurAnnonce().equals(pseudoUtilisateurAnnonce)){
        listeAnnonces.remove(i);
        return true;
      }
    }
    return false;
  }

  public boolean supprimerAnnoncesUtilisateurDatabaseById(int id){
    for(int i = 0; i < listeAnnonces.size(); i++){
      if(listeAnnonces.get(i).getIdAnnonce() == id){
        listeAnnonces.remove(i);
        return true;
      }
    }
    return false;
  }

  public boolean supprimerAnnonceDatabase(String pseudoUtilisateurAnnonce, String motDePasse, String nomAnnonce){
    boolean a = false;
    for(int i = 0; i <listeUtilisateurs.size(); i++){
      if(listeUtilisateurs.get(i).getPseudo().equals(pseudoUtilisateurAnnonce) && listeUtilisateurs.get(i).getMotDePasse().equals(motDePasse)){
        a = true;
        break;
      }
    }
    if(a){
      for(int i = 0; i < listeAnnonces.size(); i++){
        if(listeAnnonces.get(i).getUtilisateurAnnonce().equals(pseudoUtilisateurAnnonce) && listeAnnonces.get(i).getNomAnnonce().equals(nomAnnonce)){
          listeAnnonces.remove(i);
          return true;
        }
      }
    }
    return false;
  }

  public boolean ajoutUtilisateurDatabase(String pseudo, String motDePasse, String host, int port){
      for(int i = 0; i < listeUtilisateurs.size(); i++){
          if(pseudo.equals(listeUtilisateurs.get(i).getPseudo())) return false;
      }
      Utilisateur tmp = new Utilisateur(pseudo, motDePasse, host, port);
      listeUtilisateurs.add(tmp);
      //listeUtilisateursConnectes.add(tmp);
      return true;
    }


    public boolean supprimerUtilisateurDatabase(String pseudo){
      for(int i = 0; i < listeUtilisateurs.size(); i++){
        if(pseudo.equals(listeUtilisateurs.get(i).getPseudo())){
          listeUtilisateursConnectes.remove(i);
          return true;
        }
      }
      return false;
    }

    public boolean ajoutUtilisateurConnecteDatabase(String pseudo, String motDePasse, String host, int port){
      for(int i = 0; i < listeUtilisateursConnectes.size(); i++){
          if(pseudo.equals(listeUtilisateurs.get(i).getPseudo())) return false;
      }
      Utilisateur tmp = new Utilisateur(pseudo, motDePasse, host, port);
      listeUtilisateursConnectes.add(tmp);
      return true;
    }

    public boolean supprimerUtilisateurConnecteDatabase(String pseudo){
      for(int i = 0; i < listeUtilisateursConnectes.size(); i++){
        if(pseudo.equals(listeUtilisateursConnectes.get(i).getPseudo())){
          listeUtilisateursConnectes.remove(i);
          return true;
        }
      }
      return false;
    }

  public boolean utilisateurExisteDatabase(String pseudo){
    for(int i=0;i<listeUtilisateurs.size();i++){
      if(pseudo.equals(listeUtilisateurs.get(i).getPseudo())) return true;
    }
    return false;
  }

  public boolean utilisateurConnecteDatabase(String pseudo){
    for(int i = 0; i < listeUtilisateursConnectes.size(); i++){
      if(pseudo.equals(listeUtilisateursConnectes.get(i).getPseudo())) return true;
    }
    return false;
  }

}