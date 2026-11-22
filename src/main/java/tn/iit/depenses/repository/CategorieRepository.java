package tn.iit.depenses.repository;

import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.TypeOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    List<Categorie> findByMembreAndTypeOrderByNomAsc(Membre membre, TypeOperation type);
    List<Categorie> findByMembreOrderByTypeAscNomAsc(Membre membre);
    List<Categorie> findByMembreIsNullAndDefautTrueAndTypeOrderByNomAsc(TypeOperation type);
    List<Categorie> findByMembreIsNullAndDefautTrueOrderByTypeAscNomAsc();
    Optional<Categorie> findByIdAndMembre(Long id, Membre membre);
    Optional<Categorie> findByIdAndMembreIsNullAndDefautTrue(Long id);
    boolean existsByMembreAndTypeAndNomIgnoreCase(Membre membre, TypeOperation type, String nom);
    boolean existsByMembreIsNullAndDefautTrueAndTypeAndNomIgnoreCase(TypeOperation type, String nom);
    Optional<Categorie> findByMembreAndTypeAndNomIgnoreCase(Membre membre, TypeOperation type, String nom);
}
