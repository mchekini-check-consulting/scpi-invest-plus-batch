package fr.formationacademy.scpiinvestplusbatch.entity.postgrs;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class StatYear {
    @EmbeddedId
    private StatYearId yearStat;
    private BigDecimal distributionRate;
    private BigDecimal sharePrice;
    private BigDecimal reconstitutionValue;

    @ManyToOne
    @MapsId("scpiId")
    @JoinColumn(name = "scpi_id", nullable = false)
    @ToString.Exclude
    private Scpi scpi;
}
