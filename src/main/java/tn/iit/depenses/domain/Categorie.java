package tn.iit.depenses.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorie")
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false, length = 20)
    private TypeOperation type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membre_id")
    private Membre membre;

    @Column(nullable = false)
    private boolean defaut;

    protected Categorie() { }

    public Categorie(String nom, TypeOperation type, Membre membre, boolean defaut) {
        this.nom = nom;
        this.type = type;
        this.membre = membre;
        this.defaut = defaut;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public TypeOperation getType() { return type; }
    public Membre getMembre() { return membre; }
    public boolean isDefaut() { return defaut; }
    public void setNom(String nom) { this.nom = nom; }
    public void setType(TypeOperation type) { this.type = type; }
}
