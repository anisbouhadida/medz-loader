# Docker Compose PostgreSQL Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add PostgreSQL-only Docker Compose support for local development, while moving automated tests to isolated Testcontainers PostgreSQL instances.

**Architecture:** Compose owns the durable local `medz` development database and role setup. Tests use Testcontainers so they do not depend on or mutate the persistent development database. Documentation explains host and Compose-network connection strings for downstream API applications.

**Tech Stack:** Docker Compose, PostgreSQL 17 Alpine, Spring Boot 4, Spring Batch, Maven, JUnit 5, Testcontainers.

---

### Task 1: Add Local PostgreSQL Compose Setup

**Files:**
- Create: `compose.yaml`
- Create: `docker/postgres/init/01-init-medz.sql`

- [ ] **Step 1: Add Compose service**

Create `compose.yaml` with one `postgres` service using `postgres:17-alpine`, host port `5432`, a persistent named volume, and `medz-network`.

- [ ] **Step 2: Add database initialization SQL**

Create `docker/postgres/init/01-init-medz.sql` to create `medz_loader_owner`, `medz_loader_writer`, and `medz_api_reader`, grant privileges, and apply future default privileges.

- [ ] **Step 3: Validate Compose syntax**

Run: `docker compose config`

Expected: command exits successfully and renders the `postgres` service.

### Task 2: Add Testcontainers Test Infrastructure

**Files:**
- Modify: `pom.xml`
- Modify: `src/test/resources/application-test.properties`
- Create: `src/test/java/dz/anisbouhadida/medzloader/testsupport/PostgresTestContainerConfiguration.java`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/MedzLoaderApplicationTests.java`

- [ ] **Step 1: Add Testcontainers dependencies**

Add test-scoped `org.testcontainers:junit-jupiter`, `org.testcontainers:postgresql`, and `org.springframework.boot:spring-boot-testcontainers`.

- [ ] **Step 2: Remove hardcoded test datasource**

Remove `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`, and `spring.datasource.driver-class-name` from `src/test/resources/application-test.properties`.

- [ ] **Step 3: Add shared test container configuration**

Create a test configuration that starts `postgres:17-alpine` with Spring Boot's `@ServiceConnection`.

- [ ] **Step 4: Import the container configuration in the context test**

Update `MedzLoaderApplicationTests` to import the Testcontainers configuration.

- [ ] **Step 5: Run the context test**

Run: `SPRING_PROFILES_ACTIVE=test ./mvnw -Dtest=MedzLoaderApplicationTests test`

Expected: test passes after Docker starts the PostgreSQL container.

### Task 3: Update CI to Let Tests Own PostgreSQL

**Files:**
- Modify: `.github/workflows/ci.yml`
- Modify: `.github/workflows/release.yml`

- [ ] **Step 1: Remove PostgreSQL service containers**

Delete the GitHub Actions `services.postgres` blocks from CI and release workflows.

- [ ] **Step 2: Keep Docker available implicitly**

Rely on GitHub-hosted Ubuntu runners' Docker support for Testcontainers.

### Task 4: Update README

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add Docker to prerequisites**

Document Docker as the recommended local PostgreSQL path.

- [ ] **Step 2: Replace manual PostgreSQL setup with Docker Compose-first setup**

Add `docker compose up -d`, runtime credentials, API reader credentials, and reset commands.

- [ ] **Step 3: Document Testcontainers for tests**

Explain that tests provision PostgreSQL through Testcontainers rather than using the Compose database.

### Task 5: Full Verification

**Files:**
- All changed files

- [ ] **Step 1: Validate Compose**

Run: `docker compose config`

Expected: exits successfully.

- [ ] **Step 2: Run focused tests**

Run: `SPRING_PROFILES_ACTIVE=test ./mvnw -Dtest=MedzLoaderApplicationTests test`

Expected: exits successfully.

- [ ] **Step 3: Run full verification**

Run: `SPRING_PROFILES_ACTIVE=test ./mvnw verify`

Expected: exits successfully.

