package tn.iit.depenses.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tn.iit.depenses.domain.Depense;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Revenu;
import tn.iit.depenses.service.BilanService;
import tn.iit.depenses.service.dto.EvolutionMensuelle;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import tn.iit.depenses.service.dto.TotalMensuel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Vérifie les requêtes d'agrégation (SUM / GROUP BY) exécutées par la base H2. */
class BilanServiceIntegrationTest extends IntegrationTestBase {
    private static final YearMonth NOVEMBRE = YearMonth.of(2026, 11);

    @Autowired
    private BilanService bilanService;

    @Test
    void totaux_repartition_et_evolution_du_mois() {
        Membre ali = nouveauMembre("Ali");
        operations.save(new Revenu(new BigDecimal("1000.000"), novembre(5), "Salaire", ali, null));
        operations.save(new Depense(new BigDecimal("200.500"), novembre(6), "Courses", ali, categorieParDefaut("Alimentation")));
        operations.save(new Depense(new BigDecimal("99.500"), novembre(7), "Bus", ali, categorieParDefaut("Transport")));
        operations.save(new Depense(new BigDecimal("50.000"), LocalDate.of(2026, 10, 30), "Octobre", ali,
                categorieParDefaut("Transport")));
        Membre sara = nouveauMembre("Sara");
        operations.save(new Revenu(new BigDecimal("9999.000"), novembre(5), "Autre membre", sara, null));

        TotalMensuel total = bilanService.calculerTotalMensuel(ali, NOVEMBRE);
        assertThat(total.totalRevenus()).isEqualByComparingTo("1000");
        assertThat(total.totalDepenses()).isEqualByComparingTo("300");
        assertThat(total.solde()).isEqualByComparingTo("700");

        List<RepartitionCategorie> repartition = bilanService.repartitionParCategorie(ali, NOVEMBRE);
        assertThat(repartition).extracting(RepartitionCategorie::nom).containsExactly("Alimentation", "Transport");
        assertThat(repartition.get(0).pourcentage()).isEqualTo(66.83);

        List<EvolutionMensuelle> evolution = bilanService.evolutionMensuelle(ali, NOVEMBRE);
        assertThat(evolution).hasSize(6);
        assertThat(evolution.get(5).revenus()).isEqualByComparingTo("1000");
        assertThat(evolution.get(4).depenses()).isEqualByComparingTo("50");
    }

    @Test
    void un_mois_sans_operation_donne_des_totaux_a_zero() {
        Membre ali = nouveauMembre("Ali");

        TotalMensuel total = bilanService.calculerTotalMensuel(ali, NOVEMBRE);

        assertThat(total.totalRevenus()).isEqualByComparingTo("0");
        assertThat(total.solde()).isEqualByComparingTo("0");
        assertThat(bilanService.repartitionParCategorie(ali, NOVEMBRE)).isEmpty();
    }

    private static LocalDate novembre(int jour) {
        return NOVEMBRE.atDay(jour);
    }
}
