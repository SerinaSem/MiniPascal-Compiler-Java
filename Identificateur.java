public abstract class Identificateur {
    protected String nom;

    public Identificateur(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public abstract String getGenre();
}
