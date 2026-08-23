# AtlasPay Package Structure

AtlasPay modules follow a bounded-context, clean architecture layout. Public
wire contracts, database table names, Kafka topics, and domain event names must
not change as part of package refactors.

```text
com.atlaspay.<context>
  domain
    model
    valueobject
    event
    service
    repository
    exception

  application
    command
    query
    dto
    port
      in
      out
    usecase
    service
    saga

  infrastructure
    persistence
      entity
      repository
      mapper
      adapter
      query
    messaging
      consumer
      producer
      mapper
      dto
    external
      <provider>
        adapter
        client
        dto
        mapper
        config
    cache
      adapter
      mapper
    config

  presentation
    rest
      controller
      request
      response
      mapper
    websocket
    webhook
```

## Dependency Rules

- `domain` is framework-free: no Spring, JPA, Kafka, Redis, Servlet, or HTTP
  dependencies. Domain repositories are interfaces only.
- `application` owns orchestration. Use cases depend on domain types and
  application ports, not infrastructure adapters.
- `infrastructure` implements outbound ports for persistence, cache, messaging,
  security providers, and external services.
- `presentation` adapts inbound protocols such as REST, webhooks, and
  WebSockets. It delegates to application use cases.
- `atlaspay-app` is the composition root for application-wide configuration,
  security, exception handling, OpenAPI, and module wiring.

## Integration Patterns

- Kafka consumers live in `infrastructure.messaging.consumer`.
- Kafka producers live in `infrastructure.messaging.producer`.
- Integration event DTOs live in `infrastructure.messaging.dto`.
- Domain events live in `domain.event`.
- Outbox, retry, DLQ, and scheduler infrastructure belongs in the eventbus or
  shared infrastructure modules when it is platform-wide.
