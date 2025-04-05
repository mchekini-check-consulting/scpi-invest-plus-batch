package fr.formationacademy.scpiinvestplusbatch.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "scpis")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScpiDocument {

    @Id
    private String id;
    private String name;
    private String iban;
    private String bic;
    private BigDecimal sharePrice;
}
