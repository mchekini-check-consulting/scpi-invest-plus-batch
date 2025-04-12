package fr.formationacademy.scpiinvestplusbatch.reader;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.enums.ScpiField;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;

@Component
public class ScpiRequestFieldSetMapper implements FieldSetMapper<ScpiDto> {
    @Override
    public ScpiDto mapFieldSet(FieldSet fieldSet) {
        return ScpiDto.builder()
                .name(fieldSet.readString(ScpiField.NOM.getColumnName()))
                .distributedRate(fieldSet.readString(ScpiField.TAUX_DISTRIBUTION.getColumnName()))
                .sharePrice(fieldSet.readString(ScpiField.PRIX_PART.getColumnName()))
                .reconstitutionValue(fieldSet.readString(ScpiField.VALEUR_DE_RECONSTITUTION.getColumnName()))
                .minimumSubscription(fieldSet.readInt(ScpiField.MINIMUM_SOUSCRIPTION.getColumnName()))
                .manager(fieldSet.readString(ScpiField.GERANT.getColumnName()))
                .capitalization(fieldSet.readLong(ScpiField.CAPITALISATION.getColumnName()))
                .subscriptionFees(fieldSet.readBigDecimal(ScpiField.FRAIS_SOUSCRIPTION.getColumnName()))
                .managementCosts(fieldSet.readBigDecimal(ScpiField.FRAIS_GESTION.getColumnName()))
                .enjoymentDelay(fieldSet.readInt(ScpiField.DELAI_JOUISSANCE.getColumnName()))
                .iban(fieldSet.readString(ScpiField.IBAN.getColumnName()))
                .bic(fieldSet.readString(ScpiField.BIC.getColumnName()))
                .scheduledPayment(fieldSet.readString(ScpiField.VERSEMENT_PROGRAMME.getColumnName()))
                .frequencyPayment(fieldSet.readString(ScpiField.FREQUENCE_LOYERS.getColumnName()))
                .cashback(fieldSet.readFloat(ScpiField.CASHBACK.getColumnName()))
                .advertising(fieldSet.readString(ScpiField.PUBLICITE.getColumnName()))
                .locations(fieldSet.readString(ScpiField.LOCALISATION.getColumnName()))
                .sectors(fieldSet.readString(ScpiField.SECTEURS.getColumnName()))
                .build();
    }
}
