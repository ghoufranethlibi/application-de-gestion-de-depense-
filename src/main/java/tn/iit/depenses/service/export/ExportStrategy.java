package tn.iit.depenses.service.export;

import tn.iit.depenses.service.dto.DonneesRapport;

public interface ExportStrategy {
    byte[] exporter(DonneesRapport donnees);
    String extension();
    String typeContenu();
}
