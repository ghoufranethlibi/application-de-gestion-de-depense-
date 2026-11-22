package tn.iit.depenses.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.service.OperationService;
import tn.iit.depenses.service.dto.OperationRequest;

@Controller
public class OperationController {
    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @PostMapping(Routes.OPERATIONS + "/revenu")
    public String ajouterRevenu(@AuthenticationPrincipal Membre membre,
                                @Valid OperationRequest requete, RedirectAttributes redirect) {
        operationService.ajouterRevenu(membre, requete);
        return retourTableauDeBord(redirect, "Revenu ajouté au suivi.");
    }

    @PostMapping(Routes.OPERATIONS + "/depense")
    public String ajouterDepense(@AuthenticationPrincipal Membre membre,
                                 @Valid OperationRequest requete, RedirectAttributes redirect) {
        operationService.ajouterDepense(membre, requete);
        return retourTableauDeBord(redirect, "Dépense ajoutée au suivi.");
    }

    @PostMapping(Routes.OPERATIONS + "/{id}/modifier")
    public String modifier(@AuthenticationPrincipal Membre membre, @PathVariable Long id,
                           @Valid OperationRequest requete, RedirectAttributes redirect) {
        operationService.modifier(membre, id, requete);
        return retourTableauDeBord(redirect, "Opération modifiée.");
    }

    @PostMapping(Routes.OPERATIONS + "/{id}/supprimer")
    public String supprimer(@AuthenticationPrincipal Membre membre, @PathVariable Long id,
                            RedirectAttributes redirect) {
        operationService.supprimer(membre, id);
        return retourTableauDeBord(redirect, "Opération supprimée.");
    }

    private static String retourTableauDeBord(RedirectAttributes redirect, String message) {
        redirect.addFlashAttribute(MessagesFlash.SUCCES, message);
        return Routes.rediriger(Routes.TABLEAU_DE_BORD);
    }
}
