package dz.anisbouhadida.medzloader.batch.support.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;

@DisplayName("ReleaseJobParametersValidator")
class ReleaseJobParametersValidatorTest {

  private ReleaseJobParametersValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ReleaseJobParametersValidator();
  }

  @Nested
  @DisplayName("validate()")
  class ValidateTests {

    @Test
    @DisplayName("should accept a release in YYYY-MM format")
    void shouldAcceptValidRelease() {
      var parameters = releaseParameters("2026-08");

      assertThatCode(() -> validator.validate(parameters)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should reject a missing release")
    void shouldRejectMissingRelease() {
      assertThatThrownBy(() -> validator.validate(new JobParameters()))
          .isInstanceOf(InvalidJobParametersException.class)
          .hasMessage("The 'release' job parameter is required");
    }

    @ParameterizedTest(name = "should reject invalid release \"{0}\"")
    @ValueSource(strings = {"", "2026-8", "26-08", "2026/08", "2026-00", "2026-13"})
    void shouldRejectInvalidRelease(String release) {
      var parameters = releaseParameters(release);

      assertThatThrownBy(() -> validator.validate(parameters))
          .isInstanceOf(InvalidJobParametersException.class)
          .hasMessage("The 'release' job parameter must follow the YYYY-MM format");
    }
  }

  private JobParameters releaseParameters(String release) {
    return new JobParametersBuilder().addString("release", release).toJobParameters();
  }
}
