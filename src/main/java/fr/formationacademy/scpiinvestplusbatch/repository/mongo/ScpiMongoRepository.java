package fr.formationacademy.scpiinvestplusbatch.repository.mongo;


import fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScpiMongoRepository extends MongoRepository<ScpiDocument, String> {
    Optional<ScpiDocument> findByName(String name);
}