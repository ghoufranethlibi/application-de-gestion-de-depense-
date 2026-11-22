package tn.iit.depenses.support;

import org.springframework.test.util.ReflectionTestUtils;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.domain.TypeOperation;

/** Objets de test prêts à l'emploi (l'identifiant est normalement attribué par la base). */
public final class Fabrique {

    private Fabrique() {
    }

    public static Membre membre(long id, String nom) {
        Membre membre = new Membre(nom, nom.toLowerCase() + "@test.tn", "hash", Role.MEMBRE);
        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }

    public static Membre administrateur(long id) {
        Membre admin = new Membre("Admin", "admin" + id + "@test.tn", "hash", Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", id);
        return admin;
    }

    public static Categorie categorie(long id, String nom, TypeOperation type, Membre proprietaire) {
        Categorie categorie = new Categorie(nom, type, proprietaire, proprietaire == null);
        ReflectionTestUtils.setField(categorie, "id", id);
        return categorie;
    }
}
