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
import tn.iit.depenses.service.AdministrationService;
import tn.iit.depenses.service.CategorieService;
import tn.iit.depenses.service.dto.CategorieRequest;
import tn.iit.depenses.service.dto.RenommageCategorieRequest;

/** Accessible uniquement au rôle ADMIN (voir SecurityConfig). */
@Controller
public class AdministrationController {
    private final AdministrationService administrationService;
    private final CategorieService categorieService;

    public AdministrationController(AdministrationService administrationService, CategorieService categorieService) {
        this.administrationService = administrationService;
        this.categorieService = categorieService;
    }

    @GetMapping(Routes.ADMINISTRATION)
    public String administration(Model model) {
        model.addAttribute("membres", administrationService.listerMembres());
        model.addAttribute("categories", categorieService.listerCategoriesParDefaut());
        return "administration";
    }

    @PostMapping(Routes.ADMINISTRATION + "/comptes/{id}/etat")
    public String changerEtat(@AuthenticationPrincipal Membre administrateur, @PathVariable Long id,
                              RedirectAttributes redirect) {
        administrationService.inverserEtat(id, administrateur);
        return retourAdministration(redirect, "État du compte mis à jour.");
    }

    @PostMapping(Routes.ADMINISTRATION + "/categories")
    public String creerCategorie(@Valid CategorieRequest requete, RedirectAttributes redirect) {
        categorieService.creerCategorieParDefaut(requete.nom(), requete.type());
        return retourAdministration(redirect, "Catégorie par défaut créée.");
    }

    @PostMapping(Routes.ADMINISTRATION + "/categories/{id}/renommer")
    public String renommerCategorie(@PathVariable Long id, @Valid RenommageCategorieRequest requete,
                                    RedirectAttributes redirect) {
        categorieService.renommerCategorieParDefaut(id, requete.nom());
        return retourAdministration(redirect, "Catégorie par défaut renommée.");
    }

    @PostMapping(Routes.ADMINISTRATION + "/categories/{id}/supprimer")
    public String supprimerCategorie(@PathVariable Long id, RedirectAttributes redirect) {
        categorieService.supprimerCategorieParDefaut(id);
        return retourAdministration(redirect, "Catégorie supprimée et opérations associées réaffectées.");
    }

    private static String retourAdministration(RedirectAttributes redirect, String message) {
        redirect.addFlashAttribute(MessagesFlash.SUCCES, message);
        return Routes.rediriger(Routes.ADMINISTRATION);
    }
}
