package tn.iit.depenses.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.exception.ActionInterditeException;
import tn.iit.depenses.exception.CategorieDejaExistanteException;
import tn.iit.depenses.exception.CategorieIntrouvableException;
import tn.iit.depenses.exception.DonneeInvalideException;
import tn.iit.depenses.repository.CategorieRepository;
import tn.iit.depenses.repository.OperationRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class CategorieService {
    private final CategorieRepository categories;
    private final OperationRepository operations;

    public CategorieService(CategorieRepository categories, OperationRepository operations) {
        this.categories = categories;
        this.operations = operations;
    }

    // ----- Catégories visibles par un membre -----

    @Transactional(readOnly = true)
    public List<Categorie> listerPourMembre(Membre membre, TypeOperation type) {
        return Stream.concat(
                        categories.findByMembreIsNullAndDefautTrueAndTypeOrderByNomAsc(type).stream(),
                        categories.findByMembreAndTypeOrderByNomAsc(membre, type).stream())
                .sorted(Comparator.comparing(Categorie::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Categorie> listerPersonnelles(Membre membre) {
        return categories.findByMembreOrderByTypeAscNomAsc(membre);
    }

    @Transactional(readOnly = true)
    public Categorie obtenirCategorieAccessible(Membre membre, Long id, TypeOperation type) {
        Categorie categorie = categories.findByIdAndMembre(id, membre)
                .or(() -> categories.findByIdAndMembreIsNullAndDefautTrue(id))
                .orElseThrow(CategorieIntrouvableException::new);
        if (categorie.getType() != type) {
            throw new DonneeInvalideException("Cette catégorie ne correspond pas au type d’opération.");
        }
        return categorie;
    }

    // ----- Catégories personnelles -----

    @Transactional
    public Categorie creerPourMembre(Membre membre, String nom, TypeOperation type) {
        String nomPropre = nom.trim();
        if (categories.existsByMembreAndTypeAndNomIgnoreCase(membre, type, nomPropre)) {
            throw new CategorieDejaExistanteException();
        }
        return categories.save(new Categorie(nomPropre, type, membre, false));
    }

    @Transactional
    public void renommerPourMembre(Membre membre, Long id, String nouveauNom) {
        Categorie categorie = categories.findByIdAndMembre(id, membre)
                .orElseThrow(CategorieIntrouvableException::new);
        String nomPropre = nouveauNom.trim();
        boolean nomDejaPris = !categorie.getNom().equalsIgnoreCase(nomPropre)
                && categories.existsByMembreAndTypeAndNomIgnoreCase(membre, categorie.getType(), nomPropre);
        if (nomDejaPris) {
            throw new CategorieDejaExistanteException();
        }
        categorie.setNom(nomPropre);
    }

    @Transactional
    public void supprimerPourMembre(Membre membre, Long id) {
        Categorie categorie = categories.findByIdAndMembre(id, membre)
                .orElseThrow(CategorieIntrouvableException::new);
        if (estCategorieDeReaffectation(categorie)) {
            throw new ActionInterditeException("La catégorie « " + categorie.getNom()
                    + " » reçoit les opérations des catégories supprimées : elle ne peut pas être supprimée.");
        }
        reaffecterPuisSupprimer(categorie, operations.findByCategorieAndMembre(categorie, membre));
    }

    // ----- Catégories par défaut (administration) -----

    @Transactional(readOnly = true)
    public List<Categorie> listerCategoriesParDefaut() {
        return categories.findByMembreIsNullAndDefautTrueOrderByTypeAscNomAsc();
    }

    @Transactional
    public Categorie creerCategorieParDefaut(String nom, TypeOperation type) {
        String nomPropre = nom.trim();
        if (categories.existsByMembreIsNullAndDefautTrueAndTypeAndNomIgnoreCase(type, nomPropre)) {
            throw new CategorieDejaExistanteException();
        }
        return categories.save(new Categorie(nomPropre, type, null, true));
    }

    @Transactional
    public void renommerCategorieParDefaut(Long id, String nouveauNom) {
        Categorie categorie = categories.findByIdAndMembreIsNullAndDefautTrue(id)
                .orElseThrow(CategorieIntrouvableException::new);
        String nomPropre = nouveauNom.trim();
        boolean nomDejaPris = !categorie.getNom().equalsIgnoreCase(nomPropre)
                && categories.existsByMembreIsNullAndDefautTrueAndTypeAndNomIgnoreCase(categorie.getType(), nomPropre);
        if (nomDejaPris) {
            throw new CategorieDejaExistanteException();
        }
        categorie.setNom(nomPropre);
    }

    @Transactional
    public void supprimerCategorieParDefaut(Long id) {
        Categorie categorie = categories.findByIdAndMembreIsNullAndDefautTrue(id)
                .orElseThrow(CategorieIntrouvableException::new);
        reaffecterPuisSupprimer(categorie, operations.findByCategorie(categorie));
    }

    // ----- Réaffectation commune aux deux suppressions -----

    /** Déplace chaque opération vers la catégorie « Autres » de son membre, puis supprime la catégorie. */
    private void reaffecterPuisSupprimer(Categorie categorie, List<Operation> rattachees) {
        Map<Long, Categorie> reaffectationParMembre = new HashMap<>();
        for (Operation operation : rattachees) {
            Membre proprietaire = operation.getMembre();
            Categorie cible = reaffectationParMembre.computeIfAbsent(proprietaire.getId(),
                    idMembre -> categorieDeReaffectation(proprietaire, categorie.getType()));
            operation.changerCategorie(cible);
        }
        categories.delete(categorie);
    }

    private Categorie categorieDeReaffectation(Membre membre, TypeOperation type) {
        String nom = type.getCategorieDeReaffectation();
        return categories.findByMembreAndTypeAndNomIgnoreCase(membre, type, nom)
                .orElseGet(() -> categories.save(new Categorie(nom, type, membre, false)));
    }

    private static boolean estCategorieDeReaffectation(Categorie categorie) {
        return categorie.getNom().equalsIgnoreCase(categorie.getType().getCategorieDeReaffectation());
    }
}
