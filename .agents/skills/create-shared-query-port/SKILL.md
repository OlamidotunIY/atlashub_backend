---
name: create-shared-query-port
description: >-
  Use this skill to create or update a Shared Query Port interface.
---

# Create Shared Query Port Workflow

Shared Query Ports allow modules to query data from other modules without crossing domain boundaries or returning actual Entity objects. They are defined centrally in the `shared` module.

## Rule 1: Always check for existence first
Never create duplicate query ports that do the same thing. Look for existing ports in `atlashub-shared/src/main/java/com/atlashub/shared/domain/ports/query/`. 
If a port for the entity already exists (e.g., `UserQueryPort`), update it by adding new methods using `replace_file_content`.

## Rule 2: Scaffold Skeleton
If it doesn't exist, use the provided PowerShell script to safely generate the baseline interface:
```powershell
.agents\skills\create-shared-query-port\scripts\scaffold-query-port.ps1 -EntityName "<EntityName>"
```
*Example: `-EntityName "User"` will create `UserQueryPort`.*

## Rule 3: DTOs, Not Entities
**CRITICAL:** Query Ports MUST NEVER return Entity objects. They must use nested Java records (DTOs) for their return types to keep modules fully detached.
Example:
```java
public interface UserQueryPort {
    Optional<UserDto> findById(Long userId);

    record UserDto(Long id, String email, boolean isActive) {}
}
```

## Rule 4: No Inline Imports
You MUST NOT use wildcard imports (`import java.util.*`). You MUST NOT use inline fully qualified class names inside the code (e.g., `java.util.Optional<String>`). Always import explicitly at the top of the file.

## Batch Processing
This skill supports processing multiple query ports simultaneously. You can use `invoke_subagent` for large batches.

## Step 5: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated port compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-shared:compileJava`
