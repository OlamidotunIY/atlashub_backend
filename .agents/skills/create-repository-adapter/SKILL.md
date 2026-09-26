---
name: create-repository-adapter
description: >-
  Use this skill to create Repository Adapters in the infrastructure layer that implement Domain Repositories.
---

# Create Repository Adapter Workflow

Repository adapters live in the infrastructure layer under `infrastructure/persistence/repositories/`.
They map the domain entity methods to the Spring Data JPA layer.

## Pre-Requisites (Dependencies)
Before generating a Repository Adapter, you MUST verify that the following exist:
1. **JPA Entity**: The `*Jpa` entity must exist. If not, trigger `create-jpa-entity`.
2. **Spring Data Repository**: The `SpringData*Repository` interface must exist. If not, trigger `create-spring-data-repository`.
3. **Domain Mapper**: The `*Mapper` interface must exist. If not, trigger `create-domain-mapper`.

## Batch Processing (Multiple Adapters)
This skill supports processing a list of multiple adapters simultaneously.
1. You MUST process every adapter iteratively. Do not skip any.
2. **Execution Strategy:** You MUST ALWAYS use `invoke_subagent` to spawn a concurrent team of subagents when processing multiple items.

## Module-Wide Generation
If the user asks you to "create adapters for all domain repositories in `<module>`", you MUST:
1. Locate all Domain Repository interfaces in `atlashub-platform/<module>/src/main/java/com/atlashub/<module>/domain/repositories/` (e.g., using `find_by_name` or `list_dir`).
2. Identify the core Domain Entity for each repository (e.g., `AuthAccountRepository` manages `AuthAccount`).
3. Use `invoke_subagent` to spawn a concurrent team of subagents to process EVERY repository found simultaneously.
4. Each subagent will be responsible for fulfilling the Pre-Requisites (JPA Entity, Spring Data Repo, Mapper) and implementing the Adapter for its assigned entity.

## Rule 1: Scaffold Base Structure
You MUST use the provided PowerShell script to safely generate the baseline file structure and boilerplate files:
```powershell
.\.agents\skills\create-repository-adapter\scripts\scaffold-adapter.ps1 -Module "<module_name>" -EntityName "<EntityName>"
```

## Rule 2: Full Logic Implementation Requirement
After scaffolding, use `replace_file_content` to implement the Adapter logic.

**Implementation Rules:**
1. **Class Definition**: Must be annotated with `@Component`.
   ```java
   @Component
   public class <EntityName>RepositoryAdapter 
           extends JpaBaseRepository<<EntityName>, <EntityName>Jpa> 
           implements <EntityName>Repository {
   ```
2. **Constructor**:
   - Inject `SpringData<EntityName>Repository`, `<EntityName>Mapper`, `DomainSequenceGenerator`, and `DomainEventPublisher`.
   - Call `super(...)` passing these exact four arguments.
3. **Sequence Name**:
   - You MUST override `protected String getSequenceName()`.
   - It MUST return a string formatted as snake_case of the entity name with `_seq` appended (e.g. `AuthAccount` -> `auth_account_seq`).
4. **Method Overrides**:
   - **CRITICAL RULE:** Do NOT re-implement base repository methods (e.g., `save`, `findById`, `delete`). These are already handled by `JpaBaseRepository`.
   - You MUST ONLY override the custom methods that are defined in the Domain Repository interface (e.g., `findByEmail`, `findByAccountId`).
   - Use `mapper::toDomain` and `mapper::toJpa` to map between domain and JPA entities.

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
