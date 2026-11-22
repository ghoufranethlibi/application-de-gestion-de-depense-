package tn.iit.depenses.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iit.depenses.domain.Categorie;
import tn.iit.depenses.domain.Membre;
import tn.iit.depenses.domain.Operation;
import tn.iit.depenses.repository.projection.TotalCategorie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation, Long> {

    /** La catégorie est chargée dans la même requête : la vue l'affiche alors que l'association est LAZY. */
    @EntityGraph(attributePaths = "categorie")
    List<Operation> findByMembreAndDateBetweenOrderByDateDesc(Membre membre, LocalDate debut, LocalDate fin);

    List<Operation> findTop20ByMembreOrderByDateDesc(Membre membre);

    Optional<Operation> findByIdAndMembre(Long id, Membre membre);

    List<Operation> findByCategorieAndMembre(Categorie categorie, Membre membre);

    List<Operation> findByCategorie(Categorie categorie);

    // Agrégations calculées par la base (SUM / GROUP BY) au lieu de charger toutes les opérations en mémoire.

    @Query("select sum(o.montant) from Revenu o where o.membre = :membre and o.date between :debut and :fin")
    BigDecimal totalRevenus(@Param("membre") Membre membre,
                            @Param("debut") LocalDate debut,
                            @Param("fin") LocalDate fin);

    @Query("select sum(o.montant) from Depense o where o.membre = :membre and o.date between :debut and :fin")
    BigDecimal totalDepenses(@Param("membre") Membre membre,
                             @Param("debut") LocalDate debut,
                             @Param("fin") LocalDate fin);

    @Query("""
            select new tn.iit.depenses.repository.projection.TotalCategorie(c.nom, sum(o.montant))
            from Depense o left join o.categorie c
            where o.membre = :membre and o.date between :debut and :fin
            group by c.nom
            order by sum(o.montant) desc
            """)
    List<TotalCategorie> totauxDepensesParCategorie(@Param("membre") Membre membre,
                                                    @Param("debut") LocalDate debut,
                                                    @Param("fin") LocalDate fin);
}
