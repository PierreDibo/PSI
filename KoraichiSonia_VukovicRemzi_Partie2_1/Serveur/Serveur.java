import java.io.*;
import java.net.*;

public class Serveur{

	public final static int PORT = 1027;
	public static Database base;
 
 	public static void main(String[] args){
		try{
			base = new Database();
			ServerSocket serverSocket = new ServerSocket(PORT);
			System.out.println("Le serveur est Ã  l'ecoute du port "+serverSocket.getLocalPort());
			while(true){
				Socket socket = serverSocket.accept();
				System.out.println("Un utilisateur arrive.");
				Thread t = new Thread(new ServiceServeur(socket));
				t.start();
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}