package tn.iit.depenses.service.dto;

import java.math.BigDecimal;

public record EvolutionMensuelle(String libelle, BigDecimal revenus, BigDecimal depenses,
                                 int largeurRevenus, int largeurDepenses) { }
