---
name: create-domain-event
description: Create or audit an AtlasHub domain event from documented producer and consumer contracts.
---

# Create domain event

Load `atlashub-module-workflow`, resolve artifact `event`, and read the module design, saga/communication docs, producing aggregate, and consuming contracts.

- Create only documented or explicitly approved events.
- Use past-tense business names and the module's existing `DomainEvent` structure.
- Payload contains only data consumers require and the producer owns at publication time.
- Do not repeat the producer ID when `aggregateId` already carries it.
- Never include secrets, hashes, raw tokens, provider credentials, or unnecessary personal data.
- Preserve published compatibility; breaking changes require versioning or coordinated migration.
- The aggregate registers events; handlers do not fabricate events representing aggregate transitions.

Verify no equivalent event exists, use the generator with explicit payload fields, and create consumer listeners only when docs require them. Validate producer and consumers and add serialization tests.
