public class ConstanteIdent extends Identificateur {

    private int type;
    private int valeur;

    public ConstanteIdent(String nom) {
        super(nom);
    }

    @Override
    public String getGenre() {
        return "CONSTANTE";
    }
}
