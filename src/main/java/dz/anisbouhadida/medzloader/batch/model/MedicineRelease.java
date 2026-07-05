package dz.anisbouhadida.medzloader.batch.model;

import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import org.springframework.core.io.Resource;

/// One validated, dated medicine release and its ordered CSV resources.
///
/// @param period release period encoded by the source directory
/// @param relativeDirectory source directory relative to `medz.loader.input-dir`
/// @param resources nomenclature, non-renewal, and withdrawal resources in processing order
public record MedicineRelease(
    YearMonth period, Path relativeDirectory, List<Resource> resources) {

  public MedicineRelease {
    Objects.requireNonNull(period, "period must not be null");
    Objects.requireNonNull(relativeDirectory, "relativeDirectory must not be null");
    resources = List.copyOf(resources);
  }

  /// Returns the canonical release version.
  ///
  /// @return release period formatted as `YYYY-MM`
  public String version() {
    return period.toString();
  }
}
