# Release Sync History Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Load strict `YYYY-MM` release directories sequentially and append one database history row after each complete, successful three-file release.

**Architecture:** A discovery component validates and sorts immutable release descriptors. Job configuration generates a load step and a transactional metadata step for each release, reusing the existing chunk pipeline and recording counts from the completed load `StepExecution`; a unique load-step ID makes metadata restart-safe.

**Tech Stack:** Java 25, Spring Boot 4.0.6, Spring Batch 6, Spring JDBC, PostgreSQL, JUnit 5, Mockito, AssertJ, Maven.

---

## File Map

- Create `src/main/java/dz/anisbouhadida/medzloader/batch/model/MedicineRelease.java`: immutable release month, relative directory, and ordered resources.
- Create `src/main/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineCsvFileType.java`: one authoritative filename-to-type classification rule.
- Create `src/main/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscovery.java`: validate direct release directories and return chronological descriptors.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineItemReaderClassifier.java`: delegate filename recognition to `MedicineCsvFileType`.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfiguration.java`: build a fresh reader for one release rather than one global recursive reader bean.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java`: expose load-step creation for a supplied release.
- Create `src/main/java/dz/anisbouhadida/medzloader/domain/model/DataSyncHistory.java`: immutable successful-sync data.
- Create `src/main/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepository.java`: append sync history using named JDBC parameters.
- Create `src/main/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTasklet.java`: locate the completed load execution and record its statistics.
- Modify `src/main/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfiguration.java`: generate chronological load/metadata pairs and an empty-input no-op flow.
- Create `src/main/resources/sql/write/insert-data-sync-history.sql`: idempotent insert keyed by load step execution.
- Modify `src/main/resources/sql/ddl.sql`: add `data_sync_history` and its latest-release index.
- Modify `src/main/resources/sql/nuke-reset.sql`: reset the new business table with the existing schema reset.
- Modify `README.md`: document strict release directories and the history table/query.
- Add focused tests beside each production component, plus a Spring Batch flow integration test.

Keep all existing user-authored worktree changes. Every commit command below uses explicit paths; do not stage the entire worktree.

### Task 1: Centralize CSV Type Classification

**Files:**
- Create: `src/main/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineCsvFileType.java`
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineItemReaderClassifier.java`
- Create: `src/test/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineCsvFileTypeTest.java`
- Modify: `src/test/java/dz/anisbouhadida/medzloader/batch/reader/MedicineItemReaderClassifierTest.java`

- [ ] **Step 1: Write the failing filename classification tests**

```java
@DisplayName("MedicineCsvFileType")
class MedicineCsvFileTypeTest {

  @ParameterizedTest
  @CsvSource({
    "nomenclature.csv, NOMENCLATURE",
    "retraits_2026.csv, WITHDRAWAL",
    "non_renouveles.csv, NON_RENEWAL"
  })
  void fromFilename_shouldClassifyRecognizedFiles(String filename, MedicineCsvFileType expected) {
    assertThat(MedicineCsvFileType.fromFilename(filename)).isEqualTo(expected);
  }

  @Test
  void fromFilename_shouldRejectUnknownFiles() {
    assertThatThrownBy(() -> MedicineCsvFileType.fromFilename("other.csv"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown medicine CSV file type: other.csv");
  }
}
```

Update `MedicineItemReaderClassifierTest` expected unknown-file text to the same message.

- [ ] **Step 2: Run the tests and verify the new type is missing**

Run: `./mvnw -Dtest=MedicineCsvFileTypeTest,MedicineItemReaderClassifierTest test`

Expected: compilation fails because `MedicineCsvFileType` does not exist.

- [ ] **Step 3: Add the enum and route the reader classifier through it**

```java
/// Supported medicine CSV file types and their filename contract.
public enum MedicineCsvFileType {
  NOMENCLATURE("nomenclature"),
  WITHDRAWAL("retraits"),
  NON_RENEWAL("non_renouveles");

  private final String filenameToken;

  MedicineCsvFileType(String filenameToken) {
    this.filenameToken = filenameToken;
  }

  public static MedicineCsvFileType fromFilename(String filename) {
    return Arrays.stream(values())
        .filter(type -> filename.contains(type.filenameToken))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown medicine CSV file type: " + filename));
  }
}
```

Replace the classifier conditional with an exhaustive switch:

