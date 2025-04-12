package fr.formationacademy.scpiinvestplusbatch.entity.elastic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "scpi")
@Mapping(mappingPath = "mapping/scpi.json")
@Setting(settingPath = "settings/scpi.json")
public class ScpiDocument {

    @Id
    private Integer scpiId;

    @Field(type = FieldType.Text, analyzer = "edge_ngram_analyzer", searchAnalyzer = "standard")
    private String name;

    private BigDecimal distributionRate;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal sharePrice;

    private Boolean subscriptionFees;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal subscriptionFeesBigDecimal;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal managementCosts;

    private Long capitalization;

    private Integer enjoymentDelay;

    @Field(type = FieldType.Text)
    private String frequencyPayment;

    @Field(type = FieldType.Boolean)
    private Boolean scheduledPayment;

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
