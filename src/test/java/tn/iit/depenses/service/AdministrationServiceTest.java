package tn.iit.depenses.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.exception.ActionInterditeException;
import tn.iit.depenses.exception.CompteIntrouvableException;
import tn.iit.depenses.repository.MembreRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static tn.iit.depenses.support.Fabrique.administrateur;
import static tn.iit.depenses.support.Fabrique.membre;

@ExtendWith(MockitoExtension.class)
class AdministrationServiceTest {

    @Mock
    private MembreRepository membres;
    @InjectMocks
    private AdministrationService administrationService;

    private final Membre admin = administrateur(1L);

    @Test
    void inverserEtat_desactive_puis_reactive_un_membre() {
        Membre sara = membre(2L, "Sara");
        when(membres.findById(2L)).thenReturn(Optional.of(sara));

        administrationService.inverserEtat(2L, admin);
        assertThat(sara.isActif()).isFalse();

        administrationService.inverserEtat(2L, admin);
        assertThat(sara.isActif()).isTrue();
    }

    @Test
    void un_compte_administrateur_est_protege() {
        Membre autreAdmin = administrateur(3L);
        when(membres.findById(3L)).thenReturn(Optional.of(autreAdmin));

        assertThatThrownBy(() -> administrationService.inverserEtat(3L, admin))
                .isInstanceOf(ActionInterditeException.class);
        assertThat(autreAdmin.isActif()).isTrue();
    }

    @Test
    void compte_inconnu() {
        when(membres.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> administrationService.inverserEtat(42L, admin))
                .isInstanceOf(CompteIntrouvableException.class);
    }
}
