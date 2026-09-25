---
name: create-domain-event
description: >-
  Use this skill whenever the user asks to "create domain events" or "create a domain event".
  The user will provide the event name, its purpose, and the target module name.
---

# Create Domain Event

You are responsible for generating Domain Event records in the Atlashub backend codebase according to strict architectural guidelines.

## Inputs Required from User
If the user does not provide all of these, you MUST ask for the missing ones before proceeding:
1. **Event Name** (e.g., `ApiKeyRevokedEvent`)
2. **Purpose / Payload details** (what the event does or what data it carries)
3. **Module Name** (e.g., `iam`, `accounts`, etc.)

## Step 1: File Location Resolution
Do not assume the root folder (e.g., do not hardcode `atlashub-platform`).
Instead, use your search tools (like `find_by_name`) to locate the root directory matching the `<module>` name provided by the user.
Once you find the module's root directory, the file path must strictly be:
`<FoundModulePath>/src/main/java/com/atlashub/<module>/domain/events/<EventName>.java`

*Note: Replace `<module>` with the module name provided by the user in lowercase.*

## Step 2: Code Generation
You must strictly follow this exact boilerplate structure. **Do not use wildcard imports (`import java.util.*;`).**

```java
package com.atlashub.<module>.domain.events;

import com.atlashub.shared.domain.events.DomainEvent;
import java.time.ZonedDateTime;

public record <EventName>(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<<EventName>.Payload> {

    public record Payload(
        // Inject the specific payload properties requested by the user here
    ) {
    }
}
```

## Step 3: File Creation
Use your file writing tools to create the `.java` file at the exact path resolved in Step 1, populating it with the code from Step 2.

## Step 4: Verification
Confirm to the user that the file was created and provide the clickable path to the file.
