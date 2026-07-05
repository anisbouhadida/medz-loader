# Release Sync History Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace dynamic release-specific steps with one fixed Spring Batch job that loads all validated releases and appends history in a fixed follow-up tasklet step.

**Architecture:** A singleton immutable release catalog supplies one standard `MultiResourceItemReader` and one step-scoped metadata tasklet. Spring configuration declares fixed `medzLoaderStep`, `recordSyncHistoryStep`, and `medzLoaderJob` beans composed with `.start(...).next(...)`; custom code is limited to Medz directory validation and JDBC history persistence.

**Tech Stack:** Java 25, Spring Boot 4.0.6, Spring Batch 6, Spring JDBC, PostgreSQL, JUnit 5, Mockito, AssertJ, Maven.

---

## File Map

- Create `src/main/java/dz/anisbouhadida/medzloader/batch/model/MedicineReleaseCatalog.java`: immutable releases and flattened ordered resources.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscovery.java`: return the immutable catalog after strict validation.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfiguration.java`: restore fixed Spring beans for the catalog and global reader.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java`: declare fixed load, step-scoped tasklet, and history-step beans.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfiguration.java`: declare one fixed two-step job.
- Delete `src/main/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTasklet.java`: replace custom orchestration with a standard step-scoped `Tasklet` bean.
- Modify `src/main/java/dz/anisbouhadida/medzloader/domain/model/DataSyncHistory.java`: retain only release, directory, and job execution identity.
- Modify `src/main/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepository.java`: batch-insert every catalog release.
- Modify `src/main/resources/sql/ddl.sql`, `src/main/resources/sql/write/insert-data-sync-history.sql`, and `src/main/resources/sql/nuke-reset.sql`: align schema and idempotency with job/release identity.
- Replace obsolete dynamic-step tests with fixed-bean, tasklet, persistence, and PostgreSQL lifecycle tests.
- Modify `README.md`: document the fixed two-step flow and history query.

The worktree already contains unrelated user changes. Use explicit file paths for every edit and commit; do not reset, restore, or stage the whole tree.

### Task 1: Build One Immutable Release Catalog

**Files:**
- Create: `src/main/java/dz/anisbouhadida/medzloader/batch/model/MedicineReleaseCatalog.java`
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscovery.java`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscoveryTest.java`

- [ ] **Step 1: Write the failing catalog test**

```java
@Test
void discoverCatalog_shouldExposeChronologicalReleasesAndResources() throws IOException {
  createRelease("2026-04");
  createRelease("2026-02");

  var catalog = new MedicineReleaseDiscovery(inputDirectory).discoverCatalog();

  assertThat(catalog.releases())
      .extracting(MedicineRelease::version)
      .containsExactly("2026-02", "2026-04");
  assertThat(catalog.resources())
      .extracting(Resource::getFilename)
      .containsExactly(
          "nomenclature.csv", "non_renouveles.csv", "retraits.csv",
          "nomenclature.csv", "non_renouveles.csv", "retraits.csv");
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=MedicineReleaseDiscoveryTest test`

Expected: compilation fails because `discoverCatalog()` and `MedicineReleaseCatalog` do not exist.

- [ ] **Step 3: Add the immutable catalog and discovery method**

```java
/// Immutable input snapshot shared by the fixed load and metadata steps.
public record MedicineReleaseCatalog(
    List<MedicineRelease> releases, List<Resource> resources) {

  public MedicineReleaseCatalog {
    releases = List.copyOf(releases);
    resources = List.copyOf(resources);
  }

  public static MedicineReleaseCatalog from(List<MedicineRelease> releases) {
    var immutableReleases = List.copyOf(releases);
    var resources = immutableReleases.stream()
        .flatMap(release -> release.resources().stream())
        .toList();
    return new MedicineReleaseCatalog(immutableReleases, resources);
  }
}
```

Add this method while retaining `discover()` as the strict validation implementation:

```java
public MedicineReleaseCatalog discoverCatalog() throws IOException {
  return MedicineReleaseCatalog.from(discover());
}
```

- [ ] **Step 4: Verify GREEN**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=MedicineReleaseDiscoveryTest test`

Expected: all discovery and catalog tests pass.

- [ ] **Step 5: Commit the catalog boundary**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/model/MedicineReleaseCatalog.java src/main/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscovery.java src/test/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscoveryTest.java
git commit -m "refactor: share immutable release catalog"
```

### Task 2: Restore a Fixed Spring-Managed Reader and Load Step

