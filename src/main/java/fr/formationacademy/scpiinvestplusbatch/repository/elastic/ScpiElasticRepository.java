package fr.formationacademy.scpiinvestplusbatch.repository.elastic;

import fr.formationacademy.scpiinvestplusbatch.entity.elastic.ScpiDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScpiElasticRepository extends ElasticsearchRepository<ScpiDocument, String> {
    Optional<ScpiDocument> findByName(String name);
}
