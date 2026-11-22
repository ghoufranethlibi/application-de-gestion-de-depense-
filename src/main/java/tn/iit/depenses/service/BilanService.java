package tn.iit.depenses.service;

import tn.iit.depenses.domain.Depense;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.domain.Revenu;
import tn.iit.depenses.repository.OperationRepository;
import tn.iit.depenses.service.dto.EvolutionMensuelle;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import tn.iit.depenses.service.dto.TotalMensuel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BilanService {
    private static final BigDecimal ZERO = new BigDecimal("0.000");
    private final OperationRepository operations;

    public BilanService(OperationRepository operations) {
        this.operations = operations;
    }

    @Transactional(readOnly = true)
    public TotalMensuel calculerTotalMensuel(Membre membre, YearMonth periode) {
        List<Operation> duMois = operations.findByMembreAndDateBetweenOrderByDateDesc(
                membre, periode.atDay(1), periode.atEndOfMonth());
        BigDecimal revenus = duMois.stream().filter(Revenu.class::isInstance)
                .map(Operation::getMontant).reduce(ZERO, BigDecimal::add);
        BigDecimal depenses = duMois.stream().filter(Depense.class::isInstance)
                .map(Operation::getMontant).reduce(ZERO, BigDecimal::add);
        return new TotalMensuel(periode, revenus, depenses, revenus.subtract(depenses));
    }

    @Transactional(readOnly = true)
    public List<RepartitionCategorie> repartitionParCategorie(Membre membre, YearMonth periode) {
        List<Operation> duMois = operations.findByMembreAndDateBetweenOrderByDateDesc(
                membre, periode.atDay(1), periode.atEndOfMonth());
        Map<String, BigDecimal> totaux = new LinkedHashMap<>();
        BigDecimal total = ZERO;
        for (Operation operation : duMois) {
            if (operation instanceof Depense) {
                String nom = operation.getCategorie() == null ? "Sans catégorie" : operation.getCategorie().getNom();
                totaux.merge(nom, operation.getMontant(), BigDecimal::add);
                total = total.add(operation.getMontant());
            }
        }
        BigDecimal totalDepenses = total;
        return totaux.entrySet().stream()
                .map(e -> new RepartitionCategorie(e.getKey(), e.getValue(),
                        totalDepenses.signum() == 0 ? 0.0 : e.getValue().multiply(BigDecimal.valueOf(100))
                                .divide(totalDepenses, 2, RoundingMode.HALF_UP).doubleValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EvolutionMensuelle> evolutionMensuelle(Membre membre, YearMonth fin) {
        List<TotalMensuel> bilans = new ArrayList<>();
        BigDecimal maximum = ZERO;
        for (int i = 5; i >= 0; i--) {
            TotalMensuel bilan = calculerTotalMensuel(membre, fin.minusMonths(i));
            bilans.add(bilan);
            maximum = maximum.max(bilan.totalRevenus()).max(bilan.totalDepenses());
        }
        BigDecimal borne = maximum;
        return bilans.stream().map(b -> new EvolutionMensuelle(
                b.periode().getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.FRENCH),
                b.totalRevenus(), b.totalDepenses(), largeur(b.totalRevenus(), borne), largeur(b.totalDepenses(), borne)))
                .toList();
    }

    private int largeur(BigDecimal valeur, BigDecimal maximum) {
        if (maximum.signum() == 0 || valeur.signum() == 0) return 0;
        return Math.max(4, valeur.multiply(BigDecimal.valueOf(100))
                .divide(maximum, 0, RoundingMode.HALF_UP).intValue());
    }
}
