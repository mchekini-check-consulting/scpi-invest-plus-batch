package fr.formationacademy.scpiinvestplusbatch.entity.elastic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "scpi")
public class ScpiDocument {

    @Id
    private String id;

    private Integer scpiId;

    @Field(type = FieldType.Text, analyzer = "edge_ngram_analyzer", searchAnalyzer = "standard")
    private String name;

    private BigDecimal distributionRate;

    private Boolean subscriptionFees;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal subscriptionFeesBigDecimal;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal managementCosts;

    private Long capitalization;

    private Integer enjoymentDelay;

    @Field(type = FieldType.Text)
    private String frequencyPayment;

    private Integer minimumSubscription;

    @Field(type = FieldType.Object)
    private CountryDominant countryDominant;

    @Field(type = FieldType.Object)
    private SectorDominant sectorDominant;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<LocationDocument> locations;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<SectorDocument> sectors;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<StatYearDocument> statYears;

}
