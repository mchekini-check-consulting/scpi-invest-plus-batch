package fr.formationacademy.scpiinvestplusbatch.processor;

import fr.formationacademy.scpiinvestplusbatch.entity.elastic.CountryDominant;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.ScpiDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.SectorDominant;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.StatYear;
import fr.formationacademy.scpiinvestplusbatch.mapper.LocationMapper;
import fr.formationacademy.scpiinvestplusbatch.mapper.SectorMapper;
import fr.formationacademy.scpiinvestplusbatch.service.LocationService;
import fr.formationacademy.scpiinvestplusbatch.service.SectorService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;

@Component
public class ScpiToElasticProcessor implements ItemProcessor<Scpi, ScpiDocument> {

    private final LocationService locationService;
    private final SectorService sectorService;
    private final LocationMapper locationMapper;
    private final SectorMapper sectorMapper;

    public ScpiToElasticProcessor(LocationService locationService, SectorService sectorService, LocationMapper locationMapper, SectorMapper sectorMapper) {
        this.locationService = locationService;
        this.sectorService = sectorService;
        this.locationMapper = locationMapper;
        this.sectorMapper = sectorMapper;
    }

    @Override
    public ScpiDocument process(Scpi scpi) {
        BigDecimal distributionRate = null;
        BigDecimal sharePrice = null;
        if (scpi.getStatYears() != null && !scpi.getStatYears().isEmpty()) {
            StatYear latestStat = Collections.max(
                    scpi.getStatYears(),
                    Comparator.comparing(stat -> stat.getYearStat().getYearStat())
            );
            distributionRate = latestStat.getDistributionRate();
            sharePrice = latestStat.getSharePrice();
        }

        Integer minimumSubscription = scpi.getMinimumSubscription();
        CountryDominant countryDominant = locationService.getCountryDominant(scpi);
        SectorDominant sectorDominant = sectorService.getSectorDominant(scpi);

        return ScpiDocument.builder()
                .scpiId(scpi.getId())
                .name(scpi.getName())
                .distributionRate(distributionRate)
                .sharePrice(sharePrice)
                .scheduledPayment(scpi.getScheduledPayment())
                .subscriptionFeesBigDecimal(scpi.getSubscriptionFees())
                .managementCosts(scpi.getManagementCosts())
                .capitalization(scpi.getCapitalization())
                .enjoymentDelay(scpi.getEnjoymentDelay())
                .frequencyPayment(scpi.getFrequencyPayment())
                .minimumSubscription(minimumSubscription)
                .countryDominant(countryDominant)
                .sectorDominant(sectorDominant)
                .locations(locationMapper.mapLocations(scpi))
                .sectors(sectorMapper.mapSectors(scpi))
                .build();
    }
}
