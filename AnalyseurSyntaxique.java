public class AnalyseurSyntaxique {

    public static TUnilex UNILEX;

    public static void initialiser() {
        UNILEX = AnalyseurLexical.ANALEX();
    }

    public static boolean PROG() {
        System.out.println("PROG");

        if (UNILEX == TUnilex.MOTCLE &&
                AnalyseurLexical.CHAINE.equals("PROGRAMME")) {

            UNILEX = AnalyseurLexical.ANALEX();

            if (UNILEX == TUnilex.IDENT) {

                UNILEX = AnalyseurLexical.ANALEX();

                if (UNILEX == TUnilex.PTVIRG) {

                    UNILEX = AnalyseurLexical.ANALEX();

                    if (DECL_CONST()) {

                        if (DECL_VAR()) {

                            if (BLOC()) {

                                if (UNILEX == TUnilex.POINT) {
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

        if (UNILEX == TUnilex.MOTCLE &&
                AnalyseurLexical.CHAINE.equals("CONST")) {

            UNILEX = AnalyseurLexical.ANALEX();

            while (UNILEX == TUnilex.IDENT) {

                UNILEX = AnalyseurLexical.ANALEX();

                if (UNILEX == TUnilex.EG) {

                    UNILEX = AnalyseurLexical.ANALEX();

                    if (UNILEX == TUnilex.ENT ||
                            UNILEX == TUnilex.CH) {

                        UNILEX = AnalyseurLexical.ANALEX();

                        if (UNILEX == TUnilex.PTVIRG) {
                            UNILEX = AnalyseurLexical.ANALEX();
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        return true; // partie optionnelle
    }

    public static boolean DECL_VAR() {
        System.out.println("DECL_VAR");

        if (UNILEX == TUnilex.MOTCLE &&
                AnalyseurLexical.CHAINE.equals("VAR")) {

            UNILEX = AnalyseurLexical.ANALEX();

            if (UNILEX == TUnilex.IDENT) {

                UNILEX = AnalyseurLexical.ANALEX();

                while (UNILEX == TUnilex.VIRG) {

                    UNILEX = AnalyseurLexical.ANALEX();

                    if (UNILEX == TUnilex.IDENT) {
                        UNILEX = AnalyseurLexical.ANALEX();
                    } else {
                        return false;
                    }
                }

                if (UNILEX == TUnilex.PTVIRG) {
                    UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }
            }

            return false;
        }

        return true; // partie optionnelle
    }

    public static boolean BLOC() {
        System.out.println("BLOC");

        if (UNILEX == TUnilex.MOTCLE &&
                AnalyseurLexical.CHAINE.equals("DEBUT")) {

            UNILEX = AnalyseurLexical.ANALEX();

            if (INSTRUCTION()) {

                while (UNILEX == TUnilex.PTVIRG) {

                    UNILEX = AnalyseurLexical.ANALEX();

                    if (!INSTRUCTION()) {
                        return false;
                    }
                }

                if (UNILEX == TUnilex.MOTCLE &&
                        AnalyseurLexical.CHAINE.equals("FIN")) {

                    UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean INSTRUCTION() {
        System.out.println("INSTRUCTION");

        if (AFFECTATION())
            return true;
        if (LECTURE())
            return true;
        if (ECRITURE())
            return true;
        if (BLOC())
            return true;

        return false;
    }

    public static boolean AFFECTATION() {
        System.out.println("AFFECTATION");

        if (UNILEX == TUnilex.IDENT) {

            UNILEX = AnalyseurLexical.ANALEX();

            if (UNILEX == TUnilex.AFF) {

                UNILEX = AnalyseurLexical.ANALEX();

                if (EXP()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean LECTURE() {
        System.out.println("LECTURE");

        if (UNILEX == TUnilex.MOTCLE &&
                AnalyseurLexical.CHAINE.equals("LIRE")) {

            UNILEX = AnalyseurLexical.ANALEX();

            if (UNILEX == TUnilex.PAROUV) {

                UNILEX = AnalyseurLexical.ANALEX();

                if (UNILEX == TUnilex.IDENT) {

                    UNILEX = AnalyseurLexical.ANALEX();

                    while (UNILEX == TUnilex.VIRG) {

                        UNILEX = AnalyseurLexical.ANALEX();

                        if (UNILEX == TUnilex.IDENT) {
                            UNILEX = AnalyseurLexical.ANALEX();
                        } else {
                            return false;
                        }
                    }

                    if (UNILEX == TUnilex.PARFER) {
                        UNILEX = AnalyseurLexical.ANALEX();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean ECRITURE() {
        System.out.println("ECRITURE");

        if (UNILEX == TUnilex.MOTCLE &&
                AnalyseurLexical.CHAINE.equals("ECRIRE")) {

            UNILEX = AnalyseurLexical.ANALEX();

            if (UNILEX == TUnilex.PAROUV) {

                UNILEX = AnalyseurLexical.ANALEX();

                if (ECR_EXP()) {

                    while (UNILEX == TUnilex.VIRG) {

                        UNILEX = AnalyseurLexical.ANALEX();

                        if (!ECR_EXP()) {
                            return false;
                        }
                    }
                }

                if (UNILEX == TUnilex.PARFER) {
                    UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean ECR_EXP() {
        System.out.println("ECR_EXP");

        if (UNILEX == TUnilex.CH) {
            UNILEX = AnalyseurLexical.ANALEX();
            return true;
        }

        return EXP();
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

        if (UNILEX == TUnilex.PLUS ||
                UNILEX == TUnilex.MOINS ||
                UNILEX == TUnilex.MULT ||
                UNILEX == TUnilex.DIVI) {

            if (OP_BIN()) {
                return EXP();
            }

            return false;
        }

        // ε (vide)
        return true;
    }

    public static boolean TERME() {
        System.out.println("TERME");

        if (UNILEX == TUnilex.ENT ||
                UNILEX == TUnilex.IDENT) {

            UNILEX = AnalyseurLexical.ANALEX();
            return true;
        }

        if (UNILEX == TUnilex.PAROUV) {

            UNILEX = AnalyseurLexical.ANALEX();

            if (EXP()) {

                if (UNILEX == TUnilex.PARFER) {
                    UNILEX = AnalyseurLexical.ANALEX();
                    return true;
                }
            }

            return false;
        }

        if (UNILEX == TUnilex.MOINS) {

            UNILEX = AnalyseurLexical.ANALEX();
            return TERME();
        }

        return false;
    }

    public static boolean OP_BIN() {
        System.out.println("OP_BIN");

        if (UNILEX == TUnilex.PLUS ||
                UNILEX == TUnilex.MOINS ||
                UNILEX == TUnilex.MULT ||
                UNILEX == TUnilex.DIVI) {

            UNILEX = AnalyseurLexical.ANALEX();
            return true;
        }

        return false;
    }

    public static void ANASYNT() {

        // Lire la première unité lexicale
        UNILEX = AnalyseurLexical.ANALEX();

        // Vérifier la grammaire
        if (PROG() && AnalyseurLexical.CARLU == '\0') {

            System.out.println("Le programme source est syntaxiquement correct");

        } else {

            System.out.println("Erreur syntaxique");
            System.exit(3);
        }
    }

}
