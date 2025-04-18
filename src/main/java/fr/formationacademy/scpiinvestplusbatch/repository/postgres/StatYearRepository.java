package fr.formationacademy.scpiinvestplusbatch.repository.postgres;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.StatYear;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.StatYearId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatYearRepository extends JpaRepository<StatYear, StatYearId> {
    boolean existsByYearStat(StatYearId yearStat);

}
