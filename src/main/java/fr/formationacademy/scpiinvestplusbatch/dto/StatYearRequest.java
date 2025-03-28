package fr.formationacademy.scpiinvestplusbatch.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatYearRequest {

    @Min(value = 1700, message = "Year must be realistic")
    private Integer yearStat;

    @PositiveOrZero(message = "Distribution rate must be non-negative")
    private BigDecimal distributionRate;

    @PositiveOrZero(message = "Share price must be non-negative")
    private BigDecimal sharePrice;

    @PositiveOrZero(message = "Reconstitution value must be non-negative")
    private BigDecimal reconstitutionValue;

    @NotNull(message = "SCPI ID is required")
    private Integer scpiId;
}

