package fr.formationacademy.scpiinvestplusbatch.dto;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Location;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Sector;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.StatYear;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDataDto {
    private ScpiDto scpiDto;
    private Scpi scpi;
    private List<Location> locations;
    private List<Sector> sectors;
    private List<StatYear> statYears;
}