```java
return switch (MedicineCsvFileType.fromFilename(currentResource)) {
  case NOMENCLATURE -> readerFactory.createNomenclatureReader();
  case WITHDRAWAL -> readerFactory.createWithdrawalReader();
  case NON_RENEWAL -> readerFactory.createNonRenewalReader();
};
```

- [ ] **Step 4: Run classification tests**

Run: `./mvnw -Dtest=MedicineCsvFileTypeTest,MedicineItemReaderClassifierTest test`

Expected: all tests pass.

- [ ] **Step 5: Commit only classifier work**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineCsvFileType.java src/main/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineItemReaderClassifier.java src/test/java/dz/anisbouhadida/medzloader/batch/support/classifier/MedicineCsvFileTypeTest.java src/test/java/dz/anisbouhadida/medzloader/batch/reader/MedicineItemReaderClassifierTest.java
git commit -m "refactor: centralize medicine CSV classification"
```

### Task 2: Discover and Validate Release Directories

**Files:**
- Create: `src/main/java/dz/anisbouhadida/medzloader/batch/model/MedicineRelease.java`
- Create: `src/main/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscovery.java`
- Create: `src/test/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscoveryTest.java`

- [ ] **Step 1: Write failing discovery tests using `@TempDir`**

```java
@DisplayName("MedicineReleaseDiscovery")
class MedicineReleaseDiscoveryTest {

  @TempDir Path inputDirectory;

  @Test
  void discover_shouldReturnReleasesInChronologicalOrder() throws IOException {
    createRelease("2026-04");
    createRelease("2026-02");

    var discovery = new MedicineReleaseDiscovery(inputDirectory);

    assertThat(discovery.discover())
        .extracting(MedicineRelease::version)
        .containsExactly("2026-02", "2026-04");
  }

  @ParameterizedTest
  @ValueSource(strings = {"2026-4", "April-2026", "2026-13"})
  void discover_shouldRejectInvalidReleaseDirectoryNames(String directory) throws IOException {
    Files.createDirectory(inputDirectory.resolve(directory));

    assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(directory);
  }

  @Test
  void discover_shouldRejectMissingRequiredFile() throws IOException {
    var release = Files.createDirectory(inputDirectory.resolve("2026-04"));
    Files.createFile(release.resolve("nomenclature.csv"));
    Files.createFile(release.resolve("retraits.csv"));

    assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2026-04")
        .hasMessageContaining("NON_RENEWAL");
  }

  private void createRelease(String version) throws IOException {
    var release = Files.createDirectory(inputDirectory.resolve(version));
    Files.createFile(release.resolve("nomenclature.csv"));
    Files.createFile(release.resolve("non_renouveles.csv"));
    Files.createFile(release.resolve("retraits.csv"));
  }
}
```

Add cases for a duplicate type, unknown CSV, extra CSV, non-directory child, and empty input returning an empty list.

- [ ] **Step 2: Run discovery tests and verify they fail**

Run: `./mvnw -Dtest=MedicineReleaseDiscoveryTest test`

Expected: compilation fails because the release types are missing.

- [ ] **Step 3: Add the immutable release descriptor**

```java
/// One validated, dated medicine release and its ordered CSV resources.
public record MedicineRelease(
    YearMonth period, Path relativeDirectory, List<Resource> resources) {

  public MedicineRelease {
    resources = List.copyOf(resources);
  }

  public String version() {
    return period.toString();
  }
}
```

- [ ] **Step 4: Implement strict discovery and complete-set validation**

```java
public final class MedicineReleaseDiscovery {
  private static final DateTimeFormatter RELEASE_FORMAT =
      new DateTimeFormatterBuilder().appendPattern("uuuu-MM").toFormatter()
          .withResolverStyle(ResolverStyle.STRICT);
  private final Path inputDirectory;

  public MedicineReleaseDiscovery(MedzLoaderProperties properties) {
    this(Path.of(properties.inputDir()));
  }

  MedicineReleaseDiscovery(Path inputDirectory) {
    this.inputDirectory = inputDirectory.toAbsolutePath().normalize();
  }

  public List<MedicineRelease> discover() throws IOException {
    try (var children = Files.list(inputDirectory)) {
      return children.map(this::toRelease).sorted(Comparator.comparing(MedicineRelease::period)).toList();
    }
  }

