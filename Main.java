public class Main {



    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Nom du fichier manquant");
            return;
        }

        AnalyseurLexical.INITIALISER(args[0]);
        AnalyseurSyntaxique.ANASYNT();
        AnalyseurLexical.TERMINER();
        System.out.println("Analyse syntaxique terminée avec succès !");
    }

    
    
    
    
    
    
    
    
    /*public static void main(String[] args) {

        AnalyseurLexical.INITIALISER(args[0]);
        TableIdentificateurs table = new TableIdentificateurs();

        try {
            while (true) {

                TUnilex u = AnalyseurLexical.ANALEX();

                if (u == TUnilex.IDENT) {
                    String nom = AnalyseurLexical.CHAINE;

                    int indice = table.chercher(nom);

                    if (indice == -1) {
                        table.inserer(nom, GenreIdent.INDETERMINE);
                    }
                }
            }

        } catch (Exception e) {
        }

        AnalyseurLexical.TERMINER();

        table.AFFICHE_TABLE_IDENT();
    }*/
}


/*public class Main {

    public static void main(String[] args) {

        AnalyseurLexical.INITIALISER(args[0]);

        try {
            while (true) {
                TUnilex u = AnalyseurLexical.ANALEX();

                switch (u) {
                    case IDENT, MOTCLE, CH ->
                        System.out.println(u + "(" + AnalyseurLexical.CHAINE + ")");
                    case ENT ->
                        System.out.println(u + "(" + AnalyseurLexical.NOMBRE + ")");
                    default ->
                        System.out.println(u + "(" + u.getLibelle() + ")");
                }
            }
        } finally {
            AnalyseurLexical.TERMINER();
        }
    }
}
*/

/*public class Main {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage : java Main <fichier_source>");
            return;
        }

        AnalyseurLexical.INITIALISER(args[0]);

        try {
            // 2. Reconnaissance et affichage des unités lexicales
            while (true) {
                TUnilex unite = AnalyseurLexical.ANALEX();
                System.out.println(unite);
            }
        } finally {
            // 3. Fin du programme
            AnalyseurLexical.TERMINER();
        }
    }
}*/
