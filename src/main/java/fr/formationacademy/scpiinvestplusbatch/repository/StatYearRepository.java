package fr.formationacademy.scpiinvestplusbatch.repository;

import fr.formationacademy.scpiinvestplusbatch.entity.StatYear;
import fr.formationacademy.scpiinvestplusbatch.entity.StatYearId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatYearRepository extends JpaRepository<StatYear, StatYearId> {
    boolean existsByYearStat(StatYearId yearStat);

}
