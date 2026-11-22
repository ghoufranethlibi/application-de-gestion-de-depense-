package tn.iit.depenses.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.exception.EmailDejaUtiliseException;
import tn.iit.depenses.repository.MembreRepository;
import tn.iit.depenses.service.dto.InscriptionRequest;

import java.util.Locale;

@Service
public class AuthService {
    private final MembreRepository membres;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MembreRepository membres, PasswordEncoder passwordEncoder) {
        this.membres = membres;
        this.passwordEncoder = passwordEncoder;
    }

    /** Crée un compte MEMBRE. Le format des champs est déjà vérifié par @Valid sur InscriptionRequest. */
    @Transactional
    public Membre inscrire(InscriptionRequest demande) {
        String email = normaliserEmail(demande.email());
        if (membres.existsByEmailIgnoreCase(email)) {
            throw new EmailDejaUtiliseException();
        }
        String motDePasseHache = passwordEncoder.encode(demande.motDePasse());
        return membres.save(new Membre(demande.nom().trim(), email, motDePasseHache, Role.MEMBRE));
    }

    private static String normaliserEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
