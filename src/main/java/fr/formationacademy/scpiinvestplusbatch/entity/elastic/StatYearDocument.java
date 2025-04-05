package fr.formationacademy.scpiinvestplusbatch.entity.elastic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatYearDocument {
    private Integer scpiId;
    private Integer year;
    private BigDecimal distributionRate;
    private BigDecimal sharePrice;
    private BigDecimal reconstitutionValue;
}
