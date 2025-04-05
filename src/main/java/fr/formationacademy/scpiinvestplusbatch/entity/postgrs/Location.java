package fr.formationacademy.scpiinvestplusbatch.entity.postgrs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
    @EmbeddedId
    private LocationId id;
    private BigDecimal countryPercentage;

    @ManyToOne
    @MapsId("scpiId")
    @JoinColumn(name = "scpi_id",nullable = false)
    @JsonIgnore
    private Scpi scpi;
}