  private MedicineRelease toRelease(Path directory) {
    if (!Files.isDirectory(directory)) {
      throw invalid(directory, "expected a release directory");
    }
    var period = parsePeriod(directory);
    var byType = discoverCsvFiles(directory);
    var required = EnumSet.allOf(MedicineCsvFileType.class);
    if (!byType.keySet().equals(required)) {
      var missing = EnumSet.copyOf(required);
      missing.removeAll(byType.keySet());
      throw invalid(directory, "missing required file types: " + missing);
    }
    var resources = Stream.of(
            MedicineCsvFileType.NOMENCLATURE,
            MedicineCsvFileType.NON_RENEWAL,
            MedicineCsvFileType.WITHDRAWAL)
        .map(byType::get)
        .map(FileSystemResource::new)
        .map(Resource.class::cast)
        .toList();
    return new MedicineRelease(period, inputDirectory.relativize(directory), resources);
  }

  private YearMonth parsePeriod(Path directory) {
    try {
      return YearMonth.parse(directory.getFileName().toString(), RELEASE_FORMAT);
    } catch (DateTimeParseException exception) {
      throw invalid(directory, "release directory must use YYYY-MM", exception);
    }
  }

  private Map<MedicineCsvFileType, Path> discoverCsvFiles(Path directory) {
    var filesByType = new EnumMap<MedicineCsvFileType, Path>(MedicineCsvFileType.class);
    try (var children = Files.list(directory)) {
      children.forEach(
          path -> {
            if (!Files.isRegularFile(path)
                || !path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
              throw invalid(path, "release entries must be CSV files");
            }
            var type = MedicineCsvFileType.fromFilename(path.getFileName().toString());
            var duplicate = filesByType.put(type, path);
            if (duplicate != null) {
              throw invalid(path, "duplicate " + type + " files: " + duplicate + " and " + path);
            }
          });
    } catch (IOException exception) {
      throw new UncheckedIOException("Cannot inspect release directory " + directory, exception);
    }
    return Map.copyOf(filesByType);
  }

  private IllegalArgumentException invalid(Path path, String reason) {
    return new IllegalArgumentException("Invalid medicine release " + path + ": " + reason);
  }

  private IllegalArgumentException invalid(Path path, String reason, Exception cause) {
    return new IllegalArgumentException("Invalid medicine release " + path + ": " + reason, cause);
  }
}
```

Import the Java time, NIO, collection, stream, Spring `Resource`, and `FileSystemResource` types referenced above. Keep `MedicineReleaseDiscovery(Path)` package-visible for `@TempDir` tests.

- [ ] **Step 5: Run discovery tests**

Run: `./mvnw -Dtest=MedicineReleaseDiscoveryTest test`

Expected: all discovery tests pass.

- [ ] **Step 6: Commit release discovery**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/model/MedicineRelease.java src/main/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscovery.java src/test/java/dz/anisbouhadida/medzloader/batch/support/discovery/MedicineReleaseDiscoveryTest.java
git commit -m "feat: discover dated medicine releases"
```

### Task 3: Persist Append-Only Sync History

**Files:**
- Create: `src/main/java/dz/anisbouhadida/medzloader/domain/model/DataSyncHistory.java`
- Create: `src/main/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepository.java`
- Create: `src/main/resources/sql/write/insert-data-sync-history.sql`
- Modify: `src/main/resources/sql/ddl.sql`
- Modify: `src/main/resources/sql/nuke-reset.sql`
- Create: `src/test/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepositoryTest.java`

- [ ] **Step 1: Write the failing repository test**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("DataSyncHistoryRepository")
class DataSyncHistoryRepositoryTest {
  @Mock private NamedParameterJdbcTemplate jdbcTemplate;

  @Test
  void append_shouldBindEveryHistoryField() {
    var repository = new DataSyncHistoryRepository(jdbcTemplate);
    var history = new DataSyncHistory("2026-04", Path.of("2026-04"), 41L, 42L, 100L, 90L, 8L, 2L);

    repository.append(history);

    var parameters = ArgumentCaptor.forClass(MapSqlParameterSource.class);
    verify(jdbcTemplate).update(anyString(), parameters.capture());
    assertThat(parameters.getValue().getValue("releaseVersion")).isEqualTo("2026-04");
    assertThat(parameters.getValue().getValue("sourceDirectory")).isEqualTo("2026-04");
    assertThat(parameters.getValue().getValue("jobExecutionId")).isEqualTo(41L);
    assertThat(parameters.getValue().getValue("loadStepExecutionId")).isEqualTo(42L);
    assertThat(parameters.getValue().getValue("readCount")).isEqualTo(100L);
    assertThat(parameters.getValue().getValue("writeCount")).isEqualTo(90L);
    assertThat(parameters.getValue().getValue("filterCount")).isEqualTo(8L);
    assertThat(parameters.getValue().getValue("skipCount")).isEqualTo(2L);
  }
}
```

- [ ] **Step 2: Run the repository test and verify it fails**

Run: `./mvnw -Dtest=DataSyncHistoryRepositoryTest test`

Expected: compilation fails because the model and repository do not exist.

- [ ] **Step 3: Add the history record and JDBC repository**

```java
/// Metadata for one successfully loaded release.
public record DataSyncHistory(
    String releaseVersion,
    Path sourceDirectory,
    long jobExecutionId,
    long loadStepExecutionId,
    long readCount,
    long writeCount,
    long filterCount,
    long skipCount) {}
