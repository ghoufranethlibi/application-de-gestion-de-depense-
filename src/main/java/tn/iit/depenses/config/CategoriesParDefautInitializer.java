package tn.iit.depenses.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.repository.CategorieRepository;

import java.util.List;

/** Crée les catégories proposées à tous les membres si elles n'existent pas encore. */
@Component
public class CategoriesParDefautInitializer implements CommandLineRunner {

    private record CategorieParDefaut(String nom, TypeOperation type) { }

    private static final List<CategorieParDefaut> CATEGORIES_PAR_DEFAUT = List.of(
            new CategorieParDefaut("Alimentation", TypeOperation.DEPENSE),
            new CategorieParDefaut("Logement", TypeOperation.DEPENSE),
            new CategorieParDefaut("Transport", TypeOperation.DEPENSE),
            new CategorieParDefaut("Santé", TypeOperation.DEPENSE),
            new CategorieParDefaut("Loisirs", TypeOperation.DEPENSE),
            new CategorieParDefaut(TypeOperation.DEPENSE.getCategorieDeReaffectation(), TypeOperation.DEPENSE),
            new CategorieParDefaut("Salaire", TypeOperation.REVENU),
            new CategorieParDefaut(TypeOperation.REVENU.getCategorieDeReaffectation(), TypeOperation.REVENU));

    private final CategorieRepository categories;

    public CategoriesParDefautInitializer(CategorieRepository categories) {
        this.categories = categories;
    }

    @Override
    public void run(String... args) {
        for (CategorieParDefaut categorie : CATEGORIES_PAR_DEFAUT) {
            boolean existe = categories.existsByMembreIsNullAndDefautTrueAndTypeAndNomIgnoreCase(
                    categorie.type(), categorie.nom());
            if (!existe) {
                categories.save(new Categorie(categorie.nom(), categorie.type(), null, true));
            }
        }
    }
}
