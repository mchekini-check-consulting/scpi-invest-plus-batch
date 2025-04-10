package fr.formationacademy.scpiinvestplusbatch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class ScpiIndexService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "scpi";

    public ScpiIndexService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    private void createIndexWithMapping() throws IOException {
        String mappingJson = """
                {
                  "settings": {
                    "analysis": {
                      "tokenizer": {
                        "edge_ngram_tokenizer": {
                          "type": "edge_ngram",
                          "min_gram": 2,
                          "max_gram": 20,
                          "token_chars": ["letter"]
                        }
                      },
                      "analyzer": {
                        "edge_ngram_analyzer": {
                          "type": "custom",
                          "tokenizer": "edge_ngram_tokenizer",
                          "filter": ["lowercase", "asciifolding"]
                        }
                      }
                    }
                  },
                  "mappings": {
                    "properties": {
                      "id": { "type": "keyword" },
                      "name": {
                        "type": "text",
                        "analyzer": "edge_ngram_analyzer",
                        "fields": {
                          "keyword": { "type": "keyword" }
                        }
                      },
                      "distributionRate": {
                        "type": "scaled_float",
                        "scaling_factor": 100
                      },
                      "subscriptionFeesBigDecimal": {
                        "type": "scaled_float",
                        "scaling_factor": 100
                      },
                      "managementCosts": {
                        "type": "scaled_float",
                        "scaling_factor": 100
                      },
                      "capitalization": { "type": "long" },
                      "enjoymentDelay": { "type": "integer" },
                      "frequencyPayment": {
                        "type": "text",
                        "fields": {
                          "keyword": { "type": "keyword" }
                        }
                      },
                      "minimumSubscription": { "type": "integer" },
                      "countryDominant": {
                        "type": "object",
                        "properties": {
                          "country": {
                            "type": "text",
                            "fields": {
                              "keyword": { "type": "keyword" }
                            }
                          },
                          "countryPercentage": { "type": "float" }
                        }
                      },
                      "sectorDominant": {
                        "type": "object",
                        "properties": {
                          "name": {
                            "type": "text",
                            "fields": {
                              "keyword": { "type": "keyword" }
                            }
                          },
                          "sectorPercentage": { "type": "float" }
                        }
                      },
                      "locations": {
                        "type": "nested",
                        "properties": {
                          "country": {
                            "type": "text",
                            "fields": {
                              "keyword": { "type": "keyword" }
                            }
                          },
                          "countryPercentage": { "type": "float" }
                        }
                      },
                      "sectors": {
                        "type": "nested",
                        "properties": {
                          "name": {
                            "type": "text",
                            "fields": {
                              "keyword": { "type": "keyword" }
                            }
                          },
                          "sectorPercentage": { "type": "float" }
                        }
                      }
                    }
                  }
                }
                """;

        try {
            CreateIndexResponse response = elasticsearchClient.indices().create(c -> c
                    .index(INDEX_NAME)
                    .withJson(new ByteArrayInputStream(mappingJson.getBytes(StandardCharsets.UTF_8)))
            );
            log.info("Index '{}' created: {}", INDEX_NAME, response.acknowledged());
        } catch (IOException e) {
            log.error("Failed to create index '{}' with mapping", INDEX_NAME, e);
            throw e;
        }
    }

    public void createIndexIfNotExists() throws IOException {
        BooleanResponse indexExistsResponse = elasticsearchClient.indices().exists(b -> b.index(INDEX_NAME));
        boolean indexExists = indexExistsResponse.value();

        if (!indexExists) {
            log.info("Index '{}' does not exist. Creating index with mapping...", INDEX_NAME);
            createIndexWithMapping();
        } else {
            log.info("Index '{}' already exists. Skipping creation.", INDEX_NAME);
        }
    }
}
