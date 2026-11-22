package tn.iit.depenses.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.exception.DonneeInvalideException;
import tn.iit.depenses.exception.EmailDejaUtiliseException;
import tn.iit.depenses.repository.MembreRepository;

@Service
public class AuthService {
    private final MembreRepository membres;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MembreRepository membres, PasswordEncoder passwordEncoder) {
        this.membres = membres;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Membre inscrire(String nom, String email, String motDePasse) {
        String nomNettoye = nom == null ? "" : nom.trim();
        String emailNettoye = email == null ? "" : email.trim().toLowerCase();
        if (nomNettoye.isBlank() || emailNettoye.isBlank()) {
            throw new DonneeInvalideException("Le nom et l’adresse e-mail sont obligatoires.");
        }
        if (motDePasse == null || motDePasse.length() < 8) {
            throw new DonneeInvalideException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        if (membres.existsByEmailIgnoreCase(emailNettoye)) {
            throw new EmailDejaUtiliseException();
        }
        return membres.save(new Membre(nomNettoye, emailNettoye, passwordEncoder.encode(motDePasse), Role.MEMBRE));
    }
}