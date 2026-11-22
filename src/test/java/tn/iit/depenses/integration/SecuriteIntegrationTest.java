package tn.iit.depenses.integration;

import org.junit.jupiter.api.Test;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.domain.Revenu;
import tn.iit.depenses.domain.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Les données d'un membre sont isolées des autres membres, et l'administration est réservée au rôle ADMIN. */
class SecuriteIntegrationTest extends IntegrationTestBase {

    @Test
    void un_visiteur_non_connecte_est_redirige_vers_la_connexion() throws Exception {
        mvc.perform(get("/tableau-de-bord"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/connexion")));
    }

    @Test
    void l_administrateur_initial_peut_se_connecter_avec_le_mot_de_passe_configure() throws Exception {
        mvc.perform(formLogin("/connexion").user("admin@test.local").password("Admin-test-123"))
                .andExpect(authenticated().withRoles("ADMIN"));
    }

    @Test
    void un_mauvais_mot_de_passe_est_refuse() throws Exception {
        mvc.perform(formLogin("/connexion").user("admin@test.local").password("mauvais"))
                .andExpect(unauthenticated());
    }

    @Test
    void un_membre_n_accede_pas_a_l_administration() throws Exception {
        Membre ali = nouveauMembre("Ali");

        mvc.perform(get("/administration").with(user(ali)))
                .andExpect(status().isForbidden());
    }

    @Test
    void un_administrateur_accede_a_l_administration() throws Exception {
        mvc.perform(get("/administration").with(user(nouvelAdministrateur())))
                .andExpect(status().isOk())
                .andExpect(view().name("administration"));
    }

    @Test
    void un_membre_ne_peut_pas_supprimer_l_operation_d_un_autre_membre() throws Exception {
        Membre ali = nouveauMembre("Ali");
        Operation operationDeSara = operations.save(
                new Revenu(new BigDecimal("100.000"), LocalDate.now(), "Salaire", nouveauMembre("Sara"), null));

        mvc.perform(post("/operations/{id}/supprimer", operationDeSara.getId()).with(user(ali)).with(csrf()))
                .andExpect(redirectedUrl("/tableau-de-bord"))
                .andExpect(flash().attribute("erreur", "Opération introuvable ou non autorisée."));

        assertThat(operations.findById(operationDeSara.getId())).isPresent();
    }

    @Test
    void un_membre_ne_peut_pas_modifier_l_operation_d_un_autre_membre() throws Exception {
        Membre ali = nouveauMembre("Ali");
        Operation operationDeSara = operations.save(
                new Revenu(new BigDecimal("100.000"), LocalDate.now(), "Salaire", nouveauMembre("Sara"), null));

        mvc.perform(post("/operations/{id}/modifier", operationDeSara.getId()).with(user(ali)).with(csrf())
                        .param("montant", "1").param("date", "2026-11-10"))
                .andExpect(flash().attribute("erreur", "Opération introuvable ou non autorisée."));

        assertThat(operations.findById(operationDeSara.getId()).orElseThrow().getMontant())
                .isEqualByComparingTo("100");
    }

    @Test
    void un_membre_ne_peut_pas_utiliser_la_categorie_personnelle_d_un_autre_membre() throws Exception {
        Membre ali = nouveauMembre("Ali");
        Categorie voyagesDeSara = categories.save(
                new Categorie("Voyages", TypeOperation.DEPENSE, nouveauMembre("Sara"), false));

        mvc.perform(post("/operations/depense").with(user(ali)).with(csrf())
                        .param("montant", "10").param("date", "2026-11-10")
                        .param("categorieId", voyagesDeSara.getId().toString()))
                .andExpect(flash().attribute("erreur", "Catégorie introuvable ou non autorisée."));
    }

    @Test
    void un_formulaire_sans_jeton_csrf_est_refuse() throws Exception {
        mvc.perform(post("/operations/revenu").with(user(nouveauMembre("Ali")))
                        .param("montant", "10").param("date", "2026-11-10"))
                .andExpect(status().isForbidden());
    }
}
