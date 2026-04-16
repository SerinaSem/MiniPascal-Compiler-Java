public class Identificateur {

    public String nom;
    public GenreIdent typ;   // variable ou constante
    public int typc;         // 0 = entier, 1 = chaine
    public int val;          // valeur ou index
    public int adresse;      // pour variable
    public int nbParam;      // pour fonction

    public Identificateur(String nom) {
        this.nom = nom;
    }

    public GenreIdent getGenre() {
        return typ;
    }
}
