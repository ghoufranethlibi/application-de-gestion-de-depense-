package tn.iit.depenses.service.export;

import org.junit.jupiter.api.Test;
import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import tn.iit.depenses.service.dto.TotalMensuel;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportsTest {
    private static final YearMonth NOVEMBRE = YearMonth.of(2026, 11);

    private final DonneesRapport donnees = new DonneesRapport(
            new TotalMensuel(NOVEMBRE, new BigDecimal("2000.000"), new BigDecimal("457.000"), new BigDecimal("1543.000")),
            List.of(new RepartitionCategorie("Logement", new BigDecimal("457.000"), 100.0)),
            LocalDateTime.of(2026, 11, 22, 10, 0),
            "Ali (test)");

    @Test
    void le_csv_contient_les_totaux_et_la_repartition() {
        CsvExport csv = new CsvExport();

        String contenu = new String(csv.exporter(donnees), StandardCharsets.UTF_8);

        assertThat(csv.format()).isEqualTo(FormatRapport.CSV);
        assertThat(contenu).startsWith("﻿");
        assertThat(contenu).contains("Total revenus;Total dépenses;Solde");
        assertThat(contenu).contains("2000.000;457.000;1543.000");
        assertThat(contenu).contains("\"Logement\";457.000;100,00%");
    }

    @Test
    void le_pdf_est_un_document_pdf_valide_avec_le_contenu_du_rapport() {
        PdfExport pdf = new PdfExport(new GenerateurPdf());

        String contenu = new String(pdf.exporter(donnees), StandardCharsets.US_ASCII);

        assertThat(pdf.format()).isEqualTo(FormatRapport.PDF);
        assertThat(contenu).startsWith("%PDF-1.4");
        assertThat(contenu).endsWith("%%EOF");
        assertThat(contenu).contains("(GESTIONNAIRE DE DEPENSES PERSONNELLES) Tj");
        // les parenthèses du nom sont échappées pour ne pas casser le PDF
        assertThat(contenu).contains("Ali \\(test\\)");
    }

    @Test
    void le_generateur_pdf_cree_une_seconde_page_au_dela_de_29_lignes() {
        List<String> lignes = java.util.stream.IntStream.rangeClosed(1, 40).mapToObj(i -> "Ligne " + i).toList();

        String contenu = new String(new GenerateurPdf().generer(lignes), StandardCharsets.US_ASCII);

        assertThat(contenu).contains("/Count 2");
    }
}
