package tn.iit.depenses.service.export;

import org.springframework.stereotype.Component;
import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import tn.iit.depenses.service.dto.TotalMensuel;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class CsvExport implements ExportStrategy {
    private static final String BOM_UTF8 = "﻿"; // permet à Excel de lire les accents
    private static final String FIN_DE_LIGNE = "\r\n";
    private static final char SEPARATEUR = ';';

    @Override
    public FormatRapport format() {
        return FormatRapport.CSV;
    }

    @Override
    public byte[] exporter(DonneesRapport donnees) {
        TotalMensuel total = donnees.total();
        StringBuilder csv = new StringBuilder(BOM_UTF8);
        ligne(csv, "Rapport mensuel", "Gestionnaire de dépenses");
        ligne(csv, "Membre", cellule(donnees.nomMembre()));
        ligne(csv, "Période", total.libelle());
        ligne(csv, "Généré le", donnees.genereLe().toLocalDate().toString());
        csv.append(FIN_DE_LIGNE);
        ligne(csv, "Total revenus", "Total dépenses", "Solde");
        ligne(csv, total.totalRevenus().toPlainString(), total.totalDepenses().toPlainString(),
                total.solde().toPlainString());
        csv.append(FIN_DE_LIGNE);
        ligne(csv, "Répartition des dépenses");
        ligne(csv, "Catégorie", "Montant", "Pourcentage");
        for (RepartitionCategorie repartition : donnees.repartition()) {
            ligne(csv, cellule(repartition.nom()), repartition.montant().toPlainString(),
                    String.format(Locale.FRANCE, "%.2f%%", repartition.pourcentage()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void ligne(StringBuilder csv, String... valeurs) {
        csv.append(String.join(String.valueOf(SEPARATEUR), valeurs)).append(FIN_DE_LIGNE);
    }

    /** Met une valeur saisie par l'utilisateur entre guillemets (évite de casser les colonnes). */
    private static String cellule(String valeur) {
        String propre = valeur == null ? "" : valeur;
        return "\"" + propre.replace("\"", "\"\"") + "\"";
    }

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public String typeContenu() {
        return "text/csv; charset=UTF-8";
    }
}
