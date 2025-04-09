package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.*;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.StatYear;
import fr.formationacademy.scpiinvestplusbatch.mapper.LocationMapper;
import fr.formationacademy.scpiinvestplusbatch.mapper.SectorMapper;
import fr.formationacademy.scpiinvestplusbatch.repository.elastic.ScpiElasticRepository;
import fr.formationacademy.scpiinvestplusbatch.repository.mongo.ScpiMongoRepository;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

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

    public BatchService(ScpiRepository scpiRepository, ScpiElasticRepository elasticsearchRepository, LocationService locationService, ScpiMongoRepository scpiMongoRepository, LocationMapper locationMapper, SectorService sectorService, SectorMapper sectorMapper, ScpiIndexService scpiIndexService) {
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
            Scpi existingScpi = existingScpis.get(scpi.getName());
            if (existingScpi != null) {
                if (!isSame(scpi, existingScpi)) {
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

        Optional<fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument> existing = scpiMongoRepository.findByName(scpi.getName());
        BigDecimal sharePrice = scpi.getStatYears().isEmpty() ? null : scpi.getStatYears().get(0).getSharePrice();

        fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument document = fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument.builder()
                .scpiId(scpi.getId())
                .name(scpi.getName())
                .iban(scpi.getIban())
                .bic(scpi.getBic())
                .sharePrice(sharePrice)
                .build();

        if (existing.isPresent()) {
            fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument existingDoc = existing.get();
            if (isSame(document, existingDoc)) {
                return;
            } else {
                document.setId(existingDoc.getId());
                log.info("SCPI '{}' déjà présente mais différente, mise à jour dans MongoDB...", scpi.getName());
            }
        } else {
            log.info("SCPI '{}' absente de MongoDB, insertion en cours...", scpi.getName());
        }

        scpiMongoRepository.save(document);
        long scpiCount = scpiMongoRepository.count();
        log.info("Nombre total de SCPI chargées dans MongoDB : {}", scpiCount);
    }

    @Transactional
    public void saveToElastic(Scpi scpi) throws IOException {
        Optional<ScpiDocument> existing = elasticsearchRepository.findByName(scpi.getName());

        BigDecimal distributionRate = null;
        if (scpi.getStatYears() != null && !scpi.getStatYears().isEmpty()) {
            StatYear latestStat = Collections.max(
                    scpi.getStatYears(),
                    Comparator.comparing(stat -> stat.getYearStat().getYearStat())
            );
            distributionRate = latestStat.getDistributionRate();
        }

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
        List<String> scpiNames = batchDataList.stream()
                .map(dto -> dto.getScpi().getName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return scpiRepository.findByNameIn(scpiNames)
                .stream().collect(Collectors.toMap(Scpi::getName, scpi -> scpi));
    }

    private <T> void saveEntities(JpaRepository<T, ?> repository, List<T> entities, String entityName) {
        if (!entities.isEmpty()) {
            repository.saveAll(entities);
            log.info("{} entities saved: {}", entityName, entities.size());
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

    public boolean isSame(Scpi existing, Scpi scpi) {
        return Objects.equals(existing.getMinimumSubscription(), scpi.getMinimumSubscription())
                && Objects.equals(existing.getCapitalization(), scpi.getCapitalization())
                && Objects.equals(existing.getManager(), scpi.getManager())
                && Objects.equals(existing.getSubscriptionFees(), scpi.getSubscriptionFees())
                && Objects.equals(existing.getManagementCosts(), scpi.getManagementCosts())
                && Objects.equals(existing.getEnjoymentDelay(), scpi.getEnjoymentDelay())
                && Objects.equals(existing.getIban(), scpi.getIban())
                && Objects.equals(existing.getBic(), scpi.getBic())
                && Objects.equals(existing.getScheduledPayment(), scpi.getScheduledPayment())
                && Objects.equals(existing.getFrequencyPayment(), scpi.getFrequencyPayment())
                && Objects.equals(existing.getCashback(), scpi.getCashback())
                && Objects.equals(existing.getAdvertising(), scpi.getAdvertising())
                && Objects.equals(existing.getLocations(), scpi.getLocations())
                && Objects.equals(existing.getStatYears(), scpi.getStatYears())
                && Objects.equals(existing.getSectors(), scpi.getSectors());
    }

    private boolean isSame(fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument existing, fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument scpi) {
        return Objects.equals(existing.getName(), scpi.getName()) &&
                Objects.equals(existing.getIban(), scpi.getIban()) &&
                Objects.equals(existing.getBic(), scpi.getBic()) &&
                Objects.equals(existing.getSharePrice(), scpi.getSharePrice());
    }

}