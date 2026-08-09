package dz.anisbouhadida.medzloader.batch.support.validator;

import java.util.regex.Pattern;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.stereotype.Component;

/// Validates the release selected for a medicine-loading job execution.
///
/// The `release` job parameter is required and must use the `YYYY-MM` format.
///
/// @author Anis Bouhadida
/// @since 0.2.1
@Component
public class ReleaseJobParametersValidator implements JobParametersValidator {

  private static final String RELEASE_PARAMETER = "release";
  private static final Pattern RELEASE_PATTERN = Pattern.compile("\\d{4}-(0[1-9]|1[0-2])");

  /// Validates that the release parameter is present and follows the `YYYY-MM` format.
  ///
  /// @param parameters the job parameters to validate
  /// @throws InvalidJobParametersException when `release` is missing or invalid
  @Override
  public void validate(JobParameters parameters) throws InvalidJobParametersException {
    var release = parameters.getString(RELEASE_PARAMETER);

    if (release == null) {
      throw new InvalidJobParametersException("The 'release' job parameter is required");
    }

    if (!RELEASE_PATTERN.matcher(release).matches()) {
      throw new InvalidJobParametersException(
          "The 'release' job parameter must follow the YYYY-MM format");
    }
  }
}
