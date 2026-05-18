package dz.anisbouhadida.medzloader.batch.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Configuration class that defines the Spring Batch [Job] for loading medicine data.
///
/// The job consists of two sequential [Step]s:
///
/// 1. **downloadStep** – downloads CSV files from a GitHub repository using a
///    shell script triggered by push-event webhook data.
/// 2. **medzLoaderStep** – reads, processes, and writes the downloaded medicine
///    records into the target data store.
///
/// @author Anis Bouhadida
/// @since 0.0.1
/// @version 0.3.0
@Configuration
public class MedzLoaderJobConfiguration {

  /// Creates the `medzLoaderJob` batch [Job].
  ///
  /// The job starts with the download step, then proceeds to the
  /// medicine-loading step. It is persisted via the given [JobRepository].
  ///
  /// @param jobRepository   the repository used to persist job metadata
  /// @param downloadStep    the step that downloads CSV files from GitHub
  /// @param medzLoaderStep  the step that performs the medicine loading work
  /// @return a fully configured batch job
  @Bean
  public Job medzLoaderJob(
      JobRepository jobRepository,
      @Qualifier("downloadStep") Step downloadStep,
      @Qualifier("medzLoaderStep") Step medzLoaderStep) {
    return new JobBuilder("medzLoaderJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(downloadStep)
        .next(medzLoaderStep)
        .build();
  }
}
