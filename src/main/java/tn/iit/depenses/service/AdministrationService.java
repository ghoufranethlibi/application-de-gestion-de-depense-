package tn.iit.depenses.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.exception.ActionInterditeException;
import tn.iit.depenses.exception.CompteIntrouvableException;
import tn.iit.depenses.repository.MembreRepository;

import java.util.List;

@Service
public class AdministrationService {
    private final MembreRepository membres;

    public AdministrationService(MembreRepository membres) {
        this.membres = membres;
    }

    @Transactional(readOnly = true)
    public List<Membre> listerMembres() {
        return membres.findAllByOrderByNomAsc();
    }

    @Transactional
    public void inverserEtat(Long id, Membre administrateur) {
        Membre cible = membres.findById(id).orElseThrow(CompteIntrouvableException::new);
        if (cible.isAdmin()) {
            throw new ActionInterditeException("Un compte administrateur ne peut pas être désactivé depuis cet écran.");
        }
        if (cible.getId().equals(administrateur.getId())) {
            throw new ActionInterditeException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        cible.setActif(!cible.isActif());
    }
}
