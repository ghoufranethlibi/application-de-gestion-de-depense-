package tn.iit.depenses.service;

import org.springframework.stereotype.Service;
import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.exception.FormatNonSupporteException;
import tn.iit.depenses.service.dto.DonneesRapport;
import tn.iit.depenses.service.dto.FichierRapport;
import tn.iit.depenses.service.export.ExportStrategy;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Génère les rapports mensuels.
 * Spring injecte TOUTES les implémentations d'ExportStrategy : ajouter un format (Excel, JSON…)
 * se fait en créant une nouvelle classe, sans modifier ce service (principes OCP et DIP).
 */
@Service
public class RapportService {
    private final BilanService bilanService;
    private final Map<FormatRapport, ExportStrategy> strategiesParFormat;

    public RapportService(BilanService bilanService, List<ExportStrategy> strategies) {
        this.bilanService = bilanService;
        this.strategiesParFormat = indexerParFormat(strategies);
    }

    public FichierRapport generer(Membre membre, YearMonth periode, FormatRapport format) {
        ExportStrategy strategie = strategiesParFormat.get(format);
        if (strategie == null) {
            throw new FormatNonSupporteException(format);
        }
        DonneesRapport donnees = new DonneesRapport(
                bilanService.calculerTotalMensuel(membre, periode),
                bilanService.repartitionParCategorie(membre, periode),
                LocalDateTime.now(),
                membre.getNom());
        String nomFichier = "rapport-" + periode + "." + strategie.extension();
        return new FichierRapport(strategie.exporter(donnees), nomFichier, strategie.typeContenu());
    }

    private static Map<FormatRapport, ExportStrategy> indexerParFormat(List<ExportStrategy> strategies) {
        Map<FormatRapport, ExportStrategy> index = new EnumMap<>(FormatRapport.class);
        for (ExportStrategy strategie : strategies) {
            if (index.putIfAbsent(strategie.format(), strategie) != null) {
                throw new IllegalStateException("Deux stratégies d'export gèrent le format " + strategie.format());
            }
        }
        return index;
    }
}
