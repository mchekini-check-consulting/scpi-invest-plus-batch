package fr.formationacademy.scpiinvestplusbatch.dto;

import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Location;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Sector;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.StatYear;
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

