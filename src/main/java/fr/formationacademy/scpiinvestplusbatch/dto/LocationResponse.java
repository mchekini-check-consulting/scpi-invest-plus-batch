package fr.formationacademy.scpiinvestplusbatch.dto;


import fr.formationacademy.scpiinvestplusbatch.entity.postgres.LocationId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LocationResponse {
    private LocationId id;
    private BigDecimal countryPercentage;
}
