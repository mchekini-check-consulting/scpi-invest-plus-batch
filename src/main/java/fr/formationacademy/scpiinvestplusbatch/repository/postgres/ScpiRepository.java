package fr.formationacademy.scpiinvestplusbatch.repository.postgres;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface
ScpiRepository extends JpaRepository<Scpi, Integer> {
    List<Scpi> findByName(String name);
}
