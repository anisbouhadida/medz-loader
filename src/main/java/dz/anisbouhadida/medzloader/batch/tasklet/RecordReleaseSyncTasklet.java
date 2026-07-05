package dz.anisbouhadida.medzloader.batch.tasklet;

import dz.anisbouhadida.medzloader.domain.model.DataSyncHistory;
import dz.anisbouhadida.medzloader.infrastructure.jdbc.repository.DataSyncHistoryRepository;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/// Records business synchronization metadata for a completed release load step.
public final class RecordReleaseSyncTasklet implements Tasklet {

  private final String releaseVersion;
  private final Path sourceDirectory;
  private final String loadStepName;
  private final JobRepository jobRepository;
  private final DataSyncHistoryRepository repository;

  /// Creates a metadata tasklet for one release load step.
  ///
  /// @param releaseVersion source release in `YYYY-MM` format
  /// @param sourceDirectory release directory relative to the configured input directory
  /// @param loadStepName name of the corresponding load step
  /// @param jobRepository persisted Spring Batch execution lookup
  /// @param repository successful synchronization history repository
  public RecordReleaseSyncTasklet(
      String releaseVersion,
      Path sourceDirectory,
      String loadStepName,
      JobRepository jobRepository,
      DataSyncHistoryRepository repository) {
    this.releaseVersion = releaseVersion;
    this.sourceDirectory = sourceDirectory;
    this.loadStepName = loadStepName;
    this.jobRepository = jobRepository;
    this.repository = repository;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    var metadataExecution = chunkContext.getStepContext().getStepExecution();
    var jobExecution = metadataExecution.getJobExecution();
    var loadExecution =
        Optional.ofNullable(
                jobRepository.getLastStepExecution(jobExecution.getJobInstance(), loadStepName))
            .filter(execution -> execution.getStatus() == BatchStatus.COMPLETED)
            .orElseThrow(
                () -> new IllegalStateException("No completed load step named " + loadStepName));

    repository.append(
        new DataSyncHistory(
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
