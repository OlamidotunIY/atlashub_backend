---
name: create-domain-valueobject
description: >-
  Use this skill to create Enums or Record Value Objects for a specific module's domain layer.
---

# Create Domain Value Object

## Pre-Requisites (De-Duplication)
Value objects like `EmailAddress`, `Money`, `PhoneNumber`, `NUBAN`, and `CurrencyCode` already exist universally.
Before creating a new Value Object, you MUST check `atlashub-shared/src/main/java/com/atlashub/shared/domain/valueobject` to see if it already exists globally. If it does, do NOT create it. Tell the user you will just import the shared one.

## Audit / Update Mode
If asked to check/verify a Value Object:
1. Verify `enum` types have standard, capitalized constants.
2. Verify `record` types have a compact constructor with strict invariant checks (e.g., throwing a `ValidationException` if empty or negative).
3. Fix any violations using `replace_file_content`.

## Batch Processing (Multiple Value Objects)
This skill supports processing a list of value objects simultaneously for BOTH Generation Mode and Audit Mode.
You can process them sequentially in a single turn, or use `invoke_subagent` if the list is massive.


## Subagent Separation of Concerns (Vertical Slicing)
When using invoke_subagent to process multiple items, you MUST adhere to strict Separation of Concerns (SoC) via **Vertical Slicing**:
1. **One Subagent per Item**: Assign each subagent exactly ONE item (e.g., one entity, one command, one mapper).
2. **End-to-End Flow**: The subagent is responsible for checking its own pre-requisites. If any dependencies (e.g., Value Objects, Events, Entities, Mappers) are missing, the subagent MUST execute the instructions of those respective skills to generate them before proceeding.
3. **Independent Verification**: The subagent MUST run its own verification (e.g., .\gradlew compileJava for the module) to ensure its specific slice is perfect.
4. **Independent Commit**: Once verified, the subagent MUST commit its own changes to Git and end its turn. Do not wait for a parent agent to commit.

## Generation Mode (Creating New)
**Step 1: Scaffold Skeleton**
Run the PowerShell script to safely generate the baseline file structure based on type (`Enum` or `Record`):
```powershell
.agents\skills\create-domain-valueobject\scripts\scaffold-valueobject.ps1 -Module "<module>" -Name "<Name>" -Type "<Record|Enum>"
```

**Step 2: Inject Invariants (For Records Only)**
If it is a `Record`, you MUST use `replace_file_content` to add invariant checks inside the compact constructor. For example:
```java
public record MyValue(String value) {
    public MyValue {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Value cannot be blank");
        }
    }
}
```

## Step 3: Gradle Verification
Run `.\gradlew :<module_gradle_path>:compileJava` to verify it compiles.



## CRITICAL: Self-Correction & Verification Before Gradle
Before you (or your dedicated subagents) run the Gradle compiler check, you MUST ALWAYS perform a strict self-review of all created and modified files. 
- Read back the files you just wrote using `cat` or \iew_file\.
- Check against ALL rules (e.g., absolutely NO inline imports, NO wildcard imports, NO leftover `// TODO`s, NO \eturn null;\ placeholders).
- If ANY rule is violated, you MUST fix it immediately using \eplace_file_content\.
- Only after this explicit re-confirmation are you allowed to run \.\gradlew compileJava\. Dedicated subagents MUST also follow this rule.

## Final Step: Git Commit & Push
Verification is NOT the final step; committing your work is.
After your code successfully compiles and passes all verification rules, you (and every individual subagent) MUST commit and push your changes to GitHub.
1. Stage your specific files: "git add <paths_to_your_files>"
2. Commit your changes using standard Conventional Commits formatting (e.g., "feat(<module>): add <feature>", "refactor(<module>): ...").
3. Push to the remote repository: "git push origin HEAD"
**CRITICAL:** If you are a subagent, you MUST commit and push your own specific work independently as soon as it passes compilation. Do not wait for the parent agent.
