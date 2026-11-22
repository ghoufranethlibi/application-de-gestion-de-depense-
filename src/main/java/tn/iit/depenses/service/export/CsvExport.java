package tn.iit.depenses.service.export;

import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class CsvExport implements ExportStrategy {
    @Override
    public byte[] exporter(DonneesRapport donnees) {
        StringBuilder csv = new StringBuilder("\uFEFF" );
        csv.append("Rapport mensuel;Gestionnaire de dépenses\r\n");
        csv.append("Membre;").append(cellule(donnees.nomMembre())).append("\r\n");
        csv.append("Période;").append(donnees.total().libelle()).append("\r\n");
        csv.append("Généré le;").append(donnees.genereLe().toLocalDate()).append("\r\n\r\n");
        csv.append("Total revenus;Total dépenses;Solde\r\n")
                .append(donnees.total().totalRevenus().toPlainString()).append(';')
                .append(donnees.total().totalDepenses().toPlainString()).append(';')
                .append(donnees.total().solde().toPlainString()).append("\r\n\r\n");
        csv.append("Répartition des dépenses\r\nCatégorie;Montant;Pourcentage\r\n");
        for (RepartitionCategorie ligne : donnees.repartition()) {
            csv.append(cellule(ligne.nom())).append(';').append(ligne.montant().toPlainString()).append(';')
                    .append(String.format(java.util.Locale.FRANCE, "%.2f%%", ligne.pourcentage())).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String cellule(String valeur) {
        String propre = valeur == null ? "" : valeur;
        return "\"" + propre.replace("\"", "\"\"") + "\"";
    }

    @Override public String extension() { return "csv"; }
    @Override public String typeContenu() { return "text/csv; charset=UTF-8"; }
}
