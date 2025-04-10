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
public class StatYearDocument {

    @Field(type = FieldType.Integer)
    private Integer scpiId;

    @Field(type = FieldType.Integer)
    private Integer year;

    @Field(type = FieldType.Float)
    private Float distributionRate;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal sharePrice;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal reconstitutionValue;
}
