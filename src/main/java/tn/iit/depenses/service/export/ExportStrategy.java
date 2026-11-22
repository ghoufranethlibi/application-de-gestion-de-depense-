package tn.iit.depenses.service.export;

import tn.iit.depenses.domain.FormatRapport;
import tn.iit.depenses.service.dto.DonneesRapport;

/** Stratégie d'export d'un rapport mensuel (pattern Strategy). */
public interface ExportStrategy {

    /** Format géré par cette stratégie : sert de clé dans le registre de RapportService. */
    FormatRapport format();

    byte[] exporter(DonneesRapport donnees);

    String extension();

    String typeContenu();
}
