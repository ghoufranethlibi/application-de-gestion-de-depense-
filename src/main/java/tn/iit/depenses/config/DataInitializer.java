package tn.iit.depenses.config;

import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.repository.CategorieRepository;
import tn.iit.depenses.repository.MembreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initialiserDonnees(MembreRepository membres, CategorieRepository categories,
                                         PasswordEncoder passwordEncoder,
                                         @Value("${APP_ADMIN_EMAIL:admin@budget.local}") String adminEmail,
                                         @Value("${APP_ADMIN_PASSWORD:Admin123!}") String adminPassword) {
        return args -> {
            if (!membres.existsByEmailIgnoreCase(adminEmail)) {
                membres.save(new Membre("Administrateur", adminEmail.toLowerCase(),
                        passwordEncoder.encode(adminPassword), Role.ADMIN));
            }
            ajouterCategorie(categories, "Alimentation", TypeOperation.DEPENSE);
            ajouterCategorie(categories, "Logement", TypeOperation.DEPENSE);
            ajouterCategorie(categories, "Transport", TypeOperation.DEPENSE);
            ajouterCategorie(categories, "Santé", TypeOperation.DEPENSE);
            ajouterCategorie(categories, "Loisirs", TypeOperation.DEPENSE);
            ajouterCategorie(categories, "Autres", TypeOperation.DEPENSE);
            ajouterCategorie(categories, "Salaire", TypeOperation.REVENU);
            ajouterCategorie(categories, "Autres revenus", TypeOperation.REVENU);
        };
    }

    private void ajouterCategorie(CategorieRepository categories, String nom, TypeOperation type) {
        if (!categories.existsByMembreIsNullAndDefautTrueAndTypeAndNomIgnoreCase(type, nom)) {
            categories.save(new Categorie(nom, type, null, true));
        }
    }
}
