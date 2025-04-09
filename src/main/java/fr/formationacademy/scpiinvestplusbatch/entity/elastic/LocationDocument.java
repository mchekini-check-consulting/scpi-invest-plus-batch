package fr.formationacademy.scpiinvestplusbatch.entity.elastic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDocument {

    private String country;

    @Field(type = FieldType.Float)
    private BigDecimal countryPercentage;
}
