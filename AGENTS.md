# AGENTS.md

## First reads
- Follow `.github/instructions/spring.instructions.md` and `.github/instructions/java.instructions.md` for Java/Spring changes.
- Start with `README.md`, then trace the batch flow through `src/main/java/dz/anisbouhadida/medzloader/batch/config/**`.

## Big picture
- This is a Spring Batch ETL that ingests Algerian medicine CSV files and writes a normalized PostgreSQL schema.
- The code is intentionally split into `batch/` → `domain/` → `infrastructure/`:
  - `batch/`: CSV DTOs, readers, processors, writers, job/step config
  - `domain/`: immutable records and the `MedicineApi` / `MedicinePort` boundary
  - `infrastructure/`: JDBC adapter/repository implementations
- End-to-end flow: `MultiResourceItemReader` discovers `**/*.csv` under `medz.loader.input-dir` → `FileAwareMedicineItemReader` picks a reader by filename → `BeanValidatingItemProcessor` filters invalid rows → `MedicineLineMapper` maps DTOs to sealed `MedicineEvent` records → composite JDBC writers upsert `medicine`, write status history, write event-specific tables, then append to `medicine_event_history`.

## Key invariants you must preserve
- Filenames are part of behavior: `MedicineItemReaderClassifier` recognizes `nomenclature`, `retraits`, and `non_renouveles` substrings. Renaming file conventions requires classifier + tests updates.
- CSV column order is centralized in `batch/dto/constant/MedicineCsvColumns.java`; readers in `MedicineItemReaderFactory` depend on those exact arrays.
- The business key is the 5-column tuple `(registration_number, code, icd, brand_name, laboratory_holder)` from `sql/ddl.sql`. `MedicineSqlUtils.compositeKeyParams(...)`, repository lookups, and all writer SQL depend on it.
- Versioning is optimistic: `MedicineLineMapper` asks `MedicineApi` for the current version, and `sql/write/medicine-upsert.sql` only updates when `medicine.version = excluded.version`, then increments the stored version.
- Validation failures are filtered, not fatal: `beanValidatingItemProcessor().setFilter(true)` means bad rows are silently skipped rather than failing the job.

## Conventions specific to this repo
- Prefer immutable Java records and sealed interfaces for data shapes (`Medicine`, `MedicineEvent`, `NomenclatureEvent`, etc.).
- SQL lives in classpath resources under `src/main/resources/sql/{read,write}` and is loaded with `MedicineSqlUtils.loadSql(...)`; do not inline long SQL in Java config.
- MapStruct is used via `Mappers.getMapper(MedicineLineMapper.class)` in config, not `componentModel = "spring"`.
- The app entry point in `MedzLoaderApplication.java` uses a Java 25 instance `main()` style; do not “correct” it to a classic `public static void main(String[] args)` unless there is a concrete reason.
- Comments use `///` doc comments throughout; keep that style when adding public-facing code.

## When adding/changing an event or CSV format
- Touch these together: DTO record in `batch/dto/`, field order in `MedicineCsvColumns`, reader factory, filename classifier, MapStruct mappings, processor classifier, writer classifier, SQL files under `src/main/resources/sql/write`, DDL in `sql/ddl.sql`, and corresponding tests.
- Use existing event types as the template: `NomenclatureEvent`, `NonRenewalEvent`, `WithdrawalEvent`.

## Build, run, and test
- Use Java 25 and Maven 3.9+; `pom.xml` enforces both.
- Preferred verification command:
  - `./mvnw clean verify`
- `verify` also runs Spotless (`google-java-format`), JaCoCo (bundle line coverage threshold `0.50`), enforcer rules, source/javadoc jars.
- Run locally with:
  - `./mvnw spring-boot:run`
- Default config is in `src/main/resources/application.properties`; the job recursively scans `medz.loader.input-dir` and defaults to `./input`.
- Tests use `src/test/resources/application-test.properties`, which disables auto-running the batch job (`spring.batch.job.enabled=false`) and points to a local PostgreSQL test DB on `jdbc:postgresql://localhost:5432/medz_test`.

## Test style to follow
- Most tests are plain JUnit 5 + Mockito + AssertJ unit tests with `@Nested` and `@DisplayName`; copy that style for new logic.
- Good reference tests: `MedicineServiceTest`, `JdbcMedicineRepositoryTest`, `MedicineItemReaderClassifierTest`, `FileAwareMedicineItemReaderTest`.

