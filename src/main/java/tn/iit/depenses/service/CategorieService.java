package tn.iit.depenses.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.exception.CategorieDejaExistanteException;
import tn.iit.depenses.exception.CategorieIntrouvableException;
import tn.iit.depenses.exception.DonneeInvalideException;
import tn.iit.depenses.repository.CategorieRepository;
import tn.iit.depenses.repository.OperationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CategorieService {
    private final CategorieRepository categories;
    private final OperationRepository operations;

    public CategorieService(CategorieRepository categories, OperationRepository operations) {
        this.categories = categories;
        this.operations = operations;
    }

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

    @Transactional
    public Categorie creerPourMembre(Membre membre, String nom, TypeOperation type) {
        String propre = nommer(nom);
        if (categories.existsByMembreAndTypeAndNomIgnoreCase(membre, type, propre)) {
            throw new CategorieDejaExistanteException();
        }
        return categories.save(new Categorie(propre, type, membre, false));
    }

    @Transactional
    public void renommerPourMembre(Membre membre, Long id, String nouveauNom) {
        Categorie categorie = categories.findByIdAndMembre(id, membre)
                .orElseThrow(CategorieIntrouvableException::new);
        String propre = nommer(nouveauNom);
        if (categories.existsByMembreAndTypeAndNomIgnoreCase(membre, categorie.getType(), propre)
                && !categorie.getNom().equalsIgnoreCase(propre)) {
            throw new CategorieDejaExistanteException();
        }
        categorie.setNom(propre);
    }

    @Transactional
    public void supprimerPourMembre(Membre membre, Long id) {
        Categorie categorie = categories.findByIdAndMembre(id, membre)
                .orElseThrow(CategorieIntrouvableException::new);
        List<Operation> rattachees = operations.findByCategorieAndMembre(categorie, membre);
        Categorie reaffectation = categorieDeReaffectation(membre, categorie.getType());
        for (Operation operation : rattachees) {
            operation.modifier(operation.getMontant(), operation.getDate(), operation.getDescription(), reaffectation);
        }
        categories.delete(categorie);
    }

    @Transactional(readOnly = true)
    public List<Categorie> listerCategoriesParDefaut() {
        return categories.findByMembreIsNullAndDefautTrueOrderByTypeAscNomAsc();
    }

    @Transactional
    public Categorie creerCategorieParDefaut(String nom, TypeOperation type) {
        String propre = nommer(nom);
        if (categories.existsByMembreIsNullAndDefautTrueAndTypeAndNomIgnoreCase(type, propre)) {
            throw new CategorieDejaExistanteException();
        }
        return categories.save(new Categorie(propre, type, null, true));
    }

    @Transactional
    public void renommerCategorieParDefaut(Long id, String nouveauNom) {
        Categorie categorie = categories.findByIdAndMembreIsNullAndDefautTrue(id)
                .orElseThrow(CategorieIntrouvableException::new);
        String propre = nommer(nouveauNom);
        if (categories.existsByMembreIsNullAndDefautTrueAndTypeAndNomIgnoreCase(categorie.getType(), propre)
                && !categorie.getNom().equalsIgnoreCase(propre)) {
            throw new CategorieDejaExistanteException();
        }
        categorie.setNom(propre);
    }

    @Transactional
    public void supprimerCategorieParDefaut(Long id) {
        Categorie categorie = categories.findByIdAndMembreIsNullAndDefautTrue(id)
                .orElseThrow(CategorieIntrouvableException::new);
        List<Operation> rattachees = operations.findByCategorie(categorie);
        for (Operation operation : rattachees) {
            Categorie reaffectation = categorieDeReaffectation(operation.getMembre(), categorie.getType());
            operation.modifier(operation.getMontant(), operation.getDate(), operation.getDescription(), reaffectation);
        }
        categories.delete(categorie);
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

    private Categorie categorieDeReaffectation(Membre membre, TypeOperation type) {
        String nom = type == TypeOperation.DEPENSE ? "Autres" : "Autres revenus";
        return categories.findByMembreAndTypeAndNomIgnoreCase(membre, type, nom)
                .orElseGet(() -> categories.save(new Categorie(nom, type, membre, false)));
    }

    private String nommer(String nom) {
        String propre = nom == null ? "" : nom.trim();
        if (propre.isBlank() || propre.length() > 80) {
            throw new DonneeInvalideException("Le nom doit contenir entre 1 et 80 caractères.");
        }
        return propre;
    }
}