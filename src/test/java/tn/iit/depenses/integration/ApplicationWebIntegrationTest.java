package tn.iit.depenses.integration;

import org.junit.jupiter.api.Test;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Depense;
import tn.iit.depenses.domain.Membre;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Pages, validation des formulaires (@Valid), erreurs centralisées et exports, de bout en bout. */
class ApplicationWebIntegrationTest extends IntegrationTestBase {

    @Test
    void le_tableau_de_bord_affiche_les_operations_et_leur_categorie() throws Exception {
        Membre ali = nouveauMembre("Ali");
        Categorie alimentation = categorieParDefaut("Alimentation");
        operations.save(new Depense(new BigDecimal("45.500"), LocalDate.of(2026, 11, 3), "Marche central", ali, alimentation));

        mvc.perform(get("/tableau-de-bord").param("mois", "2026-11").with(user(ali)))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Marche central")))
                .andExpect(content().string(containsString("Alimentation")));
    }

    @Test
    void un_mois_invalide_n_est_plus_remplace_en_silence() throws Exception {
        mvc.perform(get("/tableau-de-bord").param("mois", "abc").with(user(nouveauMembre("Ali"))))
                .andExpect(redirectedUrl("/tableau-de-bord"))
                .andExpect(flash().attribute("erreur", "Paramètre invalide : mois"));
    }

    @Test
    void ajouter_une_depense_valide() throws Exception {
        Membre ali = nouveauMembre("Ali");
        Categorie transport = categorieParDefaut("Transport");

        mvc.perform(post("/operations/depense").with(user(ali)).with(csrf())
                        .param("montant", "12.5").param("date", "2026-11-10")
                        .param("description", "Taxi").param("categorieId", transport.getId().toString()))
                .andExpect(redirectedUrl("/tableau-de-bord"))
                .andExpect(flash().attribute("succes", "Dépense ajoutée au suivi."));

        assertThat(operations.findTop20ByMembreOrderByDateDesc(ali))
                .singleElement()
                .satisfies(depense -> assertThat(depense.getMontant()).isEqualByComparingTo("12.5"));
    }

    @Test
    void un_montant_negatif_est_refuse_par_la_validation() throws Exception {
        Membre ali = nouveauMembre("Ali");

        mvc.perform(post("/operations/revenu").with(user(ali)).with(csrf())
                        .param("montant", "-5").param("date", "2026-11-10"))
                .andExpect(redirectedUrl("/tableau-de-bord"))
                .andExpect(flash().attribute("erreur", "Le montant doit être supérieur à zéro."));

        assertThat(operations.findTop20ByMembreOrderByDateDesc(ali)).isEmpty();
    }

    @Test
    void un_montant_avec_quatre_decimales_est_refuse() throws Exception {
        mvc.perform(post("/operations/revenu").with(user(nouveauMembre("Ali"))).with(csrf())
                        .param("montant", "1.2345").param("date", "2026-11-10"))
                .andExpect(flash().attribute("erreur",
                        "Le montant doit avoir au maximum trois chiffres après la virgule."));
    }

    @Test
    void une_date_manquante_est_refusee() throws Exception {
        mvc.perform(post("/operations/revenu").with(user(nouveauMembre("Ali"))).with(csrf())
                        .param("montant", "10"))
                .andExpect(flash().attribute("erreur", "La date est obligatoire."));
    }

    @Test
    void creer_deux_fois_la_meme_categorie_affiche_un_message_d_erreur() throws Exception {
        Membre ali = nouveauMembre("Ali");

        mvc.perform(post("/categories").with(user(ali)).with(csrf()).param("nom", "Voyages").param("type", "DEPENSE"))
                .andExpect(flash().attribute("succes", "Catégorie créée."));
        mvc.perform(post("/categories").with(user(ali)).with(csrf()).param("nom", "voyages").param("type", "DEPENSE"))
                .andExpect(redirectedUrl("/categories"))
                .andExpect(flash().attribute("erreur", "Une catégorie de ce nom existe déjà."));
    }

    @Test
    void la_page_des_categories_s_affiche() throws Exception {
        mvc.perform(get("/categories").with(user(nouveauMembre("Ali"))))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"));
    }

    @Test
    void inscription_avec_un_email_invalide() throws Exception {
        mvc.perform(post("/inscription").with(csrf())
                        .param("nom", "Ali").param("email", "pas-un-email").param("motDePasse", "motdepasse123"))
                .andExpect(redirectedUrl("/inscription"))
                .andExpect(flash().attribute("erreur", "L’adresse e-mail n’est pas valide."));
    }

    @Test
    void inscription_valide_puis_email_deja_utilise() throws Exception {
        String email = "nouveau-" + System.nanoTime() + "@test.tn";

        mvc.perform(post("/inscription").with(csrf())
                        .param("nom", "Nouveau").param("email", email).param("motDePasse", "motdepasse123"))
                .andExpect(redirectedUrl("/connexion"))
                .andExpect(flash().attribute("succes", "Votre compte est créé. Vous pouvez vous connecter."));
        mvc.perform(post("/inscription").with(csrf())
                        .param("nom", "Copie").param("email", email.toUpperCase()).param("motDePasse", "motdepasse123"))
                .andExpect(flash().attribute("erreur", "Cette adresse e-mail est déjà utilisée."));
    }

    @Test
    void export_pdf_du_mois() throws Exception {
        mvc.perform(get("/rapports/PDF").param("mois", "2026-11").with(user(nouveauMembre("Ali"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("rapport-2026-11.pdf")));
    }

    @Test
    void export_csv_du_mois() throws Exception {
        mvc.perform(get("/rapports/CSV").param("mois", "2026-11").with(user(nouveauMembre("Ali"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("rapport-2026-11.csv")));
    }

    @Test
    void un_format_de_rapport_inconnu_affiche_un_message() throws Exception {
        mvc.perform(get("/rapports/XLS").with(user(nouveauMembre("Ali"))))
                .andExpect(redirectedUrl("/tableau-de-bord"))
                .andExpect(flash().attribute("erreur", "Paramètre invalide : format"));
    }
}
