package fr.formationacademy.scpiinvestplusbatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ScpiDtoOut {
    private Integer id;
    private String name;
    private Integer minimumSubscription;
    private String manager;
    private Long capitalization;
    private Float subscriptionFees;
    private Float managementCosts;
    private Integer enjoymentDelay;
    private String iban;
    private String bic;
    private Boolean scheduledPayment;
    private String frequencyPayment;
    private Float cashback;
    private String advertising;
    private List<StatYearDtoOut> statYears;
    private List<LocationDtoOut> locations;
    private List<SectorDtoOut> sectors;
    private LocationDtoOut location;
    private SectorDtoOut sector;
    private StatYearDtoOut statYear;
}
