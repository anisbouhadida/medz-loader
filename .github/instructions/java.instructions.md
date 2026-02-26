---
description: "Guidelines for building Java 25 (LTS) applications"
applyTo: "**/*.java"
---

# Java 25 Development Guidelines

## 0) Baseline and Compatibility (Java 25)

- Treat **Java 25** as the project baseline:
    - Use language level **25** (e.g., `--release 25`).
    - Prefer Java 25 standard library APIs over third-party utilities where reasonable.
- **Preview / incubator features** (e.g., Structured Concurrency preview, Vector API incubator) must be **opt-in**:
    - Only use them if the user/project explicitly allows preview/incubator usage.
    - If used, isolate behind a small façade, document it in README, and ensure CI compiles/runs with `--enable-preview` (where applicable).
- Assume **32-bit x86 is not a target** for Java 25 projects.

---

## 1) Project Setup: Static Analysis (Ask Once, Don’t Block)

- If the project does **not** already have static analysis configured, ask the user once whether to integrate:
    - **SonarQube/SonarCloud** (preferred): SonarLint in IDE + `sonar-scanner` in CI.
    - Otherwise: **SpotBugs + PMD + Checkstyle** (or Error Prone if the team uses it).
- If user says **yes**:
    - Provide a recommended setup and minimal CI snippet.
    - Store tokens/keys in CI secrets, never in repo.
    - Treat Sonar findings as the primary actionable source and reference rule keys in fixes.
- If user says **no**:
    - Note the decision in README (short section) and continue coding using the guidelines below.
- If Sonar is expected but failing, do **at most 3 checks**:
    1. Verify project binding + token/secret exists.
    2. Ensure scanner runs in CI and reports are uploaded.
    3. Ensure SonarLint is installed/configured in the IDE.
    - If still blocked: propose a fallback (SpotBugs/PMD/Checkstyle) and suggest opening a short tracking issue.

---

## 2) Build Tooling Expectations (Java 25)

- Prefer **reproducible builds**:
    - Pin plugin versions (don’t rely on implicit/latest).
    - Use dependency locking where available.
- Prefer **toolchains** (Gradle/Maven toolchains) so CI/dev machines consistently use Java 25.
- If discussing Gradle support, prefer modern Gradle versions that explicitly support Java 25.

---

## 3) Language & API Best Practices (Modern Java, Java 25-first)

### Data Modeling
- **Records** for immutable data carriers (DTOs, results, messages).
- Use **sealed** types for closed hierarchies (e.g., domain results, command trees).
- Prefer **enums** for constrained sets of values; include behavior on enums when it improves readability.

### Pattern Matching & Switch
- Use pattern matching for `instanceof` and `switch` expressions to reduce casting and branching.
- Keep `switch` exhaustive for sealed hierarchies (no default when compiler can enforce exhaustiveness).

### Type Inference
- Use `var` when the RHS makes the type obvious; otherwise, spell out the type.

### Immutability & Collections
- Default to immutability:
    - `final` fields, minimal mutability, defensive copies at boundaries.
    - Prefer `List.of()/Map.of()` for fixed values.
    - Prefer `stream.toList()` when you want an unmodifiable list.
- Avoid leaking internal mutable collections; return unmodifiable views/copies.

### Null & Optional
- Avoid `null` in *public APIs*:
    - Return `Optional<T>` for absence.
    - Use `Objects.requireNonNull(...)` for required inputs.
- Don’t use `Optional` for fields/serialization DTO members unless the project already standardizes on it.
- Prefer domain-specific “empty” values only when they are semantically correct (not as a null workaround).

### Strings & Formatting
- Prefer text blocks for multi-line literals.
- Prefer structured formatting (and clear locale handling) for user-facing text.

### Modules & Imports (Java 25)
- If the codebase is modularized, keep module boundaries clean and avoid cyclic dependencies.
- Consider **Module Import Declarations** only where it improves readability and the project style allows it; otherwise use standard imports.
### Small tools / scripts (Java 25)
- For tiny utilities, Java 25 supports **compact source files and instance main methods**; use only when it improves ergonomics and is acceptable for the project (avoid in long-lived production services unless the team wants it).

---

## 4) Concurrency & Performance

- Prefer simple, correct code first; optimize based on measurements.
- Prefer **virtual threads** for high-concurrency I/O-bound workloads when the environment supports it.
- Avoid `ThreadLocal` for request-scoped context in concurrent code; prefer **Scoped Values** for context propagation in modern Java.
- If the project opts into preview features, **Structured Concurrency (preview)** may be used to manage lifecycles of related tasks; keep it behind a small abstraction and document the preview requirement.
- Always clean up concurrency resources (shutdown executors, cancel tasks, close resources).

---

## 5) Security & Integrity

- Do not use or reintroduce **SecurityManager**; it is permanently disabled in modern JDKs.
- Avoid internal/unsafe APIs (`sun.misc.Unsafe`, internal JDK packages). Treat warnings as action items.
- Prefer standard cryptography APIs; for password/key derivation prefer the JDK’s **Key Derivation Function API** where applicable.
- Prefer the **Foreign Function & Memory API** over JNI for native interop if native interop is required.
- Never log secrets; redact sensitive fields by default.

---

## 6) Naming, Style, Documentation

- Follow Google Java style (or the project’s existing style if present).
- Names:
    - `UpperCamelCase` types, `lowerCamelCase` members, `UPPER_SNAKE_CASE` constants.
    - Prefer clear nouns for types, verbs for methods, avoid abbreviations.
- Documentation:
    - Document public APIs and non-obvious behavior (edge cases, performance constraints, thread-safety).
    - Prefer concise, example-driven docs; keep comments truthful and maintained.

---

## 7) Common Bug Patterns (Prevent Early)

- Resource management: always use try-with-resources for closeable resources.
- Equality: use `.equals()` / `Objects.equals()` for objects; never `==` for strings.
- Exceptions: never swallow exceptions; add context and preserve cause.
- Time: prefer `java.time` types; avoid legacy `Date/Calendar`.
- Concurrency: avoid shared mutable state; guard invariants; avoid “fire-and-forget” without lifecycle control.

---

## 8) Common Code Smells (Keep Codebase Healthy)

- Too many parameters: introduce a value object / record or a builder.
- Long methods: extract helpers; keep cyclomatic/cognitive complexity low.
- Duplicated literals: replace with constants/enums.
- Dead code: remove unused variables/methods; keep diffs clean.
- Over-streaming: don’t force streams when a simple loop is clearer.

---

## 9) Testing, Build, and Verification

- Always add/adjust tests alongside behavior changes (unit tests first; integration tests where needed).
- Prefer deterministic tests (control time, randomness, I/O).
- After modifications:
    - Maven: `mvn clean verify`
- Keep CI green: formatting, static analysis (if enabled), tests, and packaging must pass.