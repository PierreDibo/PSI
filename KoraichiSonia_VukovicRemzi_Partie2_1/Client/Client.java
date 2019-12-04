import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client{
   
   static boolean stop=true;
   static BufferedReader br;
   static PrintWriter pw;
   static Socket socket;
   
   public static void handleMessage(String mess) {
      //String sub=mess.substring(0, mess.length() - 3); //On supprime les 3 etoiles a la fin
      String[] tab=mess.split("\\s+");
     // String[] tab = mess.split(" ");
      //System.out.println(mess);
      //System.out.println(tab[0]);
      switch(tab[0]) {
   
      case ("ANNONCES_NB"):
         int nb_annonce=Integer.parseInt(tab[1]);
         System.out.println("Il y a actuellement " + nb_annonce + " annonces.");
         try{
	         for(int i=0;i<nb_annonce;i++){
	         	String mes=br.readLine();
               //String submes=mes.substring(0, mess.length() - 3);
               String[] tabmes=mes.split(" ");
               String annonce = " Id : " + tabmes[1] + " - Domaine : " + tabmes[2] + " - Prix : " + tabmes[3] + " ";
               for(int j=4;j<tabmes.length-1;j++){
                     annonce = annonce + " " + tabmes[j];
               }
               annonce=annonce + " - Utilisateur : " + tabmes[tabmes.length-1];
               System.out.println(annonce);
	         }
     	} catch (Exception e){
     		System.out.println(e);
     	}
         break;
      case("CONNECT_SUCCESS"):
         System.out.println("Connexion reussie!");
         break;
      case("CONNECT_ERROR"):
         System.out.println("Erreur de connexion! Vérifiez d'avoir taper le pseudo et le mot de passe correctement.");
         break;
      case("ADD_SUCCESS"):
         System.out.println("Publication de la nouvelle annonce reussie.");
         break;
      case("ADD_ERROR"):
         System.out.println("Erreur dans la publication de l'annonce!");
         break;
      case("NEW_SUCCESS"):
         System.out.println("Inscription reussie!");
         break;
      case("NEW_ERROR"):
         System.out.println("Erreur d'inscription! Pseudo déjà existant, veuillez en choisir un autre!");
         break;
      case("DELETE_SUCCESS"):
         System.out.println("Annonce supprimée!");
         break;
      case("DELETE_ERROR"):
         System.out.println("Erreur dans la suppression de l'annonce!");
         break;
      case("DOMAINE_NOT_EXIST"):
         System.out.println("Ce domaine n'existe pas. Veuillez entrer un bon domaine.");
         break;
      case("CLIENT_NOT_EXIST"):
         System.out.println("Ce client n'existe pas.");
         break;
      case("PRICE_ERROR"):
         System.out.println("Erreur dans le prix.");
         break;
      case("RECEIVED_MESSAGE"):
         String s ="";
         if(tab.length > 1){
            for(int i = 1; i < tab.length; i++){
               s = s +" "+ tab[i];
            }
         }
         System.out.println(s);
         break;
      case("QUIT_SUCCESS"):
         System.out.println("Au revoir!");
         try{
            br.close();
            pw.close();
            socket.close();
            System.exit(0);
         }catch(Exception e){
            e.printStackTrace();
         }
         break;
      case("QUIT_ERROR"):
         System.out.println("Impossible de quitter!");
         break;
      case("WELCOME"):
         System.out.println("Bienvenue!");
         break;
      case("NOPE"):
         System.out.println("Vous n'êtes pas connecté! Veuillez vous connecter ou créer un compte.");
         break;
      case("ANNONCE_NOT_EXIST"):
         System.out.println("Mauvais ID, cette annonce n'existe pas.");
         break;
      case("LISTE_DOMAINE"):
         System.out.println("Domaines : ");
         for(int i=1;i<tab.length;i++){
               System.out.println(tab[i]);
         }  
         break;
       case("DISCONNECT_SUCCESS"):
            System.out.println("Deconnexion reussie.");
       		break;
       case("DISCONNECT_ERROR"):
         	System.out.println("Erreur deconnexion : Vous n'etez pas connecte.");
      		break;
       case("DESCRIPTION"):
         System.out.println("Description : ");
         for(int i=0;i<tab.length;i++){
         	System.out.print(tab[i] + " ");
         }
         System.out.println(" ");
         break;
        case("DESCRIPTION_VIDE"):
         System.out.println("La description de cette annonce est vide.");
         break; 
         case("INVALID"):
         System.out.println("Je ne comprends pas votre demande.");
         break;
      default:
         System.out.println(mess);
         break;
      }  
      
   }

   public static void main(String[] args){
      try{
         Scanner sc = new Scanner(System.in);
         socket=new Socket(args[0],Integer.parseInt(args[1]));
         br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
         pw=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
         //String mess=br.readLine();
         //handleMessage(mess); 
         new Thread(() -> {
               try{
                  for(String line; (line = br.readLine()) != null;){
                     //System.out.println(line);
                     handleMessage(line);
                  }
               }catch(Exception e){
                  e.printStackTrace();
               }
            }).start();
         while(true){
            /*new Thread(() -> {
               try{
                  for(String line; (line = br.readLine()) != null;){
                     handleMessage(line);
                  }
               }catch(Exception e){
                  e.printStackTrace();
               }
            }).start();*/
            //for(String line; (line = br.readLine()) != null;){
               //handleMessage(line);
            //}
            String messageClient = sc.next();
            while(!messageClient.contains("***")){
               messageClient += sc.next();
            }
            pw.print(messageClient);
            System.out.println(messageClient);
            pw.flush();
            //mess=br.readLine();
            //handleMessage(mess);
         }
         
      }catch(Exception e){
         System.out.println("Erreur connexion avec le serveur");
      }
   }
}   