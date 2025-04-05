package fr.formationacademy.scpiinvestplusbatch.entity.postgrs;

import jakarta.persistence.Column;
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
public class StatYearId implements Serializable {
    @Column(name = "year_stat")
    private Integer yearStat;

    @Column(name = "scpi_id")
    private Integer scpiId;

    public StatYearId(Integer yearStat) {
        this.yearStat = yearStat;
    }

}
