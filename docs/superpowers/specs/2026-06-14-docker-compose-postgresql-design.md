# Docker Compose PostgreSQL Design

## Context

`medz-loader` is a Spring Batch ETL that writes normalized Algerian medicine data to PostgreSQL. The application currently expects a local PostgreSQL instance at `localhost:5432`, with the runtime database configured as `medz` and the test profile configured as `medz_test`.

GitHub Actions already uses `postgres:17-alpine` for verification. Local development should match that setup closely while staying lightweight.

## Goal

Add Docker Compose support for PostgreSQL only. Developers should be able to start the database with one command, run `medz-loader` from Maven, run tests against the same local PostgreSQL service, and let API applications read from the same shared `medz` database during development.

## Non-Goals

- Do not containerize the Spring Batch application in this change.
- Do not move application configuration away from the existing Spring properties.
- Do not add Flyway or Liquibase as part of this Compose change.
- Do not make this repository own the lifecycle of downstream API applications.

## Proposed Compose Shape

Add a root-level `compose.yaml` containing one `postgres` service:

- Image: `postgres:17-alpine`, matching CI.
- Host port: `5432:5432`, matching current application and test properties.
- Default database: `medz`.
- Default writer/admin credentials: `postgres/password`, matching `src/main/resources/application.properties`.
- Persistent named volume for PostgreSQL data.
- Named network, for example `medz-network`, so other local Compose projects can join the same database network.
- Healthcheck using `pg_isready`.

## Database Initialization

Add initialization SQL under `docker/postgres/init/`.

The init flow must:

- Create the `medz_test` database if it does not exist.
- Create the `medz` application database through the official Postgres image defaults.
- Create the `medz` test user with password `medz`, matching `src/test/resources/application-test.properties`.
- Create a read-only API user for local API development, for example `medz_reader/medz_reader`.
- Apply the existing medicine schema from `src/main/resources/sql/ddl.sql` to the `medz` database.
- Grant read access on public tables and sequences in `medz` to the API reader user.

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

Run verification against the Compose database:

```shell
SPRING_PROFILES_ACTIVE=test ./mvnw verify
```

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
username: medz_reader
password: medz_reader
```

## Documentation Updates

Update `README.md` to include:

- Docker Compose as the recommended local PostgreSQL setup.
- How to start and reset the database.
- How to run the loader after Compose is started.
- How to run tests against the Compose PostgreSQL service.
- Connection strings for downstream API applications.
- A note that `src/main/resources/sql/ddl.sql` initializes the medicine business schema, while Spring initializes Spring Batch metadata tables at runtime.

## Testing

Verification must include:

- `docker compose config` to validate Compose syntax.
- Starting PostgreSQL with `docker compose up -d`.
- Confirming the `medz` and `medz_test` databases exist.
- Running `SPRING_PROFILES_ACTIVE=test ./mvnw verify` when Java 25 and Docker are available.
