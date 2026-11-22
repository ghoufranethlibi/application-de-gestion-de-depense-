package tn.iit.depenses.controller;

import org.springframework.stereotype.Component;
import tn.iit.depenses.service.dto.RepartitionCategorie;

import java.util.List;
import java.util.Locale;

/**
 * Présentation : construit le dégradé CSS (conic-gradient) du graphique en anneau.
 * Extrait de PageController, qui n'a plus à connaître le CSS.
 */
@Component
public class GradientDonut {
    /** Couleurs des segments, réutilisées pour la légende (une seule source de vérité). */
    private static final List<String> PALETTE =
            List.of("#55d6be", "#ffb86b", "#7c8cff", "#ff718b", "#c084fc", "#facc15");
    private static final String ANNEAU_VIDE = "#27384e 0% 100%";

    public List<String> palette() {
        return PALETTE;
    }

    public String construire(List<RepartitionCategorie> repartition) {
        if (repartition.isEmpty()) {
            return ANNEAU_VIDE;
        }
        StringBuilder css = new StringBuilder();
        double debut = 0;
        for (int i = 0; i < repartition.size(); i++) {
            boolean dernier = i == repartition.size() - 1;
            double fin = dernier ? 100 : debut + repartition.get(i).pourcentage();
            if (i > 0) {
                css.append(", ");
            }
            css.append(PALETTE.get(i % PALETTE.size())).append(' ')
                    .append(String.format(Locale.US, "%.2f%% %.2f%%", debut, fin));
            debut = fin;
        }
        return css.toString();
    }
}
