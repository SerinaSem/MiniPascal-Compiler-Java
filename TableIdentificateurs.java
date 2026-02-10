import java.util.ArrayList;
import java.util.List;

public class TableIdentificateurs {

    private static final int NB_IDENT_MAX = 100;
    private final List<Identificateur> table;

    public TableIdentificateurs() {
        table = new ArrayList<>();
    }

    public int chercher(String nom) {
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).getNom().equals(nom)) {
                return i;
            }
        }
        return -1;
    }

    public int inserer(String nom, GenreIdent genre) {

        int index = chercher(nom);
        if (index != -1) {
            return index;
        }

        if (table.size() >= NB_IDENT_MAX) {
            throw new RuntimeException("Table des identificateurs pleine");
        }

        Identificateur ident;

        switch (genre) {
            case VARIABLE -> ident = new VariableIdent(nom);
            case CONSTANTE -> ident = new ConstanteIdent(nom);
            default -> throw new IllegalArgumentException("Genre inconnu");
        }

        table.add(ident);
        return table.size() - 1;
    }

    public void AFFICHE_TABLE_IDENT() {
        System.out.println("TABLE DES IDENTIFICATEURS :");
        for (int i = 0; i < table.size(); i++) {
            Identificateur id = table.get(i);
            System.out.println(
                i + " : " + id.getNom() + " , genre = " + id.getGenre()
            );
        }
    }
}
