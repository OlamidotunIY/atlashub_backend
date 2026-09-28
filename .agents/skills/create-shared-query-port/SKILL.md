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



## Subagent Separation of Concerns (Vertical Slicing)
When using invoke_subagent to process multiple items, group related work into a small number of subagents instead of spawning many to avoid Gradle lock contentions and context fragmentation.
1. **Group by Feature/Entity**: Assign each subagent a primary entity and ALL of its related components (e.g., its Value Objects, Exceptions, Events, Mappers, Repositories). NEVER create one subagent per single file.
2. **End-to-End Flow**: The subagent is responsible for checking its own pre-requisites and generating all missing dependencies sequentially within its own turn.
3. **Independent Verification**: The subagent MUST run its own verification (e.g., .\gradlew compileJava for the module) once for the entire group of files to ensure its specific slice is perfect.
4. **Independent Commit**: Once verified, the subagent MUST commit its own changes to Git and end its turn. Do not wait for a parent agent to commit.

## CRITICAL: Self-Correction & Verification Before Gradle
Before you (or your dedicated subagents) run the Gradle compiler check, you MUST ALWAYS perform a strict self-review of all created and modified files. 
- Read back the files you just wrote using "cat" or "view_file".
- Check against ALL rules (e.g., absolutely NO inline imports, NO wildcard imports, NO leftover "// TODO"s, NO "return null;" placeholders).
- If ANY rule is violated, you MUST fix it immediately using "replace_file_content".
- Only after this explicit re-confirmation are you allowed to run ".\gradlew compileJava". Dedicated subagents MUST also follow this rule.

## Step 5: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated port compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-shared:compileJava`


## Final Step: Git Commit & Push
Verification is NOT the final step; committing your work is.
After your code successfully compiles and passes all verification rules, you (and every individual subagent) MUST commit and push your changes to GitHub.
1. Stage your specific files: "git add <paths_to_your_files>"
2. Commit your changes using standard Conventional Commits formatting (e.g., "feat(<module>): add <feature>", "refactor(<module>): ...").
3. Push to the remote repository: "git push origin HEAD"
**CRITICAL:** If you are a subagent, you MUST commit and push your own specific work independently as soon as it passes compilation. Do not wait for the parent agent.