```

```java
@Repository
public class DataSyncHistoryRepository {
  private static final String INSERT_SQL =
      MedicineSqlUtils.loadSql("sql/write/insert-data-sync-history.sql");
  private final NamedParameterJdbcTemplate jdbcTemplate;

  public DataSyncHistoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void append(DataSyncHistory history) {
    var parameters = new MapSqlParameterSource()
        .addValue("releaseVersion", history.releaseVersion())
        .addValue("sourceDirectory", history.sourceDirectory().toString())
        .addValue("jobExecutionId", history.jobExecutionId())
        .addValue("loadStepExecutionId", history.loadStepExecutionId())
        .addValue("readCount", history.readCount())
        .addValue("writeCount", history.writeCount())
        .addValue("filterCount", history.filterCount())
        .addValue("skipCount", history.skipCount());
    jdbcTemplate.update(INSERT_SQL, parameters);
  }
}
```

- [ ] **Step 4: Add idempotent SQL and DDL**

```sql
INSERT INTO data_sync_history (
    release_version, source_directory, job_execution_id, load_step_execution_id,
    read_count, write_count, filter_count, skip_count
) VALUES (
    :releaseVersion, :sourceDirectory, :jobExecutionId, :loadStepExecutionId,
    :readCount, :writeCount, :filterCount, :skipCount
)
ON CONFLICT (load_step_execution_id) DO NOTHING;
```

Append the exact table and `(release_version DESC, loaded_at DESC)` index from the approved design to `ddl.sql`. Add `data_sync_history` before its referenced business tables in the explicit `TRUNCATE ... RESTART IDENTITY CASCADE` or drop order used by `nuke-reset.sql`.

- [ ] **Step 5: Run repository and SQL-loading tests**

Run: `./mvnw -Dtest=DataSyncHistoryRepositoryTest,MedicineSqlUtilsTest test`

Expected: all tests pass and the SQL resource loads from the classpath.

- [ ] **Step 6: Commit persistence work**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/domain/model/DataSyncHistory.java src/main/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepository.java src/main/resources/sql/write/insert-data-sync-history.sql src/main/resources/sql/ddl.sql src/main/resources/sql/nuke-reset.sql src/test/java/dz/anisbouhadida/medzloader/infrastructure/jdbc/repository/DataSyncHistoryRepositoryTest.java
git commit -m "feat: persist release sync history"
```

### Task 4: Record Completed Load-Step Statistics

**Files:**
- Create: `src/main/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTasklet.java`
- Create: `src/test/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTaskletTest.java`

- [ ] **Step 1: Write failing tasklet tests**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("RecordReleaseSyncTasklet")
class RecordReleaseSyncTaskletTest {
  @Mock private DataSyncHistoryRepository repository;
  @Mock private JobExplorer jobExplorer;

