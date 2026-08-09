package dz.anisbouhadida.medzloader.batch.config.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dz.anisbouhadida.medzloader.batch.support.validator.ReleaseJobParametersValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;

@DisplayName("MedzLoaderJobConfiguration")
@ExtendWith(MockitoExtension.class)
class MedzLoaderJobConfigurationTest {

  @Mock private JobRepository jobRepository;

  @Mock private Step medzLoaderStep;

  @Mock private Step releaseSyncHistoryStep;

  private MedzLoaderJobConfiguration configuration;
  private ReleaseJobParametersValidator validator;

  @BeforeEach
  void setUp() {
    configuration = new MedzLoaderJobConfiguration();
    validator = new ReleaseJobParametersValidator();
    when(medzLoaderStep.getName()).thenReturn("medzLoaderStep");
    when(releaseSyncHistoryStep.getName()).thenReturn("releaseSyncHistoryStep");
  }

  @Test
  @DisplayName("should load medicines before recording synchronization history")
  void shouldConfigureStepsInOrder() {
    var job =
        configuration.medzLoaderJob(
            jobRepository, medzLoaderStep, releaseSyncHistoryStep, validator);

    assertThat(job).isInstanceOf(SimpleJob.class);
    assertThat(((SimpleJob) job).getStepNames())
        .containsExactly("medzLoaderStep", "releaseSyncHistoryStep");
    assertThat(job.getJobParametersValidator()).isSameAs(validator);
    assertThat(job.getJobParametersIncrementer()).isNull();
  }
}