**Files:**
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfiguration.java`
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfigurationTest.java`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfigurationTest.java`

- [ ] **Step 1: Replace dynamic-factory expectations with failing fixed-bean tests**

```java
@Test
void multiCsvItemReader_shouldUseTheCompleteCatalog() {
  var catalog = MedicineReleaseCatalog.from(List.of(release));

  var reader = configuration.multiCsvItemReader(catalog, fileAwareReader);

  assertThat(reader.getName()).isEqualTo("multiCsvItemReader");
}
```

```java
@Test
void medzLoaderStep_shouldHaveStableBeanStepName() {
  var step = configuration.medzLoaderStep(
      jobRepository, transactionManager, reader, processor, writer);

  assertThat(step.getName()).isEqualTo("medzLoaderStep");
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=MedicineItemReaderConfigurationTest,MedzLoaderStepConfigurationTest test`

Expected: compilation fails because the current configuration exposes release-specific factory methods.

- [ ] **Step 3: Declare the fixed catalog, delegate, and reader beans**

```java
@Bean
MedicineReleaseCatalog medicineReleaseCatalog(MedicineReleaseDiscovery discovery)
    throws IOException {
  return discovery.discoverCatalog();
}

@Bean
ResourceAwareItemReaderItemStream<MedicineLine> fileAwareMedicineItemReader(
    MedicineItemReaderClassifier classifier) {
  return new FileAwareMedicineItemReader(classifier);
}

@Bean
MultiResourceItemReader<MedicineLine> multiCsvItemReader(
    MedicineReleaseCatalog catalog,
    ResourceAwareItemReaderItemStream<MedicineLine> fileAwareMedicineItemReader) {
  return new MultiResourceItemReaderBuilder<MedicineLine>()
      .name("multiCsvItemReader")
      .delegate(fileAwareMedicineItemReader)
      .resources(catalog.resources().toArray(Resource[]::new))
      .build();
}
```

Keep the existing classifier and reader-factory beans. Remove `releaseItemReader(...)`.

- [ ] **Step 4: Declare the fixed chunk step bean**

```java
@Bean
public Step medzLoaderStep(
    JobRepository jobRepository,
    PlatformTransactionManager transactionManager,
    ItemReader<MedicineLine> multiCsvItemReader,
    ItemProcessor<MedicineLine, MedicineEvent> medicineItemProcessor,
    ItemWriter<MedicineEvent> medicineCompositeItemWriter) {
  return new StepBuilder("medzLoaderStep", jobRepository)
      .<MedicineLine, MedicineEvent>chunk(100)
      .transactionManager(transactionManager)
      .reader(multiCsvItemReader)
      .processor(medicineItemProcessor)
      .writer(medicineCompositeItemWriter)
      .faultTolerant()
      .skip(ValidationException.class)
      .skipLimit(20)
      .listener(medzLoaderSkipListener())
      .build();
}
```

Remove `loadReleaseStep(...)`. Keep validation filtering set to `true`.

- [ ] **Step 5: Verify GREEN**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=MedicineItemReaderConfigurationTest,MedzLoaderStepConfigurationTest,MedicineItemProcessorConfigurationTest test`

Expected: all fixed reader, step, and validation-filter tests pass.

- [ ] **Step 6: Commit fixed reader and load-step beans**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfiguration.java src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java src/test/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfigurationTest.java src/test/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfigurationTest.java
git commit -m "refactor: restore fixed batch reader and load step"
```

### Task 3: Simplify History Persistence to Job and Release Identity

**Files:**
- Modify: `src/main/java/dz/anisbouhadida/medzloader/domain/model/DataSyncHistory.java`
- Modify: `src/main/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepository.java`
- Modify: `src/main/resources/sql/write/insert-data-sync-history.sql`
- Modify: `src/main/resources/sql/ddl.sql`
- Modify: `src/main/resources/sql/nuke-reset.sql`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepositoryTest.java`

- [ ] **Step 1: Write failing batch-persistence tests**

```java
@Test
void appendAll_shouldBatchEveryReleaseForOneJobExecution() {
  var histories = List.of(
      new DataSyncHistory("2026-03", Path.of("2026-03"), 41L),
      new DataSyncHistory("2026-04", Path.of("2026-04"), 41L));

  repository.appendAll(histories);

  var parameters = ArgumentCaptor.forClass(SqlParameterSource[].class);
  verify(jdbcTemplate).batchUpdate(anyString(), parameters.capture());
  assertThat(parameters.getValue()).hasSize(2);
  assertThat(parameters.getValue()[1].getValue("releaseVersion")).isEqualTo("2026-04");
  assertThat(parameters.getValue()[1].getValue("jobExecutionId")).isEqualTo(41L);
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=DataSyncHistoryRepositoryTest test`

Expected: compilation fails because `DataSyncHistory` still contains step statistics and `appendAll` is absent.

- [ ] **Step 3: Simplify the record and batch repository**

```java
public record DataSyncHistory(
    String releaseVersion, Path sourceDirectory, long jobExecutionId) {}
```

```java
public void appendAll(List<DataSyncHistory> histories) {
  var parameters = histories.stream()
      .map(history -> new MapSqlParameterSource()
          .addValue("releaseVersion", history.releaseVersion())
          .addValue("sourceDirectory", history.sourceDirectory().toString())
          .addValue("jobExecutionId", history.jobExecutionId()))
      .toArray(SqlParameterSource[]::new);
  if (parameters.length > 0) {
    jdbcTemplate.batchUpdate(INSERT_SQL, parameters);
  }
}
```

- [ ] **Step 4: Align SQL and DDL**

```sql
INSERT INTO data_sync_history (
    release_version, source_directory, job_execution_id
) VALUES (
    :releaseVersion, :sourceDirectory, :jobExecutionId
)
ON CONFLICT (job_execution_id, release_version) DO NOTHING;
```

Replace the current history table with the approved `sync_id`, `release_version`, `source_directory`, `synced_at`, and `job_execution_id` definition. Add `UNIQUE (job_execution_id, release_version)` and index `(release_version DESC, synced_at DESC)`. Keep `data_sync_history` in `nuke-reset.sql`.

- [ ] **Step 5: Verify GREEN**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=DataSyncHistoryRepositoryTest,MedicineSqlUtilsTest test`

Expected: repository binding and SQL resource tests pass.

- [ ] **Step 6: Commit simplified history persistence**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/domain/model/DataSyncHistory.java src/main/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepository.java src/main/resources/sql/write/insert-data-sync-history.sql src/main/resources/sql/ddl.sql src/main/resources/sql/nuke-reset.sql src/test/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepositoryTest.java
git commit -m "refactor: persist job-level release history"
```

### Task 4: Add a Step-Scoped Metadata Tasklet and Fixed Job Flow

**Files:**
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java`
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfiguration.java`
- Delete: `src/main/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTasklet.java`
- Delete: `src/test/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTaskletTest.java`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfigurationTest.java`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfigurationTest.java`

- [ ] **Step 1: Write failing fixed-flow and tasklet tests**

Test the tasklet bean directly with two releases and job execution ID `41`:

```java
@Test
void recordSyncHistoryTasklet_shouldAppendEveryCatalogRelease() throws Exception {
  var catalog = MedicineReleaseCatalog.from(List.of(marchRelease, aprilRelease));
  var tasklet = configuration.recordSyncHistoryTasklet(catalog, repository, 41L);

  assertThat(tasklet.execute(contribution, chunkContext)).isEqualTo(RepeatStatus.FINISHED);
  verify(repository).appendAll(List.of(
      new DataSyncHistory("2026-03", Path.of("2026-03"), 41L),
      new DataSyncHistory("2026-04", Path.of("2026-04"), 41L)));
}
```

Test the fixed job composition:

```java
@Test
void medzLoaderJob_shouldContainFixedLoadAndHistorySteps() {
  var job = configuration.medzLoaderJob(jobRepository, medzLoaderStep, recordSyncHistoryStep);

  assertThat(((SimpleJob) job).getStepNames())
      .containsExactly("medzLoaderStep", "recordSyncHistoryStep");
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=MedzLoaderStepConfigurationTest,MedzLoaderJobConfigurationTest test`

Expected: tests fail because configuration still dynamically generates release-specific steps.

- [ ] **Step 3: Declare the step-scoped standard tasklet and history step**

```java
@Bean
@StepScope
Tasklet recordSyncHistoryTasklet(
    MedicineReleaseCatalog catalog,
    DataSyncHistoryRepository repository,
    @Value("#{jobExecution.id}") Long jobExecutionId) {
  return (contribution, chunkContext) -> {
    var histories = catalog.releases().stream()
        .map(release -> new DataSyncHistory(
            release.version(), release.relativeDirectory(), jobExecutionId))
        .toList();
    repository.appendAll(histories);
    return RepeatStatus.FINISHED;
  };
}

@Bean
Step recordSyncHistoryStep(
    JobRepository jobRepository,
    PlatformTransactionManager transactionManager,
    Tasklet recordSyncHistoryTasklet) {
  return new StepBuilder("recordSyncHistoryStep", jobRepository)
      .tasklet(recordSyncHistoryTasklet, transactionManager)
      .build();
}
```

Delete the custom `RecordReleaseSyncTasklet` class and its obsolete test after the bean-level test is green.

- [ ] **Step 4: Replace dynamic job construction with fixed flow composition**

```java
@Bean
public Job medzLoaderJob(
    JobRepository jobRepository,
    Step medzLoaderStep,
    Step recordSyncHistoryStep) {
  return new JobBuilder("medzLoaderJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .start(medzLoaderStep)
      .next(recordSyncHistoryStep)
      .build();
}
```

Remove release discovery, reader configuration, processor, writer, transaction manager, dynamic arrays, sorting, and no-op logic from job configuration.

- [ ] **Step 5: Verify GREEN**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=MedzLoaderStepConfigurationTest,MedzLoaderJobConfigurationTest test`

Expected: tasklet mapping and fixed two-step job tests pass.

- [ ] **Step 6: Commit fixed Spring Batch flow composition**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java src/main/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfiguration.java src/main/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTasklet.java src/test/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTaskletTest.java src/test/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfigurationTest.java src/test/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfigurationTest.java
git commit -m "refactor: compose sync history with fixed batch steps"
```

### Task 5: Verify Lifecycle, Restart Semantics, and Documentation

**Files:**
- Modify: `src/test/java/dz/anisbouhadida/medzloader/batch/ReleaseSyncJobTest.java`
- Modify: `README.md`
- Modify: `pom.xml` only if the existing Java 25 Mockito premain configuration is still required.

- [ ] **Step 1: Update PostgreSQL lifecycle tests for multiple release rows**

Create two header-only releases in the static test catalog and assert both rows after success:

```java
@Test
void job_shouldAppendEveryReleaseAfterCompleteLoad() throws Exception {
  var execution = jobOperator.start(medzLoaderJob, uniqueParameters());

  assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  assertThat(jdbcTemplate.queryForList(
      "SELECT release_version FROM data_sync_history ORDER BY release_version", String.class))
      .containsExactly("2026-03", "2026-04");
}
```

Retain the malformed-file test and assert the load failure leaves `data_sync_history` empty. Add a repository-level PostgreSQL assertion that inserting the same `(job_execution_id, release_version)` twice leaves one row, and verify the latest query returns `2026-04`.

- [ ] **Step 2: Run lifecycle tests against PostgreSQL**

Run with the configured test datasource: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw -Dtest=ReleaseSyncJobTest test`

Expected: successful job inserts both releases, failed load inserts none, duplicate job/release is idempotent, and latest release is `2026-04`.

- [ ] **Step 3: Update README architecture and query**

Document the fixed flow exactly:

```text
medzLoaderJob
  -> medzLoaderStep (chunk ETL over all validated release resources)
  -> recordSyncHistoryStep (transactional tasklet)
```

Document `synced_at` and the latest query:

```sql
SELECT release_version, source_directory, synced_at
FROM data_sync_history
ORDER BY release_version DESC, synced_at DESC
LIMIT 1;
```

Remove descriptions of per-release dynamic steps, step statistics in the business history table, and `loaded_at`.

- [ ] **Step 4: Format and run full verification**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw com.diffplug.spotless:spotless-maven-plugin:3.6.0:apply`

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./mvnw clean verify`

Expected: all tests pass, PostgreSQL lifecycle coverage passes, Spotless is clean, JaCoCo remains at or above `0.50`, and source/Javadoc artifacts build.

- [ ] **Step 5: Audit scope and invariants**

Run: `git diff --check && git status --short`

Confirm with `rg` that:

```text
MedzLoaderJobConfiguration contains one .start(medzLoaderStep).next(recordSyncHistoryStep)
MedzLoaderStepConfiguration declares fixed @Bean steps
MedicineItemReaderConfiguration declares multiCsvItemReader as @Bean
no production code references loadRelease- or recordReleaseSync-
beanValidatingItemProcessor still calls setFilter(true)
upsert-medicine.sql still contains medicine.version = excluded.version
```

Do not commit unrelated pre-existing worktree changes. If feature files overlap those changes, report the boundary rather than broad-staging them.

- [ ] **Step 6: Commit lifecycle coverage and documentation**

```bash
git add src/test/java/dz/anisbouhadida/medzloader/batch/ReleaseSyncJobTest.java README.md pom.xml
git commit -m "test: verify fixed release sync flow"
```