  @Test
  void execute_shouldAppendCountsFromNamedCompletedLoadStep() throws Exception {
    var jobExecution = new JobExecution(41L, null, new JobParameters());
    var loadExecution = new StepExecution(42L, "loadRelease-2026-04", jobExecution);
    loadExecution.setStatus(BatchStatus.COMPLETED);
    loadExecution.setReadCount(100);
    loadExecution.setWriteCount(90);
    loadExecution.setFilterCount(8);
    loadExecution.setReadSkipCount(1);
    loadExecution.setWriteSkipCount(1);
    jobExecution.addStepExecution(loadExecution);
    var metadataExecution = new StepExecution("recordReleaseSync-2026-04", jobExecution);
    var chunkContext = new ChunkContext(new StepContext(metadataExecution));
    when(jobExplorer.getLastStepExecution(jobExecution.getJobInstance(), "loadRelease-2026-04"))
        .thenReturn(loadExecution);
    var tasklet = new RecordReleaseSyncTasklet(
        "2026-04", Path.of("2026-04"), "loadRelease-2026-04", jobExplorer, repository);

    var result = tasklet.execute(metadataExecution.createStepContribution(), chunkContext);

    assertThat(result).isEqualTo(RepeatStatus.FINISHED);
    verify(repository).append(new DataSyncHistory("2026-04", Path.of("2026-04"), 41L, 42L, 100L, 90L, 8L, 2L));
  }
}
```

Add a test asserting `IllegalStateException` when the named load step is absent or not `COMPLETED`.

- [ ] **Step 2: Run tasklet tests and verify they fail**

Run: `./mvnw -Dtest=RecordReleaseSyncTaskletTest test`

Expected: compilation fails because the tasklet does not exist.

- [ ] **Step 3: Implement the focused tasklet**

```java
public final class RecordReleaseSyncTasklet implements Tasklet {
  private final String releaseVersion;
  private final Path sourceDirectory;
  private final String loadStepName;
  private final JobExplorer jobExplorer;
  private final DataSyncHistoryRepository repository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    var metadataExecution = chunkContext.getStepContext().getStepExecution();
    var jobExecution = metadataExecution.getJobExecution();
    var loadExecution = Optional.ofNullable(
            jobExplorer.getLastStepExecution(jobExecution.getJobInstance(), loadStepName))
        .filter(execution -> execution.getStatus() == BatchStatus.COMPLETED)
        .orElseThrow(() -> new IllegalStateException("No completed load step named " + loadStepName));
    repository.append(new DataSyncHistory(
        releaseVersion,
        sourceDirectory,
        loadExecution.getJobExecutionId(),
        loadExecution.getId(),
        loadExecution.getReadCount(),
        loadExecution.getWriteCount(),
        loadExecution.getFilterCount(),
        loadExecution.getSkipCount()));
    return RepeatStatus.FINISHED;
  }
}
```

Add a constructor assigning all final fields and `///` documentation for the public type.

- [ ] **Step 4: Run tasklet tests**

Run: `./mvnw -Dtest=RecordReleaseSyncTaskletTest test`

Expected: all tests pass.

- [ ] **Step 5: Commit tasklet work**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTasklet.java src/test/java/dz/anisbouhadida/medzloader/batch/tasklet/RecordReleaseSyncTaskletTest.java
git commit -m "feat: record completed release statistics"
```

### Task 5: Build Release-Scoped Readers and Steps

**Files:**
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfiguration.java`
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java`
- Create: `src/test/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfigurationTest.java`
- Create: `src/test/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfigurationTest.java`

- [ ] **Step 1: Write failing release-reader tests**

```java
@Test
void releaseItemReader_shouldUseOnlyTheReleaseResources() {
  var resources = List.<Resource>of(
      new ByteArrayResource(new byte[0]) { public String getFilename() { return "nomenclature.csv"; } },
      new ByteArrayResource(new byte[0]) { public String getFilename() { return "non_renouveles.csv"; } },
      new ByteArrayResource(new byte[0]) { public String getFilename() { return "retraits.csv"; } });
  var release = new MedicineRelease(YearMonth.of(2026, 4), Path.of("2026-04"), resources);

  var reader = configuration.releaseItemReader(release, classifier);

  assertThat(reader).isNotNull();
}
```

Also add a step-configuration test asserting `loadReleaseStep(release, ...)` has name `loadRelease-2026-04` and `recordReleaseSyncStep(release, ...)` has name `recordReleaseSync-2026-04`.

- [ ] **Step 2: Run configuration tests and verify missing factory methods**

Run: `./mvnw -Dtest=MedicineItemReaderConfigurationTest,MedzLoaderStepConfigurationTest test`

Expected: compilation fails because release-scoped factory methods do not exist.

- [ ] **Step 3: Replace the global recursive reader bean with a release factory method**

```java
public MultiResourceItemReader<MedicineLine> releaseItemReader(
    MedicineRelease release, MedicineItemReaderClassifier classifier) {
  var delegate = new FileAwareMedicineItemReader(classifier);
  return new MultiResourceItemReaderBuilder<MedicineLine>()
      .name("releaseItemReader-" + release.version())
      .delegate(delegate)
      .resources(release.resources().toArray(Resource[]::new))
      .build();
}
```

Keep the existing classifier and reader-factory beans. Remove recursive `CSV_PATTERN`, `PathMatchingResourcePatternResolver`, and the global `multiCsvItemReader` bean.

- [ ] **Step 4: Convert step configuration into explicit step factories**

```java
public Step loadReleaseStep(
    MedicineRelease release,
    JobRepository jobRepository,
    PlatformTransactionManager transactionManager,
    ItemReader<MedicineLine> reader,
    ItemProcessor<MedicineLine, MedicineEvent> processor,
    ItemWriter<MedicineEvent> writer) {
  return new StepBuilder("loadRelease-" + release.version(), jobRepository)
      .<MedicineLine, MedicineEvent>chunk(100, transactionManager)
      .reader(reader)
      .processor(processor)
      .writer(writer)
      .faultTolerant()
      .skip(ValidationException.class)
      .skipLimit(20)
      .listener(medzLoaderSkipListener())
      .build();
}

