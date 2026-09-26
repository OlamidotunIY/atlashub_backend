---
name: create-repository-adapter
description: >-
  Use this skill to create Repository Adapters in the infrastructure layer that implement Domain Repositories.
---

# Create Repository Adapter Workflow

Repository adapters live in the infrastructure layer under `infrastructure/persistence/adapters/`.
They map the domain entity methods to the Spring Data JPA layer.

## Pre-Requisites (Dependencies) & Subagent Separation of Concerns
When generating Repository Adapters for multiple entities, you MUST adhere to strict Separation of Concerns (SoC).
Each subagent MUST be assigned an individual, end-to-end flow for a single entity (e.g. one subagent focused entirely on User).
The subagent in charge of that entity MUST follow this exact flow:
1. **Check Pre-requisites**: Verify if the *Jpa entity, SpringData*Repository, and *Mapper exist for its assigned entity.
2. **Generate Missing Pieces**: If any are missing, the subagent MUST execute the instructions of the respective skills (create-jpa-entity, create-spring-data-repository, create-domain-mapper) to create them.
3. **Generate Adapter**: Once all pre-requisites exist, scaffold and implement the *RepositoryAdapter.
4. **Verification**: Run .\gradlew compileJava for the target module to verify the entire flow is perfect.
5. **Commit and Finish**: Once the flow is fully complete and verified, commit the changes for this specific entity and end the subagent turn.

## Batch Processing (Multiple Adapters)
This skill supports processing a list of multiple adapters simultaneously.
1. You MUST process every adapter iteratively. Do not skip any.
2. **Execution Strategy:** You MUST ALWAYS use `invoke_subagent` to spawn a concurrent team of subagents when processing multiple items.


## Subagent Separation of Concerns (Vertical Slicing)
When using invoke_subagent to process multiple items, you MUST adhere to strict Separation of Concerns (SoC) via **Vertical Slicing**:
1. **One Subagent per Item**: Assign each subagent exactly ONE item (e.g., one entity, one command, one mapper).
2. **End-to-End Flow**: The subagent is responsible for checking its own pre-requisites. If any dependencies (e.g., Value Objects, Events, Entities, Mappers) are missing, the subagent MUST execute the instructions of those respective skills to generate them before proceeding.
3. **Independent Verification**: The subagent MUST run its own verification (e.g., .\gradlew compileJava for the module) to ensure its specific slice is perfect.
4. **Independent Commit**: Once verified, the subagent MUST commit its own changes to Git and end its turn. Do not wait for a parent agent to commit.

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


## Final Step: Git Commit & Push
Verification is NOT the final step; committing your work is.
After your code successfully compiles and passes all verification rules, you (and every individual subagent) MUST commit and push your changes to GitHub.
1. Stage your specific files: "git add <paths_to_your_files>"
2. Commit your changes using standard Conventional Commits formatting (e.g., "feat(<module>): add <feature>", "refactor(<module>): ...").
3. Push to the remote repository: "git push origin HEAD"
**CRITICAL:** If you are a subagent, you MUST commit and push your own specific work independently as soon as it passes compilation. Do not wait for the parent agent.


