package tn.iit.depenses.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.repository.MembreRepository;

import java.util.Locale;

/**
 * Crée le compte administrateur au premier démarrage.
 * Aucun mot de passe par défaut hors profil « dev » : il doit venir de la variable APP_ADMIN_PASSWORD.
 */
@Component
public class AdminInitializer implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(AdminInitializer.class);

    private final MembreRepository membres;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String motDePasse;

    public AdminInitializer(MembreRepository membres, PasswordEncoder passwordEncoder,
                            @Value("${app.admin.email}") String email,
                            @Value("${app.admin.mot-de-passe:}") String motDePasse) {
        this.membres = membres;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.motDePasse = motDePasse;
    }

    @Override
    public void run(String... args) {
        if (membres.existsByEmailIgnoreCase(email)) {
            return;
        }
        if (motDePasse.isBlank()) {
            throw new IllegalStateException(
                    "Aucun compte administrateur : définissez la variable d'environnement APP_ADMIN_PASSWORD.");
        }
        membres.save(new Membre("Administrateur", email, passwordEncoder.encode(motDePasse), Role.ADMIN));
        LOG.info("Compte administrateur créé : {}", email);
    }
}
