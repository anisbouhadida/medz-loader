package dz.anisbouhadida.medzloader.infrastructure.jdbc.repository;

import java.util.Optional;

/// Repository for read-only access to the `medicine` table.
///
/// Implementations are expected to use a plain JDBC template; this interface
/// intentionally avoids Spring Data conventions so it stays decoupled from any
/// ORM or Spring Data JDBC dependency.
///
/// @author Anis Bouhadida
/// @since 0.2.0
public interface MedicineRepository {

  /// Retrieves the current version of the medicine in the database, if it exists.
  ///
  /// The look-up is performed against the composite business key
  /// (`registration_number`, `code`, `icd`, `brand_name`,
  /// `laboratory_holder`) that backs the unique index `uq_medicine_business_key`.
  ///
  /// @param registrationNumber              the official registration number
  /// @param code                            the medicine code
  /// @param internationalCommonDenomination the International Common Denomination (INN / icd
  // column)
  /// @param brandName                       the commercial brand name
  /// @param laboratoryHolder                the laboratory holding the registration decision
  /// @return an [Optional] containing the stored `version` value,
  ///         or [Optional#empty()] when no matching row exists
  Optional<Integer> findVersionByRegistrationNumber(
      String registrationNumber,
      String code,
      String internationalCommonDenomination,
      String brandName,
      String laboratoryHolder);
}
