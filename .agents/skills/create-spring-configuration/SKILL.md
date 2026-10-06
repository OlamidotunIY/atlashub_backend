---
name: create-spring-configuration
description: Create or audit AtlasHub Spring bean/configuration wiring for domain services, adapters, security filters, provider clients, and module discovery.
---

# Create Spring configuration

Load `atlashub-module-workflow`, resolve artifact `other`, and read module docs, `atlashub-main` configuration, and the concrete constructor graph.

- Prefer component scanning for infrastructure adapters and handlers. Domain services remain framework-free and are exposed as `@Bean` from main configuration when dependencies are required.
- Do not create duplicate beans for component-scanned classes.
- Every shared query port must have one owning-module bean, and `atlashub-main` must depend on that Gradle module.
- Use typed `@ConfigurationProperties` for provider settings. Validate required values and never provide production secret defaults.
- Security filter order must reference a registered standard/custom filter order; register a custom filter before ordering another filter relative to it.
- Give same-named components in different modules explicit module-qualified bean names or distinct class names.
- Keep configuration free of business logic and repository operations.

Compile is insufficient. Add/run an application-context test or start the application far enough to complete bean discovery, JPA repository creation, and security-chain construction.
