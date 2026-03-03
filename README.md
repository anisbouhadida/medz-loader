# medz-loader

Open-source Spring Batch ETL that loads official Algerian medicine nomenclature CSV files (additions, non-renewals, withdrawals) into a normalized PostgreSQL schema with versioned status and event history.

## Features

- Discovers and processes all `*.csv` files under a configurable input directory.
- Supports official nomenclature, non-renewal, and withdrawal CSV formats.
- Validates and maps CSV rows into strongly typed domain models and events.
- Uses Spring Batch chunk-oriented processing with multi-resource readers.
- Persists data into a normalized PostgreSQL schema with:
  - `medicine` master table.
  - `medicine_status_history` for status evolution.
  - `nomenclature_event`, `non_renewal_event`, and `withdrawal_event` event tables.
  - `medicine_event_history` as a generic event log.
- Hexagonal/domain-driven design separating domain, batch, and infrastructure concerns.

## Architecture

The application is built with Spring Boot and Spring Batch and follows a hexagonal/domain-driven design:

- **Domain layer**: immutable domain records (`Medicine`, `MedicineEvent`, `NomenclatureEvent`, etc.), service interfaces (`MedicineApi`) and ports (`MedicinePort`).
- **Batch layer**: readers, processors, and writers for CSV files and medicine events.
- **Infrastructure layer**: JDBC-based persistence, SQL scripts, and PostgreSQL-specific DDL.

### Batch pipeline

At a high level, the ETL job works as follows:

1. A multi-resource CSV reader discovers all input files under `medz.loader.input-dir`.
2. A classifier chooses the correct flat-file reader per file type (nomenclature, non-renewals, withdrawals).
3. Rows are validated and mapped into domain events.
4. A composite JDBC writer:
   - Upserts the `medicine` master row using a composite business key.
   - Records status transitions in `medicine_status_history`.
   - Routes events to type-specific writers (`nomenclature_event`, `non_renewal_event`, `withdrawal_event`).
   - Appends an entry to `medicine_event_history`.

The PostgreSQL schema and enum types are defined in `src/main/resources/sql/ddl.sql`.

## Tech stack

- Java 25
- Spring Boot 4 (Spring Batch, validation, JDBC)
- PostgreSQL
- MapStruct for mapping CSV DTOs to domain models
- Lombok for boilerplate reduction

## Getting started

### Prerequisites

- Java 25
- Maven 3.9+
- PostgreSQL instance (local or remote)

### Clone and build

```bash
git clone https://github.com/anisbouhadida/medz-loader.git
cd medz-loader
mvn clean package
```

### Configure database

Create a PostgreSQL database (for example `medz`) and a user with access to it, then configure the connection in `src/main/resources/application.properties` or via environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/medz
spring.datasource.username=postgres
spring.datasource.password=password
spring.batch.jdbc.initialize-schema=always
```

The DDL for the medicine schema is provided in `src/main/resources/sql/ddl.sql`. You can apply it manually or integrate it into your migration tool.

### Configure input directory

The application reads CSV files from a base directory configured via the `medz.loader` namespace:

```properties
medz.loader.input-dir=./input
medz.loader.registration-zone-id=Africa/Algiers
```

These properties can be overridden using environment variables:

- `MEDZ_LOADER_INPUT_DIR`
- `MEDZ_LOADER_REGISTRATION_ZONE_ID`

Place your CSV files in the configured directory. The expected filenames (by convention) are typically:

- `nomenclature.csv`
- `non_renouveles.csv`
- `retraits.csv`

The multi-resource reader will recursively process all `*.csv` files in the folder.

### Run the ETL job

You can run the application using Maven:

```bash
mvn spring-boot:run
```

or by running the packaged jar:

```bash
java -jar target/medz-loader-*.jar
```

Spring Batch will start the configured job, which reads the CSV files and populates the PostgreSQL database.

## Configuration properties

Key configuration properties exposed via `MedzLoaderProperties`:

- `medz.loader.input-dir` – path to the directory containing input CSV files.
- `medz.loader.registration-zone-id` – time zone used to convert registration dates to UTC timestamps.

## Directory structure

```text
input/
  2024-08/
    nomenclature.csv
    non_renouveles.csv
    retraits.csv
  2024-12/
    nomenclature.csv
    non_renouveles.csv
    retraits.csv
src/
  main/
    java/dz/anisbouhadida/medzloader/
      batch/
      config/
      domain/
      infrastructure/
    resources/
      application.properties
      sql/
        ddl.sql
        nuke-reset.sql
```

## License

This project is open source and available under the Apache License 2.0.
