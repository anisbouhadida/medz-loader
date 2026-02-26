---
applyTo: \*\*/\*.java, \*\*/\*.kt
description: Guidelines for building modern Spring Boot applications
  (Java 25 baseline)
---

# Spring Boot Development Guidelines (Java 25)

This document defines coding, architecture, and review standards for
Spring Boot applications using **Java 25 (LTS)** and modern Spring
ecosystem best practices.

# Platform Baseline

## Java

-   Use **Java 25 (LTS)** as the primary runtime.
-   Prefer modern language features:
    -   Pattern matching
    -   Records
    -   Sealed classes
    -   Structured concurrency
-   Avoid legacy APIs and deprecated constructs.
-   Favor immutability and functional-style code.

## Spring

-   Target:
    -   Spring Boot 3.5+ or 4.x
    -   Spring Framework 6/7+
-   Always align dependencies with Spring BOM.
-   Prefer official Spring starters over manual dependency wiring.

# General Coding Principles

-   Make only high-confidence suggestions when reviewing code.
-   Prefer readability and maintainability over cleverness.
-   Document WHY design decisions were made.
-   Handle edge cases explicitly.
-   Provide meaningful exception handling.
-   Avoid magic numbers; use constants/configuration.
-   Prefer composition over inheritance.

# Dependency Injection

-   Use constructor injection only.
-   Dependencies must be:
    -   `private`
    -   `final`
-   Avoid field injection.
-   Favor immutability.

# Configuration

-   Use `application.yml`.
-   Use Spring profiles:
    -   `dev`
    -   `test`
    -   `prod`
-   Use `@ConfigurationProperties` for type-safe configuration.
-   Externalize:
    -   Secrets
    -   Credentials
    -   Environment values
-   Never commit secrets to Git.

# Architecture & Code Organization

-   Organize by **domain/feature**, not by technical layer.

Example: com.company.orders\
com.company.users\
com.company.billing

-   Controllers: Thin and orchestration-only.
-   Services: Business logic, stateless, transaction-aware.
-   Repositories: Persistence only.
-   Use DTOs between layers.
-   Do not expose persistence entities externally.

# API Design

-   Follow REST conventions.
-   Version public APIs.
-   Use OpenAPI / springdoc.
-   Validate all requests with Bean Validation.

# Validation & Error Handling

-   Use JSR‑380 annotations (`@NotNull`, `@Size`, `@Email`).
-   Implement `@RestControllerAdvice`.
-   Never leak stack traces externally.

# Logging & Observability

-   Use SLF4J.
-   Never use `System.out.println`.
-   Use parameterized logging.
-   Enable Actuator, Micrometer, and OpenTelemetry.

# Security

-   Use Spring Security by default.
-   Prefer OAuth2 / JWT / OIDC.
-   Use method-level security.
-   Validate ALL external inputs.
-   Prevent SQL injection via Spring Data or named parameters.

# Data & Persistence

-   Use Spring Data repositories.
-   Use Flyway or Liquibase migrations.
-   Transactions at service layer.
-   Avoid N+1 queries.
-   Optimize fetch strategies.

# Performance

-   Monitor thread pools, GC, DB connections.
-   Consider virtual threads and AOT/native images where appropriate.

# Testing

-   Unit tests for services.
-   Slice tests (`@WebMvcTest`, `@DataJpaTest`).
-   Integration tests with Testcontainers.
-   CI must run build + tests + static analysis.

# Spring Batch Guidelines (Spring Batch 6+)

## Platform

-   Spring Batch 6+
-   Java 25 compatible
-   Avoid XML configuration

## Configuration

Avoid deprecated APIs: - `JobBuilderFactory` - `StepBuilderFactory`

Use: - `JobBuilder` - `StepBuilder` - Explicit `JobRepository`

## Job Design

Jobs must be: - Idempotent - Restartable - Observable

## Reliability

-   Configure retry and skip policies.
-   Tune commit intervals.
-   Ensure safe partial failure handling.

# Copilot Review Heuristics

1.  Enforce constructor injection.
2.  Flag deprecated APIs.
3.  Validate logging and exception handling.
4.  Ensure DTO separation.
5.  Validate batch restartability.
