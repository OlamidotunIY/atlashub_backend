---
name: java-style-guidelines
description: Enforces strict coding style rules across all Java code generation and modification.
trigger: always_on
---

# Java Style Guidelines

Whenever you are writing, refactoring, or updating Java code in this project, you must strictly follow these rules:

1. **NO WILDCARD IMPORTS**: Never use wildcard imports (e.g., `import java.util.*;`). You must explicitly declare every single class import (e.g., `import java.util.List; import java.util.UUID;`).
2. **CLEAN RECORDS**: When creating Java 14+ `record` classes, maintain clean formatting with appropriate line breaks for readability. 
3. **ARCHITECTURE**: Respect the Domain-Driven Design (DDD) module structure (`domain`, `application`, `infrastructure`, `presentation`).