public Step recordReleaseSyncStep(
    MedicineRelease release,
    JobRepository jobRepository,
    JobExplorer jobExplorer,
    PlatformTransactionManager transactionManager,
    DataSyncHistoryRepository repository) {
  var tasklet = new RecordReleaseSyncTasklet(
      release.version(), release.relativeDirectory(), "loadRelease-" + release.version(),
      jobExplorer, repository);
  return new StepBuilder("recordReleaseSync-" + release.version(), jobRepository)
      .tasklet(tasklet, transactionManager)
      .build();
}
```

Remove the single global `medzLoaderStep` bean. Keep creation methods focused and package-visible where tests need direct access.

- [ ] **Step 5: Run configuration tests**

Run: `./mvnw -Dtest=MedicineItemReaderConfigurationTest,MedzLoaderStepConfigurationTest test`

Expected: all tests pass.

- [ ] **Step 6: Commit reader and step factories**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfiguration.java src/main/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfiguration.java src/test/java/dz/anisbouhadida/medzloader/batch/config/reader/MedicineItemReaderConfigurationTest.java src/test/java/dz/anisbouhadida/medzloader/batch/config/step/MedzLoaderStepConfigurationTest.java
git commit -m "refactor: scope batch steps to one release"
```

### Task 6: Generate the Chronological Job Flow

**Files:**
- Modify: `src/main/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfiguration.java`
- Create: `src/test/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfigurationTest.java`

- [ ] **Step 1: Write failing job-flow tests**

```java
@Test
void medzLoaderJob_shouldPairLoadAndMetadataStepsChronologically() throws Exception {
  when(discovery.discover()).thenReturn(List.of(aprilRelease, februaryRelease));

  var job = configuration.medzLoaderJob(
      jobRepository, transactionManager, discovery, readerConfiguration,
      stepConfiguration, jobExplorer, classifier, processor, writer, historyRepository);

  assertThat(((SimpleJob) job).getStepNames())
      .containsExactly(
          "loadRelease-2026-02",
          "recordReleaseSync-2026-02",
          "loadRelease-2026-04",
          "recordReleaseSync-2026-04");
}

@Test
void medzLoaderJob_shouldUseNoOpStepForEmptyInput() throws Exception {
  when(discovery.discover()).thenReturn(List.of());

  var job = configuration.medzLoaderJob(
      jobRepository, transactionManager, discovery, readerConfiguration,
      stepConfiguration, jobExplorer, classifier, processor, writer, historyRepository);

  assertThat(((SimpleJob) job).getStepNames()).containsExactly("noReleases");
}
```

- [ ] **Step 2: Run job configuration tests and verify failure**

Run: `./mvnw -Dtest=MedzLoaderJobConfigurationTest test`

Expected: compilation fails because the job method still accepts one global step.

- [ ] **Step 3: Register discovery and build a deterministic sequential job**

