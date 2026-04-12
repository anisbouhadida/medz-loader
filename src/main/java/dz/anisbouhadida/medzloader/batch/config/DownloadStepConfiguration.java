package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.config.MedzLoaderProperties;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

/// Configuration class that defines the download [Step] using a [SystemCommandTasklet].
///
/// This step executes a shell script (`download-output-files.sh`) that downloads
/// CSV files from a GitHub repository based on information from a GitHub push-event
/// webhook payload, passed as job parameters.
///
/// Expected job parameters (from the GitHub push webhook):
///
/// - `repoFullName`  – the repository identifier (e.g. `owner/repo`)
/// - `branch`        – the branch ref (e.g. `refs/heads/main`)
/// - `modifiedFiles` – comma-separated list of added/modified file paths
/// - `githubToken`   – (optional) GitHub token for private repository access
///
/// @author Anis Bouhadida
/// @since 0.3.0
@Configuration
@RequiredArgsConstructor
public class DownloadStepConfiguration {

  private static final String SCRIPT_PATH = "scripts/download-output-files.sh";
  private static final long DEFAULT_TIMEOUT_MILLIS = 300_000L;

  private final MedzLoaderProperties properties;

  /// Creates the [SystemCommandTasklet] that runs the download shell script.
  ///
  /// The bean is **step-scoped** so that late-binding of job parameters via
  /// SpEL expressions is resolved at step execution time.
  ///
  /// The tasklet passes the webhook data as environment variables to the script:
  ///
  /// | Env variable       | Job parameter     |
  /// |--------------------|-------------------|
  /// | `REPO_FULL_NAME`   | `repoFullName`    |
  /// | `BRANCH`           | `branch`          |
  /// | `MODIFIED_FILES`   | `modifiedFiles`   |
  /// | `GITHUB_TOKEN`     | `githubToken`     |
  /// | `INPUT_DIR`        | (from properties) |
  ///
  /// @param repoFullName  the GitHub repository full name (e.g. `owner/repo`)
  /// @param branch        the Git ref that was pushed (e.g. `refs/heads/main`)
  /// @param modifiedFiles comma-separated list of added/modified file paths from the push event
  /// @param githubToken   optional GitHub personal access token for private repos
  /// @return a configured [SystemCommandTasklet]
  /// @throws IOException if the script resource cannot be resolved
  @Bean
  @StepScope
  public SystemCommandTasklet downloadTasklet(
      @Value("#{jobParameters['repoFullName']}") String repoFullName,
      @Value("#{jobParameters['branch']}") String branch,
      @Value("#{jobParameters['modifiedFiles']}") String modifiedFiles,
      @Value("#{jobParameters['githubToken'] ?: ''}") String githubToken)
      throws IOException {

    var scriptFile = new ClassPathResource(SCRIPT_PATH).getFile().getAbsolutePath();
    var inputDir = Path.of(properties.inputDir()).toAbsolutePath().toString();

    var tasklet = new SystemCommandTasklet();
    tasklet.setCommand("bash", scriptFile);
    tasklet.setTimeout(DEFAULT_TIMEOUT_MILLIS);
    tasklet.setWorkingDirectory(inputDir);
    tasklet.setEnvironmentParams(
        new String[] {
          "REPO_FULL_NAME=" + repoFullName,
          "BRANCH=" + branch,
          "MODIFIED_FILES=" + modifiedFiles,
          "GITHUB_TOKEN=" + githubToken,
          "INPUT_DIR=" + inputDir
        });

    return tasklet;
  }

  /// Creates the download [Step] that wraps the [SystemCommandTasklet].
  ///
  /// @param jobRepository      the repository used to persist step metadata
  /// @param transactionManager the transaction manager for the tasklet step
  /// @param downloadTasklet    the system-command tasklet that runs the download script
  /// @return a fully configured download step
  @Bean
  public Step downloadStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      SystemCommandTasklet downloadTasklet) {
    return new StepBuilder("downloadStep", jobRepository)
        .tasklet(downloadTasklet, transactionManager)
        .build();
  }
}






