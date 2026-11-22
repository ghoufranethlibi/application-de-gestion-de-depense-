package tn.iit.depenses.service.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record TotalMensuel(
        YearMonth periode,
        BigDecimal totalRevenus,
        BigDecimal totalDepenses,
        BigDecimal solde) {
    public String libelle() {
        return periode.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH)
                + " " + periode.getYear();
    }
}
