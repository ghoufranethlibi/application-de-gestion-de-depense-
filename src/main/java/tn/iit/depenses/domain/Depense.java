package tn.iit.depenses.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("DEPENSE")
public class Depense extends Operation {
    protected Depense() { }
    public Depense(BigDecimal montant, LocalDate date, String description, Membre membre, Categorie categorie) {
        super(montant, date, description, membre, categorie);
    }
    @Override public TypeOperation getType() { return TypeOperation.DEPENSE; }
}
