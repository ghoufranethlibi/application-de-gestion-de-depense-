package tn.iit.depenses.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulaire d'inscription validé par @Valid. BCrypt ignore au-delà de 72 octets, d'où la limite. */
public record InscriptionRequest(
        @NotBlank(message = "Le nom est obligatoire.")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
        String nom,

        @NotBlank(message = "L’adresse e-mail est obligatoire.")
        @Email(message = "L’adresse e-mail n’est pas valide.")
        @Size(max = 180, message = "L’adresse e-mail ne doit pas dépasser 180 caractères.")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire.")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
        String motDePasse) {
}
