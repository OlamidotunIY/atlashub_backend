---
name: create-domain-error
description: >-
  Use this skill whenever you need to create a custom Domain Exception/Error for a specific module's business logic.
---

# Create Domain Error

You are responsible for generating custom Domain Exceptions. These errors are thrown by Domain Entities when invariants are violated.

## Inputs Required
1. **Error Name** (e.g., `EmailAlreadyVerified`)
2. **Module Name** (e.g., `iam`, `accounts`)
3. **Reason/Context** (so you can choose the correct base exception)

## Step 1: Base Exception Selection
You MUST inherit from one of the core base exceptions in the `shared` module:
- `ConflictException` (e.g., already exists, state conflict)
- `BusinessRuleException` (e.g., violated a domain invariant like negative balance)
- `NotFoundException` (e.g., entity not found)
- `ValidationException` (e.g., bad format)
- `AuthorizationException` (e.g., forbidden action)

*Import Path:* `com.atlashub.shared.domain.exception.<BaseException>`

## Step 2: Code Generation
Create the class in the module's `domain/exception` package.
Path: `<ModuleRoot>/src/main/java/com/atlashub/<module>/domain/exception/<ErrorName>.java`

**Template:**
```java
package com.atlashub.<module>.domain.exception;

import com.atlashub.shared.domain.exception.<BaseException>;

public class <ErrorName> extends <BaseException> {
    
    public <ErrorName>() {
        super("<Default Error Message>");
    }

    public <ErrorName>(String message) {
        super(message);
    }

    public <ErrorName>(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Step 3: Gradle Verification
Run `.\gradlew :<module_gradle_path>:compileJava` to verify it compiles.
