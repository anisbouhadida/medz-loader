# Docker Compose PostgreSQL Design

## Context

`medz-loader` is a Spring Batch ETL that writes normalized Algerian medicine data to PostgreSQL. The application currently expects a local PostgreSQL instance at `localhost:5432`, with the runtime database configured as `medz` and the test profile configured as `medz_test`.

GitHub Actions already uses `postgres:17-alpine` for verification. Local development should match that setup closely while staying lightweight.

## Goal

Add Docker Compose support for PostgreSQL only. Developers should be able to start the development database with one command, run `medz-loader` from Maven, and let API applications read from the same shared `medz` database during development.

Automated tests should not depend on the persistent Compose database. They should use isolated PostgreSQL containers through Testcontainers so test data is disposable, repeatable, and independent from a developer's local `medz` database.

## Non-Goals

- Do not containerize the Spring Batch application in this change.
- Do not move application configuration away from the existing Spring properties.
- Do not add Flyway or Liquibase as part of this Compose change.
- Do not make this repository own the lifecycle of downstream API applications.
- Do not use Docker Compose as the long-term test database fixture.

## Proposed Compose Shape

Add a root-level `compose.yaml` containing one `postgres` service:

- Image: `postgres:17-alpine`, matching CI.
- Host port: `5432:5432`, matching current application and test properties.
- Default database: `medz`.
- Default runtime credentials should use the Medz role naming standard described below.
- Persistent named volume for PostgreSQL data.
- Named network, for example `medz-network`, so other local Compose projects can join the same database network.
- Healthcheck using `pg_isready`.

## Database Role Naming Standard

Development database roles must use this naming pattern:

```text
medz_<app_or_scope>_<access_level>
```

Use short, explicit access levels:

- `owner`: owns the database and schema objects.
- `writer`: can read and write application data, but should not be used by read-only services.
- `reader`: can only read application data.

For this Compose setup:

| Role | Login | Purpose |
| --- | --- | --- |
| `medz_loader_owner` | No | Owns the `medz` database and initialized schema objects. |
| `medz_loader_writer` | Yes | Runtime writer role for `medz-loader`. |
| `medz_api_reader` | Yes | Read-only role for API applications that query the loaded dataset. |

The official Postgres image still needs a bootstrap superuser through `POSTGRES_USER`, but application documentation should point developers to the Medz-specific roles instead of generic `postgres` credentials.

For local development only, login role passwords may match the role name to keep setup predictable:

```text
medz_loader_writer / medz_loader_writer
medz_api_reader / medz_api_reader
```

## Database Initialization

Add initialization SQL under `docker/postgres/init/`.

The init flow must:

- Create the `medz` application database through the official Postgres image defaults.
- Create `medz_loader_owner`, `medz_loader_writer`, and `medz_api_reader` roles.
- Make `medz_loader_owner` a non-login ownership role.
- Apply the existing medicine schema from `src/main/resources/sql/ddl.sql` to the `medz` database.
- Grant read/write privileges on application tables and sequences to `medz_loader_writer`.
- Grant read-only privileges on application tables and sequences to `medz_api_reader`.

The Spring Batch metadata schema remains managed by Spring through `spring.batch.jdbc.initialize-schema=always`.

## Local Development Flow

Developers start PostgreSQL:

```shell
docker compose up -d
```

Run the loader from the host:

```shell
./mvnw spring-boot:run
```

Run verification with isolated test PostgreSQL containers:

```shell
SPRING_PROFILES_ACTIVE=test ./mvnw verify
```

The test suite should start its own PostgreSQL container through Testcontainers instead of reusing the persistent Compose database.

Reset local database state:

```shell
docker compose down -v
docker compose up -d
```

## API Application Access

An API running on the host can connect with:

```text
jdbc:postgresql://localhost:5432/medz
```

An API running in another Compose project can connect with:

```text
jdbc:postgresql://postgres:5432/medz
```

The second form requires that the API Compose service joins the same external `medz-network`, or that both services are launched from a shared Compose project.

The recommended local API credentials are the read-only user:

```text
username: medz_api_reader
password: medz_api_reader
```

## Documentation Updates

Update `README.md` to include:

- Docker Compose as the recommended local PostgreSQL setup.
- How to start and reset the database.
- How to run the loader after Compose is started.
- How tests run against Testcontainers-managed PostgreSQL instead of the Compose database.
- Connection strings for downstream API applications.
- A note that `src/main/resources/sql/ddl.sql` initializes the medicine business schema, while Spring initializes Spring Batch metadata tables at runtime.

## Testing

Verification must include:

- `docker compose config` to validate Compose syntax.
- Starting PostgreSQL with `docker compose up -d`.
- Confirming the `medz` database exists and the API reader user can connect with read-only privileges.
- Running `SPRING_PROFILES_ACTIVE=test ./mvnw verify` when Java 25 and Docker are available.

## Testcontainers Direction

Move test database setup away from `src/test/resources/application-test.properties` hardcoded `localhost:5432` assumptions and into Testcontainers-backed Spring test configuration.

The test setup should:

- Start `postgres:17-alpine`, matching CI and local Compose.
- Create an isolated database per test JVM run.
- Let Spring consume the dynamically allocated JDBC URL, username, and password.
- Keep `spring.batch.job.enabled=false` for tests.
- Keep `spring.batch.jdbc.initialize-schema=always` so Spring Batch metadata tables are initialized by Spring.
- Apply `src/main/resources/sql/ddl.sql` before JDBC-backed repository tests that need the medicine business schema.

After this migration, GitHub Actions no longer needs a PostgreSQL service container for Maven verification because tests provision their own database.
