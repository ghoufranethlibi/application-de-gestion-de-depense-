package tn.iit.depenses.controller;

import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.service.BilanService;
import tn.iit.depenses.service.CategorieService;
import tn.iit.depenses.service.OperationService;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Controller
public class PageController {
    private static final String[] COULEURS = {"#55d6be", "#ffb86b", "#7c8cff", "#ff718b", "#c084fc", "#facc15"};
    private final BilanService bilanService;
    private final CategorieService categorieService;
    private final OperationService operationService;

    public PageController(BilanService bilanService, CategorieService categorieService, OperationService operationService) {
        this.bilanService = bilanService;
        this.categorieService = categorieService;
        this.operationService = operationService;
    }

    @GetMapping("/")
    public String accueil() { return "redirect:/connexion"; }

    @GetMapping("/tableau-de-bord")
    public String tableauDeBord(@AuthenticationPrincipal Membre membre,
                               @RequestParam(required = false) String mois,
                               Model model) {
        YearMonth periode = parsePeriode(mois);
        List<RepartitionCategorie> repartition = bilanService.repartitionParCategorie(membre, periode);
        model.addAttribute("membre", membre);
        model.addAttribute("estAdmin", membre.getRole() == Role.ADMIN);
        model.addAttribute("periode", periode);
        model.addAttribute("moisSelectionne", periode.toString());
        model.addAttribute("bilan", bilanService.calculerTotalMensuel(membre, periode));
        model.addAttribute("repartition", repartition);
        model.addAttribute("donutStyle", construireGradient(repartition));
        model.addAttribute("evolution", bilanService.evolutionMensuelle(membre, periode));
        model.addAttribute("operations", operationService.lister(membre, periode));
        model.addAttribute("categoriesDepense", categorieService.listerPourMembre(membre, TypeOperation.DEPENSE));
        model.addAttribute("categoriesRevenu", categorieService.listerPourMembre(membre, TypeOperation.REVENU));
        model.addAttribute("aujourdhui", java.time.LocalDate.now());
        return "dashboard";
    }

    private YearMonth parsePeriode(String valeur) {
        try { return valeur == null ? YearMonth.now() : YearMonth.parse(valeur); }
        catch (RuntimeException ex) { return YearMonth.now(); }
    }

    private String construireGradient(List<RepartitionCategorie> repartition) {
        if (repartition.isEmpty()) return "#27384e 0% 100%";
        StringBuilder css = new StringBuilder();
        double debut = 0;
        for (int i = 0; i < repartition.size(); i++) {
            double fin = i == repartition.size() - 1 ? 100 : debut + repartition.get(i).pourcentage();
            if (i > 0) css.append(", ");
            css.append(COULEURS[i % COULEURS.length]).append(' ')
                    .append(String.format(Locale.US, "%.2f%% %.2f%%", debut, fin));
            debut = fin;
        }
        return css.toString();
    }
}
