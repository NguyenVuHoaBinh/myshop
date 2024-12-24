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

    private static final String DEFAULT_GRAPHQL_URL = "http://localhost:8080/api/graphql";

    public GraphQLService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        if (DEFAULT_GRAPHQL_URL == null || DEFAULT_GRAPHQL_URL.isBlank()) {
            logger.error("The GraphQL URL is not provided. Ensure 'datahub.graphql.url' is configured.");
            throw new IllegalArgumentException("The GraphQL URL must not be null or empty. Please check the configuration.");
        }
        logger.info("GraphQL URL successfully set: {}", DEFAULT_GRAPHQL_URL);
    }

    public String fetchTableNames(String databaseName) {
        logger.info("Fetching table names for database: {}", databaseName);

        if (databaseName == null || databaseName.isBlank()) {
            throw new IllegalArgumentException("Database name must not be null or empty");
        }

        String query = buildTableNamesQuery(databaseName);
        String jsonQuery = createJsonQuery(query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonQuery, headers);
        logger.debug("Generated GraphQL request: {}", jsonQuery);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(DEFAULT_GRAPHQL_URL, HttpMethod.POST, entity, String.class);
            logger.info("Received response for database: {}", databaseName);
            return extractTableNames(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("Failed to fetch table names for database '{}'. Status: {}, Error: {}", databaseName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new GraphQLServiceException("Request to GraphQL endpoint failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching table names for database '{}': {}", databaseName, e.getMessage(), e);
            throw new GraphQLServiceException("Unexpected error: " + e.getMessage(), e);
        }
    }

    public String fetchTableFields(String tableName) {
        logger.info("Fetching field metadata for table: {}", tableName);

        String query = buildFieldMetadataQuery(tableName);
        String jsonQuery = createJsonQuery(query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonQuery, headers);
        logger.debug("Generated GraphQL request: {}", jsonQuery);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(DEFAULT_GRAPHQL_URL, HttpMethod.POST, entity, String.class);
            logger.info("Received response for table: {}", tableName);
            return extractFieldMetadata(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("Failed to fetch fields for table '{}'. Status: {}, Error: {}", tableName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new GraphQLServiceException("Request to GraphQL endpoint failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching fields for table '{}': {}", tableName, e.getMessage(), e);
            throw new GraphQLServiceException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private String buildTableNamesQuery(String databaseName) {
        return """
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
            """.formatted(databaseName);
    }

    private String buildFieldMetadataQuery(String tableName) {
        return """
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
            """.formatted(tableName);
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
            logger.error("Error processing table names response: {}", e.getMessage(), e);
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
            logger.error("Error processing field metadata response: {}", e.getMessage(), e);
            throw new GraphQLServiceException("Error processing field metadata response: " + e.getMessage(), e);
        }
    }

    private String createJsonQuery(String query) {
        return String.format("{\"query\":\"%s\"}", query.replace("\"", "\\\"").replace("\n", " "));
    }

    public static class GraphQLServiceException extends RuntimeException {
        public GraphQLServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
