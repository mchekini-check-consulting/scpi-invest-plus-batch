package fr.formationacademy.scpiinvestplusbatch.dto;

import fr.formationacademy.scpiinvestplusbatch.entity.StatYearId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StatYearDtoOut {
    private StatYearId yearStat;
    private BigDecimal distributionRate;
    private BigDecimal sharePrice;
    private BigDecimal reconstitutionValue;
}
