package fr.formationacademy.scpiinvestplusbatch.repository.postgres;

import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Location;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.LocationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, LocationId> {

    @Query("SELECT l FROM Location l WHERE l.id.scpiId = :scpiId")
    List<Location> findByScpiId(@Param("scpiId") Integer scpiId);
}
