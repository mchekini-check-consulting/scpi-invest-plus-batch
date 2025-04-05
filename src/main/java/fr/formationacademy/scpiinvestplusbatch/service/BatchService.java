package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.LocationDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.ScpiElasticsearchDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.SectorDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.StatYearDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.elastic.ScpiElasticRepository;
import fr.formationacademy.scpiinvestplusbatch.repository.mongo.ScpiMongoRepository;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BatchService {

    private final ScpiRepository scpiRepository;
    private final ScpiMongoRepository mongoRepository;
    private final ScpiElasticRepository elasticsearchRepository;

    public BatchService(ScpiRepository scpiRepository, ScpiMongoRepository mongoRepository, ScpiElasticRepository elasticsearchRepository) {
        this.scpiRepository = scpiRepository;
        this.mongoRepository = mongoRepository;
        this.elasticsearchRepository = elasticsearchRepository;
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
        log.info("Sauvegarde de la SCPI '{}' dans MongoDB...", scpi.getName());

        Optional<ScpiDocument> existing = mongoRepository.findByName(scpi.getName());
        BigDecimal sharePrice = scpi.getStatYears().isEmpty() ? null : scpi.getStatYears().get(0).getSharePrice();

        ScpiDocument document = ScpiDocument.builder()
                .id(scpi.getId() != null ? scpi.getId().toString() : null)
                .name(scpi.getName())
                .iban(scpi.getIban())
                .bic(scpi.getBic())
                .sharePrice(sharePrice)
                .build();

        if (existing.isPresent()) {
            ScpiDocument existingDoc = existing.get();
            if (isSame(document, existingDoc)) {
                log.info("SCPI '{}' déjà présente dans MongoDB et identique, aucune mise à jour nécessaire.", scpi.getName());
                return;
            } else {
                document.setId(existingDoc.getId());
                log.info("SCPI '{}' déjà présente mais différente, mise à jour dans MongoDB...", scpi.getName());
            }
        } else {
            log.info("SCPI '{}' absente de MongoDB, insertion en cours...", scpi.getName());
        }

        mongoRepository.save(document);
        log.info("SCPI '{}' sauvegardée dans MongoDB avec l'ID '{}'.", document.getName(), document.getId());
    }

    @Transactional
    public void saveToElastic(Scpi scpi) {
        log.info("Sauvegarde de la SCPI '{}' dans Elasticsearch...", scpi.getName());
        Optional<ScpiElasticsearchDocument> existing = elasticsearchRepository.findByName(scpi.getName());

        ScpiElasticsearchDocument document = ScpiElasticsearchDocument.builder()
                .id(scpi.getId() != null ? scpi.getId().toString() : null)
                .name(scpi.getName())
                .minimumSubscription(scpi.getMinimumSubscription())
                .subscriptionFees(scpi.getSubscriptionFees())
                .managementCosts(scpi.getManagementCosts())
                .frequencyPayment(scpi.getFrequencyPayment())
                .locations(scpi.getLocations() != null ? scpi.getLocations().stream().map(location ->
                        LocationDocument.builder()
                                .id(location.getId() != null ? location.getId().getScpiId() : null)
                                .countryPercentage(location.getCountryPercentage())
                                .build()
                ).collect(Collectors.toList()) : null)
                .sectors(scpi.getSectors() != null ? scpi.getSectors().stream().map(sector ->
                        SectorDocument.builder()
                                .id(sector.getId().getScpiId())
                                .sectorPercentage(sector.getSectorPercentage())
                                .build()
                ).collect(Collectors.toList()) : null)
                .statYears(scpi.getStatYears() != null ? scpi.getStatYears().stream().map(statYear ->
                        StatYearDocument.builder()
                                .scpiId(statYear.getYearStat().getScpiId())
                                .year(statYear.getYearStat().getYearStat())
                                .distributionRate(statYear.getDistributionRate())
                                .sharePrice(statYear.getSharePrice())
                                .reconstitutionValue(statYear.getReconstitutionValue())
                                .build()
                ).collect(Collectors.toList()) : null)
                .build();

        if (existing.isPresent()) {
            ScpiElasticsearchDocument existingDoc = existing.get();
            if (Objects.equals(existingDoc.getName(), document.getName())) {
                log.info("La SCPI '{}' existe déjà dans Elasticsearch et est identique. Aucune mise à jour nécessaire.", scpi.getName());
                return;
            } else {
                document.setId(existingDoc.getId());
                log.info("La SCPI '{}' existe déjà mais est différente. Mise à jour du document dans Elasticsearch...", scpi.getName());
            }
        } else {
            log.info("La SCPI '{}' n'a pas été trouvée dans Elasticsearch. Insertion du nouveau document...", scpi.getName());
        }

        elasticsearchRepository.save(document);
        log.info("La SCPI '{}' a été sauvegardée dans Elasticsearch avec l'ID '{}'.", document.getName(), document.getId());
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

    private boolean isSame(ScpiDocument existing, ScpiDocument scpi) {
        return Objects.equals(existing.getName(), scpi.getName()) &&
                Objects.equals(existing.getIban(), scpi.getIban()) &&
                Objects.equals(existing.getBic(), scpi.getBic()) &&
                Objects.equals(existing.getSharePrice(), scpi.getSharePrice());
    }

}