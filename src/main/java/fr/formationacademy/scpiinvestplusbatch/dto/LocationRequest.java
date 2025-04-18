package fr.formationacademy.scpiinvestplusbatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationRequest {

    @NotBlank(message = "Country is required")
    private String country;

    @PositiveOrZero(message = "Country percentage must be non-negative")
    private BigDecimal countryPercentage;

    @NotNull(message = "SCPI ID is required")
    private Integer scpiId;
}
