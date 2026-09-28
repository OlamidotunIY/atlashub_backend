---
name: create-kafka-listener
description: >-
  Use this skill to create a Kafka event listener (BaseKafkaEventListener subclass) inside a module's infrastructure/messaging/listeners/ package, along with its stub event payload record.
---

# Create Kafka Listener

## Overview
Kafka listeners live in `infrastructure/messaging/listeners/` and extend `BaseKafkaEventListener`. They consume a specific Kafka topic, filter events by type using `processEventIfMatches()`, and delegate all business logic to a Command Handler — never inline.

Each listener is paired with an event payload record in `infrastructure/messaging/events/`.

## Pre-Requisites
Before scaffolding:
1. Confirm the Kafka topic name and consumer group ID with the team or module docs.
2. Confirm the event payload type name (e.g., `UserCreatedPayload`).
3. Confirm which Command Handler this listener will invoke after consuming the event.
4. If that command handler does not yet exist, trigger the `create-application-command` skill first.

## Generation Mode

**Step 1: Scaffold skeleton**
```powershell
.\.agents\skills\create-kafka-listener\scripts\scaffold-listener.ps1 `
    -Module "<module>" `
    -ListenerName "<ListenerName>" `
    -Topic "<topic.name>" `
    -GroupId "<group-id>" `
    -EventType "<EventType>"
```

Example:
```powershell
.\.agents\skills\create-kafka-listener\scripts\scaffold-listener.ps1 `
    -Module "iam" `
    -ListenerName "UserCreated" `
    -Topic "user.created" `
    -GroupId "iam-service" `
    -EventType "UserCreated"
```

This creates:
- `infrastructure/messaging/listeners/<ListenerName>Listener.java`
- `infrastructure/messaging/events/<EventType>Payload.java`

**Step 2: Inject business logic**
Use `replace_file_content` to:
1. Inject constructor dependencies (the command handler to call).
2. Complete the `processEventIfMatches()` body: deserialize the payload, build the command, and call `handler.execute(command)`.
3. Fill in the `<EventType>Payload` record fields (eventId is pre-populated; add domain-specific fields).

**Canonical listener pattern:**
```java
@Component
public class UserCreatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedListener.class);

    private final CreateUserHandler createUserHandler;

    @Autowired
    public UserCreatedListener(CreateUserHandler createUserHandler) {
        this.createUserHandler = createUserHandler;
    }

    @PostConstruct
    public void init() {
        log.info("UserCreatedListener initialized — topic: user.created");
    }

    @KafkaListener(topics = "user.created", groupId = "iam-service")
    public void listen(String message) {
        processEventIfMatches(message, UserCreatedPayload.class, payload -> {
            // TODO: Build command and call handler
            createUserHandler.execute(new CreateUserCommand(/* ... */));
        });
    }
}
```

## CRITICAL: Self-Correction & Verification Before Gradle
Before running Gradle, read back every file you created and verify:
- No wildcard imports
- No leftover `// TODO` comments
- `processEventIfMatches()` is fully implemented
- Payload record has all necessary fields

## Step 3: Gradle Verification
```
.\gradlew :atlashub-platform:<module>:compileJava
```

## Final Step: Git Commit & Push
```
git add <paths>
git commit -m "feat(<module>): add <ListenerName> Kafka listener"
git push origin HEAD
```
