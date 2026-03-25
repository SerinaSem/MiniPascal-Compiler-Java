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
            if (table.get(i).nom.equals(nom)) {
                return i;
            }
        }
        return -1;
    }

    public int inserer(Identificateur e) {

        if (table.size() >= NB_IDENT_MAX) {
            throw new RuntimeException("Table pleine");
        }

        table.add(e);
        return table.size() - 1;
    }

    public Identificateur get(int index) {
        return table.get(index);
    }

    public void afficher() {
        System.out.println("TABLE DES IDENTIFICATEURS :");

        for (int i = 0; i < table.size(); i++) {
            Identificateur id = table.get(i);

            System.out.println(
                    i + " : " +
                            id.nom +
                            " | type=" + id.typ +
                            " | typc=" + id.typc +
                            " | val=" + id.val +
                            " | adr=" + id.adresse);
        }
    }

}