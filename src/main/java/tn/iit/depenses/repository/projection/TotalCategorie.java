package tn.iit.depenses.repository.projection;

import java.math.BigDecimal;

/** Résultat d'agrégation : total des dépenses d'une catégorie (nom null = sans catégorie). */
public record TotalCategorie(String nom, BigDecimal montant) {
}
