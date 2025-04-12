package fr.formationacademy.scpiinvestplusbatch.processor;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.*;
import io.micrometer.common.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@Slf4j
public class ScpiToPostgresProcessor implements ItemProcessor<ScpiDto, Scpi> {

    @Override
    public Scpi process(@NonNull ScpiDto scpiDto) {


        Scpi scpi = Scpi.builder()
                .name(scpiDto.getName())
                .minimumSubscription(scpiDto.getMinimumSubscription())
                .manager(scpiDto.getManager())
                .capitalization(scpiDto.getCapitalization())
                .subscriptionFees(scpiDto.getSubscriptionFees())
                .managementCosts(scpiDto.getManagementCosts())
                .enjoymentDelay(scpiDto.getEnjoymentDelay())
                .iban(scpiDto.getIban())
                .bic(scpiDto.getBic())
                .frequencyPayment(scpiDto.getFrequencyPayment())
                .cashback(scpiDto.getCashback())
                .advertising(scpiDto.getAdvertising())
                .build();

        setLocations(scpiDto, scpi);
        setSectors(scpiDto, scpi);
        setStatYears(scpiDto, scpi);
        setScheduledPayment(scpiDto, scpi);
        return scpi;
    }

    private void setLocations(ScpiDto scpiDto, Scpi scpi) {
        List<Location> locations = new ArrayList<>();

        if (scpiDto.getLocations() != null && !scpiDto.getLocations().isBlank()) {
            String[] parts = scpiDto.getLocations().split(",");

            for (int i = 0; i < parts.length; i += 2) {
                String country = parts[i].trim();
                BigDecimal percentage = new BigDecimal(parts[i + 1].trim());

                LocationId locationId = LocationId.builder()
                        .scpiId(scpi.getId())
                        .country(country)
                        .build();

                Location location = Location.builder()
                        .id(locationId)
                        .countryPercentage(percentage)
                        .scpi(scpi)
                        .build();

                locations.add(location);
            }
        }

        scpi.setLocations(locations);
    }

    public void setSectors(ScpiDto dto, Scpi scpi) {
        List<Sector> sectors = new ArrayList<>();

        if (dto.getSectors() != null && !dto.getSectors().isBlank()) {
            String[] parts = dto.getSectors().split(",");

            for (int i = 0; i < parts.length; i += 2) {
                String name = parts[i].trim();
                BigDecimal percentage = new BigDecimal(parts[i + 1].trim());

                SectorId sectorId = SectorId.builder()
                        .scpiId(scpi.getId())
                        .name(name)
                        .build();

                Sector sector = Sector.builder()
                        .id(sectorId)
                        .sectorPercentage(percentage)
                        .scpi(scpi)
                        .build();

                sectors.add(sector);
            }
        }
        scpi.setSectors(sectors);
    }

    private void setStatYears(ScpiDto dto, Scpi scpi) {
        List<StatYear> statYears = new ArrayList<>();

        if (dto.getDistributedRate() == null && dto.getReconstitutionValue() == null && dto.getSharePrice() == null) {
            log.debug("Aucune statistique annuelle disponible pour '{}'", scpi.getName());
            scpi.setStatYears(Collections.emptyList());
            return;
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        String[] distributedRates = dto.getDistributedRate() != null ? dto.getDistributedRate().split(",") : new String[0];
        String[] reconstitutionValues = dto.getReconstitutionValue() != null ? dto.getReconstitutionValue().split(",") : new String[0];
        String[] sharePrices = dto.getSharePrice() != null ? dto.getSharePrice().split(",") : new String[0];

        int maxLength = Math.max(distributedRates.length, Math.max(reconstitutionValues.length, sharePrices.length));

        for (int i = 0; i < maxLength; i++) {
            int year = currentYear - 1 - i;

            BigDecimal distributionRate = parseBigDecimal(distributedRates, i);
            BigDecimal reconstitutionValue = parseBigDecimal(reconstitutionValues, i);
            BigDecimal sharePrice = parseBigDecimal(sharePrices, i);

            if (distributionRate == null && reconstitutionValue == null && sharePrice == null) {
                continue;
            }

            StatYearId statYearId = new StatYearId(year, scpi.getId());

            StatYear statYear = StatYear.builder()
                    .yearStat(statYearId)
                    .distributionRate(distributionRate)
                    .reconstitutionValue(reconstitutionValue)
                    .sharePrice(sharePrice)
                    .scpi(scpi)
                    .build();

            statYears.add(statYear);
        }

        scpi.setStatYears(statYears);
    }
    private BigDecimal parseBigDecimal(String[] array, int index) {
        if (index >= array.length || array[index].trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(array[index].trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("Valeur invalide '{}' à l'index {}", array[index], index, e);
            return null;
        }
    }

    private void setScheduledPayment(ScpiDto dto, Scpi scpi) {
        String scheduledPayment = dto.getScheduledPayment();
        if ("Oui".equalsIgnoreCase(scheduledPayment)) {
            scpi.setScheduledPayment(true);
        } else if ("Non".equalsIgnoreCase(scheduledPayment)) {
            scpi.setScheduledPayment(false);
        } else {
            log.warn("Valeur de scheduledPayment inconnue : '{}'. Valeur par défaut false appliquée.", scheduledPayment);
            scpi.setScheduledPayment(false);
        }
    }

}
















