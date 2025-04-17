package fr.formationacademy.scpiinvestplusbatch.writer;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PostgresItemWriter implements ItemWriter<Scpi> {

    private final ScpiRepository scpiRepository;

    public PostgresItemWriter(ScpiRepository scpiRepository) {
        this.scpiRepository = scpiRepository;
    }

    @Override
    public void write(Chunk<? extends Scpi> chunk) {

        List<Scpi> newScpiToSave = new ArrayList<>();

        for (Scpi newScpi : chunk.getItems()) {
            List<Scpi> existedScpi = scpiRepository.findByName(newScpi.getName());
            if (!existedScpi.isEmpty()) {
                newScpiToSave.add(updateScpi(existedScpi.get(0), newScpi));
            } else {
                newScpiToSave.add(newScpi);
            }
        }

        scpiRepository.saveAll(newScpiToSave);
        long itemsCount = scpiRepository.count();
        log.info("Nombre total des SCPI dans la PostgresSql : {}", itemsCount);
    }

    private Scpi updateScpi(Scpi oldScpi, Scpi newScpi) {
        oldScpi.getLocations().clear();
        oldScpi.getLocations().addAll(newScpi.getLocations());
        oldScpi.getLocations().forEach(loc -> {
            loc.getId().setScpiId(oldScpi.getId());
            loc.setScpi(oldScpi);
        });

        oldScpi.getSectors().clear();
        oldScpi.getSectors().addAll(newScpi.getSectors());
        oldScpi.getSectors().forEach(sec -> {
            sec.getId().setScpiId(oldScpi.getId());
            sec.setScpi(oldScpi);
        });

        oldScpi.getStatYears().clear();
        oldScpi.getStatYears().addAll(newScpi.getStatYears());
        oldScpi.getStatYears().forEach(stat -> {
            stat.getYearStat().setScpiId(oldScpi.getId());
            stat.setScpi(oldScpi);
        });
        oldScpi.setMinimumSubscription(newScpi.getMinimumSubscription());
        oldScpi.setManager(newScpi.getManager());
        oldScpi.setCapitalization(newScpi.getCapitalization());
        oldScpi.setSubscriptionFees(newScpi.getSubscriptionFees());
        oldScpi.setManagementCosts(newScpi.getManagementCosts());
        oldScpi.setEnjoymentDelay(newScpi.getEnjoymentDelay());
        oldScpi.setIban(newScpi.getIban());
        oldScpi.setBic(newScpi.getBic());
        oldScpi.setScheduledPayment(newScpi.getScheduledPayment());
        oldScpi.setFrequencyPayment(newScpi.getFrequencyPayment());
        oldScpi.setCashback(newScpi.getCashback());
        oldScpi.setAdvertising(newScpi.getAdvertising());

        return oldScpi;
    }
}
