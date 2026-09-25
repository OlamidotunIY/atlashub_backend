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
