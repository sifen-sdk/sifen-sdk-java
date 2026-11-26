# AGENTS.md

Context for AI coding agents (and people) working on this repository.

- **What this is:** a Java SDK for integrating with SIFEN (Paraguay's electronic invoicing). Rules and code
  tables live as data in `sifen-sdk/spec`; this repository implements them.
- **Read first:** `README.md`, `CONTRIBUTING.md`, `docs/adr/`.
- **Build and test:** `./mvnw verify`. Fix formatting with `./mvnw spotless:apply`.
- **Hard rules:**
  - Never round, recompute or overwrite an amount supplied by the caller. Verify and report only.
  - Never add a validation rule or a default value that SIFEN's documentation does not define. Cite the source
    (Manual Técnico field/validation, Nota Técnica number, XSD) in code and in the pull request.
  - No global or static mutable state. No secrets or document contents in logs, exceptions or `toString()`.
  - Never re-serialize a signed document.
  - `sifen-sdk-core` and `sifen-sdk-client` depend on the JDK only.
  - Code targets Java 17; do not use newer language features or APIs.
  - No real certificates, keys, security codes, taxpayer numbers or production documents anywhere.
- **Style:** public model names are readable English (`operationTotal`); the SIFEN XML name and manual code
  (`dTotOpe`, F014) appear in Javadoc and error messages. Conventional Commits, signed off (`git commit -s`).
- **Do not:** publish releases, change CI permissions, or weaken checks to make a build pass.
