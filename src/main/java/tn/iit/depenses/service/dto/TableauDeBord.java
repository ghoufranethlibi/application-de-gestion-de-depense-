package tn.iit.depenses.service.dto;

import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Operation;

import java.util.List;

/** Toutes les données affichées par le tableau de bord pour une période. */
public record TableauDeBord(
        TotalMensuel bilan,
        List<RepartitionCategorie> repartition,
        List<EvolutionMensuelle> evolution,
        List<Operation> operations,
        List<Categorie> categoriesDepense,
        List<Categorie> categoriesRevenu) {
}
