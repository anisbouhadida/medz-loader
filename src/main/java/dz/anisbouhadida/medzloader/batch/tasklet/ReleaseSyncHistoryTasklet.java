package dz.anisbouhadida.medzloader.batch.tasklet;

import static dz.anisbouhadida.medzloader.batch.support.utils.MedicineSqlUtils.loadSql;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/// Records metadata for a successfully synchronized medicine release.
///
/// The tasklet runs after the medicine-loading step and inserts one row into
/// `release_sync_history`. Loader-step metadata is retrieved from the [JobRepository] so a failed
/// history step can be restarted without rerunning the completed medicine-loading step.
///
/// @author Anis Bouhadida
/// @since 0.2.1
@Component
@RequiredArgsConstructor
public class ReleaseSyncHistoryTasklet implements Tasklet {

  private static final String RELEASE_PARAMETER = "release";
  private static final String MEDZ_LOADER_STEP = "medzLoaderStep";
  private static final String INSERT_SQL = loadSql("sql/write/release-sync-history-insert.sql");

  private final JobRepository jobRepository;
  private final NamedParameterJdbcTemplate jdbcTemplate;

  /// Inserts the release and item counts from the completed medicine-loading step.
  ///
  /// @param contribution the contribution for the current history step
  /// @param chunkContext the context for the current tasklet execution
  /// @return [RepeatStatus#FINISHED] after the history row is inserted
  /// @throws IllegalStateException when no completed medicine-loading step can be found
  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    var jobExecution = contribution.getStepExecution().getJobExecution();
    var loaderStepExecution =
        jobRepository.getLastStepExecution(jobExecution.getJobInstance(), MEDZ_LOADER_STEP);

    validateLoaderStepExecution(loaderStepExecution);

    var parameters =
        new MapSqlParameterSource()
            .addValue(
                RELEASE_PARAMETER, jobExecution.getJobParameters().getString(RELEASE_PARAMETER))
            .addValue("jobExecutionId", jobExecution.getId())
            .addValue("readCount", loaderStepExecution.getReadCount())
            .addValue("writeCount", loaderStepExecution.getWriteCount())
            .addValue("filterCount", loaderStepExecution.getFilterCount());
    jdbcTemplate.update(INSERT_SQL, parameters);

    return RepeatStatus.FINISHED;
  }

  private void validateLoaderStepExecution(StepExecution loaderStepExecution) {
    if (loaderStepExecution == null || loaderStepExecution.getStatus() != BatchStatus.COMPLETED) {
      throw new IllegalStateException("No completed medzLoaderStep execution was found");
    }
  }
}
