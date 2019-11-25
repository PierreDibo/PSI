
/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public enum MessageType {
    UTILISATEUR("L'utilisateur"),
    ANNONCE("L'annonce"),
    NEW("NEW pseudo mdp[_]***", 3),
    CONNECT("CONNECT pseudo mdp[_]***", 3),
    UPDATE("UPDATE ancien_pseudo ancien_mdp nouveau_pseudo nouveau_mdp[_]***", 5),
    DELETE("DELETE[_]***", 1),
    ADD_ANNONCE("ADD_ANNONCE nomAnnonce domaine prix description[_]***", 5),
    UPDATE_ANNONCE("UPDATE_ANNONCE id nomAnnonce domaine prix description[_]***", 6),
    DELETE_ANNONCE("DELETE_ANNONCE id[_]***", 2),
    CHECK_ALL_ANNONCES("CHECK_ALL_ANNONCES[_]***", 1),
    CHECK_ANNONCE("CHECK_ANNONCE id[_]***", 2),
    CHECK_ANNONCES_CLIENT("CHECK_ANNONCES_CLIENT idUtilisateur[_]***", 2),
    CHECK_ANNONCES_DOMAINE("CHECK_ANNONCES_DOMAINE domaine[_]***", 2),
    CHECK_DOMAINES("CHECK_DOMAINES[_]***", 1),
    OPEN_CALL("OPEN_CALL idUtilisateur[_]***", 2),
    CALL("CALL message[_]***"),
    CLOSE_CALL("CLOSE_CALL idUtilisateur[_]***", 2),
    QUIT("QUIT[_]***", 2),
    HELP("HELP[_]***", 2),
    ADDED("être ajouté"),
    CONNECTED("être connecté"),
    UPDATED("être modifié"),
    DELETED("être supprimé"),
    SUCCESS("a pu"),
    FAILURE("n'a pas pu"),
    END("***"),
    INVALID("INVALID message reçu");

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

    // <editor-fold defaultstate="collapsed" desc="MESSAGES UTILISATEUR">
    public static final String MSG_ADD_UTILISATEUR_SUCCESS
            = NEW.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_CONNECT_UTILISATEUR_SUCCESS
            = CONNECT.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + CONNECTED.getMessage();

    public static final String MSG_UPDATE_UTILISATEUR_SUCCESS
            = UPDATE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_UTILISATEUR_SUCCESS
            = DELETE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + SUCCESS.getMessage() + " "
            + DELETED.getMessage();

    public static final String MSG_ADD_UTILISATEUR_FAILURE
            = NEW.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + ADDED.getMessage();

    public static final String MSG_CONNECT_UTILISATEUR_FAILURE
            = CONNECT.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + CONNECTED.getMessage();

    public static final String MSG_UPDATE_UTILISATEUR_FAILURE
            = UPDATE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + UPDATED.getMessage();

    public static final String MSG_DELETE_UTILISATEUR_FAILURE
            = DELETE.name() + "\n"
            + UTILISATEUR.getMessage() + " "
            + FAILURE.getMessage() + " "
            + DELETED.getMessage();
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
            = "WELCOME" + "\n"
            + MSG_HELP;

    public static final String MSG_QUIT
            = "BYE";

    public static final String MSG_INVALID
            = "Message invalide. Forme des messages attendu :" + "\n"
            + MSG_HELP;

    public static final String MSG_TODO
            = "Fonction non implémenté";
    // </editor-fold>
}
