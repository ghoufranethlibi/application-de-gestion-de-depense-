package tn.iit.depenses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "operation", indexes = @Index(name = "idx_operation_membre_date", columnList = "membre_id, date_operation"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_operation")
public abstract class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal montant;

    @Column(name = "date_operation", nullable = false)
    private LocalDate date;

    @Column(length = 240)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    protected Operation() { }

    protected Operation(BigDecimal montant, LocalDate date, String description, Membre membre, Categorie categorie) {
        this.montant = montant;
        this.date = date;
        this.description = description;
        this.membre = membre;
        this.categorie = categorie;
    }

    public Long getId() { return id; }
    public BigDecimal getMontant() { return montant; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public Membre getMembre() { return membre; }
    public Categorie getCategorie() { return categorie; }
    public void modifier(BigDecimal montant, LocalDate date, String description, Categorie categorie) {
        this.montant = montant;
        this.date = date;
        this.description = description;
        this.categorie = categorie;
    }
    @Transient public abstract TypeOperation getType();
}
