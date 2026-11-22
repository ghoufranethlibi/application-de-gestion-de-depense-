package tn.iit.depenses.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.service.TableauDeBordFacade;
import tn.iit.depenses.service.dto.TableauDeBord;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

@Controller
public class PageController {
    private final TableauDeBordFacade tableauDeBordFacade;
    private final GradientDonut gradientDonut;

    public PageController(TableauDeBordFacade tableauDeBordFacade, GradientDonut gradientDonut) {
        this.tableauDeBordFacade = tableauDeBordFacade;
        this.gradientDonut = gradientDonut;
    }

    @GetMapping(Routes.ACCUEIL)
    public String accueil() {
        return Routes.rediriger(Routes.CONNEXION);
    }

    /**
     * Spring convertit ?mois=2026-11 en YearMonth. Une valeur invalide n'est plus remplacée
     * en silence par le mois courant : GestionGlobaleErreurs affiche « Paramètre invalide : mois ».
     */
    @GetMapping(Routes.TABLEAU_DE_BORD)
    public String tableauDeBord(@AuthenticationPrincipal Membre membre,
                                @RequestParam(required = false) YearMonth mois,
                                Model model) {
        YearMonth periode = Objects.requireNonNullElseGet(mois, YearMonth::now);
        TableauDeBord tableauDeBord = tableauDeBordFacade.construire(membre, periode);

        model.addAttribute("moisSelectionne", periode.toString());
        model.addAttribute("bilan", tableauDeBord.bilan());
        model.addAttribute("repartition", tableauDeBord.repartition());
        model.addAttribute("donutStyle", gradientDonut.construire(tableauDeBord.repartition()));
        model.addAttribute("couleursLegende", gradientDonut.palette());
        model.addAttribute("evolution", tableauDeBord.evolution());
        model.addAttribute("operations", tableauDeBord.operations());
        model.addAttribute("categoriesDepense", tableauDeBord.categoriesDepense());
        model.addAttribute("categoriesRevenu", tableauDeBord.categoriesRevenu());
        model.addAttribute("aujourdhui", LocalDate.now());
        return "dashboard";
    }
}
