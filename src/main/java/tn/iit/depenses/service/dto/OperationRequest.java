package tn.iit.depenses.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Données saisies pour un revenu ou une dépense.
 * Regroupe montant, date, description et catégorie (supprime le « data clump » des signatures)
 * et porte les règles de validation vérifiées par @Valid.
 */
public record OperationRequest(
        @NotNull(message = "Le montant est obligatoire.")
        @DecimalMin(value = "0.001", message = "Le montant doit être supérieur à zéro.")
        @Digits(integer = 9, fraction = 3,
                message = "Le montant doit avoir au maximum trois chiffres après la virgule.")
        BigDecimal montant,

        @NotNull(message = "La date est obligatoire.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @Size(max = 240, message = "La description est trop longue (240 caractères maximum).")
        String description,

        Long categorieId) {
}
