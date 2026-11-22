package tn.iit.depenses.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import tn.iit.depenses.domain.Membre;

/**
 * Ajoute à toutes les pages les données de la barre de navigation (membre connecté, accès admin).
 * Évite de répéter ces deux lignes dans chaque contrôleur.
 */
@ControllerAdvice
public class AttributsCommunsVue {

    @ModelAttribute
    public void ajouterMembreConnecte(@AuthenticationPrincipal Membre membre, Model model) {
        if (membre != null) {
            model.addAttribute("membre", membre);
            model.addAttribute("estAdmin", membre.isAdmin());
        }
    }
}
