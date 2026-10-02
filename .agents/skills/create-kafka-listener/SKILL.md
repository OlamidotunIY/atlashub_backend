---
name: create-kafka-listener
description: Create or audit an AtlasHub Kafka listener that deserializes one event and delegates exclusively to a command handler.
---

# Create Kafka listener

Load `atlashub-module-workflow`, resolve artifact `listener`, and read producer event, consumer module docs, saga docs, topic, group, and base listener API. Invoke `create-application-command` first if the consumer command is absent.

- Extend the actual `com.atlashub.shared.application.messaging.BaseKafkaEventListener` contract.
- Listener dependencies are `ObjectMapper`/messaging mechanics and command handlers only. Repositories, Spring Data, domain services, caches, and provider adapters are forbidden.
- Listener performs no business validation or persistence. It maps event to command and calls `handler.execute(command)`.
- Use stable topic/group constants from docs. Multiple modules with the same listener class name require unique Spring bean names or module-qualified class names.
- Register the exact event contract and use base idempotency/error handling. Do not invent a second payload shape when the existing envelope can be consumed.
- No empty/stub listeners. Delete obsolete listeners after migrations.

Test that a matching event invokes the correct command once and irrelevant/duplicate/malformed events follow base behavior. Repository mocks in listener tests indicate an architecture violation. Run validator, module tests, and context/startup verification.
