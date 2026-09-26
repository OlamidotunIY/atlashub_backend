---
name: create-jpa-entity
description: >-
  Use this skill to create JPA Entities in the infrastructure persistence layer for a given domain entity.
---

# Create JPA Entity Workflow

JPA entities live in the infrastructure layer under `infrastructure/persistence/entities/`.
They map the domain entities to the database tables.

## Batch Processing (Multiple Entities)
This skill supports processing a list of multiple entities simultaneously.
1. You MUST process every entity iteratively. Do not skip any.
2. **Execution Strategy:** Because processing many sequentially can overwhelm context limits, you MUST ALWAYS use `invoke_subagent` to spawn a concurrent team of subagents when processing multiple items.

## Module-Wide Generation
If the user asks you to "create JPA entities for all entities in `<module>`", you MUST:
1. Locate all Domain Entity classes in `atlashub-platform/<module>/src/main/java/com/atlashub/<module>/domain/entities/` (e.g., using `find_by_name` or `list_dir`).
2. Ignore standard records or value objects; focus only on the actual Domain Entities / Aggregate Roots.
3. Use `invoke_subagent` to spawn a concurrent team of subagents to process EVERY entity found simultaneously.

## Step 1: Analyze Domain Entity & Repository
Before generating the JPA entity, you must figure out:
1. **Fields and Nullability**: Read the corresponding Domain Entity. Check the static `create` method: required parameters are `nullable = false`. 
2. **Indexes**: Check the corresponding Domain Repository interfaces (e.g. `UserRepository`). If there are methods like `findByEmail`, you must add an `@Index` for the `email` column.
3. **Locking Strategy**: Check the `docs/` folder (specifically the "Distributed Architecture" section for the module). Determine if the entity uses Optimistic Locking. Only add `@Version` if optimistic locking is specified!

## Step 2: Write the JPA Entity
You MUST use the provided PowerShell script to safely generate the baseline file structure and boilerplate files:
```powershell
.\.agents\skills\create-jpa-entity\scripts\scaffold-jpa-entity.ps1 -Module "<module_name>" -EntityName "<EntityName>"
```

After scaffolding, use `replace_file_content` to implement the JPA Entity class at `atlashub-platform/<module>/src/main/java/com/atlashub/<module>/infrastructure/persistence/entities/<EntityName>Jpa.java`.

### Naming & Structure Rules:
- The class name MUST be the domain entity name with `Jpa` appended (e.g. `AuthAccountJpa`).
- The table name MUST be the entity name, formatted appropriately for databases (e.g., if the entity is `AuthAccount`, the table name could be `accounts` or `auth_accounts` as specified by the entity's domain).
- The class MUST implement `com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity` and import it.

### Required Annotations:
You MUST include exactly these class-level annotations:
```java
@Entity
@Table(
        name = "your_table_name",
        indexes = {
                // Determine these based on repository queries
                @Index(name = "Idx_tablename_column", columnList = "column_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```

### Field Annotations:
- Use `@Id` for the primary key.
- Use `@Column(name = "...", nullable = false, unique = true)` as appropriate.
- Include `@Version private Long version;` ONLY if optimistic locking is mandated by the docs.
- Include all fields from the domain entity (mapping complex objects properly).
- DO NOT use inline imports! All imports must be at the top of the file.

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

## CRITICAL RULES
- **Boolean Field Naming:** NEVER prefix boolean fields with 'is' (e.g., use private boolean builtIn; instead of private boolean isBuiltIn;). Using 'is' as a prefix breaks MapStruct and Lombok auto-mapping generation.
- **Required Optimistic Locking Field:** Because the shared DomainMapper interface forces @Mapping(target = "version", ignore = true), EVERY JPA Entity MUST declare @Version private Long version; at the bottom of the class, even if the domain design document states optimistic locking is not strictly used for business logic for that entity. Without it, MapStruct will throw an Unknown property "version" in result type error.


## Final Step: Git Commit & Push
Verification is NOT the final step; committing your work is.
After your code successfully compiles and passes all verification rules, you (and every individual subagent) MUST commit and push your changes to GitHub.
1. Stage your specific files: "git add <paths_to_your_files>"
2. Commit your changes using standard Conventional Commits formatting (e.g., "feat(<module>): add <feature>", "refactor(<module>): ...").
3. Push to the remote repository: "git push origin HEAD"
**CRITICAL:** If you are a subagent, you MUST commit and push your own specific work independently as soon as it passes compilation. Do not wait for the parent agent.