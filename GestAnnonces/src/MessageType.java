
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
    IT_IS("IT_IS adresse port[_]", 4),
    CALL_OPEN("CALL_OPEN adresseClient portClient (monPseudo monAdresse monPort)[_]", 5),
    CALL("CALL (mypseudo) pseudo message[_]", 4),
    CALL_CLOSE("CALL_CLOSE (mypseudo) pseudo[_]", 3),
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
    BAN("BAN pseudo"),
    UNBAN("BAN pseudo"),
    BYE("Connexion avec le serveur interrompu"),
    INVALID("INVALID message reçu"),
    CONNECT_SUCCESS("CONNECT_SUCCESS pseudo adresse port[_]", 5),
    ERROR(""),
    BANNED(""),
    CALL_CLOSE_OK(""),
    CALL_OPEN_SUCCESS("CALL_OPEN_SUCCESS pseudo[_]"),
    CALL_OPEN_ERROR(""),
    SENT(""),
    ALREADY_CONNECTED(""),
    CHECK_DESCRIPTION(""),
    DESCRIPTION_VIDE(""),
    DESCRIPTION(""),
    NOPE("")
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
            = CONNECT.name() + UNDERSCORE + SUCCESS.name();

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
            = NEW.name() + UNDERSCORE + ERROR.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_CONNECT_UTILISATEUR_FAILURE
            = CONNECT.name() + UNDERSCORE + ERROR.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + CONNECTED.getMessage();

    public static final String MSG_DISCONNECT_UTILISATEUR_FAILURE
            = DISCONNECT.name() + UNDERSCORE + ERROR.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + DISCONNECTED.getMessage();

    public static final String MSG_UPDATE_UTILISATEUR_FAILURE
            = UPDATE.name() + UNDERSCORE + ERROR.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_UTILISATEUR_FAILURE
            = DELETE.name() + UNDERSCORE + ERROR.name() + "\n"
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
            = ADD_ANNONCE.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + ANNONCE.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_UPDATE_ANNONCE_SUCCESS
            = UPDATE.name() + UNDERSCORE + ANNONCE.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + ANNONCE.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_ANNONCE_SUCCESS
            = DELETE.name() + UNDERSCORE + ANNONCE.name() + UNDERSCORE + SUCCESS.name() + "\n"
            + ANNONCE.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + DELETED.getMessage();

    public static final String MSG_ADD_ANNONCE_FAILURE
            = ADD_ANNONCE.name() + UNDERSCORE + ERROR.name() + "\n"
            + ANNONCE.getMessage() + " "
            + FAILURE.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_UPDATE_ANNONCE_FAILURE
            = UPDATE.name() + UNDERSCORE + ANNONCE.name() + UNDERSCORE + ERROR.name() + "\n"
            + ANNONCE.getMessage() + " "
            + FAILURE.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_ANNONCE_FAILURE
            = DELETE.name() + UNDERSCORE + ANNONCE.name() + UNDERSCORE + ERROR.name() + "\n"
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

    public static final String MSG_CALL_OPEN_SUCCESS
            = CALL_OPEN.name() + UNDERSCORE + SUCCESS.name();

    public static final String MSG_CALL_OPEN_FAILURE
            = CALL_OPEN.name() + UNDERSCORE + ERROR.name();

    public static final String MSG_CALL_CLOSE_SUCCESS
            = CALL_CLOSE.name() + UNDERSCORE + SUCCESS.name();

    public static final String MSG_CALL_CLOSE_FAILURE
            = CALL_CLOSE.name() + UNDERSCORE + ERROR.name();

    public static final String MSG_CONTACT_BANNED
            = BANNED.name();

    public static final String MSG_CONTACT_ALREADY_CONNECTED
            = "ALREADY_CONNECTED";

    public static final String MSG_QUIT
            = BYE.name() + "\n"
            + BYE.getMessage();

}
