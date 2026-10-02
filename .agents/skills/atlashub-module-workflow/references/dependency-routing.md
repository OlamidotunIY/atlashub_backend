# AtlasHub skill dependency routing

Load `atlashub-module-workflow` first for every row.

| Requested artifact | Required checks and dependent skills |
|---|---|
| Domain entity / aggregate | `create-domain-valueobject`; `create-domain-error` for every invariant failure; `create-domain-event` for every documented transition; `create-domain-repository` when persisted |
| Domain value object | Reuse shared value object when truly universal; otherwise `create-domain-error` for validation failures |
| Domain service | `create-domain-service`; create a domain port first when external information is required |
| Command | `create-application-command`; repository contract for same-module writes; shared query port for cross-module reads; domain port for external effects |
| Query | `create-application-query`; same-module repository or shared query port only; never another module's JPA projection/repository |
| Persisted aggregate | `create-jpa-entity` -> `create-domain-mapper` -> `create-spring-data-repository` -> `create-repository-adapter` |
| Cross-module read | `create-shared-query-port` -> `create-query-port-adapter` in the owning module's `infrastructure/persistence/adapters`; consuming module depends only on shared |
| Cross-module write | Domain event -> publishing aggregate/command -> `create-kafka-listener` in consumer -> consumer command handler |
| External provider | `create-domain-port` in owning module -> `create-external-adapter`; application depends on port, never provider class |
| REST endpoint | command/query first -> `create-presentation-dto` -> `create-controller`; one controller per resource/entity |
| Kafka listener | event contract and command handler first -> `create-kafka-listener`; listener calls handler only |
| Scheduler | command handler first -> `create-scheduler`; place it in `infrastructure/messaging/schedulers`; scheduler calls handler only |
| Any completed slice | `create-module-test` for domain/application/adapter behavior -> `audit-module` |

Every row also requires `create-module-test` in the same leaf-skill run. Create tests for new artifacts and update tests for changed artifacts. Add integration/context tests for persistence, messaging, external, cache, controller/security, and Spring-wiring changes. After all tests pass, run `Complete-AtlashubSkill.ps1` to commit only that leaf skill's production and test files.

Stop dependency traversal only when every referenced type exists, matches the current docs, has a real caller, has current passing tests, and has been committed by its leaf skill. Do not generate unused layers merely because they appear in this table.
