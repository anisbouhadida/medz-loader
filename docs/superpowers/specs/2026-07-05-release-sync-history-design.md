# Release Sync History Design

## Goal

Record append-only synchronization history for every dated medicine release loaded by one successful Spring Batch job execution. A downstream API can query the latest `YYYY-MM` release while retaining the history of prior loads.

## Input Contract

`medz.loader.input-dir` contains direct release subdirectories named with a strict `YYYY-MM` format:

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

Every release directory must contain exactly one CSV recognized as `nomenclature`, one as `non_renouveles`, and one as `retraits`. Directory names, missing files, duplicates, unknown CSVs, and nested entries are validated before processing.

## Spring Batch Architecture

The job uses fixed Spring-managed beans and standard Spring Batch flow composition:

1. `medzLoaderStep` is the existing chunk-oriented ETL step. Its `MultiResourceItemReader` receives all validated CSV resources, ordered first by release month and then by file type.
2. `recordSyncHistoryStep` is a tasklet step. It appends one metadata row for every validated release represented in the completed load.
3. `medzLoaderJob` is a fixed job defined with `.start(medzLoaderStep).next(recordSyncHistoryStep)`.

Spring Batch therefore owns step construction, flow transitions, transactions, job metadata, and restart decisions. The history step cannot start unless the load step completes successfully. There are no dynamically generated steps, partitions, custom launch loops, or per-release child jobs.

Custom implementation is limited to behavior the framework cannot infer:

- validating the Medz `YYYY-MM` directory and three-file contract;
- carrying the immutable discovered release catalog to the reader and tasklet; and
- inserting the business synchronization rows.

## Release Catalog

At application startup, a singleton discovery component creates an immutable, chronologically sorted release catalog. Both fixed steps consume this same catalog:

- reader configuration flattens its ordered CSV resources into one `MultiResourceItemReader`;
- the history tasklet inserts its release descriptors after the load step succeeds.

Using one immutable catalog prevents the reader and metadata tasklet from observing different filesystem states during a job execution. The existing filename classifier remains authoritative for selecting format-specific flat-file readers.

An empty input directory is valid: the chunk step reads no items and the history step inserts no rows.

## Persistence Model

`data_sync_history` stores successful business-level release loads:

```sql
CREATE TABLE IF NOT EXISTS data_sync_history (
    sync_id BIGSERIAL PRIMARY KEY,
    release_version VARCHAR(7) NOT NULL,
    source_directory TEXT NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    job_execution_id BIGINT NOT NULL,

    CONSTRAINT chk_data_sync_release_version
        CHECK (release_version ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    CONSTRAINT uq_data_sync_job_release
        UNIQUE (job_execution_id, release_version)
);
```

An index on `(release_version DESC, synced_at DESC)` supports the latest-release query. `release_version` is intentionally not globally unique: a later successful job may reload the same release and append another historical row. The composite unique constraint makes a restarted history step idempotent within the same job execution.

`source_directory` stores the normalized path relative to `medz.loader.input-dir`, avoiding machine-specific absolute paths. Spring Batch tables remain the source for operational attempts, failures, and detailed read/write counts; the business table does not duplicate those concerns.

The insert SQL lives in `src/main/resources/sql/write/insert-data-sync-history.sql` and uses `ON CONFLICT (job_execution_id, release_version) DO NOTHING`.

## Data Flow and Transactions

The reader processes all release resources through the existing pipeline:

```text
immutable release catalog
  -> MultiResourceItemReader
  -> FileAwareMedicineItemReader
  -> validating/composite processor
  -> composite JDBC writer
  -> recordSyncHistoryStep
```

The load step retains chunk transactions and restart state. A load failure rolls back the active chunk and prevents the history step from running. Previously committed chunks retain normal Spring Batch restart semantics, but no business sync row is written until the complete load step succeeds.

The history tasklet runs in its own standard tasklet transaction and inserts all release rows as one batch operation. If persistence fails, Spring Batch marks the history step and job as failed. On restart, the completed load step is skipped and only the history step is retried. The unique job/release constraint prevents duplicates if a retry occurs after a partial or uncertain client outcome.

## Downstream Query Contract

The newest source release available in the database is:

```sql
SELECT release_version, source_directory, synced_at
FROM data_sync_history
ORDER BY release_version DESC, synced_at DESC
LIMIT 1;
```

Consumers can order by `synced_at` or filter by `release_version` to expose complete synchronization history.

## Testing

Tests follow the repository's JUnit 5, Mockito, AssertJ, `@Nested`, and `@DisplayName` conventions and cover:

- strict release discovery, validation, chronological ordering, and immutable resources;
- a single global reader built from the complete catalog;
- fixed bean names and the job transition `medzLoaderStep -> recordSyncHistoryStep`;
- successful batch insertion of every catalog release;
- idempotency for the same job execution and release;
- no history rows when the load step fails;
- restart behavior where a completed load step is skipped and history is retried;
- validation failures remaining filtered rather than fatal; and
- the PostgreSQL constraint and latest-release query.

Final verification is `./mvnw clean verify` with Java 25 and PostgreSQL available.

## Out of Scope

- An HTTP or GraphQL API in this loader repository.
- Failure-attempt duplication outside Spring Batch metadata.
- File-level history rows or file checksums.
- Parallel release processing or partitioning.
- Dynamic step generation.
