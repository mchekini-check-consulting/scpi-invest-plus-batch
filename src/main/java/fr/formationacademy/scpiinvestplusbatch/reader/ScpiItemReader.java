package fr.formationacademy.scpiinvestplusbatch.reader;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.enums.ScpiField;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManagerFactory;
import java.nio.charset.StandardCharsets;

@Component
public class ScpiItemReader {

    private final EntityManagerFactory entityManagerFactory;

    public ScpiItemReader(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public FlatFileItemReader<ScpiDto> reader() {
        return new FlatFileItemReaderBuilder<ScpiDto>()
                .name("scpiRequestItemReader")
                .resource(new ClassPathResource("scpi.csv"))
                .encoding(StandardCharsets.UTF_8.name())
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                        ScpiField.NOM.getColumnName(),
                        ScpiField.TAUX_DISTRIBUTION.getColumnName(),
                        ScpiField.MINIMUM_SOUSCRIPTION.getColumnName(),
                        ScpiField.LOCALISATION.getColumnName(),
                        ScpiField.SECTEURS.getColumnName(),
                        ScpiField.PRIX_PART.getColumnName(),
                        ScpiField.CAPITALISATION.getColumnName(),
                        ScpiField.GERANT.getColumnName(),
                        ScpiField.FRAIS_SOUSCRIPTION.getColumnName(),
                        ScpiField.FRAIS_GESTION.getColumnName(),
                        ScpiField.DELAI_JOUISSANCE.getColumnName(),
                        ScpiField.FREQUENCE_LOYERS.getColumnName(),
                        ScpiField.VALEUR_RECONSTITUTION.getColumnName(),
                        ScpiField.IBAN.getColumnName(),
                        ScpiField.BIC.getColumnName(),
                        ScpiField.DECOTE_DEMEMBREMENT.getColumnName(),
                        ScpiField.DEMEMBREMENT.getColumnName(),
                        ScpiField.CASHBACK.getColumnName(),
                        ScpiField.VERSEMENT_PROGRAMME.getColumnName(),
                        ScpiField.PUBLICITE.getColumnName()
                )

                .fieldSetMapper(new ScpiRequestFieldSetMapper())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Scpi> scpiPostgresReader() {

        JpaPagingItemReader<Scpi> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT s FROM Scpi s");
        reader.setPageSize(20);
        return reader;
    }


}