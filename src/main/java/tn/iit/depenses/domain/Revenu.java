package tn.iit.depenses.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("REVENU")
public class Revenu extends Operation {
    protected Revenu() { }
    public Revenu(BigDecimal montant, LocalDate date, String description, Membre membre, Categorie categorie) {
        super(montant, date, description, membre, categorie);
    }
    @Override public TypeOperation getType() { return TypeOperation.REVENU; }
}
