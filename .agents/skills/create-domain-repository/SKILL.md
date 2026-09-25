---
name: create-domain-repository
description: >-
  Use this skill to create a new Domain Repository interface for an Aggregate Root/Entity.
---

# Create Domain Repository Workflow

Domain Repositories belong in the domain layer (`domain/repositories/`) and define the persistence contract for an Entity.

## Rule 1: Always check for existence first
Before creating a repository, check if one already exists for the entity (e.g., `UserRepository.java`). If it exists, update it rather than creating a new one.

## Rule 2: Scaffold Skeleton
Use the provided PowerShell script to safely generate the baseline interface and prevent accidental overwrites:
```powershell
.agents\skills\create-domain-repository\scripts\scaffold-repository.ps1 -Module "<module_name>" -EntityName "<EntityName>"
```

## Rule 3: Do NOT duplicate base methods
The `Repository<T>` base interface (which you are extending) already provides these exact 5 methods:
- `Long nextIdentity();`
- `T save(T entity);`
- `Optional<T> findById(Long id);`
- `void deleteById(Long id);`
- `boolean existsById(Long id);`

**CRITICAL:** Do NOT redefine these methods in your newly generated repository interface. Only add *custom* query methods (e.g., `Optional<AuthAccount> findByEmail(String email);`) if explicitly required.

## Rule 4: No Inline Imports
You MUST NOT use wildcard imports (`import java.util.*`). You MUST NOT use inline fully qualified class names inside the code (e.g., `java.util.Optional<String>`). Always import explicitly at the top of the file.

## Batch Processing
This skill supports processing multiple repositories simultaneously. You can use `invoke_subagent` for large batches.


## CRITICAL: Self-Correction & Verification Before Gradle
Before you (or your dedicated subagents) run the Gradle compiler check, you MUST ALWAYS perform a strict self-review of all created and modified files. 
- Read back the files you just wrote using "cat" or "view_file".
- Check against ALL rules (e.g., absolutely NO inline imports, NO wildcard imports, NO leftover "// TODO"s, NO "return null;" placeholders).
- If ANY rule is violated, you MUST fix it immediately using "replace_file_content".
- Only after this explicit re-confirmation are you allowed to run ".\gradlew compileJava". Dedicated subagents MUST also follow this rule.

## Step 5: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated repository compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-platform:iam:compileJava`
