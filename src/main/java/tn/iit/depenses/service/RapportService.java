package tn.iit.depenses.service;

import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.FichierRapport;
import tn.iit.depenses.service.export.CsvExport;
import tn.iit.depenses.service.export.PdfExport;
import tn.iit.depenses.service.export.ExportStrategy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class RapportService {
    private final BilanService bilanService;
    private final CsvExport csvExport;
    private final PdfExport pdfExport;

    public RapportService(BilanService bilanService, CsvExport csvExport, PdfExport pdfExport) {
        this.bilanService = bilanService;
        this.csvExport = csvExport;
        this.pdfExport = pdfExport;
    }

    public FichierRapport generer(Membre membre, YearMonth periode, FormatRapport format) {
        DonneesRapport donnees = new DonneesRapport(
                bilanService.calculerTotalMensuel(membre, periode),
                bilanService.repartitionParCategorie(membre, periode),
                LocalDateTime.now(), membre.getNom());
        ExportStrategy strategie = format == FormatRapport.PDF ? pdfExport : csvExport;
        return new FichierRapport(strategie.exporter(donnees),
                "rapport-" + periode + "." + strategie.extension(), strategie.typeContenu());
    }
}
