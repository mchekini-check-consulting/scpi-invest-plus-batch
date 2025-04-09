package fr.formationacademy.scpiinvestplusbatch.entity.postgres;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LocationId implements Serializable {
    private Integer scpiId;
    private String country;
}
