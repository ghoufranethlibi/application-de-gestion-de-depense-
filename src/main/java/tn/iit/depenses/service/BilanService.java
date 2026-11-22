package tn.iit.depenses.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.repository.OperationRepository;
import tn.iit.depenses.repository.projection.TotalCategorie;
import tn.iit.depenses.service.dto.EvolutionMensuelle;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import tn.iit.depenses.service.dto.TotalMensuel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bilans mensuels. Les sommes et regroupements sont calculés par la base de données. */
@Service
public class BilanService {
    private static final int DECIMALES_MONTANT = 3;
    private static final int NOMBRE_MOIS_EVOLUTION = 6;
    private static final int LARGEUR_MINIMALE_BARRE = 4;
    private static final BigDecimal CENT = BigDecimal.valueOf(100);
    private static final String SANS_CATEGORIE = "Sans catégorie";

    private final OperationRepository operations;

    public BilanService(OperationRepository operations) {
        this.operations = operations;
    }

    @Transactional(readOnly = true)
    public TotalMensuel calculerTotalMensuel(Membre membre, YearMonth periode) {
        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();
        BigDecimal revenus = montant(operations.totalRevenus(membre, debut, fin));
        BigDecimal depenses = montant(operations.totalDepenses(membre, debut, fin));
        return new TotalMensuel(periode, revenus, depenses, revenus.subtract(depenses));
    }

    @Transactional(readOnly = true)
    public List<RepartitionCategorie> repartitionParCategorie(Membre membre, YearMonth periode) {
        List<TotalCategorie> totaux = operations.totauxDepensesParCategorie(
                membre, periode.atDay(1), periode.atEndOfMonth());
        BigDecimal totalDepenses = totaux.stream()
                .map(TotalCategorie::montant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totaux.stream()
                .map(total -> new RepartitionCategorie(
                        total.nom() == null ? SANS_CATEGORIE : total.nom(),
                        montant(total.montant()),
                        pourcentage(total.montant(), totalDepenses)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EvolutionMensuelle> evolutionMensuelle(Membre membre, YearMonth dernierMois) {
        List<TotalMensuel> bilans = new ArrayList<>();
        for (int moisAvant = NOMBRE_MOIS_EVOLUTION - 1; moisAvant >= 0; moisAvant--) {
            bilans.add(calculerTotalMensuel(membre, dernierMois.minusMonths(moisAvant)));
        }
        BigDecimal maximum = bilans.stream()
                .map(bilan -> bilan.totalRevenus().max(bilan.totalDepenses()))
                .reduce(BigDecimal.ZERO, BigDecimal::max);
        return bilans.stream()
                .map(bilan -> new EvolutionMensuelle(
                        bilan.periode().getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH),
                        bilan.totalRevenus(),
                        bilan.totalDepenses(),
                        largeurBarre(bilan.totalRevenus(), maximum),
                        largeurBarre(bilan.totalDepenses(), maximum)))
                .toList();
    }

    private static BigDecimal montant(BigDecimal valeur) {
        BigDecimal nonNul = valeur == null ? BigDecimal.ZERO : valeur;
        return nonNul.setScale(DECIMALES_MONTANT, RoundingMode.HALF_UP);
    }

    private static double pourcentage(BigDecimal part, BigDecimal total) {
        if (total.signum() == 0) {
            return 0.0;
        }
        return part.multiply(CENT).divide(total, 2, RoundingMode.HALF_UP).doubleValue();
    }

    /** Hauteur d'une barre du graphique en % du mois le plus élevé (minimum visible si la valeur n'est pas nulle). */
    private static int largeurBarre(BigDecimal valeur, BigDecimal maximum) {
        if (maximum.signum() == 0 || valeur.signum() == 0) {
            return 0;
        }
        int largeur = valeur.multiply(CENT).divide(maximum, 0, RoundingMode.HALF_UP).intValue();
        return Math.max(LARGEUR_MINIMALE_BARRE, largeur);
    }
}
