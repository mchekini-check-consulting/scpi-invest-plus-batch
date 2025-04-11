package fr.formationacademy.scpiinvestplusbatch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class ScpiIndexService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "scpi";

    public ScpiIndexService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    private void createIndexWithMapping() throws IOException {
        InputStream mappingStream = getClass().getClassLoader().getResourceAsStream("mappings/scpi-mapping.json");

        if (mappingStream == null) {
            throw new IOException("Fichier mapping introuvable !");
        }

        elasticsearchClient.indices().create(c -> c
                .index(INDEX_NAME)
                .withJson(mappingStream)
        );
    }

    void createIndexIfNotExists() throws IOException {
        BooleanResponse indexExistsResponse = elasticsearchClient.indices().exists(b -> b.index(INDEX_NAME));
        boolean indexExists = indexExistsResponse.value();

        if (!indexExists) {
            createIndexWithMapping();
        }
    }

}