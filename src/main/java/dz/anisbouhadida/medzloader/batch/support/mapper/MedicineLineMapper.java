package dz.anisbouhadida.medzloader.batch.support.mapper;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import dz.anisbouhadida.medzloader.domain.api.MedicineApi;
import dz.anisbouhadida.medzloader.domain.model.Medicine;
import dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzloader.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzloader.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/// MapStruct mapper that converts [MedicineLine] DTOs into domain model objects.
///
/// Handles the three CSV formats read by the batch pipeline:
///
/// - [NomenclatureLine] → [NomenclatureEvent]
/// - [WithdrawalLine]   → [WithdrawalEvent]
/// - [NonRenewalLine]   → [NonRenewalEvent]
///
/// All three share the [#toMedicine(MedicineLine)] base mapping, which is
/// referenced by the event-level mappings via the `toMedicine` qualifier.
///
/// @author Anis Bouhadida
/// @since 0.0.1
/// @version 0.2.0
@Mapper
public interface MedicineLineMapper {

  /// Date-time formatter for the raw `yyyy-MM-dd HH:mm:ss` strings found in the CSV files.
  ///
  /// Used by [#parseDate(String)] to parse registration and event date columns.
  DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /// Converts a [MedicineLine] into a [Medicine] domain object.
  ///
  /// All field mappings are expressed explicitly via `@Mapping` expressions so
  /// that the generated code does not rely on name-based convention matching.
  ///
  /// @param line the source DTO (any [MedicineLine] implementation)
  /// @return a fully populated [Medicine] record
  @Named("toMedicine")
  @Mapping(target = "registrationNumber", expression = "java(line.registrationNumber())")
  @Mapping(target = "code", expression = "java(line.code())")
  @Mapping(
      target = "internationalCommonDenomination",
      expression = "java(line.internationalCommonName())")
  @Mapping(target = "brandName", expression = "java(line.brandName())")
  @Mapping(target = "form", expression = "java(line.form())")
  @Mapping(target = "dosage", expression = "java(line.dosage())")
  @Mapping(target = "packaging", expression = "java(line.packaging())")
  @Mapping(target = "list", expression = "java(line.list())")
  @Mapping(target = "p1", expression = "java(line.p1())")
  @Mapping(target = "p2", expression = "java(line.p2())")
  @Mapping(target = "laboratoryHolder", expression = "java(line.registrationHolderLaboratory())")
  @Mapping(
      target = "laboratoryCountry",
      expression = "java(line.registrationHolderLaboratoryCountry())")
  @Mapping(
      target = "initialRegistrationDate",
      expression = "java(parseDate(line.initialRegistrationDate()))")
  @Mapping(target = "type", expression = "java(toMedicineType(line.type()))")
  @Mapping(target = "origin", expression = "java(toMedicineOrigin(line.status()))")
  @Mapping(
      target = "version",
      expression =
          "java(medicineApi.getMedicineVersionByRegistrationNumber(line.registrationNumber(), line.code(), line.internationalCommonName(), line.brandName(), line.registrationHolderLaboratory()))")
  Medicine toMedicine(MedicineLine line, @Context MedicineApi medicineApi);

  /// Converts a [NomenclatureLine] into a [NomenclatureEvent].
  ///
  /// The nested [Medicine] is built using the [#toMedicine(MedicineLine)] qualified mapping.
  ///
  /// @param line the nomenclature DTO to convert
  /// @return a [NomenclatureEvent] wrapping the mapped medicine and event fields
  @Mapping(target = "medicine", source = ".", qualifiedByName = "toMedicine")
  @Mapping(target = "observations", source = "obs")
  NomenclatureEvent toMedicineEvent(NomenclatureLine line, @Context MedicineApi medicineApi);

  /// Converts a [WithdrawalLine] into a [WithdrawalEvent].
  ///
  /// The nested [Medicine] is built using the [#toMedicine(MedicineLine)] qualified mapping.
  ///
  /// @param line the withdrawal DTO to convert
  /// @return a [WithdrawalEvent] wrapping the mapped medicine and withdrawal fields
  @Mapping(target = "medicine", source = ".", qualifiedByName = "toMedicine")
  WithdrawalEvent toMedicineEvent(WithdrawalLine line, @Context MedicineApi medicineApi);

  /// Converts a [NonRenewalLine] into a [NonRenewalEvent].
  ///
  /// The nested [Medicine] is built using the [#toMedicine(MedicineLine)] qualified mapping.
  ///
  /// @param line the non-renewal DTO to convert
  /// @return a [NonRenewalEvent] wrapping the mapped medicine and non-renewal fields
  @Mapping(target = "medicine", source = ".", qualifiedByName = "toMedicine")
  @Mapping(target = "observations", source = "obs")
  NonRenewalEvent toMedicineEvent(NonRenewalLine line, @Context MedicineApi medicineApi);

  /// Parses a raw date string in `yyyy-MM-dd HH:mm:ss` format into a [LocalDateTime].
  ///
  /// Returns `null` if the string is `null`, blank, or cannot be parsed.
  ///
  /// @param dateStr the raw date string from the CSV file — may be `null` or blank
  /// @return the parsed [LocalDateTime], or `null` if the input is absent or malformed
  /// @deprecated will be replaced by a dedicated converter once the format is stable
  @Deprecated(forRemoval = true)
  default LocalDateTime parseDate(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }
    try {
      return LocalDateTime.parse(dateStr, DATE_FORMATTER);
    } catch (Exception _) {
      return null;
    }
  }

  /// Converts the raw `type` string from a CSV row into a [MedicineType] enum constant.
  ///
  /// Accepted values: `GE`, `G` → [MedicineType#GE]; `RE`, `R` → [MedicineType#RE];
  /// `BIO` → [MedicineType#BIO]. Any other value (including `null` or blank) returns `null`.
  ///
  /// @param typeStr the raw type string from the CSV file — may be `null`
  /// @return the matching [MedicineType], or `null` if unrecognised or input is `null`
  default MedicineType toMedicineType(String typeStr) {
    if (typeStr == null) {
      return null;
    }
    return switch (typeStr) {
      case "GE", "G" -> MedicineType.GE;
      case "RE", "R" -> MedicineType.RE;
      case "BIO" -> MedicineType.BIO;
      default -> null;
    };
  }

  /// Converts the raw `status` string from a CSV row into a [MedicineOrigin] enum constant.
  ///
  /// Accepted values: `F` → [MedicineOrigin#MANUFACTURED]; `I` → [MedicineOrigin#IMPORTED].
  /// Any other value (including `null`, `i` or blank) returns `null`.
  ///
  /// @param statusStr the raw origin/status string from the CSV file — may be `null`
  /// @return the matching [MedicineOrigin], or `null` if unrecognised or input is `null`
  default MedicineOrigin toMedicineOrigin(String statusStr) {
    if (statusStr == null) {
      return null;
    }
    return switch (statusStr) {
      case "F" -> MedicineOrigin.MANUFACTURED;
      case "I" -> MedicineOrigin.IMPORTED;
      default -> null;
    };
  }
}
