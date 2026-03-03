package dz.anisbouhadida.medzloader.batch.support.utils;

import dz.anisbouhadida.medzloader.domain.model.MedicineEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/// Reusable SQL helpers for [MedicineEvent] persistence.
///
/// - Building the five-column composite-key [MapSqlParameterSource] used to
///   resolve an existing `medicine` row.
/// - Loading SQL statements from classpath resources.
///
/// @author Anis Bouhadida
/// @since 0.2.0
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MedicineSqlUtils {

  /// Creates a [MapSqlParameterSource] pre-filled with the five composite-key
  /// parameters that identify a `medicine` row.
  ///
  /// Callers may chain additional `.addValue(…)` calls to append
  /// event-specific parameters before passing the source to a reader/writer.
  ///
  /// @param event the [MedicineEvent] whose embedded
  /// [dz.anisbouhadida.medzloader.domain.model.Medicine] provides the key values
  /// @return a [MapSqlParameterSource] containing `registrationNumber`, `code`,
  ///         `icd`, `brandName`, and `laboratoryHolder`
  public static MapSqlParameterSource compositeKeyParams(MedicineEvent event) {
    return new MapSqlParameterSource()
        .addValue("registrationNumber", event.medicine().registrationNumber())
        .addValue("code", event.medicine().code())
        .addValue("icd", event.medicine().internationalCommonDenomination())
        .addValue("brandName", event.medicine().brandName())
        .addValue("laboratoryHolder", event.medicine().laboratoryHolder());
  }

  /// Creates a [MapSqlParameterSource] pre-filled with the five composite-key
  /// parameters that identify a `medicine` row, using raw field values.
  ///
  /// Use this overload when the values are available individually rather than
  /// through a [MedicineEvent]. Callers may chain additional `.addValue(…)` calls
  /// to append further parameters before passing the source to a reader/writer.
  ///
  /// @param registrationNumber            the official registration number of the medicine
  /// @param code                          the unique internal code of the medicine
  /// @param internationalCommonDenomination the INN (International Non-proprietary Name)
  /// @param brandName                     the commercial brand name
  /// @param laboratoryHolder              the laboratory holding the registration decision
  /// @return a [MapSqlParameterSource] containing `registrationNumber`, `code`,
  ///         `icd`, `brandName`, and `laboratoryHolder`
  public static MapSqlParameterSource compositeKeyParams(
      String registrationNumber,
      String code,
      String internationalCommonDenomination,
      String brandName,
      String laboratoryHolder) {
    return new MapSqlParameterSource()
        .addValue("registrationNumber", registrationNumber)
        .addValue("code", code)
        .addValue("icd", internationalCommonDenomination)
        .addValue("brandName", brandName)
        .addValue("laboratoryHolder", laboratoryHolder);
  }

  /// Loads a SQL file from the classpath and returns its content as a UTF-8 string.
  ///
  /// @param resourcePath classpath-relative path to the `.sql` file
  ///                     (e.g. `sql/writers/medicine-upsert.sql`)
  /// @return the full SQL content of the resource
  /// @throws UncheckedIOException if the resource does not exist or cannot be read
  public static String loadSql(String resourcePath) {
    try {
      return new ClassPathResource(resourcePath).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load SQL resource: " + resourcePath, e);
    }
  }
}
