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
@Document(indexName = "scpis")
public class ScpiElasticsearchDocument {

    @Id
    private String id;
    private String name;
    private Integer minimumSubscription;
    private BigDecimal subscriptionFees;
    private BigDecimal managementCosts;
    private String iban;
    private String bic;
    private String frequencyPayment;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<LocationDocument> locations;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<SectorDocument> sectors;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<StatYearDocument> statYears;
}
