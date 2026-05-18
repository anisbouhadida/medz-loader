package dz.anisbouhadida.medzloader.batch.config;

import static org.assertj.core.api.Assertions.assertThat;

import dz.anisbouhadida.medzloader.batch.config.step.DownloadStepConfiguration;
import dz.anisbouhadida.medzloader.batch.support.properties.MedzLoaderProperties;
import java.io.IOException;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;

@DisplayName("DownloadStepConfiguration")
class DownloadStepConfigurationTest {

  @TempDir java.nio.file.Path tempDir;

  @Nested
  @DisplayName("downloadTasklet()")
  class DownloadTaskletTests {

    @Test
    @DisplayName("should create a non-null SystemCommandTasklet with valid parameters")
    void downloadTasklet_should_createTasklet_when_parametersProvided() throws IOException {
      var properties =
          new MedzLoaderProperties(tempDir.toString(), ZoneId.of("Africa/Algiers"));
      var config = new DownloadStepConfiguration(properties);

      SystemCommandTasklet tasklet =
          config.downloadTasklet(
              "anisbouhadida/medz-data", "refs/heads/main", "output/2024-08/nomenclature.csv", "");

      assertThat(tasklet).isNotNull();
    }

    @Test
    @DisplayName("should create tasklet with optional github token")
    void downloadTasklet_should_createTasklet_when_tokenProvided() throws IOException {
      var properties =
          new MedzLoaderProperties(tempDir.toString(), ZoneId.of("Africa/Algiers"));
      var config = new DownloadStepConfiguration(properties);

      SystemCommandTasklet tasklet =
          config.downloadTasklet(
              "anisbouhadida/medz-data",
              "main",
              "output/2024-12/retraits.csv,output/2024-12/nomenclature.csv",
              "ghp_test_token_123");

      assertThat(tasklet).isNotNull();
    }
  }
}

