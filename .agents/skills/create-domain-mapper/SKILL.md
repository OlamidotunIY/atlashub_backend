---
name: create-domain-mapper
description: Create or audit a strict MapStruct mapper between one AtlasHub aggregate and its JPA record.
---

# Create domain mapper

Load `atlashub-module-workflow`, resolve artifact `adapter`, and read domain/JPA types completely. Invoke `create-jpa-entity` first if absent.

- Follow the module's actual JPA suffix/capitalization; never assume `JpaEntity`.
- Extend `DomainMapper<Domain, Jpa>` and use Spring component model with `ReportingPolicy.ERROR`.
- Use explicit mappings where names differ; never silence unknown fields globally.
- Rehydration must not use a factory that emits events or resets timestamps/status.
- Shared `ValueObjectMapper` holds only universal mappings; module mappings stay local.
- Do not distort correct domain naming merely to satisfy MapStruct.

Test full round trips and inspect generated diagnostics. Run validator and module compile.
