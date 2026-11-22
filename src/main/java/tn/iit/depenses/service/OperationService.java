package tn.iit.depenses.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Depense;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.domain.Revenu;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.exception.DonneeInvalideException;
import tn.iit.depenses.exception.OperationIntrouvableException;
import tn.iit.depenses.repository.OperationRepository;
import tn.iit.depenses.service.dto.OperationRequest;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
public class OperationService {
    private static final int DECIMALES_MONTANT = 3;

    private final OperationRepository operations;
    private final CategorieService categories;

    public OperationService(OperationRepository operations, CategorieService categories) {
        this.operations = operations;
        this.categories = categories;
    }

    @Transactional
    public Operation ajouterRevenu(Membre membre, OperationRequest requete) {
        Categorie categorie = requete.categorieId() == null ? null
                : categories.obtenirCategorieAccessible(membre, requete.categorieId(), TypeOperation.REVENU);
        return operations.save(new Revenu(montant(requete), requete.date(), description(requete), membre, categorie));
    }

    @Transactional
    public Operation ajouterDepense(Membre membre, OperationRequest requete) {
        Categorie categorie = categorieObligatoire(membre, requete.categorieId(), TypeOperation.DEPENSE);
        return operations.save(new Depense(montant(requete), requete.date(), description(requete), membre, categorie));
    }

    @Transactional
    public void modifier(Membre membre, Long id, OperationRequest requete) {
        Operation operation = obtenir(membre, id);
        Categorie categorie = (operation instanceof Depense || requete.categorieId() != null)
                ? categorieObligatoire(membre, requete.categorieId(), operation.getType())
                : null;
        operation.modifier(montant(requete), requete.date(), description(requete), categorie);
    }

    @Transactional
    public void supprimer(Membre membre, Long id) {
        operations.delete(obtenir(membre, id));
    }

    @Transactional(readOnly = true)
    public List<Operation> lister(Membre membre, YearMonth periode) {
        return operations.findByMembreAndDateBetweenOrderByDateDesc(membre, periode.atDay(1), periode.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public List<Operation> operationsRecentes(Membre membre) {
        return operations.findTop20ByMembreOrderByDateDesc(membre);
    }

    /** Ne renvoie que les opérations du membre connecté : celles des autres sont « introuvables ». */
    @Transactional(readOnly = true)
    public Operation obtenir(Membre membre, Long id) {
        return operations.findByIdAndMembre(id, membre).orElseThrow(OperationIntrouvableException::new);
    }

    private Categorie categorieObligatoire(Membre membre, Long categorieId, TypeOperation type) {
        if (categorieId == null) {
            throw new DonneeInvalideException("Choisissez une catégorie pour la dépense.");
        }
        return categories.obtenirCategorieAccessible(membre, categorieId, type);
    }

    /** @Digits sur OperationRequest garantit au plus trois décimales : le changement d'échelle est sans arrondi. */
    private static BigDecimal montant(OperationRequest requete) {
        return requete.montant().setScale(DECIMALES_MONTANT);
    }

    private static String description(OperationRequest requete) {
        return requete.description() == null ? "" : requete.description().trim();
    }
}
