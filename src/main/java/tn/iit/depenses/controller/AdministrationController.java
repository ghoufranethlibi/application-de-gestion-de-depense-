package tn.iit.depenses.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.service.AdministrationService;
import tn.iit.depenses.service.CategorieService;

@Controller
public class AdministrationController {
    private final AdministrationService administrationService;
    private final CategorieService categorieService;

    public AdministrationController(AdministrationService administrationService, CategorieService categorieService) {
        this.administrationService = administrationService;
        this.categorieService = categorieService;
    }

    @GetMapping("/administration")
    public String administration(@AuthenticationPrincipal Membre membre, Model model) {
        model.addAttribute("membre", membre);
        model.addAttribute("estAdmin", true);
        model.addAttribute("membres", administrationService.listerMembres());
        model.addAttribute("categories", categorieService.listerCategoriesParDefaut());
        return "administration";
    }

    @PostMapping("/administration/comptes/{id}/etat")
    public String changerEtat(@AuthenticationPrincipal Membre administrateur, @PathVariable Long id,
                              RedirectAttributes redirect) {
        administrationService.inverserEtat(id, administrateur);
        redirect.addFlashAttribute("succes", "État du compte mis à jour.");
        return "redirect:/administration";
    }

    @PostMapping("/administration/categories")
    public String creerCategorie(@RequestParam String nom, @RequestParam TypeOperation type,
                                 RedirectAttributes redirect) {
        categorieService.creerCategorieParDefaut(nom, type);
        redirect.addFlashAttribute("succes", "Catégorie par défaut créée.");
        return "redirect:/administration";
    }

    @PostMapping("/administration/categories/{id}/renommer")
    public String renommerCategorie(@PathVariable Long id, @RequestParam String nom, RedirectAttributes redirect) {
        categorieService.renommerCategorieParDefaut(id, nom);
        redirect.addFlashAttribute("succes", "Catégorie par défaut renommée.");
        return "redirect:/administration";
    }

    @PostMapping("/administration/categories/{id}/supprimer")
    public String supprimerCategorie(@PathVariable Long id, RedirectAttributes redirect) {
        categorieService.supprimerCategorieParDefaut(id);
        redirect.addFlashAttribute("succes", "Catégorie supprimée et opérations associées réaffectées.");
        return "redirect:/administration";
    }
}