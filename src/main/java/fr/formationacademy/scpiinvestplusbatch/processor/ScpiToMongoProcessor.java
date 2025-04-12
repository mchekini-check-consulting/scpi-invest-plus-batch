package fr.formationacademy.scpiinvestplusbatch.processor;

import fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiMongo;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ScpiToMongoProcessor implements ItemProcessor<Scpi, ScpiMongo> {

    @Override
    public ScpiMongo process(Scpi scpi)  {

        BigDecimal sharePrice = scpi.getStatYears().isEmpty() ? null : scpi.getStatYears().get(0).getSharePrice();

        return ScpiMongo.builder()
                .scpiId(scpi.getId())
                .name(scpi.getName())
                .iban(scpi.getIban())
                .bic(scpi.getBic())
                .sharePrice(sharePrice)
                .build();
    }
}
