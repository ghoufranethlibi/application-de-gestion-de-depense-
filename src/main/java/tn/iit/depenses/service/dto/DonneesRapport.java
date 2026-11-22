package tn.iit.depenses.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DonneesRapport(TotalMensuel total, List<RepartitionCategorie> repartition,
                             LocalDateTime genereLe, String nomMembre) { }
