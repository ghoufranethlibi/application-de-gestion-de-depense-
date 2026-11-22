package tn.iit.depenses.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.service.RapportService;
import tn.iit.depenses.service.dto.FichierRapport;

import java.time.YearMonth;
import java.util.Objects;

@Controller
public class RapportController {
    private final RapportService rapportService;

    public RapportController(RapportService rapportService) {
        this.rapportService = rapportService;
    }

    /** Le format (CSV, PDF) et le mois sont convertis par Spring ; une valeur inconnue est signalée à l'utilisateur. */
    @GetMapping(Routes.RAPPORTS + "/{format}")
    public ResponseEntity<byte[]> exporter(@AuthenticationPrincipal Membre membre,
                                           @PathVariable FormatRapport format,
                                           @RequestParam(required = false) YearMonth mois) {
        YearMonth periode = Objects.requireNonNullElseGet(mois, YearMonth::now);
        FichierRapport fichier = rapportService.generer(membre, periode, format);
        ContentDisposition pieceJointe = ContentDisposition.attachment().filename(fichier.nomFichier()).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, pieceJointe.toString())
                .contentType(MediaType.parseMediaType(fichier.typeContenu()))
                .body(fichier.contenu());
    }
}
