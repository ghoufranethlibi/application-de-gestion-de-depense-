package tn.iit.depenses.controller;

import org.junit.jupiter.api.Test;
import tn.iit.depenses.service.dto.RepartitionCategorie;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GradientDonutTest {
    private final GradientDonut gradientDonut = new GradientDonut();

    @Test
    void sans_depense_l_anneau_est_vide() {
        assertThat(gradientDonut.construire(List.of())).isEqualTo("#27384e 0% 100%");
    }

    @Test
    void chaque_categorie_occupe_sa_part_et_le_dernier_segment_finit_a_100() {
        List<RepartitionCategorie> repartition = List.of(
                new RepartitionCategorie("Logement", new BigDecimal("60.000"), 60.0),
                new RepartitionCategorie("Loisirs", new BigDecimal("40.000"), 40.0));

        assertThat(gradientDonut.construire(repartition))
                .isEqualTo("#55d6be 0.00% 60.00%, #ffb86b 60.00% 100.00%");
    }
}
