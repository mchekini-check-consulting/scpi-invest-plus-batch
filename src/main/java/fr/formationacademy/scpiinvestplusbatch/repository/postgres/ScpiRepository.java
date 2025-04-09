package fr.formationacademy.scpiinvestplusbatch.repository.postgres;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface
ScpiRepository extends JpaRepository<Scpi, Integer> {
    @Query("SELECT s FROM Scpi s WHERE s.name IN :names")
    Set<Scpi> findByNameIn(@Param("names") List<String> names);

}
