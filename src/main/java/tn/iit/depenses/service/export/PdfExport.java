package tn.iit.depenses.service.export;

import org.springframework.stereotype.Component;
import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import tn.iit.depenses.service.dto.TotalMensuel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Export PDF : décide seulement du CONTENU du rapport.
 * La mécanique PDF bas niveau (pagination, objets, xref) est déléguée à GenerateurPdf (SRP).
 */
@Component
public class PdfExport implements ExportStrategy {
    private final GenerateurPdf generateurPdf;

    public PdfExport(GenerateurPdf generateurPdf) {
        this.generateurPdf = generateurPdf;
    }

    @Override
    public FormatRapport format() {
        return FormatRapport.PDF;
    }

    @Override
    public byte[] exporter(DonneesRapport donnees) {
        return generateurPdf.generer(lignesDuRapport(donnees));
    }

    private List<String> lignesDuRapport(DonneesRapport donnees) {
        TotalMensuel total = donnees.total();
        List<String> lignes = new ArrayList<>();
        lignes.add("GESTIONNAIRE DE DEPENSES PERSONNELLES");
        lignes.add("Rapport mensuel - " + total.libelle());
        lignes.add("Membre : " + donnees.nomMembre());
        lignes.add("Genere le : " + donnees.genereLe().toLocalDate());
        lignes.add("");
        lignes.add("Total des revenus : " + total.totalRevenus().toPlainString());
        lignes.add("Total des depenses : " + total.totalDepenses().toPlainString());
        lignes.add("Solde : " + total.solde().toPlainString());
        lignes.add("");
        lignes.add("Repartition des depenses par categorie :");
        if (donnees.repartition().isEmpty()) {
            lignes.add("Aucune depense pour cette periode.");
        }
        for (RepartitionCategorie repartition : donnees.repartition()) {
            lignes.add("- " + repartition.nom() + " : " + repartition.montant().toPlainString()
                    + " (" + String.format(Locale.US, "%.2f%%", repartition.pourcentage()) + ")");
        }
        return lignes;
    }

    @Override
    public String extension() {
        return "pdf";
    }

    @Override
    public String typeContenu() {
        return "application/pdf";
    }
}
