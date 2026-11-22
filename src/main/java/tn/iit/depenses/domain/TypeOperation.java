package tn.iit.depenses.domain;

/**
 * Type d'une opération financière.
 * Centralise les libellés et la catégorie de réaffectation (plus de chaînes en dur dans les services et les vues).
 */
public enum TypeOperation {
    REVENU("Revenu", "+", "Autres revenus"),
    DEPENSE("Dépense", "−", "Autres");

    private final String libelle;
    private final String signe;
    private final String categorieDeReaffectation;

    TypeOperation(String libelle, String signe, String categorieDeReaffectation) {
        this.libelle = libelle;
        this.signe = signe;
        this.categorieDeReaffectation = categorieDeReaffectation;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getSigne() {
        return signe;
    }

    public boolean isRevenu() {
        return this == REVENU;
    }

    /** Nom de la catégorie qui reçoit les opérations d'une catégorie supprimée. */
    public String getCategorieDeReaffectation() {
        return categorieDeReaffectation;
    }
}
