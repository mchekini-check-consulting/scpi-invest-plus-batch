package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.ScpiRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final ScpiRepository scpiRepository;

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

}