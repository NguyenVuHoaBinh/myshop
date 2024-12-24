package viettel.telecom.backend.datahub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import viettel.telecom.backend.entity.promptbuilder.ObjectField;
import viettel.telecom.backend.service.datahub.GraphQLService;
import viettel.telecom.backend.service.elasticsearch.ElasticsearchDatahubService;
import viettel.telecom.backend.service.elasticsearch.SchemaSyncService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class SchemaSyncServiceTest {

    @Mock
    private GraphQLService graphQLService;

    @Mock
    private ElasticsearchDatahubService elasticsearchDatahubService;

    @InjectMocks
    private SchemaSyncService schemaSyncService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSyncTableNames() throws Exception {
        // Mock GraphQL response
        String mockResponse = "[\"Users\", \"Orders\"]";
        when(graphQLService.fetchTableNames("TestDB")).thenReturn(mockResponse);

        // Execute the sync method
        schemaSyncService.syncTableNames("TestDB");

        // Verify Elasticsearch operations
        ArgumentCaptor<List<ObjectField>> captor = ArgumentCaptor.forClass(List.class);
        verify(elasticsearchDatahubService, times(1)).bulkIndexTables(captor.capture());

        // Validate captured data
        List<ObjectField> indexedTables = captor.getValue();
        assertEquals(2, indexedTables.size());
        assertEquals("Users", indexedTables.get(0).getObjectName());
        assertEquals("Orders", indexedTables.get(1).getObjectName());
    }

    @Test
    public void testSyncTableFields() throws Exception {
        // Mock GraphQL response
        String mockResponse = """
        [
          {"Field": "id", "Type": "integer"},
          {"Field": "name", "Type": "string"}
        ]
        """;
        when(graphQLService.fetchTableFields("Users")).thenReturn(mockResponse);

        // Execute the sync method
        schemaSyncService.syncTableFields("Users");

        // Verify Elasticsearch operations
        verify(elasticsearchDatahubService, times(1))
                .updateTableFields(eq("Users"), eq(List.of("id", "name")));
    }
}

