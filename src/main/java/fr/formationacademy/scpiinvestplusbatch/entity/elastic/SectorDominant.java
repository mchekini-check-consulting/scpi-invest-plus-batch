package fr.formationacademy.scpiinvestplusbatch.entity.elastic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectorDominant {

    @Field(type = FieldType.Text)
    private String name;
    private Float sectorPercentage;
}