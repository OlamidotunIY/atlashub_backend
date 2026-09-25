---
name: create-domain-mapper
description: >-
  Use this skill to create MapStruct Domain Mappers for converting between Domain Entities and JPA Entities.
---

# Create Domain Mapper Workflow

Domain Mappers live in the infrastructure layer under `infrastructure/persistence/mappers/`.

## Batch Processing (Multiple Mappers)
This skill supports processing a list of multiple mappers simultaneously.
1. You MUST process every mapper iteratively. Do not skip any.
2. **Execution Strategy:** You MUST ALWAYS use `invoke_subagent` to spawn a concurrent team of subagents when processing multiple items.

## Rule 1: Scaffold Base Structure
You MUST use the provided PowerShell script to safely generate the baseline file structure and boilerplate files:
```powershell
.\.agents\skills\create-domain-mapper\scripts\scaffold-mapper.ps1 -Module "<module_name>" -EntityName "<EntityName>"
```

## Rule 2: Full Logic Implementation Requirement
After scaffolding, use `replace_file_content` to implement the interface.

**Implementation Rules:**
1. **Naming**: The interface MUST be named `<EntityName>Mapper`.
2. **Inheritance**: MUST extend `com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper<<EntityName>, <EntityName>Jpa>`.
3. **Annotations**:
   You MUST include the following `@Mapper` annotation precisely:
   ```java
   @Mapper(
           componentModel = "spring",
           unmappedTargetPolicy = ReportingPolicy.ERROR,
           uses = {ValueObjectMapper.class}
   )
   ```
4. **Value Objects (`uses = {ValueObjectMapper.class}`)**:
   - If the Domain Entity uses nested records, collections of custom Value Objects, or enums, MapStruct cannot automatically map them to the flat database columns without help.
   - The `uses = {ValueObjectMapper.class}` property hooks it into the global/local `ValueObjectMapper` to handle these.
   - You MUST ensure `ValueObjectMapper.class` is properly imported.
5. **No Inline Imports**: All imports must be explicitly declared at the top.

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
