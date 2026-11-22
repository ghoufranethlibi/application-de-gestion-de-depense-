package tn.iit.depenses.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.repository.CategorieRepository;
import tn.iit.depenses.repository.MembreRepository;
import tn.iit.depenses.repository.OperationRepository;

import java.util.UUID;

/**
 * Base commune des tests d'intégration : application complète + base H2 en mémoire (profil « test »).
 * Une seule configuration pour que Spring réutilise le même contexte entre les classes de test.
 * Chaque test crée ses propres membres (e-mails uniques) : les tests restent indépendants.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected MembreRepository membres;
    @Autowired
    protected CategorieRepository categories;
    @Autowired
    protected OperationRepository operations;

    protected Membre nouveauMembre(String nom) {
        String email = nom.toLowerCase() + "-" + UUID.randomUUID() + "@test.tn";
        return membres.save(new Membre(nom, email, "hash-inutilise", Role.MEMBRE));
    }

    protected Membre nouvelAdministrateur() {
        return membres.save(new Membre("Admin", "admin-" + UUID.randomUUID() + "@test.tn", "hash", Role.ADMIN));
    }

    /** Catégorie par défaut créée au démarrage par CategoriesParDefautInitializer. */
    protected Categorie categorieParDefaut(String nom) {
        return categories.findByMembreIsNullAndDefautTrueAndTypeOrderByNomAsc(TypeOperation.DEPENSE).stream()
                .filter(categorie -> categorie.getNom().equals(nom))
                .findFirst()
                .orElseThrow();
    }
}
