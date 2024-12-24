package viettel.telecom.backend.service.datahub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GraphQLService {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String graphqlUrl = "http://localhost:8080/graphql";

    public GraphQLService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        if (graphqlUrl == null || graphqlUrl.isBlank()) {
            logger.error("The GraphQL URL is not provided. Ensure 'datahub.graphql.url' is configured.");
            throw new IllegalArgumentException("The GraphQL URL must not be null or empty. Please check the configuration.");
        }
        logger.info("GraphQL URL successfully injected: {}", graphqlUrl);
    }

    /**
     * Fetches table names for the specified database.
     */
    public String fetchTableNames(String databaseName) {
        logger.info("Fetching table names for database: {}", databaseName);

        String query = buildTableNamesQuery(databaseName);
        String jsonQuery = createJsonQuery(query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonQuery, headers);
        logger.debug("GraphQL request - URL: {}, Query: {}", graphqlUrl, jsonQuery);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(graphqlUrl, HttpMethod.POST, entity, String.class);
            logger.info("Received response for table names in database: {}", databaseName);
            return extractTableNames(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            throw new GraphQLServiceException(
                    String.format("Failed to fetch table names for database '%s'. HTTP Status: %s. Error: %s",
                            databaseName, e.getStatusCode(), e.getResponseBodyAsString()), e);
        } catch (Exception e) {
            throw new GraphQLServiceException(
                    String.format("Unexpected error while fetching table names for database '%s': %s",
                            databaseName, e.getMessage()), e);
        }
    }

    /**
     * Fetches fields for a specific table.
     */
    public String fetchTableFields(String tableName) {
        logger.info("Fetching field metadata for table: {}", tableName);

        String query = buildFieldMetadataQuery(tableName);
        String jsonQuery = createJsonQuery(query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonQuery, headers);
        logger.debug("GraphQL request - URL: {}, Query: {}", graphqlUrl, jsonQuery);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(graphqlUrl, HttpMethod.POST, entity, String.class);
            logger.info("Received response for table: {}", tableName);
            return extractFieldMetadata(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            throw new GraphQLServiceException(
                    String.format("Failed to fetch fields for table '%s'. HTTP Status: %s. Error: %s",
                            tableName, e.getStatusCode(), e.getResponseBodyAsString()), e);
        } catch (Exception e) {
            throw new GraphQLServiceException(
                    String.format("Unexpected error while fetching fields for table '%s': %s",
                            tableName, e.getMessage()), e);
        }
    }

    private String buildTableNamesQuery(String databaseName) {
        return String.format("""
                {
                  search(input: {
                    type: DATASET,
                    query: "%s",
                    start: 0,
                    count: 1000
                  }) {
                    searchResults {
                      entity {
                        ... on Dataset {
                          name
                        }
                      }
                    }
                  }
                }
                """, databaseName);
    }

    private String buildFieldMetadataQuery(String tableName) {
        return String.format("""
                {
                  search(input: {
                    type: DATASET,
                    query: "%s",
                    start: 0,
                    count: 1
                  }) {
                    searchResults {
                      entity {
                        ... on Dataset {
                          name
                          schemaMetadata {
                            fields {
                              fieldPath
                              description
                              nativeDataType
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """, tableName);
    }

    private String extractTableNames(String response) {
        logger.info("Extracting table names from the response.");

        ArrayNode tableNamesArray = objectMapper.createArrayNode();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode searchResults = rootNode.path("data").path("search").path("searchResults");

            for (JsonNode result : searchResults) {
                tableNamesArray.add(result.path("entity").path("name").asText());
            }

            logger.info("Successfully extracted table names.");
            return objectMapper.writeValueAsString(tableNamesArray);
        } catch (Exception e) {
            throw new GraphQLServiceException("Error processing table names response: " + e.getMessage(), e);
        }
    }

    private String extractFieldMetadata(String response) {
        logger.info("Extracting field metadata from the response.");

        ArrayNode fieldsArray = objectMapper.createArrayNode();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode fields = rootNode.path("data").path("search").path("searchResults")
                    .get(0).path("entity").path("schemaMetadata").path("fields");

            for (JsonNode field : fields) {
                ObjectNode fieldObject = objectMapper.createObjectNode();
                fieldObject.put("Field", field.path("fieldPath").asText());
                fieldObject.put("Type", field.path("nativeDataType").asText());
                fieldObject.put("Description", field.path("description").asText());
                fieldsArray.add(fieldObject);
            }

            logger.info("Successfully extracted field metadata.");
            return objectMapper.writeValueAsString(fieldsArray);
        } catch (Exception e) {
            throw new GraphQLServiceException("Error processing field metadata response: " + e.getMessage(), e);
        }
    }

    private String createJsonQuery(String query) {
        return String.format("{\"query\":\"%s\"}", query.replace("\"", "\\\""));
    }

    public static class GraphQLServiceException extends RuntimeException {
        public GraphQLServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
