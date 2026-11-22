package tn.iit.depenses.controller;

import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.service.CategorieService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategorieController {
    private final CategorieService categorieService;

    public CategorieController(CategorieService categorieService) { this.categorieService = categorieService; }

    @GetMapping("/categories")
    public String categories(@AuthenticationPrincipal Membre membre, Model model) {
        model.addAttribute("membre", membre);
        model.addAttribute("estAdmin", membre.getRole() == Role.ADMIN);
        model.addAttribute("categories", categorieService.listerPersonnelles(membre));
        return "categories";
    }

    @PostMapping("/categories")
    public String creer(@AuthenticationPrincipal Membre membre, @RequestParam String nom,
                        @RequestParam TypeOperation type, RedirectAttributes redirect) {
        try {
            categorieService.creerPourMembre(membre, nom, type);
            redirect.addFlashAttribute("succes", "Catégorie créée.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("erreur", ex.getMessage()); }
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/renommer")
    public String renommer(@AuthenticationPrincipal Membre membre, @PathVariable Long id,
                           @RequestParam String nom, RedirectAttributes redirect) {
        try {
            categorieService.renommerPourMembre(membre, id, nom);
            redirect.addFlashAttribute("succes", "Catégorie renommée.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("erreur", ex.getMessage()); }
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/supprimer")
    public String supprimer(@AuthenticationPrincipal Membre membre, @PathVariable Long id,
                            RedirectAttributes redirect) {
        try {
            categorieService.supprimerPourMembre(membre, id);
            redirect.addFlashAttribute("succes", "Catégorie supprimée. Les opérations associées ont été réaffectées.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("erreur", ex.getMessage()); }
        return "redirect:/categories";
    }
}
