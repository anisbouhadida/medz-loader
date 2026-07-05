# Release Sync History Design

## Goal

Record an append-only history entry whenever one dated medicine release is loaded successfully. A downstream API can use this history to report which release is present in PostgreSQL and when it was synchronized.

The unit of synchronization is a release directory, not an individual CSV file. Each direct child of `medz.loader.input-dir` represents one release and must be named `YYYY-MM`, for example:

```text
input/
├── 2026-03/
│   ├── nomenclature.csv
│   ├── non_renouveles.csv
│   └── retraits.csv
└── 2026-04/
    ├── nomenclature.csv
    ├── non_renouveles.csv
    └── retraits.csv
```

## Release Discovery and Validation

At job construction time, the loader discovers the direct subdirectories of the configured input directory and parses each directory name as a strict `YearMonth` using `yyyy-MM`. Releases are sorted in ascending chronological order before steps are created. Sequential processing preserves the medicine optimistic-versioning behavior and prevents an older release from racing with a newer release.

Before any release is loaded, discovery validates the complete input set:

- every discovered child is a directory named `YYYY-MM`;
- each release contains exactly one file recognized as `nomenclature`;
- each release contains exactly one file recognized as `non_renouveles`;
- each release contains exactly one file recognized as `retraits`; and
- no additional CSV file is present in a release directory.

Invalid input fails job creation with a message identifying the release and violation. Existing filename classification remains authoritative, so filename conventions and their tests continue to move together.

## Batch Architecture and Data Flow

The job is generated as a chronological sequence of two steps per release:

1. `loadRelease-YYYY-MM` uses the existing `MultiResourceItemReader`, validation processor, mapper, and composite JDBC writer to process that release's three files using the existing chunk transaction boundary.
2. `recordReleaseSync-YYYY-MM` inserts the successful synchronization metadata in its own transaction.

The metadata step runs only after the corresponding load step completes. If reading, processing, or writing fails, Spring Batch rolls back the active chunk, stops the flow, and does not execute that release's metadata step. Chunks committed before the failure retain the repository's existing restart semantics.

If metadata insertion itself fails, the job fails. On restart, Spring Batch skips the already-completed load step and retries the metadata step, avoiding unnecessary data reprocessing.

Later releases do not run after an earlier release fails. Earlier releases whose load and metadata steps completed remain valid and queryable.

## Persistence Model

Add the following table to `src/main/resources/sql/ddl.sql`:

```sql
CREATE TABLE IF NOT EXISTS data_sync_history (
    sync_id BIGSERIAL PRIMARY KEY,
    release_version VARCHAR(7) NOT NULL,
    source_directory TEXT NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    job_execution_id BIGINT NOT NULL,
    load_step_execution_id BIGINT NOT NULL,
    read_count BIGINT NOT NULL,
    write_count BIGINT NOT NULL,
    filter_count BIGINT NOT NULL,
    skip_count BIGINT NOT NULL,

    CONSTRAINT chk_data_sync_release_version
        CHECK (release_version ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    CONSTRAINT uq_data_sync_load_step_execution
        UNIQUE (load_step_execution_id)
);
```

An index on `(release_version DESC, loaded_at DESC)` supports both latest-release and release-history queries. The release version is intentionally not unique: an intentional load in a later job execution appends another audit row. The load step execution ID is unique so retrying the metadata step cannot duplicate the same successful load.

`source_directory` stores the normalized path relative to `medz.loader.input-dir`, normally the same `YYYY-MM` value. This avoids persisting machine-specific absolute paths.

Counts come from the completed load step:

- `read_count`: rows read from all three files;
- `write_count`: events passed to the composite writer;
- `filter_count`: rows removed by validation/filtering;
- `skip_count`: total read, process, and write skips.

The insert statement lives at `src/main/resources/sql/write/insert-data-sync-history.sql` and is loaded through `MedicineSqlUtils.loadSql(...)`, consistent with existing SQL organization.

## Components

### Release descriptor and discovery

An immutable release descriptor contains the `YearMonth`, relative directory, and the three ordered CSV resources. A focused discovery component reads `MedzLoaderProperties`, validates the directory contract, and returns immutable descriptors sorted by release month.

### Release-scoped reader

Reader configuration creates a `MultiResourceItemReader` for a supplied release descriptor rather than recursively combining every release. It keeps `FileAwareMedicineItemReader` and the existing filename classifier unchanged.

### Sync history writer

A small JDBC repository inserts one immutable sync-history record. A release metadata tasklet reads the completed load `StepExecution` statistics and delegates the insert. The repository owns persistence only; orchestration stays in batch configuration.

### Dynamic job configuration

Job configuration turns the discovered releases into a deterministic sequence of load and metadata steps. Step names include the release version so Spring Batch restart state is stable and inspectable.

When discovery returns no releases, the job contains one no-op step so it remains a valid Spring Batch job and completes without inserting history.

## Failure and Restart Semantics

- Validation failures retain current behavior: filtered records do not fail a release and are reflected in `filter_count`.
- A fatal load failure prevents the release history insert and prevents later releases from starting.
- A metadata insert failure fails the job; restarting retries metadata without rerunning a completed load step.
- The unique load step execution ID makes a repeated metadata insert idempotent.
- A later intentional execution of the same release creates a distinct load step execution and therefore appends a new history row.
- An empty input directory produces a valid job with no release load or history rows.

Spring Batch's own metadata tables remain the source for operational attempts and failures. `data_sync_history` contains successful business-level synchronizations only.

## Downstream Query Contract

The future API can determine the newest data release with:

```sql
SELECT *
FROM data_sync_history
ORDER BY release_version DESC, loaded_at DESC
LIMIT 1;
```

Ordering primarily by `release_version` answers which source release the database contains; `loaded_at` answers when that release was synchronized. Consumers can also filter by `release_version` to expose reload history.

## Testing

Unit tests follow the repository's JUnit 5, Mockito, AssertJ, `@Nested`, and `@DisplayName` conventions and cover:

- strict `YYYY-MM` parsing and chronological sorting;
- missing, duplicate, unknown, and extra CSV files;
- one release descriptor containing the expected three resources;
- deterministic load/metadata step ordering;
- metadata mapping from a completed load step, including aggregate skip counts;
- no metadata step after a failed load step;
- metadata retry without rerunning a completed load step; and
- repository SQL parameters and duplicate-step idempotency.

The DDL and JDBC behavior receive PostgreSQL-backed verification where the current test environment permits it. Final verification is `./mvnw clean verify`.

## Out of Scope

- Exposing an HTTP or GraphQL API in this loader repository.
- Recording failed synchronization attempts in the business table.
- Deriving release versions from CSV contents or filenames.
- Checksums and file-level audit rows.
- Parallel release processing.
