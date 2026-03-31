
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AnalyseurSyntaxique {

    static TableIdentificateurs tableIdent = new TableIdentificateurs();

    static int NB_CONST_CHAINE;
    static String[] VAL_DE_CONST_CHAINE = new String[100];
    static int DERNIERE_ADRESSE_VAR_GLOB;
    static String MESSAGE_ERREUR;
    static int[] PCODE = new int[1000];
    static int CO = 0;

    public static void initialiser() {
        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
        NB_CONST_CHAINE = 0;
        DERNIERE_ADRESSE_VAR_GLOB = -1;
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

        MESSAGE_ERREUR = "INSTRUCTION: erreur de syntaxe";
        ERREUR();
        return false;
    }

    public static boolean AFFECTATION() {
        System.out.println("AFFECTATION");

        if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

            String nom = AnalyseurLexical.CHAINE;

            int index = tableIdent.chercher(nom);
            if (index == -1) {
                MESSAGE_ERREUR = "Erreur semantique : variable non declaree";
                ERREUR();
            }

            Identificateur id = tableIdent.get(index);

            if (id.getGenre() != GenreIdent.VARIABLE) {
                MESSAGE_ERREUR = "Erreur semantique : affectation sur constante";
                ERREUR();
            }

            GEN(TCode.EMPI, id.adresse);

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

                    int index = tableIdent.chercher(nom);
                    if (index == -1) {
                        MESSAGE_ERREUR = "Erreur semantique : variable non declaree";
                        ERREUR();
                    }

                    Identificateur id = tableIdent.get(index);
                    if (id.getGenre() != GenreIdent.VARIABLE) {
                        MESSAGE_ERREUR = "Erreur semantique : lecture sur constante";
                        ERREUR();
                    }

                    GEN(TCode.EMPI, id.adresse);
                    GEN(TCode.LIRE);

                    AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                    while (AnalyseurLexical.UNILEX == TUnilex.VIRG) {

                        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

                        if (AnalyseurLexical.UNILEX == TUnilex.IDENT) {

                            nom = AnalyseurLexical.CHAINE;

                            index = tableIdent.chercher(nom);
                            if (index == -1) {
                                MESSAGE_ERREUR = "Erreur semantique : variable non declaree";
                                ERREUR();
                            }

                            id = tableIdent.get(index);
                            if (id.getGenre() != GenreIdent.VARIABLE) {
                                MESSAGE_ERREUR = "Erreur semantique : lecture sur constante";
                                ERREUR();
                            }

                            GEN(TCode.EMPI, id.adresse);
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
            } else {
                GEN(TCode.EMPI, id.adresse);
                GEN(TCode.CONT);
            }

            AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();
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

        AnalyseurLexical.UNILEX = AnalyseurLexical.ANALEX();

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

        if (instruction == TCode.EMPI) {
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

        if (instruction == TCode.EMPI) {
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
                    MEMVAR[PILEX[SOM_PILEX - 1]] = PILEX[SOM_PILEX];
                    SOM_PILEX = SOM_PILEX - 2;
                    CO = CO + 1;
                    break;

                case TCode.LIRE:
                    java.util.Scanner sc = new java.util.Scanner(System.in);
                    MEMVAR[PILEX[SOM_PILEX]] = sc.nextInt();
                    SOM_PILEX = SOM_PILEX - 1;
                    CO = CO + 1;
                    break;

                case TCode.ECRE:
                    System.out.println(PILEX[SOM_PILEX]);
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
                    PILEX[SOM_PILEX] = MEMVAR[PILEX[SOM_PILEX]];
                    CO = CO + 1;
                    break;
            }
        }
    }
}
