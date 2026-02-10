public class VariableIdent extends Identificateur {

    private int type;    
    private int adresse; 

    public VariableIdent(String nom) {
        super(nom);
    }

    @Override
    public String getGenre() {
        return "VARIABLE";
    }
}
