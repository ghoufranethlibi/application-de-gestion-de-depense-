package tn.iit.depenses.service.export;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Écrit un PDF texte simple (A4, Helvetica 12) sans bibliothèque externe.
 * Responsabilité unique : la mécanique du format PDF (pagination, objets, table xref).
 */
@Component
public class GenerateurPdf {
    private static final int CARACTERES_PAR_LIGNE = 78;
    private static final int LIGNES_PAR_PAGE = 29;
    private static final int ID_PREMIERE_PAGE = 4; // 1 = catalogue, 2 = arbre des pages, 3 = police
    private static final String EN_TETE_TEXTE = "BT\n/F1 12 Tf\n50 790 Td\n";
    private static final String INTERLIGNE = "0 -24 Td\n";

    public byte[] generer(List<String> lignes) {
        List<List<String>> pages = paginer(lignes);
        List<byte[]> objets = new ArrayList<>();
        objets.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        objets.add(ascii(arbreDesPages(pages.size())));
        objets.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));
        for (int i = 0; i < pages.size(); i++) {
            int idContenu = idPage(i) + 1;
            objets.add(ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + idContenu + " 0 R >>"));
            objets.add(fluxDeContenu(pages.get(i)));
        }
        return assembler(objets);
    }

    private List<List<String>> paginer(List<String> lignes) {
        List<String> lignesCoupees = lignes.stream()
                .flatMap(ligne -> couper(ligne).stream())
                .toList();
        List<List<String>> pages = new ArrayList<>();
        for (int debut = 0; debut < lignesCoupees.size(); debut += LIGNES_PAR_PAGE) {
            pages.add(lignesCoupees.subList(debut, Math.min(debut + LIGNES_PAR_PAGE, lignesCoupees.size())));
        }
        return pages;
    }

    private static int idPage(int indexPage) {
        return ID_PREMIERE_PAGE + indexPage * 2;
    }

    private static String arbreDesPages(int nombreDePages) {
        StringBuilder arbre = new StringBuilder("<< /Type /Pages /Kids [");
        for (int i = 0; i < nombreDePages; i++) {
            arbre.append(idPage(i)).append(" 0 R ");
        }
        return arbre.append("] /Count ").append(nombreDePages).append(" >>").toString();
    }

    private byte[] fluxDeContenu(List<String> lignesDeLaPage) {
        StringBuilder texte = new StringBuilder(EN_TETE_TEXTE);
        for (String ligne : lignesDeLaPage) {
            texte.append('(').append(echapper(ligne)).append(") Tj\n").append(INTERLIGNE);
        }
        texte.append("ET\n");
        byte[] flux = ascii(texte.toString());
        ByteArrayOutputStream objet = new ByteArrayOutputStream();
        objet.writeBytes(ascii("<< /Length " + flux.length + " >>\nstream\n"));
        objet.writeBytes(flux);
        objet.writeBytes(ascii("endstream"));
        return objet.toByteArray();
    }

    /** Écrit l'en-tête, les objets numérotés, la table des positions (xref) et le trailer. */
    private static byte[] assembler(List<byte[]> objets) {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.writeBytes(ascii("%PDF-1.4\n"));
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < objets.size(); i++) {
            positions.add(pdf.size());
            pdf.writeBytes(ascii((i + 1) + " 0 obj\n"));
            pdf.writeBytes(objets.get(i));
            pdf.writeBytes(ascii("\nendobj\n"));
        }
        int positionXref = pdf.size();
        pdf.writeBytes(ascii("xref\n0 " + (objets.size() + 1) + "\n0000000000 65535 f \n"));
        for (int position : positions) {
            pdf.writeBytes(ascii(String.format(Locale.US, "%010d 00000 n \n", position)));
        }
        pdf.writeBytes(ascii("trailer\n<< /Size " + (objets.size() + 1) + " /Root 1 0 R >>\nstartxref\n"
                + positionXref + "\n%%EOF"));
        return pdf.toByteArray();
    }

    private static List<String> couper(String ligne) {
        if (ligne.length() <= CARACTERES_PAR_LIGNE) {
            return List.of(ligne);
        }
        List<String> morceaux = new ArrayList<>();
        String restant = ligne;
        while (restant.length() > CARACTERES_PAR_LIGNE) {
            int coupure = restant.lastIndexOf(' ', CARACTERES_PAR_LIGNE);
            if (coupure <= 0) {
                coupure = CARACTERES_PAR_LIGNE;
            }
            morceaux.add(restant.substring(0, coupure));
            restant = restant.substring(coupure).stripLeading();
        }
        if (!restant.isEmpty()) {
            morceaux.add(restant);
        }
        return morceaux;
    }

    /** Retire les accents (Helvetica de base) et échappe les caractères spéciaux du PDF. */
    private static String echapper(String valeur) {
        String sansAccents = Normalizer.normalize(valeur, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        StringBuilder resultat = new StringBuilder();
        for (char c : sansAccents.toCharArray()) {
            char imprimable = (c >= 32 && c <= 126) ? c : '?';
            if (imprimable == '(' || imprimable == ')' || imprimable == '\\') {
                resultat.append('\\');
            }
            resultat.append(imprimable);
        }
        return resultat.toString();
    }

    private static byte[] ascii(String valeur) {
        return valeur.getBytes(StandardCharsets.US_ASCII);
    }
}
