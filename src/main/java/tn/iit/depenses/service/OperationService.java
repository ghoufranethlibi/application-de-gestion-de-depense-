package tn.iit.depenses.service;

import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Depense;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.domain.Revenu;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.repository.OperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class OperationService {
    private final OperationRepository operations;
    private final CategorieService categories;

    public OperationService(OperationRepository operations, CategorieService categories) {
        this.operations = operations;
        this.categories = categories;
    }

    @Transactional
    public Operation ajouterRevenu(Membre membre, BigDecimal montant, LocalDate date, String description, Long categorieId) {
        BigDecimal montantValide = validerMontant(montant);
        validerDate(date);
        Categorie categorie = categorieId == null ? null : categories.obtenirCategorieAccessible(membre, categorieId, TypeOperation.REVENU);
        return operations.save(new Revenu(montantValide, date, nettoyerDescription(description), membre, categorie));
    }

    @Transactional
    public Operation ajouterDepense(Membre membre, BigDecimal montant, LocalDate date, String description, Long categorieId) {
        BigDecimal montantValide = validerMontant(montant);
        validerDate(date);
        if (categorieId == null) throw new IllegalArgumentException("Choisissez une catégorie pour la dépense.");
        Categorie categorie = categories.obtenirCategorieAccessible(membre, categorieId, TypeOperation.DEPENSE);
        return operations.save(new Depense(montantValide, date, nettoyerDescription(description), membre, categorie));
    }

    @Transactional
    public void modifier(Membre membre, Long id, BigDecimal montant, LocalDate date, String description, Long categorieId) {
        Operation operation = obtenir(membre, id);
        BigDecimal montantValide = validerMontant(montant);
        validerDate(date);
        Categorie categorie = null;
        if (operation instanceof Depense || categorieId != null) {
            if (categorieId == null) throw new IllegalArgumentException("Choisissez une catégorie pour la dépense.");
            categorie = categories.obtenirCategorieAccessible(membre, categorieId, operation.getType());
        }
        operation.modifier(montantValide, date, nettoyerDescription(description), categorie);
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

    @Transactional(readOnly = true)
    public Operation obtenir(Membre membre, Long id) {
        return operations.findByIdAndMembre(id, membre)
                .orElseThrow(() -> new IllegalArgumentException("Opération introuvable ou non autorisée."));
    }

    private BigDecimal validerMontant(BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro.");
        }
        try {
            return montant.setScale(3, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Le montant doit avoir au maximum trois chiffres après la virgule.");
        }
    }

    private void validerDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("La date est obligatoire.");
    }

    private String nettoyerDescription(String description) {
        String valeur = description == null ? "" : description.trim();
        if (valeur.length() > 240) throw new IllegalArgumentException("La description est trop longue (240 caractères maximum).");
        return valeur;
    }
}
