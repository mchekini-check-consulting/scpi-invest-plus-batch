package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.CountryDominant;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.ScpiDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.SectorDominant;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.StatYear;
import fr.formationacademy.scpiinvestplusbatch.mapper.LocationMapper;
import fr.formationacademy.scpiinvestplusbatch.mapper.SectorMapper;
import fr.formationacademy.scpiinvestplusbatch.repository.elastic.ScpiElasticRepository;
import fr.formationacademy.scpiinvestplusbatch.repository.mongo.ScpiMongoRepository;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BatchService {

    private final ScpiRepository scpiRepository;
    private final ScpiElasticRepository elasticsearchRepository;
    private final LocationService locationService;
    private final ScpiMongoRepository scpiMongoRepository;
    private final LocationMapper locationMapper;
    private final SectorService sectorService;
    private final SectorMapper sectorMapper;
    private final ScpiIndexService scpiIndexService;

    public BatchService(
            ScpiRepository scpiRepository,
            ScpiElasticRepository elasticsearchRepository,
            LocationService locationService,
            ScpiMongoRepository scpiMongoRepository,
            LocationMapper locationMapper,
            SectorService sectorService,
            SectorMapper sectorMapper,
            ScpiIndexService scpiIndexService) {
        this.scpiRepository = scpiRepository;
        this.elasticsearchRepository = elasticsearchRepository;
        this.locationService = locationService;
        this.scpiMongoRepository = scpiMongoRepository;
        this.locationMapper = locationMapper;
        this.sectorService = sectorService;
        this.sectorMapper = sectorMapper;
        this.scpiIndexService = scpiIndexService;
    }

    @Transactional
    public void saveOrUpdateBatchData(List<BatchDataDto> batchDataList) {
        if (batchDataList == null || batchDataList.isEmpty()) {
            log.debug("Batch data list is empty. Skipping processing.");
            return;
        }

        Map<String, Scpi> existingScpis = getExistingScpis(batchDataList);
        List<Scpi> scpisToInsert = new ArrayList<>();
        List<Scpi> scpisToUpdate = new ArrayList<>();

        for (BatchDataDto batchData : batchDataList) {
            Scpi scpi = batchData.getScpi();
            Scpi existing = existingScpis.get(scpi.getName());

            if (existing != null) {
                if (!isSame(existing, scpi)) {
                    scpi.setId(existing.getId()); // Update existing record
                    scpisToUpdate.add(scpi);
                }
            } else {
                scpisToInsert.add(scpi);
            }
        }

        saveEntities(scpiRepository, scpisToInsert, "New SCPIs");
        saveEntities(scpiRepository, scpisToUpdate, "Updated SCPIs");
    }

    @Transactional
    public void saveToMongo(Scpi scpi) {
        Optional<fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument> optionalExisting =
                scpiMongoRepository.findByName(scpi.getName());

        BigDecimal sharePrice = scpi.getStatYears().isEmpty() ? null :
                scpi.getStatYears().get(0).getSharePrice();

        var document = fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument.builder()
                .scpiId(scpi.getId())
                .name(scpi.getName())
                .iban(scpi.getIban())
                .bic(scpi.getBic())
                .sharePrice(sharePrice)
                .build();

        optionalExisting.ifPresent(existing -> {
            if (!isSame(existing, document)) {
                document.setId(existing.getId());
                log.info("SCPI '{}' déjà présente mais différente, mise à jour dans MongoDB...", scpi.getName());
                scpiMongoRepository.save(document);
            }
        });

        if (optionalExisting.isEmpty()) {
            log.info("SCPI '{}' absente de MongoDB, insertion en cours...", scpi.getName());
            scpiMongoRepository.save(document);
        }

        long count = scpiMongoRepository.count();
        log.info("Nombre total de SCPI chargées dans MongoDB : {}", count);
    }

    @Transactional
    public void saveToElastic(Scpi scpi) throws IOException {
        Optional<ScpiDocument> existing = elasticsearchRepository.findByName(scpi.getName());

        BigDecimal distributionRate = scpi.getStatYears().stream()
                .max(Comparator.comparing(stat -> stat.getYearStat().getYearStat()))
                .map(StatYear::getDistributionRate)
                .orElse(null);

        Integer minimumSubscription = scpi.getMinimumSubscription();
        CountryDominant countryDominant = locationService.getCountryDominant(scpi);
        SectorDominant sectorDominant = sectorService.getSectorDominant(scpi);

        scpiIndexService.createIndexIfNotExists();

        ScpiDocument document = ScpiDocument.builder()
                .scpiId(scpi.getId())
                .name(scpi.getName())
                .distributionRate(distributionRate)
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

        existing.ifPresent(existingDoc -> document.setId(existingDoc.getId()));
        elasticsearchRepository.save(document);

        long total = elasticsearchRepository.count();
        log.info("Nombre total de SCPI chargées dans Elasticsearch : {}", total);
    }

    private Map<String, Scpi> getExistingScpis(List<BatchDataDto> batchDataList) {
        List<String> names = batchDataList.stream()
                .map(dto -> dto.getScpi().getName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return scpiRepository.findByNameIn(names)
                .stream()
                .collect(Collectors.toMap(Scpi::getName, scpi -> scpi));
    }

    private <T> void saveEntities(JpaRepository<T, ?> repository, List<T> entities, String logLabel) {
        if (!entities.isEmpty()) {
            repository.saveAll(entities);
            log.info("{} entities saved: {}", logLabel, entities.size());
        }
    }

    public BatchDataDto convertToBatchData(ScpiDto request) {
        return BatchDataDto.builder()
                .scpiDto(request)
                .scpi(new Scpi())
                .locations(new ArrayList<>())
                .sectors(new ArrayList<>())
                .statYears(new ArrayList<>())
                .build();
    }

    public boolean isSame(Scpi a, Scpi b) {
        return Objects.equals(a.getMinimumSubscription(), b.getMinimumSubscription())
                && Objects.equals(a.getCapitalization(), b.getCapitalization())
                && Objects.equals(a.getManager(), b.getManager())
                && Objects.equals(a.getSubscriptionFees(), b.getSubscriptionFees())
                && Objects.equals(a.getManagementCosts(), b.getManagementCosts())
                && Objects.equals(a.getEnjoymentDelay(), b.getEnjoymentDelay())
                && Objects.equals(a.getIban(), b.getIban())
                && Objects.equals(a.getBic(), b.getBic())
                && Objects.equals(a.getScheduledPayment(), b.getScheduledPayment())
                && Objects.equals(a.getFrequencyPayment(), b.getFrequencyPayment())
                && Objects.equals(a.getCashback(), b.getCashback())
                && Objects.equals(a.getAdvertising(), b.getAdvertising())
                && Objects.equals(a.getLocations(), b.getLocations())
                && Objects.equals(a.getStatYears(), b.getStatYears())
                && Objects.equals(a.getSectors(), b.getSectors());
    }

    private boolean isSame(
            fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument a,
            fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument b) {
        return Objects.equals(a.getName(), b.getName())
                && Objects.equals(a.getIban(), b.getIban())
                && Objects.equals(a.getBic(), b.getBic())
                && Objects.equals(a.getSharePrice(), b.getSharePrice());
    }
}
