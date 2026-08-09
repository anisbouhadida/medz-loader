package dz.anisbouhadida.medzloader.batch.config.step;

import dz.anisbouhadida.medzloader.batch.tasklet.ReleaseSyncHistoryTasklet;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/// Configures the final step that records a successful release synchronization.
///
/// @author Anis Bouhadida
/// @since 0.2.1
@Configuration
public class ReleaseSyncHistoryStepConfiguration {

  /// Creates the transactional tasklet step that persists release synchronization metadata.
  ///
  /// @param jobRepository the repository used to access and persist batch metadata
  /// @param transactionManager the transaction manager governing the history insert
  /// @param releaseSyncHistoryTasklet the tasklet that inserts the history row
  /// @return the configured release synchronization history step
  @Bean
  public Step releaseSyncHistoryStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ReleaseSyncHistoryTasklet releaseSyncHistoryTasklet) {
    return new StepBuilder("releaseSyncHistoryStep", jobRepository)
        .tasklet(releaseSyncHistoryTasklet, transactionManager)
        .build();
  }
}
