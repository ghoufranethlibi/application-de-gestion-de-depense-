package tn.iit.depenses.controller;

import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.service.OperationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class OperationController {
    private final OperationService operationService;

    public OperationController(OperationService operationService) { this.operationService = operationService; }

    @PostMapping("/operations/revenu")
    public String ajouterRevenu(@AuthenticationPrincipal Membre membre,
                                @RequestParam BigDecimal montant,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) Long categorieId,
                                RedirectAttributes redirect) {
        return executer(redirect, () -> operationService.ajouterRevenu(membre, montant, date, description, categorieId),
                "Revenu ajouté au suivi.");
    }

    @PostMapping("/operations/depense")
    public String ajouterDepense(@AuthenticationPrincipal Membre membre,
                                 @RequestParam BigDecimal montant,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) Long categorieId,
                                 RedirectAttributes redirect) {
        return executer(redirect, () -> operationService.ajouterDepense(membre, montant, date, description, categorieId),
                "Dépense ajoutée au suivi.");
    }

    @PostMapping("/operations/{id}/modifier")
    public String modifier(@AuthenticationPrincipal Membre membre, @PathVariable Long id,
                           @RequestParam BigDecimal montant,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) Long categorieId,
                           RedirectAttributes redirect) {
        return executer(redirect, () -> operationService.modifier(membre, id, montant, date, description, categorieId),
                "Opération modifiée.");
    }

    @PostMapping("/operations/{id}/supprimer")
    public String supprimer(@AuthenticationPrincipal Membre membre, @PathVariable Long id, RedirectAttributes redirect) {
        return executer(redirect, () -> operationService.supprimer(membre, id), "Opération supprimée.");
    }

    private String executer(RedirectAttributes redirect, Action action, String succes) {
        try {
            action.execute();
            redirect.addFlashAttribute("succes", succes);
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/tableau-de-bord";
    }

    @FunctionalInterface private interface Action { void execute(); }
}
