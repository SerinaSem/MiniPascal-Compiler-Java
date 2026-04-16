
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class AnalyseurSyntaxique {

    static class ContexteFonction {
        String nom;
        Map<String, Integer> deplacements = new HashMap<>();
        Identificateur ident;
    }

    static TableIdentificateurs tableIdent = new TableIdentificateurs();

    static int NB_CONST_CHAINE;
    static String[] VAL_DE_CONST_CHAINE = new String[100];
    static int DERNIERE_ADRESSE_VAR_GLOB = -1;
    static String MESSAGE_ERREUR;
    static int[] PCODE = new int[1000];
    static int CO = 0;
    static int[] PILOP = new int[100];
    static int SOM_PILOP = -1;
    static ContexteFonction fonctionCourante = null;
    static final int BASE_ADRESSE_RELATIVE = 100000;

    public static void initialiser() {
        tableIdent = new TableIdentificateurs();
        PCODE = new int[1000];
        VAL_DE_CONST_CHAINE = new String[100];
        CO = 0;
        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
        NB_CONST_CHAINE = 0;
        DERNIERE_ADRESSE_VAR_GLOB = -1;
        SOM_PILOP = -1;
        fonctionCourante = null;
    }

    public static void ERREUR() {
        System.out.println("Ligne " + AnalyseurLexical.NUM_LIGNE + " : " + MESSAGE_ERREUR);
        System.exit(3);
    }

    public static boolean PROG() {
        System.out.println("PROG");

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("PROGRAMME")) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                if (AnalyseurLexical.UNILEX == TUnilex.PTVIRG) {

                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                    if (DECL_CONST()) {

                        if (DECL_VAR()) {

                            GEN(TCode.ALLE, 0);
                            int adrSautMain = CO - 1;

                            while (DEF_FCT()) {
                            }

                            PCODE[adrSautMain] = CO;

                            if (BLOC()) {

                                if (AnalyseurLexical.UNILEX == TUnilex.POINT) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean DEF_FCT() {
        System.out.println("DEF_FCT");

        if (!(AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("FONCTION"))) {
            return false;
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (AnalyseurLexical.UNILEX != TUnilex.IDENT) {
            MESSAGE_ERREUR = "identificateur attendu apres FONCTION";
            ERREUR();
        }

        String nomFonction = AnalyseurLexical.CHAINE;

        if (tableIdent.chercher(nomFonction) != -1) {
            MESSAGE_ERREUR = "Erreur semantique : identificateur deja declare";
            ERREUR();
        }

        Identificateur fct = new Identificateur(nomFonction);
        fct.typ = GenreIdent.FONCTION;
        fct.typc = 0;
        tableIdent.inserer(fct);

        ContexteFonction ancienContexte = fonctionCourante;
        fonctionCourante = new ContexteFonction();
        fonctionCourante.nom = nomFonction;
        fonctionCourante.ident = fct;
        fonctionCourante.deplacements.put(nomFonction, -2);

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (AnalyseurLexical.UNILEX != TUnilex.PAROUV) {
            MESSAGE_ERREUR = "'(' attendu dans la definition de fonction";
            ERREUR();
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        int nbParam = 0;
        if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {
            while (true) {
                String nomParam = AnalyseurLexical.CHAINE;

                if (fonctionCourante.deplacements.containsKey(nomParam)) {
                    MESSAGE_ERREUR = "Erreur semantique : parametre duplique";
                    ERREUR();
                }

                fonctionCourante.deplacements.put(nomParam, nbParam);
                nbParam++;

                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                if (AnalyseurLexical.UNILEX == TUnilex.VIRG) {
                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                    if (AnalyseurLexical.UNILEX != TUnilex.IDENT) {
                        MESSAGE_ERREUR = "identificateur attendu dans la liste des parametres";
                        ERREUR();
                    }
                    continue;
                }
                break;
            }
        }

        if (AnalyseurLexical.UNILEX != TUnilex.PARFER) {
            MESSAGE_ERREUR = "')' attendu dans la definition de fonction";
            ERREUR();
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (AnalyseurLexical.UNILEX != TUnilex.DEUXPTS) {
            MESSAGE_ERREUR = "':' attendu avant le type de retour";
            ERREUR();
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (!TYP()) {
            MESSAGE_ERREUR = "type ENTIER attendu";
            ERREUR();
        }

        if (AnalyseurLexical.UNILEX != TUnilex.PTVIRG) {
            MESSAGE_ERREUR = "';' attendu apres l'entete de fonction";
            ERREUR();
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        fct.nbParam = nbParam;
        fct.adresse = CO;

        if (!BLOC()) {
            MESSAGE_ERREUR = "bloc de fonction incorrect";
            ERREUR();
        }

        GEN(TCode.RETOUR);

        fonctionCourante = ancienContexte;

        if (AnalyseurLexical.UNILEX != TUnilex.PTVIRG) {
            MESSAGE_ERREUR = "';' attendu apres la fonction";
            ERREUR();
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
        return true;
    }

    public static boolean TYP() {
        System.out.println("TYP");

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("ENTIER")) {
            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
            return true;
        }

        return false;
    }

    public static boolean DECL_CONST() {
    System.out.println("DECL_CONST");

    if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
            && AnalyseurLexical.CHAINE.equals("CONST")) {

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (AnalyseurLexical.UNILEX != TUnilex.IDENT) {
            MESSAGE_ERREUR = "identificateur attendu apres CONST";
            ERREUR();
        }

        while (true) {

            String nomConst = AnalyseurLexical.CHAINE;

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX != TUnilex.EG) {
                MESSAGE_ERREUR = "'=' attendu";
                ERREUR();
            }

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX != TUnilex.ENT
                    && AnalyseurLexical.UNILEX != TUnilex.CH) {
                MESSAGE_ERREUR = "ENT ou CH attendu";
                ERREUR();
            }

            if (!DEFINIR_CONSTANTE(nomConst, AnalyseurLexical.UNILEX)) {
                return false;
            }

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX == TUnilex.VIRG) {
                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                continue;
            }

            if (AnalyseurLexical.UNILEX == TUnilex.PTVIRG) {
                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                break;
            }

            MESSAGE_ERREUR = "',' ou ';' attendu";
            ERREUR();
        }
    }

    return true;
}

    public static boolean DECL_VAR() {
        System.out.println("DECL_VAR");

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("VAR")) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

                do {

                    String nomVar = AnalyseurLexical.CHAINE;

                    if (!DEFINIR_VARIABLE(nomVar)) {
                        MESSAGE_ERREUR = "Erreur syntaxique dans declaration VAR";
                        ERREUR();
                    }

                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                    if (AnalyseurLexical.UNILEX == TUnilex.VIRG) {
                        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                    } else {
                        break;
                    }

                } while (AnalyseurLexical.UNILEX == TUnilex.IDENT);

                if (AnalyseurLexical.UNILEX == TUnilex.PTVIRG) {
                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                } else {
                    MESSAGE_ERREUR = "Erreur syntaxique dans declaration VAR";
                    ERREUR();
                }
            }

            MESSAGE_ERREUR = "Erreur syntaxique dans declaration VAR";
            ERREUR();
        }

        return true;
    }

    public static boolean BLOC() {
        System.out.println("BLOC");

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("DEBUT")) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (INSTRUCTION()) {

                while (AnalyseurLexical.UNILEX == TUnilex.PTVIRG) {

                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                    if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                            && AnalyseurLexical.CHAINE.equals("FIN")) {
                        break;
                    }

                    if (!INSTRUCTION()) {
                        MESSAGE_ERREUR = "pas d'instruction apres le point virgule";
                        ERREUR();
                        return false;
                    }
                }

                if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                        && AnalyseurLexical.CHAINE.equals("FIN")) {

                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }
            }
        }
        MESSAGE_ERREUR = "BLOC: erreur de syntaxe";
        ERREUR();
        return false;
    }

    public static boolean INSTRUCTION() {

        System.out.println("INSTRUCTION");

        if (INST_COND()) {
            return true;
        }

        if (INST_NON_COND()) {
            return true;
        }

        MESSAGE_ERREUR = "INSTRUCTION: erreur de syntaxe";
        ERREUR();
        return false;
    }

    public static boolean INST_NON_COND() {
        System.out.println("INST_NON_COND");

        if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {
            return AFFECTATION();
        }

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("LIRE")) {
            return LECTURE();
        }

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("ECRIRE")) {
            return ECRITURE();
        }

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("DEBUT")) {
            return BLOC();
        }

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("TANTQUE")) {
            return INST_REPE();
        }

        return false;
    }

    public static boolean INST_COND() {
        System.out.println("INST_COND");

        if (!(AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("SI"))) {
            return false;
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (!EXP()) {
            MESSAGE_ERREUR = "expression attendue apres SI";
            ERREUR();
        }

        if (!(AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("ALORS"))) {
            MESSAGE_ERREUR = "ALORS attendu";
            ERREUR();
        }

        GEN(TCode.ALSN, 0);
        empiler(CO - 1);

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (!INSTRUCTION()) {
            MESSAGE_ERREUR = "instruction attendue apres ALORS";
            ERREUR();
        }

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("SINON")) {

            PCODE[depiler()] = CO + 2;
            GEN(TCode.ALLE, 0);
            empiler(CO - 1);

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (!INSTRUCTION()) {
                MESSAGE_ERREUR = "instruction attendue apres SINON";
                ERREUR();
            }

            PCODE[depiler()] = CO;
        } else {
            PCODE[depiler()] = CO;
        }

        return true;
    }

    public static boolean INST_REPE() {
        System.out.println("INST_REPE");

        if (!(AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("TANTQUE"))) {
            return false;
        }

        empiler(CO);

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (!EXP()) {
            MESSAGE_ERREUR = "expression attendue apres TANTQUE";
            ERREUR();
        }

        if (!(AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("FAIRE"))) {
            MESSAGE_ERREUR = "FAIRE attendu";
            ERREUR();
        }

        GEN(TCode.ALSN, 0);
        empiler(CO - 1);

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        if (!INSTRUCTION()) {
            MESSAGE_ERREUR = "instruction attendue apres FAIRE";
            ERREUR();
        }

        int adrAlsn = depiler();
        int debutBoucle = depiler();
        PCODE[adrAlsn] = CO + 2;
        GEN(TCode.ALLE, debutBoucle);

        return true;
    }

    public static boolean AFFECTATION() {
        System.out.println("AFFECTATION");

        if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

            String nom = AnalyseurLexical.CHAINE;
            Integer deplacement = deplacementLocal(nom);

            if (deplacement != null) {
                GEN(TCode.EMPI, encoderAdresseRelative(deplacement));
            } else {
                int index = tableIdent.chercher(nom);
                if (index == -1) {
                    MESSAGE_ERREUR = "Erreur semantique : variable non declaree";
                    ERREUR();
                }

                Identificateur id = tableIdent.get(index);

                if (id.getGenre() != GenreIdent.VARIABLE) {
                    MESSAGE_ERREUR = "Erreur semantique : affectation sur constante ou fonction";
                    ERREUR();
                }

                GEN(TCode.EMPI, id.adresse);
            }

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX == TUnilex.AFF) {

                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                if (EXP()) {

                    GEN(TCode.AFFE);

                    return true;
                }
            } else {
                MESSAGE_ERREUR = ":= attendu";
                ERREUR();
            }
        }

        return false;
    }

    public static boolean LECTURE() {
        System.out.println("LECTURE");

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("LIRE")) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX == TUnilex.PAROUV) {

                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

                    String nom = AnalyseurLexical.CHAINE;
                    Integer deplacement = deplacementLocal(nom);

                    if (deplacement != null) {
                        GEN(TCode.EMPI, encoderAdresseRelative(deplacement));
                    } else {
                        int index = tableIdent.chercher(nom);
                        if (index == -1) {
                            MESSAGE_ERREUR = "Erreur semantique : variable non declaree";
                            ERREUR();
                        }

                        Identificateur id = tableIdent.get(index);
                        if (id.getGenre() != GenreIdent.VARIABLE) {
                            MESSAGE_ERREUR = "Erreur semantique : lecture sur constante ou fonction";
                            ERREUR();
                        }

                        GEN(TCode.EMPI, id.adresse);
                    }
                    GEN(TCode.LIRE);

                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                    while (AnalyseurLexical.UNILEX == TUnilex.VIRG) {

                        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                        if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

                            nom = AnalyseurLexical.CHAINE;
                            deplacement = deplacementLocal(nom);

                            if (deplacement != null) {
                                GEN(TCode.EMPI, encoderAdresseRelative(deplacement));
                            } else {
                                int index = tableIdent.chercher(nom);
                                if (index == -1) {
                                    MESSAGE_ERREUR = "Erreur semantique : variable non declaree";
                                    ERREUR();
                                }

                                Identificateur id = tableIdent.get(index);
                                if (id.getGenre() != GenreIdent.VARIABLE) {
                                    MESSAGE_ERREUR = "Erreur semantique : lecture sur constante ou fonction";
                                    ERREUR();
                                }

                                GEN(TCode.EMPI, id.adresse);
                            }
                            GEN(TCode.LIRE);

                            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                        } else {
                            return false;
                        }
                    }

                    if (AnalyseurLexical.UNILEX == TUnilex.PARFER) {
                        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean ECRITURE() {
        System.out.println("ECRITURE");

        if (AnalyseurLexical.UNILEX == TUnilex.MOTCLE
                && AnalyseurLexical.CHAINE.equals("ECRIRE")) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX == TUnilex.PAROUV) {

                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                if (AnalyseurLexical.UNILEX == TUnilex.PARFER) {
                    GEN(TCode.ECRL);
                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }

                if (ECR_EXP()) {

                    while (AnalyseurLexical.UNILEX == TUnilex.VIRG) {

                        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                        if (!ECR_EXP()) {
                            return false;
                        }
                    }
                }

                if (AnalyseurLexical.UNILEX == TUnilex.PARFER) {
                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean ECR_EXP() {
        System.out.println("ECR_EXP");

        if (AnalyseurLexical.UNILEX == TUnilex.CH) {
            String ch = AnalyseurLexical.CHAINE;

            GEN(TCode.ECRC);
            for (int i = 0; i < ch.length(); i++) {
                GEN((int) ch.charAt(i));
            }
            GEN(TCode.FINC);

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
            return true;
        }

        if (EXP()) {
            GEN(TCode.ECRE);
            return true;
        }

        return false;
    }

    public static boolean EXP() {
        System.out.println("EXP");

        if (TERME()) {
            return SUITE_TERME();
        }

        return false;
    }

    public static boolean SUITE_TERME() {
        System.out.println("SUITE_TERME");

        if (AnalyseurLexical.UNILEX == TUnilex.PLUS
                || AnalyseurLexical.UNILEX == TUnilex.MOINS
                || AnalyseurLexical.UNILEX == TUnilex.MULT
                || AnalyseurLexical.UNILEX == TUnilex.DIVI) {

            TUnilex op = AnalyseurLexical.UNILEX;

            if (OP_BIN()) {

                if (TERME()) {

                    switch (op) {
                        case PLUS:
                            GEN(TCode.ADDI);
                            break;
                        case MOINS:
                            GEN(TCode.SOUS);
                            break;
                        case MULT:
                            GEN(TCode.MULT);
                            break;
                        case DIVI:
                            GEN(TCode.DIVI);
                            break;
                    }

                    return SUITE_TERME();
                }
            }

            return false;
        }

        return true;
    }

    public static boolean TERME() {
        System.out.println("TERME");

        if (AnalyseurLexical.UNILEX == TUnilex.ENT) {

            GEN(TCode.EMPI, AnalyseurLexical.NOMBRE);
            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
            return true;
        }

        if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

            String nom = AnalyseurLexical.CHAINE;
            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (AnalyseurLexical.UNILEX == TUnilex.PAROUV) {
                return APP_FCT(nom);
            }

            chargerIdentifiant(nom);
            return true;
        }

        if (AnalyseurLexical.UNILEX == TUnilex.PAROUV) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (EXP()) {

                if (AnalyseurLexical.UNILEX == TUnilex.PARFER) {
                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }
            }

            return false;
        }

        if (AnalyseurLexical.UNILEX == TUnilex.MOINS) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

            if (TERME()) {
                GEN(TCode.MOIN);
                return true;
            }

            return false;
        }

        return false;
    }

    public static boolean APP_FCT(String nomFonction) {
        System.out.println("APP_FCT");

        int index = tableIdent.chercher(nomFonction);
        if (index == -1) {
            MESSAGE_ERREUR = "fonction non declaree";
            ERREUR();
        }

        Identificateur id = tableIdent.get(index);
        if (id.getGenre() != GenreIdent.FONCTION) {
            MESSAGE_ERREUR = "appel sur un identificateur qui n'est pas une fonction";
            ERREUR();
        }

        GEN(TCode.EMPI, 0);
        GEN(TCode.SAVEBP);

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

        int nbArgs = 0;
        if (AnalyseurLexical.UNILEX != TUnilex.PARFER) {
            if (!EXP()) {
                MESSAGE_ERREUR = "expression attendue dans l'appel de fonction";
                ERREUR();
            }
            nbArgs++;

            while (AnalyseurLexical.UNILEX == TUnilex.VIRG) {
                AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
                if (!EXP()) {
                    MESSAGE_ERREUR = "expression attendue apres ','";
                    ERREUR();
                }
                nbArgs++;
            }
        }

        if (AnalyseurLexical.UNILEX != TUnilex.PARFER) {
            MESSAGE_ERREUR = "')' attendu dans l'appel de fonction";
            ERREUR();
        }

        if (nbArgs != id.nbParam) {
            MESSAGE_ERREUR = "nombre de parametres incorrect dans l'appel de fonction";
            ERREUR();
        }

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
        GEN(TCode.APPEL, id.adresse);
        return true;
    }

    public static boolean OP_BIN() {
        System.out.println("OP_BIN");

        if (AnalyseurLexical.UNILEX == TUnilex.PLUS
                || AnalyseurLexical.UNILEX == TUnilex.MOINS
                || AnalyseurLexical.UNILEX == TUnilex.MULT
                || AnalyseurLexical.UNILEX == TUnilex.DIVI) {

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
            return true;
        }

        return false;
    }

    public static void ANASYNT() {

        initialiser();

        if (PROG()) {

            GEN(TCode.STOP);
            System.out.println("Le programme source est syntaxiquement correct");

        } else {
            ERREUR();
        }
        tableIdent.afficher();
        afficherPCODE();
        ecrirePCODEDansFichier("PCODE_genere.txt");

        System.out.println("\n  EXECUTION : \n");
        INTERPRETER();
    }

    public static boolean DEFINIR_CONSTANTE(String nom, TUnilex ul) {

        if (tableIdent.chercher(nom) != -1) {
            MESSAGE_ERREUR = "Erreur semantique : identificateur deja declare";
            ERREUR();
            return false;
        }

        Identificateur e = new Identificateur(nom);
        e.typ = GenreIdent.CONSTANTE;

        if (ul == TUnilex.ENT) {
            e.typc = 0;
            e.val = AnalyseurLexical.NOMBRE;
        } else {
            e.typc = 1;
            VAL_DE_CONST_CHAINE[NB_CONST_CHAINE] = AnalyseurLexical.CHAINE;
            e.val = NB_CONST_CHAINE;
            NB_CONST_CHAINE++;
        }

        tableIdent.inserer(e);

        return true;
    }

    public static boolean DEFINIR_VARIABLE(String nom) {

        if (tableIdent.chercher(nom) != -1) {
            MESSAGE_ERREUR = "Erreur semantique : deja declare";
            ERREUR();
            return false;
        }

        Identificateur e = new Identificateur(nom);

        e.typ = GenreIdent.VARIABLE;
        e.typc = 0;

        DERNIERE_ADRESSE_VAR_GLOB++;
        e.adresse = DERNIERE_ADRESSE_VAR_GLOB;

        tableIdent.inserer(e);

        return true;
    }

    public static Integer deplacementLocal(String nom) {
        if (fonctionCourante == null) {
            return null;
        }
        return fonctionCourante.deplacements.get(nom);
    }

    public static int encoderAdresseRelative(int deplacement) {
        return -BASE_ADRESSE_RELATIVE - deplacement;
    }

    public static boolean estAdresseRelative(int adresse) {
        return adresse <= -BASE_ADRESSE_RELATIVE + 2;
    }

    public static int decoderAdresseRelative(int adresse) {
        return -BASE_ADRESSE_RELATIVE - adresse;
    }

    public static void chargerIdentifiant(String nom) {
        Integer deplacement = deplacementLocal(nom);

        if (deplacement != null) {
            GEN(TCode.LIBP, deplacement);
            return;
        }

        int index = tableIdent.chercher(nom);
        if (index == -1) {
            MESSAGE_ERREUR = "identificateur non declare";
            ERREUR();
        }

        Identificateur id = tableIdent.get(index);

        if (id.typc != 0) {
            MESSAGE_ERREUR = "type incorrect dans expression arithmetique";
            ERREUR();
        }

        if (id.getGenre() == GenreIdent.CONSTANTE) {
            GEN(TCode.EMPI, id.val);
            return;
        }

        if (id.getGenre() == GenreIdent.VARIABLE) {
            GEN(TCode.EMPI, id.adresse);
            GEN(TCode.CONT);
            return;
        }

        MESSAGE_ERREUR = "une fonction doit etre appelee avec des parentheses";
        ERREUR();
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    public static void GEN(int instruction) {
        PCODE[CO++] = instruction;
    }

    public static void GEN(int instruction, int valeur) {
        PCODE[CO++] = instruction;
        PCODE[CO++] = valeur;
    }

    public static void empiler(int valeur) {
        PILOP[++SOM_PILOP] = valeur;
    }

    public static int depiler() {
        return PILOP[SOM_PILOP--];
    }

    public static String nomInstruction(int code) {
        switch (code) {
            case TCode.ADDI:
                return "ADDI";
            case TCode.SOUS:
                return "SOUS";
            case TCode.MULT:
                return "MULT";
            case TCode.DIVI:
                return "DIVI";
            case TCode.MOIN:
                return "MOIN";
            case TCode.AFFE:
                return "AFFE";
            case TCode.LIRE:
                return "LIRE";
            case TCode.ECRE:
                return "ECRE";
            case TCode.ECRL:
                return "ECRL";
            case TCode.ECRC:
                return "ECRC";
            case TCode.FINC:
                return "FINC";
            case TCode.EMPI:
                return "EMPI";
            case TCode.CONT:
                return "CONT";
            case TCode.STOP:
                return "STOP";
            case TCode.ALLE:
                return "ALLE";
            case TCode.ALSN:
                return "ALSN";
            case TCode.APPEL:
                return "APPEL";
            case TCode.RETOUR:
                return "RETOUR";
            case TCode.SAVEBP:
                return "SAVEBP";
            case TCode.RSTRBP:
                return "RSTRBP";
            case TCode.DUPL:
                return "DUPL";
            case TCode.LIBP:
                return "LIBP";
            default:
                return "UNKNOWN";
        }
    }

    public static String chainePCodeCompacte(int debut) {
        StringBuilder sb = new StringBuilder();
        int i = debut;

        while (i < CO && PCODE[i] != TCode.FINC) {
            char c = (char) PCODE[i];
            if (c == '\'') {
                sb.append("''");
            } else {
                sb.append(c);
            }
            i++;
        }

        return sb.toString();
    }


    public static String lignePCode(int debut) {
        int instruction = PCODE[debut];
        StringBuilder sb = new StringBuilder(nomInstruction(instruction));

        if (instruction == TCode.EMPI || instruction == TCode.ALLE || instruction == TCode.ALSN
                || instruction == TCode.APPEL || instruction == TCode.LIBP) {
            sb.append(" ").append(PCODE[debut + 1]);
            return sb.toString();
        }

        if (instruction == TCode.ECRC) {
            sb.append(" '").append(chainePCodeCompacte(debut + 1)).append("' FINC");
        }

        return sb.toString();
    }

    public static int instructionSuivante(int debut) {
        int instruction = PCODE[debut];

        if (instruction == TCode.EMPI || instruction == TCode.ALLE || instruction == TCode.ALSN
                || instruction == TCode.APPEL || instruction == TCode.LIBP) {
            return debut + 2;
        }

        if (instruction == TCode.ECRC) {
            int i = debut + 1;
            while (i < CO && PCODE[i] != TCode.FINC) {
                i++;
            }
            return i + 1;
        }

        return debut + 1;
    }

    public static String chainePCodeLisible(int debut) {
        StringBuilder sb = new StringBuilder();
        int i = debut;

        while (i < CO && PCODE[i] != TCode.FINC) {
            char c = (char) PCODE[i];
            if (c == '\\' || c == '"') {
                sb.append('\\');
            }
            sb.append(c);
            i++;
        }

        return sb.toString();
    }

    public static void afficherPCODE() {

        System.out.println("\nPCODE (format PDF) :\n");
        System.out.println((DERNIERE_ADRESSE_VAR_GLOB + 1) + " mot(s) reserve(s) pour les variables globales");

        int i = 0;

        while (i < CO) {
            System.out.println(lignePCode(i));
            i = instructionSuivante(i);
        }
    }

    public static void ecrirePCODEDansFichier(String nomFichier) {
        try (PrintWriter out = new PrintWriter(new FileWriter(nomFichier))) {
            out.println((DERNIERE_ADRESSE_VAR_GLOB + 1) + " mot(s) reserve(s) pour les variables globales");
            int i = 0;
            while (i < CO) {
                out.println(lignePCode(i));
                i = instructionSuivante(i);
            }
        } catch (IOException e) {
            System.out.println("Impossible d'ecrire le fichier " + nomFichier);
        }
    }

    public static void INTERPRETER() {

        CO = 0;
        int SOM_PILEX = -1;
        int[] PILEX = new int[1000];
        int[] MEMVAR = new int[100];
        int BP = 0;
        int[] pileRetourSlot = new int[100];
        int[] pileSommetAppelant = new int[100];
        int[] pileBPAppelant = new int[100];
        int SOM_APPEL = -1;
        int[] pileSauveBP = new int[100];
        int SOM_SAUVEBP = -1;
        while (PCODE[CO] != TCode.STOP) {

            switch (PCODE[CO]) {

                case TCode.ADDI:
                    PILEX[SOM_PILEX - 1] = PILEX[SOM_PILEX - 1] + PILEX[SOM_PILEX];
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.SOUS:
                    PILEX[SOM_PILEX - 1] = PILEX[SOM_PILEX - 1] - PILEX[SOM_PILEX];
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.MULT:
                    PILEX[SOM_PILEX - 1] = PILEX[SOM_PILEX - 1] * PILEX[SOM_PILEX];
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.DIVI:
                    PILEX[SOM_PILEX - 1] = PILEX[SOM_PILEX - 1] / PILEX[SOM_PILEX];
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.MOIN:
                    PILEX[SOM_PILEX] = -PILEX[SOM_PILEX];
                    CO = CO + 1;
                    break;

                case TCode.AFFE:
                    if (estAdresseRelative(PILEX[SOM_PILEX - 1])) {
                        int deplacement = decoderAdresseRelative(PILEX[SOM_PILEX - 1]);
                        PILEX[BP + deplacement] = PILEX[SOM_PILEX];
                    } else {
                        MEMVAR[PILEX[SOM_PILEX - 1]] = PILEX[SOM_PILEX];
                    }
                    SOM_PILEX = SOM_PILEX - 2;
                    CO = CO + 1;
                    break;

                case TCode.LIRE:
                    java.util.Scanner sc = new java.util.Scanner(System.in);
                    if (estAdresseRelative(PILEX[SOM_PILEX])) {
                        int deplacement = decoderAdresseRelative(PILEX[SOM_PILEX]);
                        PILEX[BP + deplacement] = sc.nextInt();
                    } else {
                        MEMVAR[PILEX[SOM_PILEX]] = sc.nextInt();
                    }
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.ECRE:
                    System.out.print(PILEX[SOM_PILEX]);
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.ECRL:
                    System.out.println();
                    CO = CO + 1;
                    break;

                case TCode.ECRC:
                    CO = CO + 1;
                    while (PCODE[CO] != TCode.FINC) {
                        System.out.print((char) PCODE[CO]);
                        CO = CO + 1;
                    }
                    CO = CO + 1;
                    break;

                case TCode.EMPI:
                    SOM_PILEX = SOM_PILEX + 1;
                    PILEX[SOM_PILEX] = PCODE[CO + 1];
                    CO = CO + 2;
                    break;

                case TCode.CONT:
                    if (estAdresseRelative(PILEX[SOM_PILEX])) {
                        int deplacement = decoderAdresseRelative(PILEX[SOM_PILEX]);
                        PILEX[SOM_PILEX] = PILEX[BP + deplacement];
                    } else {
                        PILEX[SOM_PILEX] = MEMVAR[PILEX[SOM_PILEX]];
                    }
                    CO = CO + 1;
                    break;

                case TCode.ALLE:
                    CO = PCODE[CO + 1];
                    break;

                case TCode.ALSN:
                    if (PILEX[SOM_PILEX] == 0) {
                        CO = PCODE[CO + 1];
                    } else {
                        CO = CO + 2;
                    }
                    SOM_PILEX = SOM_PILEX - 1;
                    break;

                case TCode.SAVEBP:
                    SOM_PILEX = SOM_PILEX + 1;
                    PILEX[SOM_PILEX] = BP;
                    SOM_SAUVEBP = SOM_SAUVEBP + 1;
                    pileSauveBP[SOM_SAUVEBP] = SOM_PILEX;
                    CO = CO + 1;
                    break;

                case TCode.RSTRBP:
                    BP = PILEX[SOM_PILEX];
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.DUPL:
                    SOM_PILEX = SOM_PILEX + 1;
                    PILEX[SOM_PILEX] = PILEX[SOM_PILEX - 1];
                    CO = CO + 1;
                    break;

                case TCode.LIBP:
                    SOM_PILEX = SOM_PILEX + 1;
                    PILEX[SOM_PILEX] = PILEX[BP + PCODE[CO + 1]];
                    CO = CO + 2;
                    break;

                case TCode.APPEL:
                    int indexSauveBP = pileSauveBP[SOM_SAUVEBP];
                    SOM_SAUVEBP = SOM_SAUVEBP - 1;
                    SOM_APPEL = SOM_APPEL + 1;
                    pileRetourSlot[SOM_APPEL] = indexSauveBP - 1;
                    pileSommetAppelant[SOM_APPEL] = indexSauveBP - 2;
                    pileBPAppelant[SOM_APPEL] = PILEX[indexSauveBP];
                    SOM_PILEX = SOM_PILEX + 1;
                    PILEX[SOM_PILEX] = CO + 2;
                    BP = indexSauveBP + 1;
                    CO = PCODE[CO + 1];
                    break;

                case TCode.RETOUR:
                    int adresseRetour = PILEX[SOM_PILEX];
                    SOM_PILEX = pileSommetAppelant[SOM_APPEL];
                    PILEX[++SOM_PILEX] = PILEX[pileRetourSlot[SOM_APPEL]];
                    BP = pileBPAppelant[SOM_APPEL];
                    SOM_APPEL = SOM_APPEL - 1;
                    CO = adresseRetour;
                    break;
            }
        }
    }
}
