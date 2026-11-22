package tn.iit.depenses.service.dto;

import java.math.BigDecimal;

public record RepartitionCategorie(String nom, BigDecimal montant, double pourcentage) { }
