package tn.iit.depenses.service.export;

import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.RepartitionCategorie;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfExport implements ExportStrategy {
    @Override
    public byte[] exporter(DonneesRapport donnees) {
        List<String> lignes = new ArrayList<>();
        lignes.add("GESTIONNAIRE DE DEPENSES PERSONNELLES");
        lignes.add("Rapport mensuel - " + donnees.total().libelle());
        lignes.add("Membre : " + donnees.nomMembre());
        lignes.add("Genere le : " + donnees.genereLe().toLocalDate());
        lignes.add("");
        lignes.add("Total des revenus : " + donnees.total().totalRevenus().toPlainString());
        lignes.add("Total des depenses : " + donnees.total().totalDepenses().toPlainString());
        lignes.add("Solde : " + donnees.total().solde().toPlainString());
        lignes.add("");
        lignes.add("Repartition des depenses par categorie :");
        if (donnees.repartition().isEmpty()) lignes.add("Aucune depense pour cette periode.");
        for (RepartitionCategorie ligne : donnees.repartition()) {
            lignes.add("- " + ligne.nom() + " : " + ligne.montant().toPlainString()
                    + " (" + String.format(java.util.Locale.US, "%.2f%%", ligne.pourcentage()) + ")");
        }
        return construirePdf(lignes);
    }

    private byte[] construirePdf(List<String> lignes) {
        List<String> lignesFormatees = lignes.stream().flatMap(ligne -> couper(ligne, 78).stream()).toList();
        List<List<String>> pages = new ArrayList<>();
        for (int debut = 0; debut < lignesFormatees.size(); debut += 29) {
            pages.add(lignesFormatees.subList(debut, Math.min(debut + 29, lignesFormatees.size())));
        }
        List<byte[]> objets = new ArrayList<>();
        objets.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder referencesPages = new StringBuilder("<< /Type /Pages /Kids [");
        for (int i = 0; i < pages.size(); i++) referencesPages.append(4 + i * 2).append(" 0 R ");
        referencesPages.append("] /Count ").append(pages.size()).append(" >>");
        objets.add(ascii(referencesPages.toString()));
        objets.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));
        for (int i = 0; i < pages.size(); i++) {
            int pageId = 4 + i * 2;
            int contenuId = pageId + 1;
            objets.add(ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + contenuId + " 0 R >>"));
            StringBuilder contenu = new StringBuilder("BT\n/F1 12 Tf\n50 790 Td\n");
            for (String ligne : pages.get(i)) {
                contenu.append('(').append(echapperAscii(ligne)).append(") Tj\n0 -24 Td\n");
            }
            contenu.append("ET\n");
            byte[] flux = contenu.toString().getBytes(StandardCharsets.US_ASCII);
            ByteArrayOutputStream fluxObjet = new ByteArrayOutputStream();
            fluxObjet.writeBytes(ascii("<< /Length " + flux.length + " >>\nstream\n"));
            fluxObjet.writeBytes(flux);
            fluxObjet.writeBytes(ascii("endstream"));
            objets.add(fluxObjet.toByteArray());
        }

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.writeBytes(ascii("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objets.size(); i++) {
            offsets.add(pdf.size());
            pdf.writeBytes(ascii((i + 1) + " 0 obj\n"));
            pdf.writeBytes(objets.get(i));
            pdf.writeBytes(ascii("\nendobj\n"));
        }
        int xref = pdf.size();
        pdf.writeBytes(ascii("xref\n0 " + (objets.size() + 1) + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++) {
            pdf.writeBytes(ascii(String.format(java.util.Locale.US, "%010d 00000 n \n", offsets.get(i))));
        }
        pdf.writeBytes(ascii("trailer\n<< /Size " + (objets.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF"));
        return pdf.toByteArray();
    }

    private List<String> couper(String ligne, int tailleMax) {
        if (ligne.length() <= tailleMax) return List.of(ligne);
        List<String> morceaux = new ArrayList<>();
        String restant = ligne;
        while (restant.length() > tailleMax) {
            int coupure = restant.lastIndexOf(' ', tailleMax);
            if (coupure <= 0) coupure = tailleMax;
            morceaux.add(restant.substring(0, coupure));
            restant = restant.substring(coupure).stripLeading();
        }
        if (!restant.isEmpty()) morceaux.add(restant);
        return morceaux;
    }

    private String echapperAscii(String valeur) {
        String sansAccents = Normalizer.normalize(valeur, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        StringBuilder resultat = new StringBuilder();
        for (char c : sansAccents.toCharArray()) {
            char imprimable = c >= 32 && c <= 126 ? c : '?';
            if (imprimable == '(' || imprimable == ')' || imprimable == '\\') resultat.append('\\');
            resultat.append(imprimable);
        }
        return resultat.toString();
    }

    private byte[] ascii(String valeur) { return valeur.getBytes(StandardCharsets.US_ASCII); }

    @Override public String extension() { return "pdf"; }
    @Override public String typeContenu() { return "application/pdf"; }
}
