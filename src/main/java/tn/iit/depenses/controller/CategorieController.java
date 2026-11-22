package tn.iit.depenses.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.service.CategorieService;
import tn.iit.depenses.service.dto.CategorieRequest;
import tn.iit.depenses.service.dto.RenommageCategorieRequest;

@Controller
public class CategorieController {
    private final CategorieService categorieService;

    public CategorieController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    @GetMapping(Routes.CATEGORIES)
    public String categories(@AuthenticationPrincipal Membre membre, Model model) {
        model.addAttribute("categories", categorieService.listerPersonnelles(membre));
        return "categories";
    }

    @PostMapping(Routes.CATEGORIES)
    public String creer(@AuthenticationPrincipal Membre membre, @Valid CategorieRequest requete,
                        RedirectAttributes redirect) {
        categorieService.creerPourMembre(membre, requete.nom(), requete.type());
        return retourCategories(redirect, "Catégorie créée.");
    }

    @PostMapping(Routes.CATEGORIES + "/{id}/renommer")
    public String renommer(@AuthenticationPrincipal Membre membre, @PathVariable Long id,
                           @Valid RenommageCategorieRequest requete, RedirectAttributes redirect) {
        categorieService.renommerPourMembre(membre, id, requete.nom());
        return retourCategories(redirect, "Catégorie renommée.");
    }

    @PostMapping(Routes.CATEGORIES + "/{id}/supprimer")
    public String supprimer(@AuthenticationPrincipal Membre membre, @PathVariable Long id,
                            RedirectAttributes redirect) {
        categorieService.supprimerPourMembre(membre, id);
        return retourCategories(redirect, "Catégorie supprimée. Les opérations associées ont été réaffectées.");
    }

    private static String retourCategories(RedirectAttributes redirect, String message) {
        redirect.addFlashAttribute(MessagesFlash.SUCCES, message);
        return Routes.rediriger(Routes.CATEGORIES);
    }
}
