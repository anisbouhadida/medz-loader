package dz.anisbouhadida.medzloader.batch.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@DisplayName("ReleaseSyncHistoryTasklet")
@ExtendWith(MockitoExtension.class)
class ReleaseSyncHistoryTaskletTest {

  @Mock private JobRepository jobRepository;

  @Mock private NamedParameterJdbcTemplate jdbcTemplate;

  @Mock private ChunkContext chunkContext;

  private JobInstance jobInstance;
  private JobExecution jobExecution;
  private StepExecution loaderStepExecution;
  private StepContribution contribution;
  private ReleaseSyncHistoryTasklet tasklet;

  @BeforeEach
  void setUp() {
    jobInstance = new JobInstance(11L, "medzLoaderJob");
    var parameters = new JobParametersBuilder().addString("release", "2026-08").toJobParameters();
    jobExecution = new JobExecution(42L, jobInstance, parameters);
    contribution =
        new StepContribution(new StepExecution(101L, "releaseSyncHistoryStep", jobExecution));

    loaderStepExecution = new StepExecution(102L, "medzLoaderStep", jobExecution);
    loaderStepExecution.setStatus(BatchStatus.COMPLETED);
    loaderStepExecution.setReadCount(120L);
    loaderStepExecution.setWriteCount(115L);
    loaderStepExecution.setFilterCount(5L);

    tasklet = new ReleaseSyncHistoryTasklet(jobRepository, jdbcTemplate);
  }

  @Nested
  @DisplayName("execute()")
  class ExecuteTests {

    @Test
    @DisplayName("should insert metadata from the completed loader step")
    void shouldInsertCompletedReleaseMetadata() {
      when(jobRepository.getLastStepExecution(jobInstance, "medzLoaderStep"))
          .thenReturn(loaderStepExecution);

      var result = tasklet.execute(contribution, chunkContext);

      var parametersCaptor = ArgumentCaptor.forClass(SqlParameterSource.class);
      verify(jdbcTemplate).update(anyString(), parametersCaptor.capture());
      var insertedParameters = parametersCaptor.getValue();
      assertThat(insertedParameters.getValue("release")).isEqualTo("2026-08");
      assertThat(insertedParameters.getValue("jobExecutionId")).isEqualTo(42L);
      assertThat(insertedParameters.getValue("readCount")).isEqualTo(120L);
      assertThat(insertedParameters.getValue("writeCount")).isEqualTo(115L);
      assertThat(insertedParameters.getValue("filterCount")).isEqualTo(5L);
      assertThat(result).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    @DisplayName("should reject missing loader step metadata")
    void shouldRejectMissingLoaderStepMetadata() {
      when(jobRepository.getLastStepExecution(jobInstance, "medzLoaderStep")).thenReturn(null);

      assertThatThrownBy(() -> tasklet.execute(contribution, chunkContext))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("No completed medzLoaderStep execution was found");
      verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("should reject non-completed loader step metadata")
    void shouldRejectNonCompletedLoaderStepMetadata() {
      loaderStepExecution.setStatus(BatchStatus.FAILED);
      when(jobRepository.getLastStepExecution(jobInstance, "medzLoaderStep"))
          .thenReturn(loaderStepExecution);

      assertThatThrownBy(() -> tasklet.execute(contribution, chunkContext))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("No completed medzLoaderStep execution was found");
      verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("should propagate database failures")
    void shouldPropagateDatabaseFailure() {
      var failure = new DataAccessResourceFailureException("Database unavailable");
      when(jobRepository.getLastStepExecution(jobInstance, "medzLoaderStep"))
          .thenReturn(loaderStepExecution);
      when(jdbcTemplate.update(anyString(), any(SqlParameterSource.class))).thenThrow(failure);

      assertThatThrownBy(() -> tasklet.execute(contribution, chunkContext)).isSameAs(failure);
    }
  }
}
