---
name: create-spring-data-repository
description: >-
  Use this skill to create Spring Data JPA Repository interfaces for a given JPA entity.
---

# Create Spring Data Repository Workflow

Spring Data repositories live in the infrastructure layer under `infrastructure/persistence/repositories/`.

## Batch Processing (Multiple Repositories)
This skill supports processing a list of multiple repositories simultaneously.
1. You MUST process every repository iteratively. Do not skip any.
2. **Execution Strategy:** You MUST ALWAYS use `invoke_subagent` to spawn a concurrent team of subagents when processing multiple items.


## Subagent Separation of Concerns (Vertical Slicing)
When using invoke_subagent to process multiple items, you MUST adhere to strict Separation of Concerns (SoC) via **Vertical Slicing**:
1. **One Subagent per Item**: Assign each subagent exactly ONE item (e.g., one entity, one command, one mapper).
2. **End-to-End Flow**: The subagent is responsible for checking its own pre-requisites. If any dependencies (e.g., Value Objects, Events, Entities, Mappers) are missing, the subagent MUST execute the instructions of those respective skills to generate them before proceeding.
3. **Independent Verification**: The subagent MUST run its own verification (e.g., .\gradlew compileJava for the module) to ensure its specific slice is perfect.
4. **Independent Commit**: Once verified, the subagent MUST commit its own changes to Git and end its turn. Do not wait for a parent agent to commit.

## Module-Wide Generation
If the user asks you to "create spring data repositories for all entities in `<module>`", you MUST:
1. Locate all Domain Entity classes in `atlashub-platform/<module>/src/main/java/com/atlashub/<module>/domain/entities/` (e.g., using `find_by_name` or `list_dir`).
2. Ignore standard records or value objects; focus only on the actual Domain Entities / Aggregate Roots.
3. Use `invoke_subagent` to spawn a concurrent team of subagents to process EVERY entity found simultaneously.

## Rule 1: Scaffold Base Structure
You MUST use the provided PowerShell script to safely generate the baseline file structure and boilerplate files:
```powershell
.\.agents\skills\create-spring-data-repository\scripts\scaffold-spring-data.ps1 -Module "<module_name>" -EntityName "<EntityName>"
```

## Rule 2: Full Logic Implementation Requirement
After scaffolding, use `replace_file_content` to implement the interface.

**Implementation Rules:**
1. **Naming**: The interface MUST be named `SpringData<EntityName>Repository`.
2. **Inheritance**: MUST extend `org.springframework.data.jpa.repository.JpaRepository<<EntityName>Jpa, Long>`.
3. **Custom Methods**:
   - You MUST mirror any custom lookup methods defined in the corresponding Domain Repository.
   - For example, if the Domain Repository has `Optional<AuthAccount> findByAccountId(String accountId);`, you must add `Optional<AuthAccountJpa> findByAccountId(String accountId);`.
   - If a method name cannot be automatically resolved by Spring Data (too complex), you MUST write the explicit `@Query(...)` using valid JPQL or native SQL.
4. **No Inline Imports**: All imports must be explicitly declared at the top.

## CRITICAL: Self-Correction & Verification Before Gradle
Before you (or your dedicated subagents) run the Gradle compiler check, you MUST ALWAYS perform a strict self-review of all created and modified files. 
- Read back the files you just wrote using `cat` or `view_file`.
- Check against ALL rules (e.g., absolutely NO inline imports, NO wildcard imports, NO leftover `// TODO`s, NO `return null;` placeholders).
- If ANY rule is violated, you MUST fix it immediately using `replace_file_content`.
- Only after this explicit re-confirmation are you allowed to run `.\gradlew compileJava`. Dedicated subagents MUST also follow this rule.

## Step 3: Gradle Compilation Check
You MUST run the Gradle compiler to prove to the user that your generated code compiles properly.
**CRITICAL RULE:** NEVER run `.\gradlew compileJava` globally. You MUST strictly target the module you are working on.
Example: `.\gradlew :atlashub-platform:iam:compileJava`


## Final Step: Git Commit & Push
Verification is NOT the final step; committing your work is.
After your code successfully compiles and passes all verification rules, you (and every individual subagent) MUST commit and push your changes to GitHub.
1. Stage your specific files: "git add <paths_to_your_files>"
2. Commit your changes using standard Conventional Commits formatting (e.g., "feat(<module>): add <feature>", "refactor(<module>): ...").
3. Push to the remote repository: "git push origin HEAD"
**CRITICAL:** If you are a subagent, you MUST commit and push your own specific work independently as soon as it passes compilation. Do not wait for the parent agent.
