package tn.iit.depenses.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Depense;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.TypeOperation;
import tn.iit.depenses.exception.ActionInterditeException;
import tn.iit.depenses.exception.CategorieDejaExistanteException;
import tn.iit.depenses.exception.CategorieIntrouvableException;
import tn.iit.depenses.exception.DonneeInvalideException;
import tn.iit.depenses.repository.CategorieRepository;
import tn.iit.depenses.repository.OperationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
class CategorieServiceTest {

    @Mock
    private CategorieRepository categories;
    @Mock
    private OperationRepository operations;
    @InjectMocks
    private CategorieService categorieService;

    private final Membre ali = membre(1L, "Ali");

    @Test
    void creerPourMembre_refuse_un_nom_deja_utilise() {
        when(categories.existsByMembreAndTypeAndNomIgnoreCase(ali, TypeOperation.DEPENSE, "Voyages")).thenReturn(true);

        assertThatThrownBy(() -> categorieService.creerPourMembre(ali, "  Voyages ", TypeOperation.DEPENSE))
                .isInstanceOf(CategorieDejaExistanteException.class);
        verify(categories, never()).save(any());
    }

    @Test
    void supprimerPourMembre_reaffecte_les_operations_vers_autres() {
        Categorie loisirs = categorie(5L, "Loisirs", TypeOperation.DEPENSE, ali);
        Depense cinema = new Depense(new BigDecimal("12.000"), LocalDate.now(), "Cinéma", ali, loisirs);
        when(categories.findByIdAndMembre(5L, ali)).thenReturn(Optional.of(loisirs));
        when(operations.findByCategorieAndMembre(loisirs, ali)).thenReturn(List.of(cinema));
        when(categories.findByMembreAndTypeAndNomIgnoreCase(ali, TypeOperation.DEPENSE, "Autres"))
                .thenReturn(Optional.empty());
        when(categories.save(any(Categorie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categorieService.supprimerPourMembre(ali, 5L);

        assertThat(cinema.getCategorie().getNom()).isEqualTo("Autres");
        assertThat(cinema.getCategorie().getMembre()).isSameAs(ali);
        verify(categories).delete(loisirs);
    }

    @Test
    void supprimerPourMembre_interdit_de_supprimer_la_categorie_de_reaffectation() {
        Categorie autres = categorie(6L, "Autres", TypeOperation.DEPENSE, ali);
        when(categories.findByIdAndMembre(6L, ali)).thenReturn(Optional.of(autres));

        assertThatThrownBy(() -> categorieService.supprimerPourMembre(ali, 6L))
                .isInstanceOf(ActionInterditeException.class);
        verify(categories, never()).delete(any());
    }

    @Test
    void obtenirCategorieAccessible_refuse_la_categorie_d_un_autre_membre() {
        when(categories.findByIdAndMembre(9L, ali)).thenReturn(Optional.empty());
        when(categories.findByIdAndMembreIsNullAndDefautTrue(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categorieService.obtenirCategorieAccessible(ali, 9L, TypeOperation.DEPENSE))
                .isInstanceOf(CategorieIntrouvableException.class);
    }

    @Test
    void obtenirCategorieAccessible_refuse_une_categorie_du_mauvais_type() {
        Categorie salaire = categorie(2L, "Salaire", TypeOperation.REVENU, null);
        when(categories.findByIdAndMembre(2L, ali)).thenReturn(Optional.empty());
        when(categories.findByIdAndMembreIsNullAndDefautTrue(2L)).thenReturn(Optional.of(salaire));

        assertThatThrownBy(() -> categorieService.obtenirCategorieAccessible(ali, 2L, TypeOperation.DEPENSE))
                .isInstanceOf(DonneeInvalideException.class);
    }
}
