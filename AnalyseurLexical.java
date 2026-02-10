import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AnalyseurLexical {

    public static BufferedReader SOURCE;
    public static int CARLU;
    public static int NOMBRE;
    public static String CHAINE;
    public static int NUM_LIGNE;
    public static String[] TABLE_MOTS_RESERVES = new String[Constantes.NB_MOTS_RESERVES];

    public static void ERREUR(int numErreur) {
        String message;

        message = switch (numErreur) {
            case 1 -> "fin de fichier atteinte";
            case 2 -> "nombre entier trop grand";
            case 3 -> "chaine de caracteres trop longue";
            case 4 -> "identificateur trop long";
            case 5 -> "symbole inconnu";
            default -> "erreur lexicale inconnue";
        };

        System.err.println("Erreur ligne " + NUM_LIGNE + " : " + message);

        System.exit(1);
    }

    public static void LIRE_CAR() {
        try {
            CARLU = SOURCE.read();
            if (CARLU == -1) {
                return;
            }
            if (CARLU == '\n') {
                NUM_LIGNE++;
            }
        } catch (IOException e) {
            ERREUR(1);
        }
    }

    /*
     * public static void LIRE_CAR() {
     * try {
     * CARLU = SOURCE.read();
     * if (CARLU == -1) {
     * ERREUR(1);
     * }
     * if (CARLU == '\n') {
     * NUM_LIGNE++;
     * }
     * } catch (IOException e) {
     * ERREUR(1);
     * }
     * }
     */     

    public static void SAUTER_SEPARETEURS() {
        boolean encore = true;
        while (encore) {
            while (CARLU == ' ' || CARLU == '\t' || CARLU == '\n' || CARLU == '\r') {
                LIRE_CAR();
            }

            if (CARLU == '{') {
                int niveauCommentaire = 1;
                LIRE_CAR();
                while (niveauCommentaire > 0) {
                    if (CARLU == '{') {
                        niveauCommentaire++;
                    } else if (CARLU == '}') {
                        niveauCommentaire--;
                    }
                    LIRE_CAR();
                }
            } else {
                encore = false;
            }
        }
    }

    public static TUnilex RECO_ENTIER() {

        int valeur = 0;

        while (Character.isDigit(CARLU)) {

            valeur = valeur * 10 + (CARLU - '0');

            if (valeur > Constantes.MAXINT) {
                ERREUR(2);
            }
            LIRE_CAR();
        }

        NOMBRE = valeur;

        return TUnilex.ENT;
    }

    public static TUnilex RECO_CHAINE() {

        StringBuilder chaine = new StringBuilder();

        // On est sur l'apostrophe ouvrante
        LIRE_CAR();

        while (CARLU != -1) {

            if (CARLU == '\'') {
                LIRE_CAR();

                // Apostrophe doublée -> apostrophe dans la chaîne
                if (CARLU == '\'') {
                    chaine.append('\'');
                    LIRE_CAR();
                } else {
                    // Fin de la chaîne
                    CHAINE = chaine.toString();
                    return TUnilex.CH;
                }
            } else {
                chaine.append((char) CARLU);

                if (chaine.length() > Constantes.LONG_MAX_CHAINE) {
                    ERREUR(3); // chaîne trop longue
                }

                LIRE_CAR();
            }
        }

        // Fin de fichier atteinte sans apostrophe fermante
        ERREUR(1);
        return null; // jamais atteint
    }

    private static boolean EST_UN_MOT_RESERVE(String ident) {
        for (int i = 0; i < Constantes.NB_MOTS_RESERVES; i++) {
            if (TABLE_MOTS_RESERVES[i].equals(ident)) {
                return true;
            }
        }
        return false;
    }

    public static TUnilex RECO_IDENT_OU_MOT_RESERVE() {
        StringBuilder ident = new StringBuilder();

        while (Character.isLetterOrDigit(CARLU) || CARLU == '_') {

            if (ident.length() < Constantes.LONG_MAX_IDENT) {
                ident.append(Character.toUpperCase((char) CARLU));
            }
            LIRE_CAR();
        }

        CHAINE = ident.toString();

        if (EST_UN_MOT_RESERVE(CHAINE)) {
            return TUnilex.MOTCLE;
        } else {
            return TUnilex.IDENT;
        }
    }

    public static TUnilex RECO_SYMB() {
        return switch (CARLU) {
            case ';' -> {
                LIRE_CAR();
                yield TUnilex.PTVIRG;
            }
            case ',' -> {
                LIRE_CAR();
                yield TUnilex.VIRG;
            }
            case '.' -> {
                LIRE_CAR();
                yield TUnilex.POINT;
            }
            case '(' -> {
                LIRE_CAR();
                yield TUnilex.PAROUV;
            }
            case ')' -> {
                LIRE_CAR();
                yield TUnilex.PARFER;
            }
            case '+' -> {
                LIRE_CAR();
                yield TUnilex.PLUS;
            }
            case '-' -> {
                LIRE_CAR();
                yield TUnilex.MOINS;
            }
            case '*' -> {
                LIRE_CAR();
                yield TUnilex.MULT;
            }
            case '/' -> {
                LIRE_CAR();
                yield TUnilex.DIVI;
            }
            case '=' -> {
                LIRE_CAR();
                yield TUnilex.EG;
            }
            // symboles composés
            case '<' -> {
                LIRE_CAR();
                switch (CARLU) {
                    case '=':
                        LIRE_CAR();
                        yield TUnilex.INFE;
                    case '>':
                        LIRE_CAR();
                        yield TUnilex.DIFF;
                    default:
                        yield TUnilex.INF;
                }
            }
            case '>' -> {
                LIRE_CAR();
                if (CARLU == '=') {
                    LIRE_CAR();
                    yield TUnilex.SUPE;
                } else {
                    yield TUnilex.SUP;
                }
            }
            case ':' -> {
                LIRE_CAR();
                if (CARLU == '=') {
                    LIRE_CAR();
                    yield TUnilex.AFF;
                } else {
                    yield TUnilex.DEUXPTS;
                }
            }
            default -> {
                ERREUR(5);
                yield null;
            }
        };
    }

    public static TUnilex ANALEX() {

        SAUTER_SEPARETEURS();

        if (CARLU == -1) {
            throw new RuntimeException("EOF");
        }

        if (Character.isDigit(CARLU)) {
            return RECO_ENTIER();
        }

        if (CARLU == '\'') {
            return RECO_CHAINE();
        }

        if (Character.isLetter(CARLU)) {
            return RECO_IDENT_OU_MOT_RESERVE();
        }

        return RECO_SYMB();
    }

    public static void INITIALISER(String nomFichier) {

        NUM_LIGNE = 1;

        try {
            SOURCE = new BufferedReader(new FileReader(nomFichier));
        } catch (IOException e) {
            ERREUR(0);
        }

        TABLE_MOTS_RESERVES = new String[Constantes.NB_MOTS_RESERVES];

        INSERE_TABLE_MOTS_RESERVES("PROGRAMME");
        INSERE_TABLE_MOTS_RESERVES("DEBUT");
        INSERE_TABLE_MOTS_RESERVES("FIN");
        INSERE_TABLE_MOTS_RESERVES("CONST");
        INSERE_TABLE_MOTS_RESERVES("VAR");
        INSERE_TABLE_MOTS_RESERVES("ECRIRE");
        INSERE_TABLE_MOTS_RESERVES("LIRE");

        LIRE_CAR();
    }

    private static int nbMotsInseres = 0;

    private static void INSERE_TABLE_MOTS_RESERVES(String mot) {

        int i = nbMotsInseres - 1;

        while (i >= 0 && TABLE_MOTS_RESERVES[i].compareTo(mot) > 0) {
            TABLE_MOTS_RESERVES[i + 1] = TABLE_MOTS_RESERVES[i];
            i--;
        }

        TABLE_MOTS_RESERVES[i + 1] = mot;
        nbMotsInseres++;
    }

    public static void TERMINER() {
        try {
            if (SOURCE != null) {
                SOURCE.close();
            }
        } catch (IOException e) {
        }
    }

}
