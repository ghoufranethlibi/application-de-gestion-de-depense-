package tn.iit.depenses.service.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public record TotalMensuel(
        YearMonth periode,
        BigDecimal totalRevenus,
        BigDecimal totalDepenses,
        BigDecimal solde) {

    public String libelle() {
        return periode.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + periode.getYear();
    }
}
