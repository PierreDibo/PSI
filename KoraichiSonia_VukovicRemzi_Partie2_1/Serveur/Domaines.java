public enum Domaines{
	
	moto, 
	voiture, 
	musique, 
	electromenager, 
	telephone, 
	autres;

	public static String descripteur() {
        return " " + Domaines.voiture.name() + " "
                + Domaines.moto.name() + " "
                + Domaines.musique.name() + " "
                + Domaines.electromenager.name() + " "
                + Domaines.telephone.name() + " "
                + Domaines.autres.name();
    }
	
	public static Domaines contains(String s) {
		Domaines dom=null;
		for (Domaines d : Domaines.values()) {
			if(s.toLowerCase().equals(d.name())) { //Si le domaine tapé existe bien
				dom=d;
			}
		}
		return dom;
	}
	
}