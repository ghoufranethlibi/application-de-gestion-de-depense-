package tn.iit.depenses.repository;

import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findByMembreAndDateBetweenOrderByDateDesc(Membre membre, LocalDate debut, LocalDate fin);
    List<Operation> findTop20ByMembreOrderByDateDesc(Membre membre);
    Optional<Operation> findByIdAndMembre(Long id, Membre membre);
    List<Operation> findByCategorieAndMembre(Categorie categorie, Membre membre);
    List<Operation> findByCategorie(Categorie categorie);
}
