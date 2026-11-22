package tn.iit.depenses.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.service.dto.TableauDeBord;

import java.time.YearMonth;

/**
 * Façade du tableau de bord : un seul point d'entrée pour le contrôleur,
 * qui ne dépend plus directement de trois services (couplage réduit).
 */
@Service
public class TableauDeBordFacade {
    private final BilanService bilanService;
    private final OperationService operationService;
    private final CategorieService categorieService;

    public TableauDeBordFacade(BilanService bilanService, OperationService operationService,
                               CategorieService categorieService) {
        this.bilanService = bilanService;
        this.operationService = operationService;
        this.categorieService = categorieService;
    }

    @Transactional(readOnly = true)
    public TableauDeBord construire(Membre membre, YearMonth periode) {
        return new TableauDeBord(
                bilanService.calculerTotalMensuel(membre, periode),
                bilanService.repartitionParCategorie(membre, periode),
                bilanService.evolutionMensuelle(membre, periode),
                operationService.lister(membre, periode),
                categorieService.listerPourMembre(membre, TypeOperation.DEPENSE),
                categorieService.listerPourMembre(membre, TypeOperation.REVENU));
    }
}
