package dz.anisbouhadida.medzloader.batch.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dz.anisbouhadida.medzloader.domain.model.DataSyncHistory;
import dz.anisbouhadida.medzloader.infrastructure.jdbc.repository.DataSyncHistoryRepository;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordReleaseSyncTasklet")
class RecordReleaseSyncTaskletTest {

  private static final String LOAD_STEP_NAME = "loadRelease-2026-04";

  @Mock private DataSyncHistoryRepository repository;
  @Mock private JobRepository jobRepository;

  private JobInstance jobInstance;
  private JobExecution jobExecution;
  private StepExecution metadataExecution;

  @BeforeEach
  void setUp() {
    jobInstance = new JobInstance(40L, "medzLoaderJob");
    jobExecution = new JobExecution(41L, jobInstance, new JobParameters());
    metadataExecution = new StepExecution(43L, "recordReleaseSync-2026-04", jobExecution);
  }

  @Test
  void execute_shouldAppendCountsFromPersistedCompletedLoadStep() throws Exception {
    var loadExecution = new StepExecution(42L, LOAD_STEP_NAME, jobExecution);
    loadExecution.setStatus(BatchStatus.COMPLETED);
    loadExecution.setReadCount(100);
    loadExecution.setWriteCount(90);
    loadExecution.setFilterCount(8);
    loadExecution.setReadSkipCount(1);
    loadExecution.setWriteSkipCount(1);
    when(jobRepository.getLastStepExecution(jobInstance, LOAD_STEP_NAME)).thenReturn(loadExecution);
    var tasklet = createTasklet();

    var result =
        tasklet.execute(
            metadataExecution.createStepContribution(),
            new ChunkContext(new StepContext(metadataExecution)));

    assertThat(result).isEqualTo(RepeatStatus.FINISHED);
    verify(repository)
        .append(
            new DataSyncHistory(
                "2026-04", Path.of("2026-04"), 41L, 42L, 100L, 90L, 8L, 2L));
  }

  @Test
  void execute_shouldRejectMissingLoadStep() {
    when(jobRepository.getLastStepExecution(jobInstance, LOAD_STEP_NAME)).thenReturn(null);

    assertThatThrownBy(
            () ->
                createTasklet()
                    .execute(
                        metadataExecution.createStepContribution(),
                        new ChunkContext(new StepContext(metadataExecution))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No completed load step named " + LOAD_STEP_NAME);
  }

  @Test
  void execute_shouldRejectFailedLoadStep() {
    var loadExecution = new StepExecution(42L, LOAD_STEP_NAME, jobExecution);
    loadExecution.setStatus(BatchStatus.FAILED);
    when(jobRepository.getLastStepExecution(jobInstance, LOAD_STEP_NAME)).thenReturn(loadExecution);

    assertThatThrownBy(
            () ->
                createTasklet()
                    .execute(
                        metadataExecution.createStepContribution(),
                        new ChunkContext(new StepContext(metadataExecution))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No completed load step named " + LOAD_STEP_NAME);
  }

  private RecordReleaseSyncTasklet createTasklet() {
    return new RecordReleaseSyncTasklet(
        "2026-04", Path.of("2026-04"), LOAD_STEP_NAME, jobRepository, repository);
  }
}
