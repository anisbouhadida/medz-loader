package dz.anisbouhadida.medzloader.batch.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Configuration class that defines the Spring Batch [Job] for loading medicine data.
///
/// The job consists of a single [Step] that reads, processes, and writes
/// medicine records from CSV files into the target data store.
@Configuration
public class MedzLoaderJobConfiguration {

    /// Creates the `medzLoaderJob` batch [Job].
    ///
    /// The job starts with the provided [Step] and is persisted via the
    /// given [JobRepository].
    ///
    /// @param jobRepository   the repository used to persist job metadata
    /// @param medzLoaderStep  the step that performs the medicine loading work
    /// @return a fully configured batch job
    @Bean
    public Job medzLoaderJob(JobRepository jobRepository, Step medzLoaderStep) {
        return new JobBuilder("medzLoaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(medzLoaderStep)
                .build();
    }
}
