/*public enum TUnilex {
    MOTCLE,IDENT,ENT,CH,
    VIRG,PTVIRG,POINT,DEUXPTS,
    PAROUV,PARFER,
    INF,SUP,EG,
    PLUS,MOINS,MULT,DIVI,
    INFE,SUPE,DIFF,
    AFF
}*/

public enum TUnilex {

    MOTCLE("MOTCLE"),
    IDENT("IDENT"),
    ENT("ENT"),
    CH("CH"),

    PTVIRG(";"),
    VIRG(","),
    POINT("."),
    PAROUV("("),
    PARFER(")"),

    AFF(":="),
    EG("="),
    INF("<"),
    SUP(">"),
    INFE("<="),
    SUPE(">="),
    DIFF("<>"),

    PLUS("+"),
    MOINS("-"),
    MULT("*"),
    DIVI("/"),

    DEUXPTS(":");

    private final String libelle;

    TUnilex(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}

