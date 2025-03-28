package fr.formationacademy.scpiinvestplusbatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SectorId implements java.io.Serializable {

    @Column(name = "scpi_id")
    private Integer scpiId;

    @Column(name = "name")
    private String name;


}
