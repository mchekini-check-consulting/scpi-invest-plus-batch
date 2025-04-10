package fr.formationacademy.scpiinvestplusbatch.dto;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.StatYear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

import static fr.formationacademy.scpiinvestplusbatch.utils.Constants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScpiDto {

    private Integer id;

    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 0, message = "Minimum subscription must be positive")
    private Integer minimumSubscription;

    @NotBlank(message = "Manager is required")
    private String manager;

    @PositiveOrZero(message = "Capitalization must be non-negative")
    private Long capitalization;

    @DecimalMin(value = "0.0", inclusive = true, message = "Subscription fees must be non-negative")
    private BigDecimal subscriptionFees;

    @DecimalMin(value = "0.0", inclusive = true, message = "Management costs must be non-negative")
    private BigDecimal managementCosts;

    @Min(value = 0, message = "Enjoyment delay must be non-negative")
    private Integer enjoymentDelay;

    @Pattern(regexp = IBAN_PATTERN, message = INVALID_IBAN)
    private String iban;

    @Pattern(regexp = BIC_PATTERN, message = INVALID_BIC)
    private String bic;

    private Boolean scheduledPayment;

    private String frequencyPayment;

    @PositiveOrZero(message = "Cashback must be non-negative")
    private Float cashback;

    private String advertising;
    // J'ai besoin de les traiter comme une chaine ici
    private String distributedRate;
    private String sharePrice;
    private String reconstitutionValue;

    private String locations;
    private String sectors;
    private Integer statYear;

    private List<StatYear> statYears;
}