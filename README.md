# medz-loader

![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java 25](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F.svg)
[![CI](https://github.com/anisbouhadida/medz-loader/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/anisbouhadida/medz-loader/actions/workflows/ci.yml)

`medz-loader` is an open-source Spring Batch ETL that loads official Algerian medicine CSV releases into a normalized PostgreSQL schema with versioned status and event history.

It is part of the broader **Medz** ecosystem: a community-oriented effort to make Algerian medicine data easier to extract, normalize, load, and reuse in reliable developer-friendly systems.

This project is built for **Algerian developers**, open data contributors, civic tech builders, and anyone who wants reproducible medicine ingestion pipelines instead of one-off scripts.

---

## Table of Contents

- [Why this project](#why-this-project)
- [Where `medz-loader` fits in Medz](#where-medz-loader-fits-in-medz)
- [Project status](#project-status)
- [Features](#features)
- [Input contract](#input-contract)
- [Quick start](#quick-start)
  - [1) Prerequisites](#1-prerequisites)
  - [2) Clone and build](#2-clone-and-build)
  - [3) Prepare PostgreSQL](#3-prepare-postgresql)
  - [4) Configure the loader](#4-configure-the-loader)
  - [5) Add input files](#5-add-input-files)
  - [6) Run the batch job](#6-run-the-batch-job)
- [How it works](#how-it-works)
- [Database model](#database-model)
- [Configuration](#configuration)
- [Project structure](#project-structure)
- [Quality and CI](#quality-and-ci)
- [Contributing](#contributing)
- [Community note](#community-note)
- [License](#license)

---

## Why this project

Official Algerian medicine releases are useful, but they are not immediately convenient for application development, analytics, or historical tracking.

Developers often need more than raw files:

- a stable ingestion workflow,
- a consistent naming contract,
- a queryable relational schema,
- version-aware updates across release cycles,
- and a clear separation between extraction, normalization, and persistence.

`medz-loader` exists to make that ingestion step explicit and reusable.

Instead of treating every release as an ad hoc import, this project turns CSV releases into a durable PostgreSQL dataset that other Medz components and community projects can build on.

---

## Where `medz-loader` fits in Medz

The Medz vision is bigger than a single repository.

Today, the ecosystem already has a clear upstream/downstream flow:

1. [`medz-extractor`](https://github.com/anisbouhadida/medz-extractor) converts official Excel nomenclature files into clean CSV outputs.
2. `medz-loader` consumes those CSV files and writes them into a normalized PostgreSQL schema.
3. [`medz-gql-api`](https://github.com/anisbouhadida/medz-gql-api) exposes that normalized dataset through a Spring Boot GraphQL API.
4. [`medz-explorer`](https://github.com/anisbouhadida/medz-explorer) provides an Angular frontend to explore the data and try the API.
5. Other Medz services, dashboards, and community tools can build on top of the same shared data foundation instead of parsing source files directly.

In other words:

```text
Official source files -> medz-extractor -> CSV contract -> medz-loader -> PostgreSQL -> medz-gql-api -> medz-explorer and downstream Medz apps
```

If you are an Algerian developer building search tools, APIs, dashboards, or public-interest data products, this repository is meant to be a dependable ingestion building block.

---

## Project status

The Medz ecosystem is still a **work in progress**.

Today, the main building blocks already exist:

- extraction with [`medz-extractor`](https://github.com/anisbouhadida/medz-extractor),
- loading and normalization with `medz-loader`,
- querying through [`medz-gql-api`](https://github.com/anisbouhadida/medz-gql-api),
- and frontend exploration with [`medz-explorer`](https://github.com/anisbouhadida/medz-explorer).

The overall direction is clear: move from a solid open-source prototype ecosystem toward a production-ready public platform for Algerian medicine data.

That means some parts are still evolving, contracts may continue to improve, and documentation will keep being refined as the system gets closer to a production release.

---

## Features

- Recursive discovery of all `*.csv` files under a configurable input directory.
- Support for the 3 expected medicine release file types:
  - `nomenclature.csv`
  - `non_renouveles.csv`
  - `retraits.csv`
- Filename-based reader classification using the file name contract already used across Medz.
- Bean Validation filtering for invalid rows without aborting the whole batch.
- Mapping from CSV DTOs to immutable domain records and sealed event types.
- Chunk-oriented Spring Batch processing.
- Normalized PostgreSQL persistence with:
  - `medicine`
  - `medicine_status_history`
  - `nomenclature_event`
  - `non_renewal_event`
  - `withdrawal_event`
  - `medicine_event_history`
- Optimistic version-aware medicine upserts.
- CI workflow with build, tests, formatting checks, and PostgreSQL-backed verification.

---

## Input contract

`medz-loader` is intentionally strict about the CSV contract it consumes.

For each release cycle, the recommended layout is:

```text
input/YYYY-MM/
├── nomenclature.csv
├── non_renouveles.csv
└── retraits.csv
```

The loader scans recursively, so the `YYYY-MM` directory name is a project convention rather than a hard runtime requirement. What **does** matter is the filename contract:

- filenames containing `nomenclature` are treated as nomenclature updates,
- filenames containing `non_renouveles` are treated as non-renewals,
- filenames containing `retraits` are treated as withdrawals.

This contract matches the outputs generated by [`medz-extractor`](https://github.com/anisbouhadida/medz-extractor), making the two repositories work well together.

If a CSV file does not match one of those filename patterns, the loader cannot classify it.

---

## Quick start

### 1) Prerequisites

- Java 25
- Maven 3.9+
- Docker or a compatible container runtime

### 2) Clone and build

```bash
git clone https://github.com/anisbouhadida/medz-loader.git
cd medz-loader
./mvnw clean verify
```

### 3) Prepare PostgreSQL

The recommended local setup uses Docker Compose to start PostgreSQL 17 with the
medicine schema and development roles already initialized:

```bash
docker compose up -d
```

Compose creates the durable development database:

```text
database: medz
host: localhost
port: 5432
```

Development roles follow the `medz_<app_or_scope>_<access_level>` naming
standard:

```text
medz_loader_writer / medz_loader_writer
medz_api_reader    / medz_api_reader
```

Use `medz_loader_writer` to run `medz-loader`. Use `medz_api_reader` for
local API applications that only need to read the loaded dataset.

An API running on the host can connect with:

```text
jdbc:postgresql://localhost:5432/medz
```

An API running in another Compose project can connect with:

```text
jdbc:postgresql://postgres:5432/medz
```

The second form requires the API service to join the `medz-network` Docker
network created by this Compose file.

To reset local database state:

```bash
docker compose down -v
docker compose up -d
```

Notes:

- `spring.batch.jdbc.initialize-schema=always` initializes the **Spring Batch metadata tables**.
- The medicine business schema is defined in `src/main/resources/sql/ddl.sql` and is applied by the Compose initialization scripts.
- Automated tests use Testcontainers-managed PostgreSQL instances instead of the persistent Compose database.

### 4) Configure the loader

Default runtime properties live in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/medz
spring.datasource.username=medz_loader_writer
spring.datasource.password=medz_loader_writer
spring.batch.jdbc.initialize-schema=always

medz.loader.input-dir=./input
medz.loader.registration-zone-id=Africa/Algiers
```

You can override the Medz-specific properties with environment variables:

- `MEDZ_LOADER_INPUT_DIR`
- `MEDZ_LOADER_REGISTRATION_ZONE_ID`

### 5) Add input files

Add release-cycle CSVs under the configured input directory.

Example:

```text
input/
└── 2025-11/
    ├── nomenclature.csv
    ├── non_renouveles.csv
    └── retraits.csv
```

### 6) Run the batch job

Run with Maven:

```bash
./mvnw spring-boot:run
```

Or run the packaged JAR:

```bash
java -jar target/medz-loader-*.jar
```

On startup, Spring Batch launches the configured job, scans the input directory recursively, and persists recognized medicine events into PostgreSQL.

---

## How it works

At a high level, the ETL pipeline is:

1. A `MultiResourceItemReader` discovers every `**/*.csv` under `medz.loader.input-dir`.
2. `FileAwareMedicineItemReader` switches delegate readers based on the current filename.
3. A `BeanValidatingItemProcessor` filters invalid rows instead of failing the whole job.
4. A classifier-based processor maps each CSV line into a sealed domain event:
   - `NomenclatureEvent`
   - `NonRenewalEvent`
   - `WithdrawalEvent`
5. The writer pipeline then:
   - upserts the `medicine` row,
   - records the current status in `medicine_status_history`,
   - writes the event-specific table,
   - appends to `medicine_event_history`.

### Important runtime conventions

- **Filename-driven behavior**: `nomenclature`, `non_renouveles`, and `retraits` are part of the runtime contract.
- **Composite business key**: medicine identity is based on `(registration_number, code, icd, brand_name, laboratory_holder)`.
- **Optimistic versioning**: updates depend on the current stored version matching the incoming version.
- **Validation failures are filtered**: invalid rows are skipped rather than treated as fatal job errors.

---

## Database model

The database schema lives in `src/main/resources/sql/ddl.sql`.

Main tables:

- `medicine`: master record for a medicine definition.
- `medicine_status_history`: tracks distinct status states over time.
- `nomenclature_event`: event-specific nomenclature payload.
- `non_renewal_event`: event-specific non-renewal payload.
- `withdrawal_event`: event-specific withdrawal payload.
- `medicine_event_history`: generic event log across event types.

The schema is designed for normalized storage and repeatable ingestion across multiple release cycles.

---

## Configuration

The main typed properties are exposed through `MedzLoaderProperties`:

- `medz.loader.input-dir`: base directory scanned recursively for CSV files.
- `medz.loader.registration-zone-id`: source timezone used to convert registration dates to UTC timestamps for storage.

Default values are currently defined in `src/main/resources/application.properties`.

---

## Project structure

```text
src/main/java/dz/anisbouhadida/medzloader/
├── MedzLoaderApplication.java
├── batch/
│   ├── config/      # job, step, reader, processor, writer wiring
│   ├── dto/         # CSV line records and column definitions
│   ├── reader/      # resource-aware file reader
│   └── support/     # classifiers, mappers, properties, SQL helpers
├── domain/
│   ├── api/         # use-case boundary
│   ├── model/       # immutable records and sealed events
│   ├── service/     # domain services
│   └── spi/         # persistence ports
└── infrastructure/
    └── jdbc/        # JDBC adapters and repository implementations

src/main/resources/
├── application.properties
└── sql/
    ├── ddl.sql
    └── write/

src/test/
├── java/            # unit and JDBC-backed tests
└── resources/
    └── application-test.properties
```

---

## Quality and CI

Preferred verification command:

```bash
./mvnw clean verify
```

The Maven build currently includes:

- unit and integration-style tests,
- Testcontainers-managed PostgreSQL for Spring context and JDBC-backed tests,
- Spotless formatting checks,
- JaCoCo coverage verification,
- Maven Enforcer rules for Java and Maven versions,
- source and Javadoc artifact generation.

GitHub Actions CI:

- runs on pushes and pull requests,
- activates the `test` profile,
- lets Testcontainers provision PostgreSQL 17 during the Maven test run,
- executes `./mvnw --batch-mode verify`.

There is also a release workflow that builds and publishes versioned JARs from `release/*` branches.

---

## Contributing

Contributions are welcome, especially from developers who care about open Algerian data, public-interest tooling, and reproducible backend systems.

Good contributions include:

- improving ingestion robustness,
- tightening tests around CSV or SQL contracts,
- improving documentation,
- clarifying release-cycle assumptions,
- preparing this loader for future Medz services.

Before opening a pull request:

1. Keep the CSV filename contract intact unless you also update the classifier and tests.
2. Preserve the composite-key and versioning behavior unless the change is intentional and documented.
3. Add or update tests alongside behavior changes.
4. Run:

```bash
./mvnw clean verify
```

Focused pull requests are especially appreciated.

---

## Community note

This repository is open source, but more importantly it is meant to be **useful**.

The goal is to help the Algerian developer community build better tools on top of medicine data with less duplicated effort and more shared infrastructure.

If you are building something around the Medz ecosystem, feel free to open an issue, propose an improvement, or contribute documentation that helps the next developer move faster.

Related Medz repositories:

- [`medz-extractor`](https://github.com/anisbouhadida/medz-extractor) — Excel to CSV extraction
- [`medz-loader`](https://github.com/anisbouhadida/medz-loader) — CSV to PostgreSQL loading
- [`medz-gql-api`](https://github.com/anisbouhadida/medz-gql-api) — GraphQL access layer
- [`medz-explorer`](https://github.com/anisbouhadida/medz-explorer) — Angular exploration UI

---

## License

This project is distributed under the Apache License 2.0.
