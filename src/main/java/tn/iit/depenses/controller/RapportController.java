package tn.iit.depenses.controller;

import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.service.RapportService;
import tn.iit.depenses.service.dto.FichierRapport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;

@Controller
public class RapportController {
    private final RapportService rapportService;

    public RapportController(RapportService rapportService) { this.rapportService = rapportService; }

    @GetMapping("/rapports/{format}")
    public ResponseEntity<byte[]> exporter(@AuthenticationPrincipal Membre membre,
                                           @PathVariable String format,
                                           @RequestParam(required = false) String mois) {
        FormatRapport type = FormatRapport.valueOf(format.toUpperCase(java.util.Locale.ROOT));
        YearMonth periode;
        try { periode = mois == null ? YearMonth.now() : YearMonth.parse(mois); }
        catch (RuntimeException ex) { periode = YearMonth.now(); }
        FichierRapport fichier = rapportService.generer(membre, periode, type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fichier.nomFichier() + "\"")
                .contentType(MediaType.parseMediaType(fichier.typeContenu()))
                .body(fichier.contenu());
    }
}
