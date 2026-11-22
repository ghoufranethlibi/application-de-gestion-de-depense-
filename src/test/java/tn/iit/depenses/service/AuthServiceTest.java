package tn.iit.depenses.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.exception.EmailDejaUtiliseException;
import tn.iit.depenses.repository.MembreRepository;
import tn.iit.depenses.service.dto.InscriptionRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MembreRepository membres;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;

    @Test
    void inscrire_normalise_l_email_hache_le_mot_de_passe_et_cree_un_membre() {
        when(membres.existsByEmailIgnoreCase("ali@exemple.tn")).thenReturn(false);
        when(passwordEncoder.encode("motdepasse123")).thenReturn("HASH");
        when(membres.save(any(Membre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Membre membre = authService.inscrire(new InscriptionRequest("  Ali  ", "  Ali@Exemple.TN ", "motdepasse123"));

        assertThat(membre.getNom()).isEqualTo("Ali");
        assertThat(membre.getEmail()).isEqualTo("ali@exemple.tn");
        assertThat(membre.getPassword()).isEqualTo("HASH");
        assertThat(membre.getRole()).isEqualTo(Role.MEMBRE);
    }

    @Test
    void inscrire_refuse_un_email_deja_utilise() {
        when(membres.existsByEmailIgnoreCase("ali@exemple.tn")).thenReturn(true);

        assertThatThrownBy(() -> authService.inscrire(new InscriptionRequest("Ali", "ali@exemple.tn", "motdepasse123")))
                .isInstanceOf(EmailDejaUtiliseException.class);
        verify(membres, never()).save(any());
    }
}
