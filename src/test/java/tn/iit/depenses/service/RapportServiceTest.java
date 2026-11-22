package tn.iit.depenses.service;

import org.junit.jupiter.api.Test;
import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.exception.FormatNonSupporteException;
import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.FichierRapport;
import tn.iit.depenses.service.dto.TotalMensuel;
import tn.iit.depenses.service.export.ExportStrategy;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tn.iit.depenses.support.Fabrique.membre;

/** Vérifie le registre de stratégies : le bon export est choisi selon le format, sans ternaire. */
class RapportServiceTest {
    private static final YearMonth NOVEMBRE = YearMonth.of(2026, 11);

    private final BilanService bilanService = mock(BilanService.class);
    private final Membre ali = membre(1L, "Ali");

    /** Stratégie de test : écrit simplement le nom du format. */
    private record ExportFactice(FormatRapport format) implements ExportStrategy {
        @Override
        public byte[] exporter(DonneesRapport donnees) {
            return format.name().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String extension() {
            return format.name().toLowerCase();
        }

        @Override
        public String typeContenu() {
            return "test/" + extension();
        }
    }

    @Test
    void generer_utilise_la_strategie_du_format_demande() {
        when(bilanService.calculerTotalMensuel(ali, NOVEMBRE))
                .thenReturn(new TotalMensuel(NOVEMBRE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE));
        when(bilanService.repartitionParCategorie(ali, NOVEMBRE)).thenReturn(List.of());
        RapportService service = new RapportService(bilanService,
                List.of(new ExportFactice(FormatRapport.CSV), new ExportFactice(FormatRapport.PDF)));

        FichierRapport fichier = service.generer(ali, NOVEMBRE, FormatRapport.PDF);

        assertThat(new String(fichier.contenu(), StandardCharsets.UTF_8)).isEqualTo("PDF");
        assertThat(fichier.nomFichier()).isEqualTo("rapport-2026-11.pdf");
        assertThat(fichier.typeContenu()).isEqualTo("test/pdf");
    }

    @Test
    void un_format_sans_strategie_est_refuse() {
        RapportService service = new RapportService(bilanService, List.of(new ExportFactice(FormatRapport.CSV)));

        assertThatThrownBy(() -> service.generer(ali, NOVEMBRE, FormatRapport.PDF))
                .isInstanceOf(FormatNonSupporteException.class);
    }

    @Test
    void deux_strategies_pour_le_meme_format_sont_detectees_au_demarrage() {
        List<ExportStrategy> doublon = List.of(new ExportFactice(FormatRapport.CSV), new ExportFactice(FormatRapport.CSV));

        assertThatThrownBy(() -> new RapportService(bilanService, doublon))
                .isInstanceOf(IllegalStateException.class);
    }
}
