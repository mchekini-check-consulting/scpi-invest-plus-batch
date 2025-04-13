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
    }

    private Scpi updateScpi(Scpi oldScpi, Scpi newScpi) {

        newScpi.getLocations()
                .forEach(localization -> {
                    localization.getId().setScpiId(oldScpi.getId());
                    localization.setScpi(oldScpi);
                });


        newScpi.getSectors()
                .forEach(sector -> {
                    sector.getId().setScpiId(oldScpi.getId());
                    sector.setScpi(oldScpi);
                });

        newScpi.getStatYears()
                .forEach(statYear -> {
                    statYear.getYearStat().setScpiId(oldScpi.getId());
                    statYear.setScpi(oldScpi);
                });

        return Scpi.builder()
                .id(oldScpi.getId())
                .name(oldScpi.getName())
                .minimumSubscription(newScpi.getMinimumSubscription())
                .manager(newScpi.getManager())
                .capitalization(newScpi.getCapitalization())
                .subscriptionFees(newScpi.getSubscriptionFees())
                .managementCosts(newScpi.getManagementCosts())
                .enjoymentDelay(newScpi.getEnjoymentDelay())
                .iban(newScpi.getIban())
                .bic(newScpi.getBic())
                .scheduledPayment(newScpi.getScheduledPayment())
                .frequencyPayment(newScpi.getFrequencyPayment())
                .cashback(newScpi.getCashback())
                .advertising(newScpi.getAdvertising())
                .locations(newScpi.getLocations())
                .sectors(newScpi.getSectors())
                .statYears(newScpi.getStatYears())
                .build();
    }
}
