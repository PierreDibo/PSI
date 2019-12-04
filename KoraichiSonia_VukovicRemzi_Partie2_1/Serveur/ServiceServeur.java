import java.net.*;
import java.io.*;
import java.lang.*;
import java.security.*;


public class ServiceServeur implements Runnable{

	public Socket socket;
	public BufferedReader br;
	public PrintWriter pw;
	public boolean finish;
	public String currentPseudo;
	public DatagramSocket ds;

	private static final String salt = "é*♫5Ma   ■";

	public ServiceServeur(Socket s){
		this.socket=s;
		try {
			br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			ds=new DatagramSocket();
			this.currentPseudo =null;
			this.finish = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String executeSaltMD5(String passwordToHash){
		StringBuffer stringBuffer = new StringBuffer();
		try{
        	String machin=passwordToHash+salt;
        	//return DigestUtils.md5Hex(machin);
        	MessageDigest messageDigest;            
			messageDigest = MessageDigest.getInstance("MD5");            
			messageDigest.update(machin.getBytes());            
			byte[] messageDigestMD5 = messageDigest.digest();
			//pour affichage en hexa                        
			for (byte bytes : messageDigestMD5) {                
				stringBuffer.append(String.format("%02x", bytes & 0xff));            
			}                                
		} catch (NoSuchAlgorithmException exception) {            
			exception.printStackTrace();        
		}
		return stringBuffer.toString();
    }
	
	public void new_client(String pseudo, String motDePasse) {
		if(!Serveur.base.utilisateurExisteDatabase(pseudo)){
			if(Serveur.base.ajoutUtilisateurDatabase(pseudo, executeSaltMD5(motDePasse), this.socket.getInetAddress().toString(),this.socket.getPort())){
				pw.print("NEW_SUCCESS***\n");
				pw.flush();
			}
		}
		else{
			pw.print("NEW_ERROR***\n");
			pw.flush();
		}
	}

	public void add_annonce(String nomAnnonce, Domaines domaine, int prix, String utilisateurAnnonce, String description){
		System.out.println("Ajout de l'annonce");
		if(Serveur.base.ajoutAnnonceDatabase(nomAnnonce, domaine, prix, description, utilisateurAnnonce)){
			pw.print("ADD_SUCCESS***\n");
			pw.flush();
		}
		else{
			pw.print("ADD_ERROR***\n");
			pw.flush();
		}
	}
	
	//Supprimer une annonce
	public void delete_annonce(int id){
		if(Serveur.base.supprimerAnnoncesUtilisateurDatabaseById(id)){
			pw.print("DELETE_SUCCESS***\n");
			pw.flush();
		}else{
			pw.print("DELETE_ERROR***\n");
			pw.flush();
		}
	}

	public void connexion_client(String pseudo, String motDePasse){
		if(Serveur.base.utilisateurConnecteDatabase(pseudo)){
			pw.print("CONNECT_ERROR***\n");
			pw.flush();
		}
		Utilisateur tmp = Serveur.base.rechercheUtilisateurParPseudo(pseudo);
		if(tmp.getMotDePasse().equals(executeSaltMD5(motDePasse))){
			if(Serveur.base.ajoutUtilisateurConnecteDatabase(tmp.getPseudo(), executeSaltMD5(tmp.getMotDePasse()), tmp.getHost(), tmp.getPort())){
				this.currentPseudo = pseudo;
				pw.print("CONNECT_SUCCESS***\n");
				pw.flush();
			}
		}else{
			pw.print("CONNECT_ERROR***\n");
			pw.flush();
		}
	}

	public void nombre_annonce(){
		int nombre = Serveur.base.getNombreAnnonces();
		System.out.println("Il y a actuellement " + nombre + " annonces ");

		pw.print("ANNONCES_NB "+nombre+"***\n");
		pw.flush();
	}

	public void annonce(int n){
		for(int i = 0; i < n; i++){
			Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(i);
			pw.print("ANNONCE " + a.getIdAnnonce() + " " + a.getDomaine() + " " + a.getPrix() +" "+ a.getNomAnnonce() +" "+ a.getUtilisateurAnnonce()+"***\n");
		//	pw.print("Id : " + a.getIdAnnonce() + " - Nom de l'annonce: "+a.getNomAnnonce()+ " - Domaine: "+a.getDomaine()+" - Prix: "+a.getPrix()+" - Descriptif: "+a.getDescriptif()+ " - De l'utilisateur: "+a.getUtilisateurAnnonce()+"\n");
			pw.flush();
		}
	 }

	public void annonce_client(String pseudo){

		for(int i = 0; i < Serveur.base.getListeAnnonces().size(); i++){
			Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(i);
			if(a.getUtilisateurAnnonce().equals(pseudo)){		
				pw.print("ANNONCE " + a.getIdAnnonce() + " " + a.getDomaine() + " " + a.getPrix() +" "+ a.getNomAnnonce() +" "+ a.getUtilisateurAnnonce()+"***\n");
				pw.flush();
			}
		}
	}

	public void annonce_price(int prix){

		for(int i = 0; i < Serveur.base.getListeAnnonces().size(); i++){
			Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(i);
			if(a.getPrix()==prix){		
				pw.print("ANNONCE " + a.getIdAnnonce() + " " + a.getDomaine() + " " + a.getPrix() +" "+ a.getNomAnnonce() +" "+ a.getUtilisateurAnnonce()+"***\n");
				pw.flush();
			}
		}
	}

	public void annonce_domaine(int n,Domaines domaine){

		for(int i = 0; i < Serveur.base.getListeAnnonces().size(); i++){
			Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(i);
			if(a.getDomaine().equals(domaine)){		
				pw.print("ANNONCE " + a.getIdAnnonce() + " " + a.getDomaine() + " " + a.getPrix() +" "+ a.getNomAnnonce() +" "+ a.getUtilisateurAnnonce()+"***\n");
				pw.flush();
			}
		}
	}

	//Liste de toutes les annonces
	public void check_all_annonces() {

		int nombre = Serveur.base.getNombreAnnonces();
		System.out.println("Il y a actuellement " + nombre + " annonces ");

		pw.print("ANNONCES_NB "+nombre+"***\n");
		pw.flush();
		annonce(nombre);
	}
	
	//Liste de toutes les annonces d'un domaine
	public void check_annonce_domaine(Domaines d) {
		boolean b = false;
		int compteur=0;
		for(int i = 0; i < Serveur.base.getListeAnnonces().size(); i++){
			Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(i);
			if(a.getDomaine().equals(d)){
				b = true;
				compteur++;
			}
		}

		if(b == false){
			pw.print("DOMAINE_NOT_EXIST***\n");
			pw.flush();
		} else {
			pw.print("ANNONCES_NB "+compteur+"***\n");
			pw.flush();
			annonce_domaine(compteur,d);
		}
	}
	
	//Liste de toutes les annonces postées par un client
	public void check_annonce_client(String pseudo) {
		boolean b = false;
		int compteur = 0;
		for(int i = 0; i < Serveur.base.getListeUtilisateurs().size(); i++){
			Utilisateur u = (Utilisateur)Serveur.base.getListeUtilisateurs().get(i);
			if(u.getPseudo().equals(pseudo)){
				b =true;

				for(int j = 0; j < Serveur.base.getListeAnnonces().size(); j++){
					Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(j);
					if(a.getUtilisateurAnnonce().equals(pseudo)){
						
					compteur++;

					}
				}
			}
		}
		if(b == false){
			pw.print("CLIENT_NOT_EXIST***\n");
			pw.flush();
		} else {
			pw.print("ANNONCES_NB "+compteur+"***\n");
			pw.flush();
			annonce_client(pseudo);
		}

	}

	public void check_annonce_price(int price){
		boolean b = false;
		int compteur = 0;
		for(int i = 0; i < Serveur.base.getListeAnnonces().size(); i++){
			Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(i);
			if(a.getPrix() <= price){
				b = true;
				compteur++;
			}
		}
		if(b == false){
			pw.print("Aucune annonce dans cette tranche de prix.***\n");
			pw.flush();
		} else {
			pw.print("ANNONCES_NB "+compteur+"***\n");
			pw.flush();
			annonce_price(price);
		}
	}

	public void wrong_id(){
		pw.print("ANNONCE_NOT_EXIST***\n");
		pw.flush();
	}

	public void check_description(int idAnnonce){
		boolean found=false;
		for(int i = 0; i < Serveur.base.getListeAnnonces().size(); i++){
			Annonce a = (Annonce)Serveur.base.getListeAnnonces().get(i);
			if(a.getIdAnnonce() == idAnnonce){
				found=true;
			//	System.out.println("description demandé : " + a.getDescriptif());
				if(a.getDescriptif().length()!=0){
					pw.print("DESCRIPTION "+a.getDescriptif()+"***\n");
					pw.flush();
				} else {
					pw.print("DESCRIPTION_VIDE***\n");
					pw.flush();
				}
				break;
			}
		}
		if(found==false){
			wrong_id();
		}
	}
	
	//TO DO LATER : contact entre deux clients
	/*public void contact(String pseudo, String message) {
		if(Serveur.base.utilisateurExisteDatabase(pseudo) && Serveur.base.utilisateurConnecteDatabase(pseudo)){
			pw.print("RECEIVED_MESSAGE "+message+"***\n");
			pw.flush();
		}
	}*/

	public void infos_domaines(){
		String mes="LISTE_DOMAINE"+Domaines.descripteur()+"***\n";
		pw.print(mes);
		pw.flush();
	}
	
	public void help() {
		String mess = 
					" - Inscription : NEW nom prenom pseudo MotDePasse" + 
					" - Connexion : CONNECT pseudo MotDePasse" +
					" - Poster une annonce: ADD_ANNONCE titre || domaine prix description*** " +
				    " - Supprimer une annonce: DELETE id*** " +
				    " - Toutes les annonces: CHECK_ALL_ANNONCES*** " +
				    " - Toutes les annonces d'un client: CHECK_ANNONCES_CLIENT id*** " +
				    " - Liste des commandes : HELP*** " +
				    " - Quitter : QUIT***" +"\n";
				pw.print(mess);
				pw.flush();
	}

	public void disconnect(){
		if(this.currentPseudo!=null){
			Serveur.base.supprimerUtilisateurConnecteDatabase(this.currentPseudo);
			this.currentPseudo=null;
			pw.print("DISCONNECT_SUCCESS***\n");
			pw.flush();
		} else {
			error_disconnect();
		}
	}
	
	//Deconnexion du client
	public void quit() {
		pw.print("QUIT_SUCCESS***\n");
		pw.flush();
		pw.close();
		try {
			br.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		Serveur.base.supprimerUtilisateurConnecteDatabase(this.currentPseudo);
		System.out.println("Le client " + this.currentPseudo + " est parti!\n");
	}
	
	//Envoie au client ce message si la commande tapé n'a pas le bon nombre d'arguments
	public void error_command() {
		pw.print("Erreur dans le nombre/type d'arguments\n");
		pw.flush();
	}

	public void error_disconnect(){
		pw.print("DISCONNECT_ERROR***\n");
		pw.flush();
	}
	
	public void error_domaine() {
		pw.print("Erreur dans le nom de domaine\n");
		pw.flush();
	}

	public void error_price() {
		pw.print("Erreur : le prix doit etre un entier\n");
		pw.flush();
	}

	public void error_connexion() {
		pw.print("NOPE***\n");
		pw.flush();
	}


	public void treat_string(String s) {
		String sub=s.substring(0, s.length() - 3); //On supprime les 3 etoiles a la fin
		String[] tab=sub.split(" ");
		switch(tab[0]) {
		case ("NEW"):
			if(tab.length==3) {
				String pseudo=tab[1];
				String mdp=tab[2];
				new_client(pseudo,mdp);
			} else {
				error_command();
			}
			break;
		case ("CONNECT"):
			System.out.println("Connexion client");
			if(tab.length==3) {
				String pseudo=tab[1];
				String mdp=tab[2];
				connexion_client(pseudo, mdp); 
			} else {
				error_command();
			}
			break;
		case ("ANNONCES_NB"):
			nombre_annonce();
		case ("ADD_ANNONCE"):
			if(tab.length>=4) { //Minimum 3 arguments 

				String titre="";
				int prix=0;
				String description="";
				
				int pos = s.indexOf("||");
		        if (pos == -1) {
		            error_command();
		        }
		        titre = s.substring(0, pos);
		        String[] titreTab = titre.split(" ", 2);
		        titre=titreTab[1];
				//System.out.println("Le titre : " +titre);

				///////////////////////////////////////////////
				int posB=sub.lastIndexOf("||");
				String rest=sub.substring(posB+2);
				String[] comTab=rest.split(" ");
				String domaine=comTab[0];
				int compteur=1;
				System.out.println("domaine : " + domaine);
				Domaines d=Domaines.contains(domaine); 			//Verifier si le domaine existe bien
				if(d!=null) {
					try {
					prix = Integer.parseInt(comTab[compteur]);
					//System.out.println(prix);
					compteur++;
						if(compteur<tab.length) {
							System.out.println("on a trouve une description");
							for(int i=compteur;i<comTab.length;i++){
								description+=comTab[i]+ " ";
								System.out.println(comTab[i]);
								compteur++;
							}
						}
						System.out.println(description);
						if(this.currentPseudo!=null){
							add_annonce(titre, d, prix, this.currentPseudo, description);
						} else {
							error_connexion();
						}

					} catch(Exception e) {
						error_price();
					}

					

				} else {
					error_domaine();
				}
			} else {
				error_command();
			}
			break;
		case ("DELETE_ANNONCE"):
			if(tab.length == 2){
				try{
					int id = Integer.parseInt(tab[1]);
					delete_annonce(id);
				} catch(Exception e){
					error_command();
				}
			} else{
				error_command();
			}
			break;
		case ("CHECK_ALL_ANNONCES"):
			check_all_annonces();
			break;
		case ("CHECK_ANNONCES_CLIENT"):
			if(tab.length==2) { //Minimum 3 arguments 
				String client=tab[1];
				check_annonce_client(client);
			} else {
				error_command();
			}
			break;
		case ("CHECK_ANNONCES_DOMAINE"):
			String dom=tab[1];
			Domaines d=Domaines.contains(dom);
				if(d!=null) { //Si le domaine tapé existe bien on l'ajoute
					check_annonce_domaine(d);
				} else {
					error_domaine();
				}
			break;
		case ("CHECK_ANNONCES_PRICE"):
			System.out.println("Check annonces price");
			if(tab.length == 2){
				try {
				int price = Integer.parseInt(tab[1]);
				check_annonce_price(price);
				} catch(Exception e){
					pw.print("PRICE_ERROR***");
					pw.flush();
				}
			}
			else{
				error_command();
			}
		break;

		case("INFO_ALL_CLIENTS"):
		//	info_all_clients();
		break;

		case("CHECK_DESCRIPTION"):
			if(tab.length == 2){
				try{
					int id = Integer.parseInt(tab[1]);
					check_description(id);
				} catch(Exception e){
					error_command();
				}
			}
			else{
				error_command();
			}
			break;
		case ("CONTACT"):
			if(tab.length >= 2){
				String i= tab[1];
				String message = "";
				for(int j = 2; j < tab.length; j++){
					message = message+" "+tab[j];
				}
				try{
					contact(this.currentPseudo, i, message);
				}catch(Exception e){
					System.out.println("Erreur contact");
				}
			}
			else{
				error_command();
			}
			break;
		case ("HELP"):
			//System.out.println("Help");
			help();
			break;
		case ("QUIT"):
			this.finish = true;
			quit();
			break;
		case("INFO_DOMAINES"):
			infos_domaines();
			break;
		case ("DISCONNECT"):
			disconnect();
			break;
		default:
			pw.print("INVALID***\n");
			pw.flush();
			break;
		}
	}

	public boolean contact(String id, String id2, String mess) throws IOException{
        Utilisateur u = Serveur.base.rechercheUtilisateurParPseudo(id);//get utilisateur par pseudo
        if (u == null) {
            return false;
        }

        InetAddress address = InetAddress.getByName(u.getHost());
        int port = u.getPort();
        byte[] buffer = makeSendBuffer(id2, mess);

        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, address, port);
        ds.send(dp);
        return true;
    }

    private byte[] makeSendBuffer(String id2, String mess) {
        byte[] out = new byte[id2.length() + mess.length() + 9];
        int offset = 0;
        offset = putStringInBuffer("MESS ", out, offset);
        offset = putStringInBuffer(id2, out, offset);
        offset = putStringInBuffer(" ", out, offset);
        offset = putStringInBuffer(mess, out, offset);
        putStringInBuffer("***", out, offset);
        return out;
    }

    private int putStringInBuffer(String s, byte[] buffer, int offset) {
        int i;
        for (i = 0; i < s.length() && offset + i < buffer.length; i++) {
            buffer[offset + i] = (byte)s.charAt(i);
        }
        return offset + i;
    }

	public void run(){
		try{
				System.out.println("Nouveau client !");
				String mess = 
					" - Inscription : NEW nom prenom pseudo MotDePasse" + 
					" - Connexion : CONNECT pseudo MotDePasse" +
					" - Domaines : INFOS_DOMAINES " +
					" - Poster une annonce: ADD_ANNONCE titre || domaine prix description " +
				    " - Supprimer une annonce: DELETE id " +
				    " - Toutes les annonces: CHECK_ALL_ANNONCES " +
				    " - Toutes les annonces d'un client: CHECK_ANNONCES_CLIENT id " +
				    " - Toutes les annonces d'un domaine : CHECK_ANNONCES_DOMAINE domaine " +
				    " - Toutes les annonces: CHECK_ANNONCES_PRICE prix " +
				    " - Description d'une annonce : CHECK_DESCRIPTION id " +
				    " - Liste des commandes : HELP " +
				    " - Quitter : QUIT" +"\n";
				pw.print(mess);
				pw.flush();
			while(!this.finish){
				String recu = br.readLine();
				System.out.println("Message recu du client " + this.currentPseudo + " : " + recu);
				treat_string(recu);
			}

		}catch(Exception e){
			System.out.println("Un client est parti.");
			//e.printStackTrace();
		}
	}
	

}