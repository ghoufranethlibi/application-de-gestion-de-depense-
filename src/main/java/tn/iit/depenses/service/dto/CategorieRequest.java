package tn.iit.depenses.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.iit.depenses.domain.TypeOperation;

/** Création d'une catégorie (personnelle ou par défaut). */
public record CategorieRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire.")
        @Size(max = 80, message = "Le nom doit contenir au maximum 80 caractères.")
        String nom,

        @NotNull(message = "Le type de la catégorie est obligatoire.")
        TypeOperation type) {
}
