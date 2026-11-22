package tn.iit.depenses.repository;

import tn.iit.depenses.domain.Membre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembreRepository extends JpaRepository<Membre, Long> {
    Optional<Membre> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<Membre> findAllByOrderByNomAsc();
}
