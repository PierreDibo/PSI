
/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public enum MessageType {
    UTILISATEUR("L'utilisateur"),
    ANNONCE("L'annonce"),
    NEW("NEW pseudo mdp[_]", 4),
    CONNECT("CONNECT pseudo mdp port[_]", 5),
    DISCONNECT("DISCONNECT[_]", 2),
    UPDATE("UPDATE nouveau_pseudo nouveau_mdp nouveau_port[_]", 5),
    DELETE("DELETE[_]", 2),
    ADD_ANNONCE("ADD_ANNONCE nomAnnonce domaine prix description with space[_]", 6),
    UPDATE_ANNONCE("UPDATE_ANNONCE id nomAnnonce domaine prix description with space[_]", 7),
    DELETE_ANNONCE("DELETE_ANNONCE id[_]", 3),
    CHECK_ALL_ANNONCES("CHECK_ALL_ANNONCES[_]", 2),
    CHECK_ANNONCE("CHECK_ANNONCE id[_]", 3),
    CHECK_ANNONCES_CLIENT("CHECK_ANNONCES_CLIENT idUtilisateur[_]", 3),
    CHECK_ANNONCES_DOMAINE("CHECK_ANNONCES_DOMAINE domaine[_]", 3),
    CHECK_DOMAINES("CHECK_DOMAINES[_]", 2),
    WHOIS("WHOIS idUtilisateur[_]", 3),
    CALL_OPEN("CALL_OPEN idUtilisateur[_]", 3),
    CALL("CALL message[_]", 2),
    CALL_CLOSE("CALL_CLOSE idUtilisateur[_]", 3),
    QUIT("QUIT[_]", 2),
    HELP("HELP[_]", 2),
    ADDED("être ajouté"),
    CONNECTED("être connecté"),
    DISCONNECTED("être déconnecté"),
    NOT_CONNECTED("n'est pas connecté"),
    CLIENT_NOT_EXIST("n'existe pas"),
    UPDATED("être modifié"),
    DELETED("être supprimé"),
    SUCCESS("a pu"),
    FAILURE("n'a pas pu"),
    END("***"),
    BYE("Connexion avec le serveur interrompu"),
    INVALID("INVALID message reçu"),
    CONNECT_SUCCESS("")
    
    
    ;

    private final String message;
    private final int params;

    MessageType(String msg) {
        this.message = msg;
        this.params = 0;
    }

    MessageType(String msg, int prs) {
        this.message = msg;
        this.params = prs;
    }

    public String getMessage() {
        return this.message;
    }

    public int getParameters() {
        return this.params;
    }

    public static final String UNDERSCORE = "_";
    // <editor-fold defaultstate="collapsed" desc="MESSAGES UTILISATEUR">
    public static final String MSG_ADD_UTILISATEUR_SUCCESS
            = NEW.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_CONNECT_UTILISATEUR_SUCCESS
            = CONNECT.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + CONNECTED.getMessage();

    public static final String MSG_DISCONNECT_UTILISATEUR_SUCCESS
            = DISCONNECT.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + DISCONNECTED.getMessage();

    public static final String MSG_UPDATE_UTILISATEUR_SUCCESS
            = UPDATE.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_UTILISATEUR_SUCCESS
            = DELETE.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + DELETED.getMessage();

    public static final String MSG_ADD_UTILISATEUR_FAILURE
            = NEW.name() + UNDERSCORE + FAILURE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_CONNECT_UTILISATEUR_FAILURE
            = CONNECT.name() + UNDERSCORE + FAILURE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + CONNECTED.getMessage();

    public static final String MSG_DISCONNECT_UTILISATEUR_FAILURE
            = DISCONNECT.name() + UNDERSCORE + FAILURE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + DISCONNECTED.getMessage();

    public static final String MSG_UPDATE_UTILISATEUR_FAILURE
            = UPDATE.name() + UNDERSCORE + FAILURE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_UTILISATEUR_FAILURE
            = DELETE.name() + UNDERSCORE + FAILURE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + DELETED.getMessage();

    public static final String MSG_WHOIS_SUCCESS
            = WHOIS.name() + UNDERSCORE + SUCCESS.name() + "\n";
    public static final String MSG_WHOIS_FAILURE
            = WHOIS.name() + UNDERSCORE + FAILURE.name() + "\n";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MESSAGES ANNONCES">
    public static final String MSG_ADD_ANNONCE_SUCCESS
            = ADD_ANNONCE.name() + "\n"
            + ANNONCE.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_UPDATE_ANNONCE_SUCCESS
            = UPDATE.name() + "\n"
            + ANNONCE.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_ANNONCE_SUCCESS
            = DELETE.name() + "\n"
            + ANNONCE.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + DELETED.getMessage();

    public static final String MSG_ADD_ANNONCE_FAILURE
            = ADD_ANNONCE.name() + "\n"
            + ANNONCE.getMessage() + " "
            + FAILURE.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_UPDATE_ANNONCE_FAILURE
            = UPDATE.name() + "\n"
            + ANNONCE.getMessage() + " "
            + FAILURE.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_ANNONCE_FAILURE
            = DELETE.name() + "\n"
            + ANNONCE.getMessage() + " "
            + FAILURE.getMessage() + " "
            + DELETED.getMessage();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MESSAGES AUTRE">
    public static final String MSG_HELP
            = NEW.getMessage() + "\n"
            + CONNECT.getMessage() + "\n"
            + UPDATE.getMessage() + "\n"
            + DELETE.getMessage() + "\n"
            + ADD_ANNONCE.getMessage() + "\n"
            + UPDATE_ANNONCE.getMessage() + "\n"
            + DELETE_ANNONCE.getMessage() + "\n"
            + CHECK_ALL_ANNONCES.getMessage() + "\n"
            + CHECK_ANNONCE.getMessage() + "\n"
            + CHECK_ANNONCES_CLIENT.getMessage() + "\n"
            + CHECK_ANNONCES_DOMAINE.getMessage() + "\n"
            + CHECK_DOMAINES.getMessage() + "\n"
            + QUIT.getMessage() + "\n"
            + HELP.getMessage();

    public static final String MSG_WELCOME
            = "WELCOME " + "\n"
            + MSG_HELP;

    public static final String MSG_INVALID
            = INVALID.name() + "\n"
            + "Message invalide." + "\n";

    public static final String MSG_TODO
            = "Fonction non implémenté." + "\n";

    public static final String MSG_IS_NOT_CONNECTED
            = UTILISATEUR.getMessage() + " "
            + NOT_CONNECTED.getMessage() + "\n";

    public static final String MSG_IS_NOT_EXISTS
            = CLIENT_NOT_EXIST.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + NOT_CONNECTED.getMessage() + "\n";
    // </editor-fold>
}
