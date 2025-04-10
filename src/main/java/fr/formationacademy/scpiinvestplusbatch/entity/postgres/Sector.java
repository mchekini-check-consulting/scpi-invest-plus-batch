package fr.formationacademy.scpiinvestplusbatch.entity.postgres;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Sector {
    @EmbeddedId
    private SectorId id;
    private BigDecimal sectorPercentage;

    @ManyToOne
    @MapsId("scpiId")
    @JoinColumn(name = "scpi_id", nullable = false)
    @ToString.Exclude
    private Scpi scpi;
}