```java
@Bean
MedicineReleaseDiscovery medicineReleaseDiscovery(MedzLoaderProperties properties) {
  return new MedicineReleaseDiscovery(properties);
}

@Bean
Job medzLoaderJob(
    JobRepository jobRepository,
    PlatformTransactionManager transactionManager,
    MedicineReleaseDiscovery discovery,
    MedicineItemReaderConfiguration readerConfiguration,
    MedzLoaderStepConfiguration stepConfiguration,
    JobExplorer jobExplorer,
    MedicineItemReaderClassifier classifier,
    ItemProcessor<MedicineLine, MedicineEvent> processor,
    ItemWriter<MedicineEvent> writer,
    DataSyncHistoryRepository historyRepository) throws IOException {
  var releases = discovery.discover().stream()
      .sorted(Comparator.comparing(MedicineRelease::period))
      .toList();
  var jobBuilder = new JobBuilder("medzLoaderJob", jobRepository)
      .incrementer(new RunIdIncrementer());
  if (releases.isEmpty()) {
    return jobBuilder.start(noReleasesStep(jobRepository, transactionManager)).build();
  }

  var steps = new ArrayList<Step>();
  for (var release : releases) {
    var reader = readerConfiguration.releaseItemReader(release, classifier);
    steps.add(stepConfiguration.loadReleaseStep(
        release, jobRepository, transactionManager, reader, processor, writer));
    steps.add(stepConfiguration.recordReleaseSyncStep(
        release, jobRepository, jobExplorer, transactionManager, historyRepository));
  }
  var flow = jobBuilder.start(steps.getFirst());
  steps.stream().skip(1).forEach(flow::next);
  return flow.build();
}
```

Build `noReleases` with a tasklet returning `RepeatStatus.FINISHED`. Do not add an async executor or split flow.

- [ ] **Step 4: Run job configuration tests**

Run: `./mvnw -Dtest=MedzLoaderJobConfigurationTest test`

Expected: chronological and empty-input tests pass.

- [ ] **Step 5: Commit dynamic job configuration**

```bash
git add src/main/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfiguration.java src/test/java/dz/anisbouhadida/medzloader/batch/config/job/MedzLoaderJobConfigurationTest.java
git commit -m "feat: load releases in chronological job steps"
```

### Task 7: Verify Success, Failure, and Restart Behavior

**Files:**
- Create: `src/test/java/dz/anisbouhadida/medzloader/batch/ReleaseSyncJobTest.java`
- Modify: `README.md`

- [ ] **Step 1: Add PostgreSQL-backed job-flow tests**

Use `@SpringBatchTest`, `@SpringBootTest`, `@ActiveProfiles("test")`, `JobLauncherTestUtils`, `JdbcTemplate`, and `@TempDir`. Override `medz.loader.input-dir` with `@DynamicPropertySource`. Create valid minimal CSV fixtures using the exact headers and column order from `MedicineCsvColumns`.

```java
@Test
void job_shouldAppendOneHistoryRowAfterACompleteRelease() throws Exception {
  createValidRelease("2026-04");

  var execution = jobLauncherTestUtils.launchJob(uniqueParameters());

  assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  assertThat(jdbcTemplate.queryForMap(
      "SELECT release_version, source_directory FROM data_sync_history"))
      .containsEntry("release_version", "2026-04")
      .containsEntry("source_directory", "2026-04");
}

@Test
void job_shouldNotAppendHistoryWhenReleaseLoadingFails() throws Exception {
  createReleaseWithMalformedNomenclature("2026-04");

  var execution = jobLauncherTestUtils.launchJob(uniqueParameters());

  assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
  assertThat(jdbcTemplate.queryForObject(
      "SELECT count(*) FROM data_sync_history WHERE release_version = '2026-04'", Long.class))
      .isZero();
}
```

Define the fixture and job-parameter helpers explicitly:

```java
private static void createValidRelease(String version) throws IOException {
  var directory = Files.createDirectories(inputDirectory.resolve(version));
  writeHeader(directory.resolve("nomenclature.csv"), MedicineCsvColumns.nomenclatureFields());
  writeHeader(directory.resolve("non_renouveles.csv"), MedicineCsvColumns.nonRenewalFields());
  writeHeader(directory.resolve("retraits.csv"), MedicineCsvColumns.withdrawalFields());
}

private static void createReleaseWithMalformedNomenclature(String version) throws IOException {
  createValidRelease(version);
  Files.writeString(
      inputDirectory.resolve(version).resolve("nomenclature.csv"),
      "not-an-integer\n",
      StandardOpenOption.APPEND);
}

private static void writeHeader(Path file, String[] fields) throws IOException {
  Files.writeString(file, String.join(",", fields) + System.lineSeparator());
}

private static JobParameters uniqueParameters() {
  return new JobParametersBuilder()
      .addLong("test.id", System.nanoTime(), true)
      .toJobParameters();
}
```

Add a two-release test proving `2026-03` history remains when `2026-04` fails:

