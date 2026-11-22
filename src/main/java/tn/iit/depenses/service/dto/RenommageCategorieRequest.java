package tn.iit.depenses.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nouveau nom d'une catégorie. */
public record RenommageCategorieRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire.")
        @Size(max = 80, message = "Le nom doit contenir au maximum 80 caractères.")
        String nom) {
}
