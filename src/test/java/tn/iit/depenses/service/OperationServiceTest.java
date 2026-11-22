package tn.iit.depenses.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Depense;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.domain.Revenu;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.exception.DonneeInvalideException;
import tn.iit.depenses.exception.OperationIntrouvableException;
import tn.iit.depenses.repository.OperationRepository;
import tn.iit.depenses.service.dto.OperationRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tn.iit.depenses.support.Fabrique.categorie;
import static tn.iit.depenses.support.Fabrique.membre;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 15);

    @Mock
    private OperationRepository operations;
    @Mock
    private CategorieService categories;
    @InjectMocks
    private OperationService operationService;

    private final Membre ali = membre(1L, "Ali");

    @Test
    void ajouterRevenu_sans_categorie_enregistre_le_montant_avec_trois_decimales() {
        when(operations.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation revenu = operationService.ajouterRevenu(ali,
                new OperationRequest(new BigDecimal("1500.5"), DATE, "  Salaire  ", null));

        assertThat(revenu).isInstanceOf(Revenu.class);
        assertThat(revenu.getMontant()).isEqualTo(new BigDecimal("1500.500"));
        assertThat(revenu.getDescription()).isEqualTo("Salaire");
        assertThat(revenu.getCategorie()).isNull();
    }

    @Test
    void ajouterDepense_exige_une_categorie() {
        OperationRequest sansCategorie = new OperationRequest(BigDecimal.TEN, DATE, "Courses", null);

        assertThatThrownBy(() -> operationService.ajouterDepense(ali, sansCategorie))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessage("Choisissez une catégorie pour la dépense.");
        verify(operations, never()).save(any());
    }

    @Test
    void ajouterDepense_utilise_une_categorie_accessible_au_membre() {
        Categorie alimentation = categorie(7L, "Alimentation", TypeOperation.DEPENSE, null);
        when(categories.obtenirCategorieAccessible(ali, 7L, TypeOperation.DEPENSE)).thenReturn(alimentation);
        when(operations.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation depense = operationService.ajouterDepense(ali,
                new OperationRequest(new BigDecimal("42"), DATE, null, 7L));

        assertThat(depense).isInstanceOf(Depense.class);
        assertThat(depense.getCategorie()).isSameAs(alimentation);
        assertThat(depense.getDescription()).isEmpty();
    }

    @Test
    void un_membre_ne_peut_pas_obtenir_l_operation_d_un_autre_membre() {
        when(operations.findByIdAndMembre(99L, ali)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationService.supprimer(ali, 99L))
                .isInstanceOf(OperationIntrouvableException.class);
        verify(operations, never()).delete(any());
    }

    @Test
    void modifier_met_a_jour_l_operation_du_membre() {
        Revenu revenu = new Revenu(new BigDecimal("10.000"), DATE, "Ancien", ali, null);
        when(operations.findByIdAndMembre(3L, ali)).thenReturn(Optional.of(revenu));

        operationService.modifier(ali, 3L, new OperationRequest(new BigDecimal("25.25"), DATE.plusDays(1), "Nouveau", null));

        assertThat(revenu.getMontant()).isEqualTo(new BigDecimal("25.250"));
        assertThat(revenu.getDate()).isEqualTo(DATE.plusDays(1));
        assertThat(revenu.getDescription()).isEqualTo("Nouveau");
    }
}