```java
@Test
void job_shouldKeepEarlierReleaseHistoryWhenLaterReleaseFails() throws Exception {
  createValidRelease("2026-03");
  createReleaseWithMalformedNomenclature("2026-04");

  var execution = jobLauncherTestUtils.launchJob(uniqueParameters());

  assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
  assertThat(jdbcTemplate.queryForList(
      "SELECT release_version FROM data_sync_history ORDER BY release_version", String.class))
      .containsExactly("2026-03");
}
```

For restart behavior, define a nested repository and test configuration:

```java
static final class FailingOnceDataSyncHistoryRepository extends DataSyncHistoryRepository {
  private final AtomicBoolean failNext = new AtomicBoolean();

  FailingOnceDataSyncHistoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  void failNextAppend() {
    failNext.set(true);
  }

  @Override
  public void append(DataSyncHistory history) {
    if (failNext.compareAndSet(true, false)) {
      throw new DataAccessResourceFailureException("metadata failure for restart test");
    }
    super.append(history);
  }
}

@TestConfiguration
static class RestartTestConfiguration {
  @Bean
  @Primary
  FailingOnceDataSyncHistoryRepository failingOnceHistoryRepository(
      NamedParameterJdbcTemplate jdbcTemplate) {
    return new FailingOnceDataSyncHistoryRepository(jdbcTemplate);
  }
}
```

Launch twice with identical identifying parameters and verify persisted-step reuse:

```java
@Test
void restart_shouldRetryMetadataWithoutRerunningCompletedLoadStep() throws Exception {
  createValidRelease("2026-04");
  failingHistoryRepository.failNextAppend();
  var parameters = uniqueParameters();

  var first = jobLauncherTestUtils.launchJob(parameters);
  var second = jobLauncherTestUtils.launchJob(parameters);

  assertThat(first.getStatus()).isEqualTo(BatchStatus.FAILED);
  assertThat(second.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  assertThat(jdbcTemplate.queryForObject(
      "SELECT count(*) FROM data_sync_history WHERE release_version = '2026-04'", Long.class))
      .isEqualTo(1L);
  assertThat(jobExplorer.getStepExecutionCount(
      second.getJobInstance(), "loadRelease-2026-04"))
      .isEqualTo(1L);
}
```

- [ ] **Step 2: Run the integration test and observe the initial failure**

Run: `./mvnw -Dtest=ReleaseSyncJobTest test`

Expected: tests fail until the test schema is initialized from `sql/ddl.sql` and fixtures match the reader contract.

- [ ] **Step 3: Make test setup deterministic**

Add `@Sql(scripts = "/sql/ddl.sql", executionPhase = BEFORE_TEST_CLASS)` and clean business tables in `@BeforeEach` using the existing reset approach. Keep Spring Batch metadata so restart assertions use real persisted executions. Ensure fixture dates use the configured `Africa/Algiers` conversion and invalid-row filtering is asserted through `filter_count`, not treated as a job failure.

- [ ] **Step 4: Run integration tests**

Run: `./mvnw -Dtest=ReleaseSyncJobTest test`

Expected: success, fatal failure, earlier-release persistence, filtering counts, and restart tests pass against PostgreSQL.

- [ ] **Step 5: Update README input and database documentation**

Replace language saying the loader recursively accepts arbitrary CSV layout with the strict direct-child contract:

```text
input/
└── YYYY-MM/
    ├── nomenclature.csv
    ├── non_renouveles.csv
    └── retraits.csv
```

Document that releases run oldest-first and add `data_sync_history` to the database table list. Include this downstream example:

```sql
SELECT release_version, loaded_at
FROM data_sync_history
ORDER BY release_version DESC, loaded_at DESC
LIMIT 1;
```

- [ ] **Step 6: Run formatting and the full verification build**

Run: `./mvnw spotless:apply`

Expected: Google Java formatting is applied only to touched Java sources.

Run: `./mvnw clean verify`

Expected: all unit and PostgreSQL-backed tests pass, Spotless passes, JaCoCo bundle line coverage remains at or above `0.50`, and source/Javadoc artifacts build.

- [ ] **Step 7: Review the final diff for scope and invariants**

Run: `git diff --check && git diff --stat && git status --short`

Expected: no whitespace errors; only release-sync files plus pre-existing user changes are present. Confirm the five-column medicine business key, optimistic upsert predicate, validation filtering, CSV column arrays, and Java 25 instance `main()` remain unchanged.

- [ ] **Step 8: Commit integration coverage and docs**

```bash
git add src/test/java/dz/anisbouhadida/medzloader/batch/ReleaseSyncJobTest.java README.md
git commit -m "test: verify release sync lifecycle"
```
