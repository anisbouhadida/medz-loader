package dz.anisbouhadida.medzloader.batch.config.job;

import dz.anisbouhadida.medzloader.batch.support.validator.ReleaseJobParametersValidator;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Configuration class that defines the Spring Batch [Job] for loading medicine data.
///
/// The job loads medicine records from CSV files and then records the successful release
/// synchronization.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Configuration
public class MedzLoaderJobConfiguration {

  /// Creates the `medzLoaderJob` batch [Job].
  ///
  /// The job loads the medicine data first, then records the release synchronization metadata.
  ///
  /// @param jobRepository   the repository used to persist job metadata
  /// @param medzLoaderStep  the step that performs the medicine loading work
  /// @param releaseSyncHistoryStep the step that records the successful synchronization
  /// @param releaseJobParametersValidator the validator for the required release parameter
  /// @return a fully configured batch job
  @Bean
  public Job medzLoaderJob(
      JobRepository jobRepository,
      @Qualifier("medzLoaderStep") Step medzLoaderStep,
      @Qualifier("releaseSyncHistoryStep") Step releaseSyncHistoryStep,
      ReleaseJobParametersValidator releaseJobParametersValidator) {
    return new JobBuilder("medzLoaderJob", jobRepository)
        .validator(releaseJobParametersValidator)
        .start(medzLoaderStep)
        .next(releaseSyncHistoryStep)
        .build();
  }
}
