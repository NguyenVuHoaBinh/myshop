@startuml
actor User as U
package "Frontend UI" {
  component "Prompt Builder UI" as UI
}

package "Backend Services" {
  component "API Gateway" as API_GW
  component "Prompt Service" as Prompt_SVC
  component "Metadata Service" as Meta_SVC
  component "LLM Service" as LLM_SVC
}

package "Data & Models" {
  database "MariaDB" as DB_RDBMS
  database "Redis (Cache/LLM Memory)" as DB_Redis
  database "Elasticsearch" as DB_ES
  component "DataHub" as DataHub
  component "LLM Models" as LLM
}

U --> UI : Enters Prompt Inputs\nand Model Selection
UI --> API_GW : Submit Prompt Config or Test Request
API_GW --> Meta_SVC : Fetch Metadata (Objects/Fields)
Meta_SVC --> DB_ES : Query Metadata
Meta_SVC --> DB_Redis : Cache Metadata
Meta_SVC --> API_GW : Return Metadata

API_GW --> Prompt_SVC : Store/Fetch Prompt Config
Prompt_SVC --> DB_RDBMS : Store Config
Prompt_SVC --> DB_Redis : Cache Config

API_GW --> LLM_SVC : Send Test Prompt Request
LLM_SVC --> DB_Redis : Retrieve LLM Context (Memory)
LLM_SVC --> LLM : Send Prompt to Selected Model
LLM --> LLM_SVC : Return Generated Output
LLM_SVC --> DB_Redis : Update LLM Memory
LLM_SVC --> API_GW : Return Response

API_GW --> UI : Return LLM Output
UI --> U : Display LLM Response
@enduml


@startuml
title Schema Management Flow

actor User

User -> SchemaManagementController: Interacts via API
SchemaManagementController -> SchemaSyncService: Sync Table Names/Fields
SchemaSyncService -> GraphQLService: Fetch Table Names/Fields
GraphQLService -> DataHub: Query Metadata

SchemaManagementController -> ElasticsearchDatahubService: Fetch Table Names/Fields
ElasticsearchDatahubService -> Elasticsearch: Perform Query/Indexing

note right of SchemaManagementController
Unified Schema APIs:
- Sync Tables
- Sync Fields
- Fetch Tables
- Fetch Fields
end note

@enduml

@startuml
title Ingestion Management Flow

actor User

User -> IngestionController: Start Ingestion
IngestionController -> ConfigGeneratorService: Generate Config
ConfigGeneratorService -> FileSystem: Write Config to File

IngestionController -> DataHubIngestionService: Trigger Ingestion Pipeline
DataHubIngestionService -> IngestionProcessService: Start Pipeline Process
IngestionProcessService -> PipelineScript: Execute Script
PipelineScript --> IngestionProcessService: Log Output

IngestionController -> ElasticsearchDatahubService: Save Ingestion Results
ElasticsearchDatahubService -> Elasticsearch: Index Results

note right of IngestionController
Ingestion APIs:
- Start Ingestion
- Fetch History
end note
@enduml
