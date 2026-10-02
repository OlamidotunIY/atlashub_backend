---
name: create-module
description: Create or audit a complete AtlasHub Gradle module and its Clean Architecture package skeleton before feature artifacts are added.
---

# Create module

Load `atlashub-module-workflow` when auditing an existing module. For a brand-new module, first read `docs/design.md`, `docs/architecture/module-communication.md`, the intended module design, `settings.gradle`, root Gradle conventions, and two nearest sibling modules.

Do not create a module without an approved design document and explicit ownership boundary.

- Choose `atlashub-platform`, `atlashub-pay`, or `atlashub-infrastructure` according to domain ownership; do not duplicate an existing bounded context.
- Register exactly one Gradle project in `settings.gradle`, add only required dependencies, and avoid circular module dependencies.
- Create the package root and only necessary layers. Empty placeholder classes and speculative adapters are forbidden.
- Add the executable dependency to `atlashub-main` only when runtime beans/entities/repositories must be discovered.
- Cross-module reads/writes follow the mandatory workflow from the first artifact.
- Add a context smoke test and module compile task before feature work.

After the shell exists, invoke `implement-module-feature` for each documented vertical slice. Run full module audit before calling the module complete.
